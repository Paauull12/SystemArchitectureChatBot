import { readdirSync, readFileSync } from "fs";
import path from "path";
/**
 * Instability (I) - Change Susceptibility
 * 
 * Ratio of efferent coupling to total coupling (Ce/(Ca+Ce)).
 * Measures the class's resilience to change (0=stable, 1=unstable).
 * 
 * Interpretation based on industry standards:
 * - Very Good: I ≤ 0.2 (very stable)
 * - Acceptable: 0.2 < I ≤ 0.5 (balanced)
 * - Warning: 0.5 < I ≤ 0.8 (unstable)
 * - Bad: I > 0.8 (highly unstable)
 */
export function calculateInstabilityDirect(filepath: string): number {
    try {
        const content = readFileSync(filepath, { encoding: 'utf8', flag: 'r' });
        const currentClassName = extractClassName(content);
        
        if (!currentClassName) {
            return -1;
        }
        
        // Calculează Ce - dependențe efferente
        const efferentDependencies = new Set<string>();
        
        // Pattern-uri pentru dependențe efferente (Ce)
        const importPattern = /import\s+(static\s+)?([^;]+);/g;
        const imports = [...content.matchAll(importPattern)];
        
        for (const match of imports) {
            const importPath = match[2].trim();
            const parts = importPath.split('.');
            const className = parts[parts.length - 1];
            
            if (className !== '*' && !importPath.startsWith('java.')) {
                efferentDependencies.add(className);
            }
        }
        
        // Caută alte tipuri de dependențe în cod
        const patterns = [
            /extends\s+(\w+)/g,
            /implements\s+([^{]+)/g,
            /(\w+)\s+\w+\s*[;=]/g,
            /<(\w+)>/g,
            /new\s+(\w+)\s*\(/g
        ];
        
        for (const pattern of patterns) {
            const matches = [...content.matchAll(pattern)];
            for (const match of matches) {
                if (match[1] && isValidClassName(match[1])) {
                    efferentDependencies.add(match[1]);
                }
            }
        }
        
        // Elimină self-references și tipuri comune
        efferentDependencies.delete(currentClassName);
        const commonTypes = new Set(['String', 'Integer', 'Boolean', 'Object', 'List', 'Map']);
        for (const common of commonTypes) {
            efferentDependencies.delete(common);
        }
        
        const ce = efferentDependencies.size;
        
        // Calculează Ca - dependențe afferente
        const directory = path.dirname(filepath);
        const files = readdirSync(directory).filter(file => file.endsWith('.java'));
        let ca = 0;
        
        for (const file of files) {
            if (file === path.basename(filepath)) {continue;}
            
            const otherFilePath = path.join(directory, file);
            try {
                const otherContent = readFileSync(otherFilePath, { encoding: 'utf8', flag: 'r' });
                
                // Verifică dacă fișierul curent depinde de clasa noastră
                if (hasDependencyOn(otherContent, currentClassName)) {
                    ca++;
                }
            } catch (e) {
                // Ignoră fișierele care nu pot fi citite
            }
        }
        
        // Calculează instabilitatea
        const total = ce + ca;
        if (total === 0) {
            return 0;
        }
        
        return ce / total;
        
    } catch (error) {
        return -1;
    }
}

function extractClassName(content: string): string {
    const classMatch = content.match(/(?:public\s+)?(?:abstract\s+)?(?:final\s+)?class\s+(\w+)/);
    if (classMatch) {return classMatch[1];}
    
    const interfaceMatch = content.match(/(?:public\s+)?interface\s+(\w+)/);
    if (interfaceMatch) {return interfaceMatch[1];}
    
    return '';
}

function isValidClassName(name: string): boolean {
    if (!name || name.length === 0) {return false;}
    if (!/^[A-Z]/.test(name)) {return false;}
    if (!/^[a-zA-Z_$][a-zA-Z0-9_$]*$/.test(name)) {return false;}
    
    const keywords = new Set(['class', 'interface', 'enum', 'extends', 'implements']);
    return !keywords.has(name);
}

function hasDependencyOn(content: string, targetClass: string): boolean {
    const patterns = [
        new RegExp(`\\b${targetClass}\\s+\\w+`),
        new RegExp(`\\bnew\\s+${targetClass}\\s*\\(`),
        new RegExp(`\\b${targetClass}\\.\\w+`),
        new RegExp(`\\bextends\\s+${targetClass}\\b`),
        new RegExp(`\\bimplements\\s+.*${targetClass}`),
        new RegExp(`\\(\\s*${targetClass}\\s+\\w+\\s*\\)`),
        new RegExp(`<\\s*${targetClass}\\s*>`),
        new RegExp(`\\b${targetClass}\\[\\]`)
    ];
    
    return patterns.some(pattern => pattern.test(content));
}

