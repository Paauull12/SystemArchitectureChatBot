import { readFileSync } from 'fs';
/**
 * Efferent Coupling (Ce) - Outgoing Dependencies
 * 
 * Number of classes that a given class depends upon.
 * Indicates the class's independence and reusability.
 * 
 * Interpretation based on industry standards:
 * - Very Good: Ce ≤ 5 (highly independent)
 * - Acceptable: 6 ≤ Ce ≤ 10 (moderate dependencies)
 * - Warning: 11 ≤ Ce ≤ 20 (many dependencies)
 * - Bad: Ce > 20 (too dependent, low reusability)
 */
export function calculateEfferentCoupling(filepath: string): number {
    try {
        const content = readFileSync(filepath, { encoding: 'utf8', flag: 'r' });
        
        const dependencies = new Set<string>();
        
        const importPattern = /import\s+(static\s+)?([^;]+);/g;
        const classPattern = /(?:public\s+)?(?:abstract\s+)?(?:final\s+)?class\s+\w+(?:\s+extends\s+(\w+))?(?:\s+implements\s+([^{]+))?/;
        const interfacePattern = /interface\s+\w+(?:\s+extends\s+([^{]+))?/;
        
        const imports = [...content.matchAll(importPattern)];
        for (const match of imports) {
            const importPath = match[2].trim();
            
            let className: string;
            if (match[1]) { // static import
                const parts = importPath.split('.');
                className = parts[parts.length - 2];
            } else {
                const parts = importPath.split('.');
                className = parts[parts.length - 1];
            }
            
            if (className && className !== '*' && !importPath.startsWith('java.')) {
                dependencies.add(className);
            }
        }
        
        const classMatch = content.match(classPattern);
        if (classMatch) {
            // Extends
            if (classMatch[1]) {
                dependencies.add(classMatch[1].trim());
            }
            
            // Implements
            if (classMatch[2]) {
                const interfaces = classMatch[2].split(',');
                for (const iface of interfaces) {
                    dependencies.add(iface.trim());
                }
            }
        }
        
        const interfaceMatch = content.match(interfacePattern);
        if (interfaceMatch && interfaceMatch[1]) {
            const extendedInterfaces = interfaceMatch[1].split(',');
            for (const iface of extendedInterfaces) {
                dependencies.add(iface.trim());
            }
        }
        
        const lines = content.split('\n');
        
        for (const line of lines) {
            const trimmedLine = line.trim();
            
            if (trimmedLine.startsWith('//') || trimmedLine.startsWith('/*') || trimmedLine.startsWith('*')) {
                continue;
            }
            
           
            const patterns = [
                /^\s*(\w+)\s+\w+\s*[;=]/g,
                /<(\w+)>/g,
                /new\s+(\w+)\s*\(/g,
                // Parametri de metodă: methodName(ClassName param)
                /\(\s*(\w+)\s+\w+(?:\s*,\s*(\w+)\s+\w+)*\s*\)/g,
                // Return type: public ClassName methodName()
                /(?:public|protected|private)\s+(\w+)\s+\w+\s*\(/g,
                // Throws declaration: throws ClassName
                /throws\s+([\w,\s]+)/g,
                // Cast: (ClassName) object
                /\(\s*(\w+)\s*\)/g,
                // Static method calls: ClassName.method()
                /(\w+)\.\w+\s*\(/g,
                // Type declarations in generics: Map<String, ClassName>
                /[<,]\s*(\w+)\s*[>,]/g
            ];
            
            for (const pattern of patterns) {
                const matches = [...trimmedLine.matchAll(pattern)];
                for (const match of matches) {
                    for (let i = 1; i < match.length; i++) {
                        if (match[i] && isValidJavaClass(match[i])) {
                            dependencies.add(match[i]);
                        }
                    }
                }
            }
        }
        
        const primitiveAndCommonTypes = new Set([
            'void', 'int', 'long', 'double', 'float', 'boolean', 'byte', 'short', 'char',
            'String', 'Integer', 'Long', 'Double', 'Float', 'Boolean', 'Byte', 'Short', 'Character',
            'Object', 'Class', 'System', 'Math', 'StringBuilder', 'StringBuffer'
        ]);
        
        const currentClassName = getCurrentClassName(content);
        dependencies.delete(currentClassName);
        
        const finalDependencies = new Set<string>();
        for (const dep of dependencies) {
            if (!primitiveAndCommonTypes.has(dep) && dep !== currentClassName) {
                finalDependencies.add(dep);
            }
        }
        
        return finalDependencies.size;
        
    } catch (error) {
        console.error('Error in calculateEfferentCoupling:', error);
        return -1;
    }
}

function isValidJavaClass(name: string): boolean {
    if (!name || name.length === 0) {return false;}
    
    if (!/^[a-zA-Z_$][a-zA-Z0-9_$]*$/.test(name)) {return false;}
    
    const javaKeywords = new Set([
        'abstract', 'assert', 'boolean', 'break', 'byte', 'case', 'catch', 'char',
        'class', 'const', 'continue', 'default', 'do', 'double', 'else', 'enum',
        'extends', 'final', 'finally', 'float', 'for', 'goto', 'if', 'implements',
        'import', 'instanceof', 'int', 'interface', 'long', 'native', 'new', 'package',
        'private', 'protected', 'public', 'return', 'short', 'static', 'strictfp',
        'super', 'switch', 'synchronized', 'this', 'throw', 'throws', 'transient',
        'try', 'void', 'volatile', 'while', 'true', 'false', 'null'
    ]);
    
    if (javaKeywords.has(name)) {return false;}
    
    
    return true;
}

function getCurrentClassName(content: string): string {
    const classMatch = content.match(/(?:public\s+)?(?:abstract\s+)?(?:final\s+)?class\s+(\w+)/);
    if (classMatch) {return classMatch[1];}
    
    const interfaceMatch = content.match(/(?:public\s+)?interface\s+(\w+)/);
    if (interfaceMatch) {return interfaceMatch[1];}
    
    const enumMatch = content.match(/(?:public\s+)?enum\s+(\w+)/);
    if (enumMatch) {return enumMatch[1];}
    
    return '';
}