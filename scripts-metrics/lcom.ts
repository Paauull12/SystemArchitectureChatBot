import { readFileSync } from "fs";

export function calculateLCOM(filepath: string): number {
    try {
        // Citire read-only cu flag explicit
        const content = readFileSync(filepath, { encoding: 'utf8', flag: 'r' });
        
        // Totul se procesează în memorie
        const methodVariableMap = new Map<string, Set<string>>();
        const instanceVars = new Set<string>();
        
        let currentMethod: string = ''; // String gol în loc de null
        let braceLevel = 0;
        let insideClass = false;
        
        // Analiză line-by-line în memorie
        content.split('\n').forEach(line => {
            const trimmed = line.trim();
            
            // Start clasă
            if (!insideClass && /class\s+\w+/.test(trimmed)) {
                insideClass = true;
                return;
            }
            
            if (!insideClass) {return;}
            
            // Variabile instanță (doar în afara metodelor)
            if (currentMethod === '' && isInstanceVar(trimmed)) {
                const varName = getVarName(trimmed);
                if (varName) {instanceVars.add(varName);}
            }
            
            // Start metodă
            if (currentMethod === '' && isMethod(trimmed)) {
                const methodName = getMethodName(trimmed);
                if (methodName) {
                    currentMethod = methodName;
                    methodVariableMap.set(currentMethod, new Set());
                }
                braceLevel = 0;
            }
            
            // În metodă
            if (currentMethod !== '') {
                // Track braces
                braceLevel += (trimmed.match(/{/g) || []).length;
                braceLevel -= (trimmed.match(/}/g) || []).length;
                
                // Găsește variabile folosite
                instanceVars.forEach(varName => {
                    if (new RegExp(`\\b(this\\.)?${varName}\\b`).test(trimmed)) {
                        const varSet = methodVariableMap.get(currentMethod);
                        if (varSet) {
                            varSet.add(varName);
                        }
                    }
                });
                
                // End metodă
                if (braceLevel === 0 && trimmed.includes('}')) {
                    currentMethod = '';
                }
            }
        });
        
        // Calculează LCOM
        return computeLCOM(methodVariableMap);
        
    } catch (error) {
        return -1; // Eroare silențioasă
    }
}

function isInstanceVar(line: string): boolean {
    return /^(private|protected|public)?\s*(?!static)\w+(\[])*\s+\w+\s*(=.*)?;/.test(line);
}

function getVarName(line: string): string {
    const match = line.match(/\s+(\w+)\s*(=.*)?;/);
    return match ? match[1] : '';
}

function isMethod(line: string): boolean {
    return /^(public|private|protected|static|\s)*(void|int|String|boolean|double|float|long|char|byte|short|\w+)\s+\w+\s*\([^)]*\)\s*({|$)/.test(line);
}

function getMethodName(line: string): string {
    const match = line.match(/\s+(\w+)\s*\(/);
    return match ? match[1] : '';
}

function computeLCOM(methodVarMap: Map<string, Set<string>>): number {
    const methods = Array.from(methodVarMap.values());
    const n = methods.length;
    
    if (n < 2) {return 0;}
    
    let disjoint = 0;
    let shared = 0;
    
    for (let i = 0; i < n; i++) {
        for (let j = i + 1; j < n; j++) {
            let hasCommon = false;
            
            for (const var1 of methods[i]) {
                if (methods[j].has(var1)) {
                    hasCommon = true;
                    break;
                }
            }
            
            if (hasCommon) {
                shared++;
            } else {
                disjoint++;
            }
        }
    }
    
    return Math.max(0, disjoint - shared);
}