// test-java-files.ts
import { writeFileSync, unlinkSync } from 'fs';
import { calculateCyclomaticComplexity } from '../../scripts/cyclomaticComplexity';

// Funcție helper pentru a crea fișiere Java de test
function createJavaTestFile(filename: string, content: string): void {
    writeFileSync(filename, content, 'utf8');
}

// Teste pentru fișiere Java
function runJavaTests(): void {
    console.log('Testare complexitate ciclomatică pentru fișiere Java\n');
    
    // Test 1: Clasă simplă fără structuri de control
    createJavaTestFile('SimpleClass.java', `
        public class SimpleClass {
            public int getValue() {
                return 42;
            }
        }
    `);
    const result1 = calculateCyclomaticComplexity('SimpleClass.java');
    console.log(`Test 1 - Clasă simplă: Complexitate = ${result1} (așteptat: 1)`);
    
    // Test 2: Metodă cu un if
    createJavaTestFile('IfExample.java', `
        public class IfExample {
            public int checkPositive(int x) {
                if (x > 0) {
                    return x;
                }
                return 0;
            }
        }
    `);
    const result2 = calculateCyclomaticComplexity('IfExample.java');
    console.log(`Test 2 - Cu un if: Complexitate = ${result2} (așteptat: 2)`);
    
    // Test 3: Metodă complexă Java
    createJavaTestFile('ComplexMethod.java', `
        public class ComplexMethod {
            public String processData(int x, int y) {
                if (x > 0) {
                    if (y > 0) {
                        return "both positive";
                    } else if (y == 0) {
                        return "y is zero";
                    } else {
                        return "y is negative";
                    }
                } else if (x == 0) {
                    while (y > 0) {
                        y--;
                    }
                    return "x is zero";
                } else {
                    for (int i = 0; i < Math.abs(x); i++) {
                        if (i % 2 == 0) {
                            System.out.println("even");
                        }
                    }
                    return "x is negative";
                }
            }
        }
    `);
    const result3 = calculateCyclomaticComplexity('ComplexMethod.java');
    console.log(`Test 3 - Metodă complexă: Complexitate = ${result3} (așteptat: ~8-9)`);
    
    // Test 4: Switch statement în Java
    createJavaTestFile('SwitchExample.java', `
        public class SwitchExample {
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
    const result4 = calculateCyclomaticComplexity('SwitchExample.java');
    console.log(`Test 4 - Switch statement: Complexitate = ${result4} (așteptat: 6)`);
    
    // Test 5: Try-catch și operatori logici
    createJavaTestFile('TryCatchExample.java', `
        public class TryCatchExample {
            public boolean processFile(String file) {
                try {
                    if (file != null && file.length() > 0) {
                        return file.endsWith(".java") || file.endsWith(".class");
                    }
                    return false;
                } catch (Exception e) {
                    return false;
                }
            }
        }
    `);
    const result5 = calculateCyclomaticComplexity('TryCatchExample.java');
    console.log(`Test 5 - Try-catch și operatori: Complexitate = ${result5} (așteptat: 4-5)`);
    
    // Test 6: Enhanced for loop și streams
    createJavaTestFile('ModernJava.java', `
        public class ModernJava {
            public int sumPositive(List<Integer> numbers) {
                int sum = 0;
                for (Integer num : numbers) {
                    if (num > 0) {
                        sum += num;
                    }
                }
                return sum;
            }
            
            public boolean hasNegative(List<Integer> numbers) {
                return numbers.stream()
                    .anyMatch(n -> n < 0 || n == 0);
            }
        }
    `);
    const result6 = calculateCyclomaticComplexity('ModernJava.java');
    console.log(`Test 6 - Enhanced for și lambda: Complexitate = ${result6} (așteptat: 4)`);
    
    // Curăță fișierele de test
    const testFiles = [
        'SimpleClass.java',
        'IfExample.java',
        'ComplexMethod.java',
        'SwitchExample.java',
        'TryCatchExample.java',
        'ModernJava.java'
    ];
    
    testFiles.forEach(file => {
        try {
            unlinkSync(file);
        } catch(e) {}
    });
    
    console.log('\nTestele s-au finalizat!');
}

// Rulează testele
runJavaTests();