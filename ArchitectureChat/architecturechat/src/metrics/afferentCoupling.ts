import { readFileSync } from "fs";

/**
 * Afferent Coupling (Ca) - Incoming Dependencies
 * 
 * Measures the number of classes that depend on a given class.
 * Indicates the class's responsibility and potential impact of changes.
 * 
 * Interpretation based on industry standards:
 * - Very Good: Ca ≤ 3 (excellent isolation, minimal impact)
 * - Acceptable: 4 ≤ Ca ≤ 7 (normal level for most classes)
 * - Warning: 8 ≤ Ca ≤ 12 (needs monitoring, possible refactoring)
 * - Bad: Ca > 12 (too many dependencies, refactoring recommended)
 */
export function calculateAfferentCoupling(filepath: string): number {
    try {
        // Citește conținutul clasei țintă
        const content = readFileSync(filepath, { encoding: 'utf8', flag: 'r' });
        const targetClass = getClassName(content);
        
        if (!targetClass) {return 0;}
        
        // Configurare pentru scanarea directorului
        const path = require('path');
        const fs = require('fs');
        const dir = path.dirname(filepath);
        const currentFile = path.basename(filepath);
        
        let dependentClasses = 0;
        
        // Scanează doar fișierele .java
        const javaFiles = fs.readdirSync(dir)
            .filter((f: string) => f.endsWith('.java') && f !== currentFile);
        
        // Verifică fiecare fișier pentru dependențe
        javaFiles.forEach((file: string) => {
            const filePath = path.join(dir, file);
            try {
                const fileContent = readFileSync(filePath, { encoding: 'utf8', flag: 'r' });
                if (isDependentOn(fileContent, targetClass)) {
                    dependentClasses++;
                }
            } catch (e) {
                // Ignoră erori de citire
            }
        });
        
        return dependentClasses;
        
    } catch (error) {
        return -1;
    }
}

function getClassName(content: string): string {
    const match = content.match(/(?:public\s+)?class\s+(\w+)/);
    return match ? match[1] : '';
}

function isDependentOn(content: string, className: string): boolean {
    // Elimină comentarii și string-uri pentru analiză mai precisă
    const cleanContent = removeCommentsAndStrings(content);
    
    // Pattern-uri pentru detectarea dependențelor
    const dependencyPatterns = [
        `\\b${className}\\s+\\w+`,              // Type declaration
        `\\bnew\\s+${className}\\s*\\(`,        // Instantiation
        `\\b${className}\\.`,                   // Static access
        `\\bextends\\s+${className}\\b`,        // Inheritance
        `\\bimplements\\s+[^{]*${className}`,   // Interface
        `\\(\\s*${className}\\s+`,              // Method parameter
        `<[^>]*${className}[^>]*>`,             // Generic type
        `\\b${className}\\[\\]`,                // Array type
        `\\bimport\\s+[^;]*\\.${className}\\s*;` // Import
    ];
    
    const regex = new RegExp(dependencyPatterns.join('|'));
    return regex.test(cleanContent);
}

function removeCommentsAndStrings(content: string): string {
    // Elimină comentarii și string-uri pentru analiză mai precisă
    return content
        .replace(/\/\*[\s\S]*?\*\//g, '')  // Block comments
        .replace(/\/\/.*$/gm, '')          // Line comments
        .replace(/"([^"\\]|\\.)*"/g, '""') // String literals
        .replace(/'([^'\\]|\\.)*'/g, "''"); // Char literals
}