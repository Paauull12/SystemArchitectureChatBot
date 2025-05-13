// test-afferent-coupling.ts
import { writeFileSync, unlinkSync, mkdirSync, rmdirSync } from 'fs';
import { calculateAfferentCoupling } from '../../scripts/afferentCoupling';
import * as path from 'path';

const TEST_DIR = './test-ca';

function setupTestDirectory(): void {
    try {
        mkdirSync(TEST_DIR);
    } catch (e) {
        // Directory already exists
    }
}

function cleanupTestDirectory(): void {
    const fs = require('fs');
    const files = fs.readdirSync(TEST_DIR);
    files.forEach((file: string) => {
        unlinkSync(path.join(TEST_DIR, file));
    });
    rmdirSync(TEST_DIR);
}

function createTestFile(filename: string, content: string): void {
    writeFileSync(path.join(TEST_DIR, filename), content, 'utf8');
}

function runAfferentCouplingTests(): void {
    console.log('Testing Afferent Coupling (Ca) Calculator\n');
    
    setupTestDirectory();
    
    // Test 1: No dependencies - isolated class
    createTestFile('IndependentClass.java', `
        public class IndependentClass {
            private int value;
            
            public int getValue() {
                return value;
            }
        }
    `);
    
    createTestFile('UnrelatedClass.java', `
        public class UnrelatedClass {
            private String name;
            
            public String getName() {
                return name;
            }
        }
    `);
    
    const result1 = calculateAfferentCoupling(path.join(TEST_DIR, 'IndependentClass.java'));
    console.log(`Test 1 - No dependencies: Ca = ${result1} (expected: 0)`);
    
    // Test 2: One class depends on target
    createTestFile('TargetClass.java', `
        public class TargetClass {
            public void doSomething() {
                System.out.println("Target");
            }
        }
    `);
    
    createTestFile('DependentClass1.java', `
        public class DependentClass1 {
            private TargetClass target;  // +1 dependency
            
            public void useTarget() {
                target = new TargetClass();
                target.doSomething();
            }
        }
    `);
    
    const result2 = calculateAfferentCoupling(path.join(TEST_DIR, 'TargetClass.java'));
    console.log(`Test 2 - One dependency: Ca = ${result2} (expected: 1)`);
    
    // Test 3: Multiple classes depend on target
    createTestFile('DependentClass2.java', `
        public class DependentClass2 {
            public void process(TargetClass tc) {  // +1 dependency
                tc.doSomething();
            }
        }
    `);
    
    createTestFile('DependentClass3.java', `
        public class DependentClass3 extends TargetClass {  // +1 dependency
            @Override
            public void doSomething() {
                super.doSomething();
                System.out.println("Extended");
            }
        }
    `);
    
    const result3 = calculateAfferentCoupling(path.join(TEST_DIR, 'TargetClass.java'));
    console.log(`Test 3 - Multiple dependencies: Ca = ${result3} (expected: 3)`);
    
    // Test 4: Static method access
    createTestFile('UtilityClass.java', `
        public class UtilityClass {
            public static void utilMethod() {
                System.out.println("Utility");
            }
        }
    `);
    
    createTestFile('StaticUser.java', `
        public class StaticUser {
            public void useStatic() {
                UtilityClass.utilMethod();  // +1 dependency
            }
        }
    `);
    
    const result4 = calculateAfferentCoupling(path.join(TEST_DIR, 'UtilityClass.java'));
    console.log(`Test 4 - Static access: Ca = ${result4} (expected: 1)`);
    
    // Test 5: Generic types
    createTestFile('GenericClass.java', `
        public class GenericClass<T> {
            private T data;
            public T getData() { return data; }
        }
    `);
    
    createTestFile('GenericUser.java', `
        public class GenericUser {
            private GenericClass<String> stringContainer;  // +1 dependency
            private GenericClass<Integer> intContainer;    // same class, no additional count
            
            public void useGenerics() {
                stringContainer = new GenericClass<String>();
                intContainer = new GenericClass<Integer>();
            }
        }
    `);
    
    const result5 = calculateAfferentCoupling(path.join(TEST_DIR, 'GenericClass.java'));
    console.log(`Test 5 - Generic usage: Ca = ${result5} (expected: 1)`);
    
    // Test 6: Array types
    createTestFile('ArrayClass.java', `
        public class ArrayClass {
            public void processArray() {
                // Process array
            }
        }
    `);
    
    createTestFile('ArrayUser.java', `
        public class ArrayUser {
            private ArrayClass[] arrayOfClasses;  // +1 dependency
            
            public void useArrays() {
                arrayOfClasses = new ArrayClass[10];
                arrayOfClasses[0] = new ArrayClass();
            }
        }
    `);
    
    const result6 = calculateAfferentCoupling(path.join(TEST_DIR, 'ArrayClass.java'));
    console.log(`Test 6 - Array types: Ca = ${result6} (expected: 1)`);
    
    // Test 7: Interface implementation
    createTestFile('MyInterface.java', `
        public interface MyInterface {
            void interfaceMethod();
        }
    `);
    
    createTestFile('InterfaceImpl1.java', `
        public class InterfaceImpl1 implements MyInterface {  // +1 dependency
            public void interfaceMethod() {
                System.out.println("Implementation 1");
            }
        }
    `);
    
    createTestFile('InterfaceImpl2.java', `
        public class InterfaceImpl2 implements MyInterface {  // +1 dependency
            public void interfaceMethod() {
                System.out.println("Implementation 2");
            }
        }
    `);
    
    const result7 = calculateAfferentCoupling(path.join(TEST_DIR, 'MyInterface.java'));
    console.log(`Test 7 - Interface implementations: Ca = ${result7} (expected: 2)`);
    
    // Test 8: Comments and strings should be ignored
    createTestFile('CommentTestClass.java', `
        public class CommentTestClass {
            public void testMethod() {
                System.out.println("Test");
            }
        }
    `);
    
    createTestFile('CommentUser.java', `
        public class CommentUser {
            // This comment mentions CommentTestClass but shouldn't count
            /* This block comment also mentions CommentTestClass */
            private String description = "Uses CommentTestClass in string";
            
            public void validUse() {
                CommentTestClass ctc = new CommentTestClass();  // +1 only valid dependency
                ctc.testMethod();
            }
        }
    `);
    
    const result8 = calculateAfferentCoupling(path.join(TEST_DIR, 'CommentTestClass.java'));
    console.log(`Test 8 - Comments and strings: Ca = ${result8} (expected: 1)`);
    
    // Test 9: Import dependencies
    createTestFile('ImportedClass.java', `
        package com.example;
        
        public class ImportedClass {
            public void importedMethod() {
                System.out.println("Imported");
            }
        }
    `);
    
    createTestFile('ImportUser.java', `
        package com.example;
        
        import com.example.ImportedClass;  // +1 dependency
        
        public class ImportUser {
            private ImportedClass imported;
            
            public void useImported() {
                imported = new ImportedClass();
            }
        }
    `);
    
    const result9 = calculateAfferentCoupling(path.join(TEST_DIR, 'ImportedClass.java'));
    console.log(`Test 9 - Import dependencies: Ca = ${result9} (expected: 1)`);
    
    // Test 10: Complex dependency patterns
    createTestFile('CoreClass.java', `
        public class CoreClass {
            public void coreMethod() {
                System.out.println("Core");
            }
        }
    `);
    
    createTestFile('ComplexDependent.java', `
        public class ComplexDependent extends CoreClass {
            private CoreClass coreInstance;      // Multiple uses but still
            private CoreClass[] coreArray;       // one dependent class
            
            public void complexMethod(CoreClass param) {
                coreInstance = new CoreClass();
                CoreClass local = param;
                coreArray = new CoreClass[5];
                CoreClass.class.getName();
            }
            
            public CoreClass returnCore() {
                return new CoreClass();
            }
        }
    `);
    
    const result10 = calculateAfferentCoupling(path.join(TEST_DIR, 'CoreClass.java'));
    console.log(`Test 10 - Complex dependencies: Ca = ${result10} (expected: 1)`);
    
    // Clean up test files
    cleanupTestDirectory();
    
    console.log('\nAll tests completed!');
}

// Run the tests
runAfferentCouplingTests();