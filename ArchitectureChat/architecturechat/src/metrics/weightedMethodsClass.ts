import { readFileSync } from "fs";

/**
 * Weighted Method Count (WMC) - Class Complexity Sum
 * 
 * Sum of complexities of all methods in a class.
 * Indicates overall class complexity and maintenance difficulty.
 * 
 * Interpretation based on industry standards:
 * - Very Good: WMC ≤ 10 (simple class)
 * - Acceptable: 11 ≤ WMC ≤ 20 (moderate complexity)
 * - Warning: 21 ≤ WMC ≤ 40 (complex class)
 * - Bad: WMC > 40 (overly complex, split recommended)
 */
export function calculateWMC(filepath: string): number {
    try {
        const content = readFileSync(filepath, { encoding: 'utf8', flag: 'r' });
        
        let totalComplexity = 0;
        let currentMethodComplexity = 0;
        let insideMethod = false;
        let braceStack: number[] = [];
        
        content.split('\n').forEach(line => {
            const trimmed = line.trim();
            
            if (!insideMethod && isJavaMethod(trimmed)) {
                insideMethod = true;
                currentMethodComplexity = 1;
                braceStack = [];
            }
            
            if (insideMethod) {
                for (const char of trimmed) {
                    if (char === '{') {
                        braceStack.push(1);
                    } else if (char === '}') {
                        braceStack.pop();
                        
                        if (braceStack.length === 0) {
                            totalComplexity += currentMethodComplexity;
                            insideMethod = false;
                            currentMethodComplexity = 0;
                        }
                    }
                }
                
                if (hasControlFlow(trimmed)) {
                    currentMethodComplexity++;
                }
                
                currentMethodComplexity += countLogicalOperators(trimmed);
            }
        });
        
        return totalComplexity;
        
    } catch (error) {
        return -1;
    }
}

function isJavaMethod(line: string): boolean {
    return /^(public|private|protected|static|\s)*(void|int|String|boolean|double|float|long|char|byte|short|\w+)\s+\w+\s*\([^)]*\)\s*({|$)/.test(line);
}

function hasControlFlow(line: string): boolean {
    return /\b(if|else|for|while|do|switch|case|catch|finally)\b/.test(line);
}

function countLogicalOperators(line: string): number {
    return (line.match(/&&|\|\|/g) || []).length;
}