import { readFileSync } from "fs";
/**
 * Lack of Cohesion of Methods (LCOM) - Class Unity
 * 
 * Measures how well class methods use instance variables together.
 * Higher values indicate the class might need to be split.
 * 
 * Interpretation based on industry standards:
 * - Very Good: LCOM = 0 (perfect cohesion)
 * - Acceptable: 1 ≤ LCOM ≤ 2 (good cohesion)
 * - Warning: 3 ≤ LCOM ≤ 5 (low cohesion)
 * - Bad: LCOM > 5 (poor cohesion, split recommended)
 */

export function calculateLCOM(filepath: string): number {
    try {
        const content = readFileSync(filepath, { encoding: 'utf8', flag: 'r' });
        
        const methodVariableMap = new Map<string, Set<string>>();
        const instanceVars = new Set<string>();
        
        let currentMethod: string = '';
        let braceLevel = 0;
        let insideClass = false;
        
        content.split('\n').forEach(line => {
            const trimmed = line.trim();
            
            if (!insideClass && /class\s+\w+/.test(trimmed)) {
                insideClass = true;
                return;
            }
            
            if (!insideClass) {return;}
            
            if (currentMethod === '' && isInstanceVar(trimmed)) {
                const varName = getVarName(trimmed);
                if (varName) {instanceVars.add(varName);}
            }
            
            if (currentMethod === '' && isMethod(trimmed)) {
                const methodName = getMethodName(trimmed);
                if (methodName) {
                    currentMethod = methodName;
                    methodVariableMap.set(currentMethod, new Set());
                }
                braceLevel = 0;
            }
            
            if (currentMethod !== '') {
                braceLevel += (trimmed.match(/{/g) || []).length;
                braceLevel -= (trimmed.match(/}/g) || []).length;
                
                instanceVars.forEach(varName => {
                    if (new RegExp(`\\b(this\\.)?${varName}\\b`).test(trimmed)) {
                        const varSet = methodVariableMap.get(currentMethod);
                        if (varSet) {
                            varSet.add(varName);
                        }
                    }
                });
                
                if (braceLevel === 0 && trimmed.includes('}')) {
                    currentMethod = '';
                }
            }
        });
        
        return computeLCOM(methodVariableMap);
        
    } catch (error) {
        return -1;
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