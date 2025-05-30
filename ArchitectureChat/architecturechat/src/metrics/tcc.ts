import { readFileSync } from "fs";
/**
 * Tight Class Cohesion (TCC) - Internal Connectivity
 * 
 * Ratio of directly connected method pairs to total possible pairs.
 * Measures how well methods work together within a class.
 * 
 * Interpretation based on industry standards:
 * - Very Good: TCC ≥ 0.8 (highly cohesive)
 * - Acceptable: 0.5 ≤ TCC < 0.8 (good cohesion)
 * - Warning: 0.3 ≤ TCC < 0.5 (low cohesion)
 * - Bad: TCC < 0.3 (poor cohesion, consider splitting)
 */
export function calculateTightClassCohesion(filepath: string): number {
    try {
        const content = readFileSync(filepath, { encoding: 'utf8', flag: 'r' });
        
        const methods = new Map<string, Set<string>>();
        const instanceVariables = new Set<string>();
        
        let currentMethod: string = '';
        let braceLevel = 0;
        let insideClass = false;
        let insideMethod = false;
        
        const lines = content.split('\n');
        
        for (const line of lines) {
            const trimmedLine = line.trim();
            
            if (!insideClass && /(?:public\s+)?(?:abstract\s+)?(?:final\s+)?class\s+\w+/.test(trimmedLine)) {
                insideClass = true;
            }
            
            if (!insideClass) {continue;}
            
            if (!insideMethod && isInstanceVariable(trimmedLine)) {
                const varName = extractVariableName(trimmedLine);
                if (varName) {
                    instanceVariables.add(varName);
                }
            }
            
            if (!insideMethod && isMethodDeclaration(trimmedLine)) {
                const methodName = extractMethodName(trimmedLine);
                if (methodName) {
                    currentMethod = methodName;
                    methods.set(currentMethod, new Set());
                    insideMethod = true;
                    braceLevel = 0;
                }
            }
            
            if (insideMethod) {
                const openBraces = (trimmedLine.match(/{/g) || []).length;
                const closeBraces = (trimmedLine.match(/}/g) || []).length;
                braceLevel += openBraces - closeBraces;
                
                for (const variable of instanceVariables) {
                    if (isVariableAccessed(trimmedLine, variable)) {
                        methods.get(currentMethod)?.add(variable);
                    }
                }
                
                if (braceLevel === 0 && closeBraces > 0) {
                    insideMethod = false;
                    currentMethod = '';
                }
            }
        }
        
        return calculateTCCValue(methods);
        
    } catch (error) {
        return -1;
    }
}

function isInstanceVariable(line: string): boolean {
    const pattern = /^(private|protected|public)?\s*(?!static)(?!final\s+static)(?!static\s+final)\w+(?:\[\])*\s+\w+\s*(?:=.*)?;/;
    return pattern.test(line);
}

function extractVariableName(line: string): string {
    const match = line.match(/\s+(\w+)\s*(?:=.*)?;/);
    return match ? match[1] : '';
}

function isMethodDeclaration(line: string): boolean {
    const pattern = /^(public|private|protected|static|\s)*(void|int|String|boolean|double|float|long|char|byte|short|\w+)\s+\w+\s*\([^)]*\)\s*({|$)/;
    return pattern.test(line);
}

function extractMethodName(line: string): string {
    const match = line.match(/\s+(\w+)\s*\(/);
    return match ? match[1] : '';
}

function isVariableAccessed(line: string, variable: string): boolean {
    // Include: this.variable, variable direct, și accesări în expresii
    const patterns = [
        new RegExp(`\\bthis\\.${variable}\\b`),
        new RegExp(`\\b${variable}\\b(?!\\s*=\\s*new)(?!\\s*;\\s*$)`),
        new RegExp(`\\b${variable}\\s*[\\+\\-\\*\\/\\%\\=\\<\\>\\!]`),
        new RegExp(`[\\+\\-\\*\\/\\%\\=\\<\\>\\!]\\s*${variable}\\b`),
        new RegExp(`\\(.*${variable}.*\\)`),
        new RegExp(`\\[${variable}\\]`),
        new RegExp(`${variable}\\[`),
        new RegExp(`${variable}\\.\\w+`)
    ];
    
    return patterns.some(pattern => pattern.test(line));
}

function calculateTCCValue(methods: Map<string, Set<string>>): number {
    const methodList = Array.from(methods.entries());
    const numMethods = methodList.length;
    
    if (numMethods < 2) {
        return 0;
    }
    
    let connectedPairs = 0;
    let totalPairs = 0;
    
    for (let i = 0; i < numMethods; i++) {
        for (let j = i + 1; j < numMethods; j++) {
            totalPairs++;
            
            const method1Vars = methodList[i][1];
            const method2Vars = methodList[j][1];
            
            let hasCommonVariable = false;
            for (const var1 of method1Vars) {
                if (method2Vars.has(var1)) {
                    hasCommonVariable = true;
                    break;
                }
            }
            
            if (hasCommonVariable) {
                connectedPairs++;
            }
        }
    }
    
    if (totalPairs === 0) {
        return 0;
    }
    
    return connectedPairs / totalPairs;
}


