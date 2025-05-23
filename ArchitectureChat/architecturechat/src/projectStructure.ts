import * as vscode from 'vscode';
import * as path from 'path';

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

export async function analyzeProjectStructure(): Promise<ProjectNode | null> {
    if (!vscode.workspace.workspaceFolders || vscode.workspace.workspaceFolders.length === 0) {
        vscode.window.showErrorMessage('Nu există niciun workspace deschis');
        return null;
    }

    const rootPath = vscode.workspace.workspaceFolders[0].uri.fsPath;
    const rootNode = await buildProjectStructure(rootPath);
    
    return rootNode;
}

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

function addFileToTree(rootNode: ProjectNode, pathParts: string[], isFile: boolean): void {
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
    
    if (!currentNode.children.some(child => child.name === fileName && child.isFile)) {
        currentNode.addChild(new ProjectNode(fileName, isFile));
    }
}

export function formatProjectStructure(node: ProjectNode, prefix: string = '', isLast: boolean = true): string {
    let result = '';
    
    result += `${prefix}${isLast ? '└─' : '├─'}${node.name}${node.isFile ? '' : '/'}\n`;
    
    const childPrefix = prefix + (isLast ? '  ' : '│ ');
    
    for (let i = 0; i < node.children.length; i++) {
        const isLastChild = i === node.children.length - 1;
        result += formatProjectStructure(node.children[i], childPrefix, isLastChild);
    }
    
    return result;
}

export function convertToJavaObjectString(node: ProjectNode): string {
    if (node.children.length === 0) {
        return `new ProjectNode("${node.name}", ${node.isFile})`;
    }
    
    const childrenStr = node.children
        .map(child => convertToJavaObjectString(child))
        .join(', ');
    
    return `new ProjectNode("${node.name}", ${node.isFile}, [${childrenStr}])`;
}