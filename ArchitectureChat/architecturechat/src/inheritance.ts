import * as vscode from 'vscode';
import * as path from 'path';
import * as fs from 'fs';

// Clasa pentru nodurile din arbore
export class ProjectNode {
    name: string;
    isFile: boolean;
    children: ProjectNode[];
    // Adăugăm informații despre clasă/interfață
    javaType: string | null = null; // 'class', 'interface', sau 'enum'
    extendsFrom: string[] = [];
    implements: string[] = [];

    constructor(name: string, isFile: boolean = false) {
        this.name = name;
        this.isFile = isFile;
        this.children = [];
    }

    addChild(child: ProjectNode): void {
        this.children.push(child);
    }
}

// Interfață pentru a stoca relațiile între clase
interface JavaRelation {
    from: string;
    to: string;
    type: 'extends' | 'implements';
}

// Funcție pentru a construi structura proiectului
export async function buildProjectStructure(rootPath: string): Promise<ProjectNode> {
    const rootName = path.basename(rootPath);
    const rootNode = new ProjectNode(rootName);
    
    const javaFiles = await vscode.workspace.findFiles('**/*.java','{**/build/**,**/target/**,**/bin/**,**/out/**,**/node_modules/**,**/.gradle/**,**/.idea/**,**/.settings/**}');
    const javaRelations: JavaRelation[] = [];
    const javaClasses: Map<string, ProjectNode> = new Map();
    
    // Prima pasă: găsește toate fișierele și creează structura de foldere
    for (const fileUri of javaFiles) {
        const filePath = fileUri.fsPath;
        const relativePath = path.relative(rootPath, filePath);
        
        // Adaugă fișierul în arbore
        const pathParts = relativePath.split(path.sep);
        const fileNode = addFileToTree(rootNode, pathParts, true);
        
        // Parsează conținutul fișierului pentru a găsi clase, interfețe etc.
        const fileContent = fs.readFileSync(filePath, 'utf8');
        parseJavaFile(fileContent, fileNode, javaRelations, javaClasses);
    }
    
    // A doua pasă: adaugă relațiile între clase
    processJavaRelations(javaRelations, javaClasses);
    
    return rootNode;
}

// Funcție modificată pentru a returna nodul adăugat
function addFileToTree(rootNode: ProjectNode, pathParts: string[], isFile: boolean): ProjectNode {
    let currentNode = rootNode;
    
    // Pentru fiecare componentă a căii, exceptând ultimul element (numele fișierului)
    for (let i = 0; i < pathParts.length - 1; i++) {
        const part = pathParts[i];
        
        // Căutăm dacă directorul există deja în nodurile copil
        let childNode = currentNode.children.find(child => child.name === part && !child.isFile);
        
        // Dacă nu există, îl creăm
        if (!childNode) {
            childNode = new ProjectNode(part, false);
            currentNode.addChild(childNode);
        }
        
        // Avansăm la nivelul următor
        currentNode = childNode;
    }
    
    // Adăugăm fișierul (ultimul element din cale)
    const fileName = pathParts[pathParts.length - 1];
    
    // Verificăm dacă fișierul există deja pentru a evita duplicatele
    let fileNode = currentNode.children.find(child => child.name === fileName && child.isFile);
    if (!fileNode) {
        fileNode = new ProjectNode(fileName, isFile);
        currentNode.addChild(fileNode);
    }
    
    return fileNode;
}

