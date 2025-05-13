// test-cognitive-complexity.ts
import { writeFileSync, unlinkSync } from 'fs';
import { calculateCognitiveComplexity } from '../../scripts/cognitiveComplexity';

function createTestFile(filename: string, content: string): void {
    writeFileSync(filename, content, 'utf8');
}

function runCognitiveTests(): void {
    console.log('Testing Cognitive Complexity Calculator\n');
    
    // Test 1: Simple function - no complexity
    createTestFile('simple.java', `
        public class Simple {
            public int getValue() {
                return 42;
            }
        }
    `);
    const result1 = calculateCognitiveComplexity('simple.java');
    console.log(`Test 1 - Simple function: Complexity = ${result1} (expected: 0)`);
    
    // Test 2: Single if statement
    createTestFile('singleIf.java', `
        public class SingleIf {
            public int check(int x) {
                if (x > 0) {      // +1
                    return x;
                }
                return 0;
            }
        }
    `);
    const result2 = calculateCognitiveComplexity('singleIf.java');
    console.log(`Test 2 - Single if: Complexity = ${result2} (expected: 1)`);
    
    // Test 3: Nested if statements
    createTestFile('nestedIf.java', `
        public class NestedIf {
            public String check(int x, int y) {
                if (x > 0) {              // +1
                    if (y > 0) {          // +2 (1 + 1 nesting)
                        return "both";
                    }
                    return "x only";
                }
                return "none";
            }
        }
    `);
    const result3 = calculateCognitiveComplexity('nestedIf.java');
    console.log(`Test 3 - Nested if: Complexity = ${result3} (expected: 3)`);
    
    // Test 4: if-else chain
    createTestFile('ifElseChain.java', `
        public class IfElseChain {
            public String grade(int score) {
                if (score >= 90) {        // +1
                    return "A";
                } else if (score >= 80) { // +2 (1 + 1 chain)
                    return "B";
                } else if (score >= 70) { // +2 (1 + 1 chain)
                    return "C";
                } else {                  // +1
                    return "F";
                }
            }
        }
    `);
    const result4 = calculateCognitiveComplexity('ifElseChain.java');
    console.log(`Test 4 - If-else chain: Complexity = ${result4} (expected: 6)`);
    
    // Test 5: Nested loops
    createTestFile('nestedLoops.java', `
        public class NestedLoops {
            public void process() {
                for (int i = 0; i < 10; i++) {     // +1
                    for (int j = 0; j < 10; j++) {  // +2 (1 + 1 nesting)
                        if (i == j) {              // +3 (1 + 2 nesting)
                            System.out.println(i);
                        }
                    }
                }
            }
        }
    `);
    const result5 = calculateCognitiveComplexity('nestedLoops.java');
    console.log(`Test 5 - Nested loops: Complexity = ${result5} (expected: 6)`);
    
    // Test 6: Logical operators
    createTestFile('logicalOps.java', `
        public class LogicalOps {
            public boolean check(int a, int b, int c) {
                if (a > 0 && b > 0) {      // +1 for if, +1 for &&
                    return true;
                }
                if (a > 0 || b > 0 || c > 0) { // +1 for if, +2 for ||
                    return true;
                }
                return false;
            }
        }
    `);
    const result6 = calculateCognitiveComplexity('logicalOps.java');
    console.log(`Test 6 - Logical operators: Complexity = ${result6} (expected: 5)`);
    
    // Test 7: Switch statement
    createTestFile('switchCase.java', `
        public class SwitchCase {
            public String getDayName(int day) {
                switch (day) {            // +1
                    case 1:
                        return "Monday";
                    case 2:
                        return "Tuesday";
                    case 3:
                        return "Wednesday";
                    default:
                        return "Other";
                }
            }
        }
    `);
    const result7 = calculateCognitiveComplexity('switchCase.java');
    console.log(`Test 7 - Switch statement: Complexity = ${result7} (expected: 1)`);
    
    // Test 8: Try-catch blocks
    createTestFile('tryCatch.java', `
        public class TryCatch {
            public void processFile(String file) {
                try {
                    if (file != null) {    // +1
                        readFile(file);
                    }
                } catch (IOException e) {  // +1
                    if (e != null) {       // +2 (1 + 1 nesting)
                        log(e);
                    }
                } finally {               // +1
                    cleanup();
                }
            }
        }
    `);
    const result8 = calculateCognitiveComplexity('tryCatch.java');
    console.log(`Test 8 - Try-catch: Complexity = ${result8} (expected: 5)`);
    
    // Test 9: Complex nested structure
    createTestFile('complex.java', `
        public class Complex {
            public int process(int[] arr) {
                int result = 0;
                for (int i = 0; i < arr.length; i++) {       // +1
                    if (arr[i] > 0) {                        // +2 (1 + 1 nesting)
                        for (int j = i; j < arr.length; j++) { // +3 (1 + 2 nesting)
                            if (arr[j] % 2 == 0) {           // +4 (1 + 3 nesting)
                                result += arr[j];
                            } else if (arr[j] % 3 == 0) {    // +5 (1 + 1 chain + 3 nesting)
                                result -= arr[j];
                            }
                        }
                    } else if (arr[i] < 0) {                 // +3 (1 + 1 chain + 1 nesting)
                        result = result || arr[i];           // +1 for ||
                    }
                }
                return result;
            }
        }
    `);
    const result9 = calculateCognitiveComplexity('complex.java');
    console.log(`Test 9 - Complex nested: Complexity = ${result9} (expected: ~19)`);
    
    // Test 10: Ternary operators
    createTestFile('ternary.java', `
        public class Ternary {
            public String check(int x, int y) {
                return x > 0 ? (y > 0 ? "both" : "x") : "none"; // +2 for ternary
            }
        }
    `);
    const result10 = calculateCognitiveComplexity('ternary.java');
    console.log(`Test 10 - Ternary operators: Complexity = ${result10} (expected: 2)`);
    
    // Clean up test files
    const testFiles = [
        'simple.java',
        'singleIf.java',
        'nestedIf.java',
        'ifElseChain.java',
        'nestedLoops.java',
        'logicalOps.java',
        'switchCase.java',
        'tryCatch.java',
        'complex.java',
        'ternary.java'
    ];
    
    testFiles.forEach(file => {
        try {
            unlinkSync(file);
        } catch(e) {}
    });
    
    console.log('\nAll tests completed!');
}

// Run the tests
runCognitiveTests();