import { readFileSync } from 'fs';

export function calculateCognitiveComplexity(filepath: string): number {
    try {
        // Doar citește, nu modifică
        const content = readFileSync(filepath, 'utf8');
        
        let complexity = 0;
        let nestingLevel = 0;
        const nestingStack: string[] = [];
        
        // Procesare în memorie
        const lines = content.split('\n');
        
        for (let i = 0; i < lines.length; i++) {
            const line = lines[i].trim();
            
            // Detectează deschiderea de blocuri
            if (line.includes('{')) {
                const controlMatch = line.match(/\b(if|for|while|switch|try|catch)\s*\(/);
                if (controlMatch) {
                    nestingStack.push(controlMatch[1]);
                    nestingLevel = nestingStack.length;
                }
            }
            
            // Detectează închiderea de blocuri
            if (line.includes('}')) {
                if (nestingStack.length > 0) {
                    nestingStack.pop();
                    nestingLevel = nestingStack.length;
                }
            }
            
            // Calculează complexitatea
            const controlPattern = /\b(if|else\s+if|else|for|while|do|switch|catch|finally)\b/;
            if (controlPattern.test(line)) {
                complexity += 1 + nestingLevel;
                
                // Penalizări suplimentare
                if (/\belse\s+if\b/.test(line)) {
                    complexity += 1;
                }
            }
            
            // Operatori logici
            complexity += (line.match(/\&\&|\|\|/g) || []).length;
            
            // Operatori ternari
            complexity += (line.match(/\?[^:]*:/g) || []).length;
        }
        
        return complexity;
        
    } catch (error) {
        // Returnează eroare fără a afecta fișierul
        return -1;
    }
}