// Funcție pentru a parsa un fișier Java și a identifica clasele și relațiile
function parseJavaFile(content: string, fileNode: ProjectNode, relations: JavaRelation[], classes: Map<string, ProjectNode>): void {
    // Regex pentru a găsi declarații de clasă, interfață sau enum
    const classRegex = /\b(public|private|protected)?\s*(abstract|final)?\s*(class|interface|enum)\s+(\w+)(?:\s*<[^>]*>)?(?:\s+extends\s+([^{;]+))?(?:\s+implements\s+([^{;]+))?/g;
    
    let match;
    while ((match = classRegex.exec(content)) !== null) {
        const [_, access, modifier, type, name, extendsClasses, implementsList] = match;
        
        // Creăm un nod pentru clasă/interfață/enum
        const classNode = new ProjectNode(name, false);
        classNode.javaType = type;
        
        // Adăugăm clasa la nodul fișier pentru a o păstra în structura de arbore
        fileNode.addChild(classNode);
        
        // Adăugăm nodul la colecția de clase
        classes.set(name, classNode);
        
        // Adăugăm informații despre moștenire
        if (extendsClasses) {
            // Procesăm corect relațiile extends care pot conține generice complexe
            const extendsList = parseDeclarationList(extendsClasses);
            for (const parent of extendsList) {
                if (parent) {
                    const cleanParent = handleGenericTypes(parent);
                    classNode.extendsFrom.push(cleanParent);
                    relations.push({
                        from: name,
                        to: cleanParent,
                        type: 'extends'
                    });
                }
            }
        }
        
        // Adăugăm informații despre implementări
        if (implementsList) {
            // Procesăm corect relațiile implements care pot conține generice complexe
            const interfacesList = parseDeclarationList(implementsList);
            for (const iface of interfacesList) {
                if (iface) {
                    const cleanIface = handleGenericTypes(iface);
                    classNode.implements.push(cleanIface);
                    relations.push({
                        from: name,
                        to: cleanIface,
                        type: 'implements'
                    });
                }
            }
        }
    }
}

// Funcție pentru a parsa corect listele de declarații (extends sau implements)
// care pot conține tipuri generice complexe
function parseDeclarationList(declarationList: string): string[] {
    const result: string[] = [];
    let currentItem = '';
    let depth = 0;
    
    for (let i = 0; i < declarationList.length; i++) {
        const char = declarationList[i];
        
        if (char === '<') {
            depth++;
            currentItem += char;
        } else if (char === '>') {
            depth--;
            currentItem += char;
        } else if (char === ',' && depth === 0) {
            // Am găsit un separator valid între elemente
            result.push(currentItem.trim());
            currentItem = '';
        } else {
            currentItem += char;
        }
    }
    
    // Adăugăm ultimul element dacă există
    if (currentItem.trim()) {
        result.push(currentItem.trim());
    }
    
    return result;
}

// Funcție pentru a gestiona tipurile generice în mod corect
function handleGenericTypes(typeString: string): string {
    // Dacă nu avem generice, returnăm ca atare
    if (!typeString.includes('<')) {
        return typeString;
    }
    
    // Pentru a gestiona corect genericele complexe, trebuie să ținem cont de
    // adâncimea parantezelor unghiulare pentru a nu confunda tipul principal cu
    // un alt tip generic în caz de generice imbricate
    let baseType = '';
    let genericParts = '';
    let depth = 0;
    let inGeneric = false;
    
    for (let i = 0; i < typeString.length; i++) {
        const char = typeString[i];
        
        if (char === '<') {
            depth++;
            inGeneric = true;
            genericParts += char;
        } else if (char === '>') {
            depth--;
            genericParts += char;
        } else if (!inGeneric) {
            baseType += char;
        } else {
            genericParts += char;
        }
    }
    
    baseType = baseType.trim();
    
    // Curățăm genericele de spații în plus după virgule
    let cleanedGenericParts = '';
    let inString = false;
    let skipSpace = false;
    
    for (let i = 0; i < genericParts.length; i++) {
        const char = genericParts[i];
        
        if (char === '"' || char === "'") {
            inString = !inString;
            cleanedGenericParts += char;
        } else if (char === ',' && !inString) {
            cleanedGenericParts += char;
            skipSpace = true;
        } else if (char === ' ' && skipSpace && !inString) {
            // Ignorăm spațiile după virgulă
            continue;
        } else {
            skipSpace = false;
            cleanedGenericParts += char;
        }
    }
    
    return baseType + cleanedGenericParts;
}

// Funcție pentru a procesa relațiile între clase
function processJavaRelations(relations: JavaRelation[], classes: Map<string, ProjectNode>): void {
    // Aici putem face ceva cu relațiile, de exemplu construi o reprezentare pentru vizualizare
}

// Funcție pentru a genera reprezentarea cerută exact conform formatului specificat
export function generateJavaRelationsOutput(rootNode: ProjectNode): string {
    const classNodes: ProjectNode[] = [];
    const relations: JavaRelation[] = [];
    
    // Funcție recursivă pentru a colecta toate clasele
    function collectClasses(node: ProjectNode): void {
        if (node.javaType) {
            classNodes.push(node);
        }
        
        for (const child of node.children) {
            collectClasses(child);
        }
    }
    
    // Colectăm toate clasele
    collectClasses(rootNode);
    
    // Colectăm toate relațiile
    for (const cls of classNodes) {
        for (const extendClass of cls.extendsFrom) {
            relations.push({
                from: cls.name,
                to: extendClass,
                type: 'extends'
            });
        }
        
        for (const implementInterface of cls.implements) {
            relations.push({
                from: cls.name,
                to: implementInterface,
                type: 'implements'
            });
        }
    }
    
    // Construim un mapping de la nume la tipul lor (class, interface, enum)
    const classTypeMap = new Map<string, string>();
    for (const c of classNodes) {
        if (c.javaType) {
            classTypeMap.set(c.name, c.javaType);
        }
    }
    
    // Generăm output-ul
    let output: string[] = [];
    
    // Adăugăm toate declarațiile de clasă/interfață/enum
    for (const cls of classNodes) {
        output.push(`${cls.javaType} ${cls.name}`);
    }
    
    // Organizăm relațiile în funcție de clasa de origine
    const classRelations = new Map<string, {extends: string[], implements: string[]}>();
    
    for (const rel of relations) {
        if (!classRelations.has(rel.from)) {
            classRelations.set(rel.from, {extends: [], implements: []});
        }
        
        const classRel = classRelations.get(rel.from);
        if (rel.type === 'extends') {
            classRel!.extends.push(rel.to);
        } else if (rel.type === 'implements') {
            classRel!.implements.push(rel.to);
        }
    }
    
    // Acum generăm relațiile grupate pe clasă
    for (const [className, relInfo] of classRelations.entries()) {
        // Dacă avem atât extends cât și implements
        if (relInfo.extends.length > 0 && relInfo.implements.length > 0) {
            // Formatul special: class A->class B(extends)-|>class C(implements)
            for (const extClass of relInfo.extends) {
                output.push(`${className} -> ${extClass}`);
            }
            
            for (const implInterface of relInfo.implements) {
                output.push(`${className} -|> ${implInterface}`);
            }
        } else {
            // Formatul standard pentru fiecare tip de relație
            for (const extClass of relInfo.extends) {
                output.push(`${className} -> ${extClass}`);
            }
            
            for (const implInterface of relInfo.implements) {
                output.push(`${className} -|> ${implInterface}`);
            }
        }
    }
    
    // În cazul în care nu avem clase sau relații, adăugăm un mesaj informativ
    if (output.length === 0) {
        output.push("// Nu s-au găsit clase sau relații între ele.");
        output.push("// Verificați că există fișiere Java valide în proiect.");
    }
    
    return output.join('\n');
}

// Actualizăm pentru a afișa în terminal
export default function activateStructureAnalyzer(context: vscode.ExtensionContext) {
    // Creăm un canal de output pentru extensia noastră
    const outputChannel = vscode.window.createOutputChannel('Java Structure Analyzer');
    
    let disposable = vscode.commands.registerCommand('structureanalyze.analyzeJavaStructure', async () => {
        const workspaceFolders = vscode.workspace.workspaceFolders;
        
        if (!workspaceFolders) {
            vscode.window.showErrorMessage('Nu există niciun workspace deschis.');
            return;
        }
        
        // Arătăm canalul de output
        outputChannel.show(true); // true înseamnă "preserve focus"
        outputChannel.appendLine('Analizăm structura Java...');
        
        const rootPath = workspaceFolders[0].uri.fsPath;
        try {
            // Verificăm dacă există fișiere Java
            const javaFiles = await vscode.workspace.findFiles('**/*.java','{**/build/**,**/target/**,**/bin/**,**/out/**,**/node_modules/**,**/.gradle/**,**/.idea/**,**/.settings/**}');
            outputChannel.appendLine(`S-au găsit ${javaFiles.length} fișiere Java pentru analiză.`);
            
            if (javaFiles.length === 0) {
                outputChannel.appendLine('Nu s-au găsit fișiere Java pentru analiză.');
                vscode.window.showInformationMessage('Nu s-au găsit fișiere Java în workspace.');
                return;
            }
            
            const projectStructure = await buildProjectStructure(rootPath);
            
            // Adăugăm debug info despre structura construită
            let classCount = 0;
            let relationCount = 0;
            
            function countClasses(node: ProjectNode): void {
                if (node.javaType) {
                    classCount++;
                    relationCount += node.extendsFrom.length + node.implements.length;
                }
                
                for (const child of node.children) {
                    countClasses(child);
                }
            }
            
            countClasses(projectStructure);
            outputChannel.appendLine(`S-au identificat ${classCount} clase/interfețe și ${relationCount} relații între ele.`);
            
            // Generăm output-ul cu relațiile
            const output = generateJavaRelationsOutput(projectStructure);
            
            if (output.trim() === '') {
                outputChannel.appendLine('AVERTISMENT: Output-ul generat este gol. Verificați parserul de fișiere Java.');
            }
            
            // Afișăm rezultatul în canal
            outputChannel.appendLine('Rezultatul analizei:');
            outputChannel.appendLine('====================');
            outputChannel.appendLine(output);
            outputChannel.appendLine('====================');
            outputChannel.appendLine('Analiză completată cu succes!');
            
            // Informăm utilizatorul că analiza s-a terminat
            vscode.window.showInformationMessage('Analiza structurii Java a fost finalizată. Verificați Output Panel.');
        } catch (error) {
            // Afișăm eroarea în canal
            outputChannel.appendLine(`Eroare: ${error instanceof Error ? error.message : String(error)}`);
            vscode.window.showErrorMessage(`Eroare la analizarea structurii: ${error instanceof Error ? error.message : String(error)}`);
        }
    });
    
    context.subscriptions.push(disposable, outputChannel);
}