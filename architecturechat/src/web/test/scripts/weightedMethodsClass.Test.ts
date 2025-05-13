// test-wmc.ts
import { writeFileSync, unlinkSync } from 'fs';
import { calculateWMC } from '../../scripts/weightedMethodsClass';

function createTestFile(filename: string, content: string): void {
    writeFileSync(filename, content, 'utf8');
}

function runWMCTests(): void {
    console.log('Testing Weighted Methods per Class (WMC) Calculator\n');
    
    // Test 1: Class with single simple method
    createTestFile('SimpleClass.java', `
        public class SimpleClass {
            public int getValue() {
                return 42;
            }
        }
    `);
    const result1 = calculateWMC('SimpleClass.java');
    console.log(`Test 1 - Simple class: WMC = ${result1} (expected: 1)`);
    
    // Test 2: Class with multiple simple methods
    createTestFile('MultipleMethodsClass.java', `
        public class MultipleMethodsClass {
            public int getX() {
                return 10;
            }
            
            private void setX(int x) {
                this.x = x;
            }
            
            protected String toString() {
                return "Object";
            }
        }
    `);
    const result2 = calculateWMC('MultipleMethodsClass.java');
    console.log(`Test 2 - Multiple simple methods: WMC = ${result2} (expected: 3)`);
    
    // Test 3: Method with if statement
    createTestFile('IfMethodClass.java', `
        public class IfMethodClass {
            public int checkPositive(int x) {
                if (x > 0) {
                    return x;
                }
                return 0;
            }
        }
    `);
    const result3 = calculateWMC('IfMethodClass.java');
    console.log(`Test 3 - Method with if: WMC = ${result3} (expected: 2)`);
    
    // Test 4: Method with multiple control structures
    createTestFile('ComplexMethodClass.java', `
        public class ComplexMethodClass {
            public String processData(int x) {
                if (x > 0) {
                    for (int i = 0; i < x; i++) {
                        if (i % 2 == 0) {
                            System.out.println(i);
                        }
                    }
                    return "positive";
                } else {
                    return "negative";
                }
            }
        }
    `);
    const result4 = calculateWMC('ComplexMethodClass.java');
    console.log(`Test 4 - Complex method: WMC = ${result4} (expected: 5)`);
    
    // Test 5: Class with multiple complex methods
    createTestFile('MultipleComplexClass.java', `
        public class MultipleComplexClass {
            public int calculate(int a, int b) {
                if (a > b) {
                    return a - b;
                } else if (a < b) {
                    return b - a;
                } else {
                    return 0;
                }
            }
            
            public void processArray(int[] arr) {
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i] < 0) {
                        arr[i] = -arr[i];
                    }
                }
            }
            
            private boolean validate(String str) {
                if (str != null && str.length() > 0) {
                    return true;
                }
                return false;
            }
        }
    `);
    const result5 = calculateWMC('MultipleComplexClass.java');
    console.log(`Test 5 - Multiple complex methods: WMC = ${result5} (expected: 9)`);
    
    // Test 6: Method with switch statement
    createTestFile('SwitchMethodClass.java', `
        public class SwitchMethodClass {
            public String getDayName(int day) {
                switch (day) {
                    case 1:
                        return "Monday";
                    case 2:
                        return "Tuesday";
                    case 3:
                        return "Wednesday";
                    case 4:
                        return "Thursday";
                    case 5:
                        return "Friday";
                    default:
                        return "Weekend";
                }
            }
        }
    `);
    const result6 = calculateWMC('SwitchMethodClass.java');
    console.log(`Test 6 - Switch method: WMC = ${result6} (expected: 7)`);
    
    // Test 7: Method with logical operators
    createTestFile('LogicalOperatorsClass.java', `
        public class LogicalOperatorsClass {
            public boolean checkConditions(int x, int y, int z) {
                if (x > 0 && y > 0) {
                    return true;
                }
                if (x < 0 || y < 0 || z < 0) {
                    return false;
                }
                return x == 0 && y == 0 && z == 0;
            }
        }
    `);
    const result7 = calculateWMC('LogicalOperatorsClass.java');
    console.log(`Test 7 - Logical operators: WMC = ${result7} (expected: 8)`);
    
    // Test 8: Nested loops and conditions
    createTestFile('NestedComplexClass.java', `
        public class NestedComplexClass {
            public int findMax(int[][] matrix) {
                int max = Integer.MIN_VALUE;
                for (int i = 0; i < matrix.length; i++) {
                    for (int j = 0; j < matrix[i].length; j++) {
                        if (matrix[i][j] > max) {
                            max = matrix[i][j];
                        }
                    }
                }
                return max;
            }
            
            public void printPattern(int n) {
                for (int i = 1; i <= n; i++) {
                    for (int j = 1; j <= i; j++) {
                        System.out.print("*");
                    }
                    System.out.println();
                }
            }
        }
    `);
    const result8 = calculateWMC('NestedComplexClass.java');
    console.log(`Test 8 - Nested structures: WMC = ${result8} (expected: 7)`);
    
    // Test 9: Try-catch blocks
    createTestFile('ExceptionHandlingClass.java', `
        public class ExceptionHandlingClass {
            public String readFile(String filename) {
                try {
                    if (filename == null) {
                        throw new IllegalArgumentException();
                    }
                    return "content";
                } catch (IllegalArgumentException e) {
                    return "error";
                } finally {
                    System.out.println("cleanup");
                }
            }
        }
    `);
    const result9 = calculateWMC('ExceptionHandlingClass.java');
    console.log(`Test 9 - Exception handling: WMC = ${result9} (expected: 4)`);
    
    // Test 10: Large class with many methods
    createTestFile('LargeClass.java', `
        public class LargeClass {
            public void method1() {
                if (true) {
                    System.out.println("1");
                }
            }
            
            public int method2(int x) {
                for (int i = 0; i < x; i++) {
                    if (i % 2 == 0) {
                        continue;
                    }
                }
                return x;
            }
            
            private boolean method3(String s) {
                return s != null && s.length() > 0;
            }
            
            protected void method4() {
                int x = 0;
                while (x < 10) {
                    x++;
                }
            }
            
            public static void method5(int[] arr) {
                for (int val : arr) {
                    switch (val) {
                        case 1:
                        case 2:
                            break;
                        default:
                            continue;
                    }
                }
            }
        }
    `);
    const result10 = calculateWMC('LargeClass.java');
    console.log(`Test 10 - Large class: WMC = ${result10} (expected: 13)`);
    
    // Clean up test files
    const testFiles = [
        'SimpleClass.java',
        'MultipleMethodsClass.java',
        'IfMethodClass.java',
        'ComplexMethodClass.java',
        'MultipleComplexClass.java',
        'SwitchMethodClass.java',
        'LogicalOperatorsClass.java',
        'NestedComplexClass.java',
        'ExceptionHandlingClass.java',
        'LargeClass.java'
    ];
    
    testFiles.forEach(file => {
        try {
            unlinkSync(file);
        } catch(e) {}
    });
    
    console.log('\nAll WMC tests completed!');
}

// Run the tests
runWMCTests();