import * as vscode from 'vscode';
import * as path from 'path';
import * as fs from 'fs';

/**
 * Represents a node in the project structure tree.
 * @param name - Name of the file or directory.
 * @param isFile - Whether the node represents a file.
 * @returns void
 */
export class ProjectNode {
    name: string;
    isFile: boolean;
    children: ProjectNode[];
    javaType: string | null = null; 
    extendsFrom: string[] = [];
    implements: string[] = [];

    constructor(name: string, isFile: boolean = false) {
        this.name = name;
        this.isFile = isFile;
        this.children = [];
    }


    /**
     * Adds a child node to this node.
     * @param child - The child ProjectNode to add.
     * @returns void
     */
    addChild(child: ProjectNode): void {
        this.children.push(child);
    }
}

interface JavaRelation {
    from: string;
    to: string;
    type: 'extends' | 'implements';
}

/**
 * Builds the project structure and extracts Java class relationships.
 * @param rootPath - Root path of the project.
 * @returns ProjectNode representing the root of the structure.
 */
export async function buildProjectStructure(rootPath: string): Promise<ProjectNode> {
    const rootName = path.basename(rootPath);
    const rootNode = new ProjectNode(rootName);
    
    const javaFiles = await vscode.workspace.findFiles('**/*.java','{**/build/**,**/target/**,**/bin/**,**/out/**,**/node_modules/**,**/.gradle/**,**/.idea/**,**/.settings/**}');
    const javaRelations: JavaRelation[] = [];
    const javaClasses: Map<string, ProjectNode> = new Map();
    
    for (const fileUri of javaFiles) {
        const filePath = fileUri.fsPath;
        const relativePath = path.relative(rootPath, filePath);
        
        const pathParts = relativePath.split(path.sep);
        const fileNode = addFileToTree(rootNode, pathParts, true);
        
        const fileContent = fs.readFileSync(filePath, 'utf8');
        parseJavaFile(fileContent, fileNode, javaRelations, javaClasses);
    }
    
    processJavaRelations(javaRelations, javaClasses);
    
    return rootNode;
}

/**
 * Adds a file or directory to the project tree.
 * @param rootNode - The root ProjectNode.
 * @param pathParts - Path parts from the root to the file.
 * @param isFile - Whether the node is a file.
 * @returns ProjectNode that was added.
 */
function addFileToTree(rootNode: ProjectNode, pathParts: string[], isFile: boolean): ProjectNode {
    let currentNode = rootNode;
    
    for (let i = 0; i < pathParts.length - 1; i++) {
        const part = pathParts[i];
        
        let childNode = currentNode.children.find(child => child.name === part && !child.isFile);
        
        if (!childNode) {
            childNode = new ProjectNode(part, false);
            currentNode.addChild(childNode);
        }
        
        currentNode = childNode;
    }
    
    const fileName = pathParts[pathParts.length - 1];
    
    let fileNode = currentNode.children.find(child => child.name === fileName && child.isFile);
    if (!fileNode) {
        fileNode = new ProjectNode(fileName, isFile);
        currentNode.addChild(fileNode);
    }
    
    return fileNode;
}

/**
 * Parses a Java file and extracts classes and their relationships.
 * @param content - Java file content.
 * @param fileNode - File node in the project tree.
 * @param relations - Array to store class relations.
 * @param classes - Map of class names to ProjectNode instances.
 * @returns void
 */
