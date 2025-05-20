import * as vscode from 'vscode';
import * as path from 'path';

//clasa pentru noduri->fisiere
export class ProjectNode {
    name: string;
    isFile: boolean;
    children: ProjectNode[];

    constructor(name: string, isFile: boolean = false, children: ProjectNode[] = []) {
        this.name = name;
        this.isFile = isFile;
        this.children = children;
    }

    addChild(child: ProjectNode): void {
        this.children.push(child);
    }
}

//main function for building and printing
export async function analyzeProjectStructure(): Promise<ProjectNode | null> {
    if (!vscode.workspace.workspaceFolders || vscode.workspace.workspaceFolders.length === 0) {
        vscode.window.showErrorMessage('Nu există niciun workspace deschis');
        return null;
    }

    const rootPath = vscode.workspace.workspaceFolders[0].uri.fsPath;
    const rootNode = await buildProjectStructure(rootPath);
    
    return rootNode;
}

//Building the project structure as a node tree
export async function buildProjectStructure(rootPath: string): Promise<ProjectNode> {
    const rootName = path.basename(rootPath);
    const rootNode = new ProjectNode(rootName);
    
    const javaFiles = await vscode.workspace.findFiles('**/*.java', '**/node_modules/**');
    
    for (const fileUri of javaFiles) {
        const filePath = fileUri.fsPath;
        const relativePath = path.relative(rootPath, filePath);
        
        addFileToTree(rootNode, relativePath.split(path.sep), true);
    }
    
    return rootNode;
}

// Funcție pentru adăugarea unui fișier în arbore, creând directoarele necesare
function addFileToTree(rootNode: ProjectNode, pathParts: string[], isFile: boolean): void {
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
    if (!currentNode.children.some(child => child.name === fileName && child.isFile)) {
        currentNode.addChild(new ProjectNode(fileName, isFile));
    }
}

// Formatează structura proiectului pentru afișare
export function formatProjectStructure(node: ProjectNode, prefix: string = '', isLast: boolean = true): string {
    let result = '';
    
    // Adăugăm nodul curent la rezultat
    result += `${prefix}${isLast ? '└─' : '├─'}${node.name}${node.isFile ? '' : '/'}\n`;
    
    // Actualizăm prefixul pentru copii
    const childPrefix = prefix + (isLast ? '  ' : '│ ');
    
    // Adăugăm copiii în mod recursiv
    for (let i = 0; i < node.children.length; i++) {
        const isLastChild = i === node.children.length - 1;
        result += formatProjectStructure(node.children[i], childPrefix, isLastChild);
    }
    
    return result;
}

// Convertește structura în format similar cu obiectele Java
export function convertToJavaObjectString(node: ProjectNode): string {
    if (node.children.length === 0) {
        return `new ProjectNode("${node.name}", ${node.isFile})`;
    }
    
    const childrenStr = node.children
        .map(child => convertToJavaObjectString(child))
        .join(', ');
    
    return `new ProjectNode("${node.name}", ${node.isFile}, [${childrenStr}])`;
}