// calculateCyclomaticComplexity.ts
import { readFileSync } from 'fs';

function calculateCyclomaticComplexity(filepath: string): number {
    try {
        // Citește conținutul fișierului
        const content = readFileSync(filepath, 'utf8');
        
        // Inițializează complexitatea cu valoarea de bază (1)
        let complexity = 1;
        
        // Expresii regulate pentru structuri de control în Java
        const controlStructures = [
            /\bif\s*\(/g,           // if statements
            /\belse\s+if\s*\(/g,    // else if (Java style)
            /\bwhile\s*\(/g,        // while loops
            /\bfor\s*\(/g,          // for loops (including enhanced for)
            /\bcase\s+[^:]+:/g,     // case statements
            /\bcatch\s*\(/g,        // catch blocks
            /\b\?\s*[^:]+:/g,       // ternary operators
            /\&\&/g,                // logical AND
            /\|\|/g                 // logical OR
        ];
        
        // Calculează complexitatea parcurgând codul
        controlStructures.forEach(pattern => {
            const matches = content.match(pattern);
            if (matches) {
                complexity += matches.length;
            }
        });
        
        return complexity;
        
    } catch (error) {
        console.error(`Error reading file ${filepath}:`, error);
        return -1;
    }
}

export { calculateCyclomaticComplexity };