function parseJavaFile(content: string, fileNode: ProjectNode, relations: JavaRelation[], classes: Map<string, ProjectNode>): void {
    const classRegex = /\b(public|private|protected)?\s*(abstract|final)?\s*(class|interface|enum)\s+(\w+)(?:\s*<[^>]*>)?(?:\s+extends\s+([^{;]+))?(?:\s+implements\s+([^{;]+))?/g;
    
    let match;
    while ((match = classRegex.exec(content)) !== null) {
        const [_, access, modifier, type, name, extendsClasses, implementsList] = match;
        
        const classNode = new ProjectNode(name, false);
        classNode.javaType = type;
        
        fileNode.addChild(classNode);
        
        classes.set(name, classNode);
        
        if (extendsClasses) {
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
        
        if (implementsList) {
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

/**
 * Parses comma-separated type declarations handling nested generics.
 * @param declarationList - The string containing type declarations.
 * @returns string[] - Array of type names.
 */
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
            result.push(currentItem.trim());
            currentItem = '';
        } else {
            currentItem += char;
        }
    }
    
    if (currentItem.trim()) {
        result.push(currentItem.trim());
    }
    
    return result;
}

/**
 * Cleans generic types and extracts base type and parameters.
 * @param typeString - Type string possibly with generics.
 * @returns string - Cleaned type string.
 */
function handleGenericTypes(typeString: string): string {
    if (!typeString.includes('<')) {
        return typeString;
    }
    
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
            continue;
        } else {
            skipSpace = false;
            cleanedGenericParts += char;
        }
    }
    
    return baseType + cleanedGenericParts;
}


function processJavaRelations(relations: JavaRelation[], classes: Map<string, ProjectNode>): void {}

/**
 * Generates output for Java class structure and relationships.
 * @param rootNode - Root node of the project structure.
 * @returns string - Formatted output string.
 */
export function generateJavaRelationsOutput(rootNode: ProjectNode): string {
    const classNodes: ProjectNode[] = [];
    const relations: JavaRelation[] = [];
    
    function collectClasses(node: ProjectNode): void {
        if (node.javaType) {
            classNodes.push(node);
        }
        
        for (const child of node.children) {
            collectClasses(child);
        }
    }
    
    collectClasses(rootNode);
    
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
    
    const classTypeMap = new Map<string, string>();
    for (const c of classNodes) {
        if (c.javaType) {
            classTypeMap.set(c.name, c.javaType);
        }
    }
    
    let output: string[] = [];
    
    for (const cls of classNodes) {
        output.push(`${cls.javaType} ${cls.name}`);
    }
    
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
    
    for (const [className, relInfo] of classRelations.entries()) {
        if (relInfo.extends.length > 0 && relInfo.implements.length > 0) {
            for (const extClass of relInfo.extends) {
                output.push(`${className} -> ${extClass}`);
            }
            
            for (const implInterface of relInfo.implements) {
                output.push(`${className} -|> ${implInterface}`);
            }
        } else {
            for (const extClass of relInfo.extends) {
                output.push(`${className} -> ${extClass}`);
            }
            
            for (const implInterface of relInfo.implements) {
                output.push(`${className} -|> ${implInterface}`);
            }
        }
    }
    
    if (output.length === 0) {
        output.push("// No classes or relationships found.");
        output.push("// Make sure there are valid Java files in the project.");
    }
    
    return output.join('\n');
}

/**
 * Activates the Java structure analyzer command.
 * @param context - Extension context.
 * @returns Output channel used for logging.
 */
export default function activateStructureAnalyzer(context: vscode.ExtensionContext) {
    
    let disposable = vscode.commands.registerCommand('structureanalyze.analyzeJavaStructure', async () => {
    const workspaceFolders = vscode.workspace.workspaceFolders;

    if (!workspaceFolders) {
        vscode.window.showErrorMessage('No workspace is currently open.');
        return;
    }

    let outputText = '';

    const rootPath = workspaceFolders[0].uri.fsPath;
    try {
        const javaFiles = await vscode.workspace.findFiles(
            '**/*.java',
            '{**/build/**,**/target/**,**/bin/**,**/out/**,**/node_modules/**,**/.gradle/**,**/.idea/**,**/.settings/**}'
        );
        outputText += `Found ${javaFiles.length} Java files for analysis.\n`;

        if (javaFiles.length === 0) {
            outputText += 'No Java files found for analysis.\n';
            vscode.window.showInformationMessage('No Java files were found in the workspace.');
            return;
        }

        const projectStructure = await buildProjectStructure(rootPath);
            
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
        outputText += `Identified ${classCount} classes/interfaces and ${relationCount} relationships among them.\n`;

        const output = generateJavaRelationsOutput(projectStructure);

        if (output.trim() === '') {
            outputText += 'WARNING: The generated output is empty. Check the Java file parser.\n';
        }

        outputText += 'Analysis result:\n';
        outputText += '====================\n';
        outputText += output + '\n';
        outputText += '====================\n';
        outputText += 'Analysis completed successfully!\n';

        vscode.window.showInformationMessage('Java structure analysis has been completed.');
        console.log(outputText); 
    } catch (error) {
        const errorMsg = `Error during structure analysis: ${error instanceof Error ? error.message : String(error)}\n`;
        outputText += errorMsg;
        vscode.window.showErrorMessage(errorMsg);
    }

    return outputText; 
});

    context.subscriptions.push(disposable);
}
