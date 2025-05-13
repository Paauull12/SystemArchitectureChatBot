// test-lcom.ts
import { writeFileSync, unlinkSync } from 'fs';
import { calculateLCOM } from '../../scripts/lcom';

function createTestFile(filename: string, content: string): void {
    writeFileSync(filename, content, 'utf8');
}

function runLCOMTests(): void {
    console.log('Testing Lack of Cohesion in Methods (LCOM) Calculator\n');
    
    // Test 1: Perfect cohesion - all methods use all instance variables
    createTestFile('PerfectCohesion.java', `
        public class PerfectCohesion {
            private int x;
            private int y;
            
            public void setValues(int a, int b) {
                this.x = a;
                this.y = b;
            }
            
            public int getSum() {
                return x + y;
            }
            
            public int getProduct() {
                return x * y;
            }
        }
    `);
    const result1 = calculateLCOM('PerfectCohesion.java');
    console.log(`Test 1 - Perfect cohesion: LCOM = ${result1} (expected: 0)`);
    
    // Test 2: No cohesion - methods use different variables
    createTestFile('NoCohesion.java', `
        public class NoCohesion {
            private int x;
            private int y;
            private String name;
            
            public void setX(int value) {
                this.x = value;
            }
            
            public void setY(int value) {
                this.y = value;
            }
            
            public void setName(String n) {
                this.name = n;
            }
        }
    `);
    const result2 = calculateLCOM('NoCohesion.java');
    console.log(`Test 2 - No cohesion: LCOM = ${result2} (expected: 3)`);
    
    // Test 3: Partial cohesion
    createTestFile('PartialCohesion.java', `
        public class PartialCohesion {
            private int width;
            private int height;
            private String color;
            
            public int getArea() {
                return width * height;
            }
            
            public int getPerimeter() {
                return 2 * (width + height);
            }
            
            public void setColor(String c) {
                this.color = c;
            }
            
            public String getColor() {
                return color;
            }
        }
    `);
    const result3 = calculateLCOM('PartialCohesion.java');
    console.log(`Test 3 - Partial cohesion: LCOM = ${result3} (expected: 2)`);
    
    // Test 4: Single method class
    createTestFile('SingleMethod.java', `
        public class SingleMethod {
            private int value;
            
            public int getValue() {
                return value;
            }
        }
    `);
    const result4 = calculateLCOM('SingleMethod.java');
    console.log(`Test 4 - Single method: LCOM = ${result4} (expected: 0)`);
    
    // Test 5: Methods with no variable usage
    createTestFile('NoVariableUsage.java', `
        public class NoVariableUsage {
            private int x;
            private int y;
            
            public void printHello() {
                System.out.println("Hello");
            }
            
            public int returnConstant() {
                return 42;
            }
            
            public void doNothing() {
                // Empty method
            }
        }
    `);
    const result5 = calculateLCOM('NoVariableUsage.java');
    console.log(`Test 5 - No variable usage: LCOM = ${result5} (expected: 3)`);
    
    // Test 6: Complex class with groups of methods
    createTestFile('ComplexGroups.java', `
        public class ComplexGroups {
            private int x;
            private int y;
            private String firstName;
            private String lastName;
            
            // Group 1: Coordinate methods
            public void setCoordinates(int a, int b) {
                this.x = a;
                this.y = b;
            }
            
            public double getDistance() {
                return Math.sqrt(x * x + y * y);
            }
            
            // Group 2: Name methods
            public void setFullName(String first, String last) {
                this.firstName = first;
                this.lastName = last;
            }
            
            public String getFullName() {
                return firstName + " " + lastName;
            }
            
            public String getInitials() {
                return firstName.charAt(0) + "." + lastName.charAt(0) + ".";
            }
        }
    `);
    const result6 = calculateLCOM('ComplexGroups.java');
    console.log(`Test 6 - Complex groups: LCOM = ${result6} (expected: 6)`);
    
    // Test 7: Methods using multiple variables
    createTestFile('MultipleVariableUsage.java', `
        public class MultipleVariableUsage {
            private int width;
            private int height;
            private int depth;
            
            public int getVolume() {
                return width * height * depth;
            }
            
            public int getSurfaceArea() {
                return 2 * (width * height + width * depth + height * depth);
            }
            
            public void scale(int factor) {
                width *= factor;
                height *= factor;
                depth *= factor;
            }
        }
    `);
    const result7 = calculateLCOM('MultipleVariableUsage.java');
    console.log(`Test 7 - Multiple variable usage: LCOM = ${result7} (expected: 0)`);
    
    // Test 8: Inheritance scenario
    createTestFile('InheritanceTest.java', `
        public class InheritanceTest extends BaseClass {
            private String data;
            private int count;
            
            public void setData(String d) {
                this.data = d;
                count++;
            }
            
            public String getData() {
                return data;
            }
            
            public int getCount() {
                return count;
            }
            
            public void reset() {
                data = null;
                count = 0;
            }
        }
    `);
    const result8 = calculateLCOM('InheritanceTest.java');
    console.log(`Test 8 - Inheritance: LCOM = ${result8} (expected: 0)`);
    
    // Test 9: Static and instance methods mixed
    createTestFile('StaticMixed.java', `
        public class StaticMixed {
            private int instanceVar;
            private static int staticVar;
            
            public void setInstance(int val) {
                this.instanceVar = val;
            }
            
            public int getInstance() {
                return instanceVar;
            }
            
            public static void setStatic(int val) {
                staticVar = val;
            }
            
            public static int getStatic() {
                return staticVar;
            }
        }
    `);
    const result9 = calculateLCOM('StaticMixed.java');
    console.log(`Test 9 - Static mixed: LCOM = ${result9} (expected: 0)`);
    
    // Test 10: Maximum cohesion issue
    createTestFile('MaximumCohesionIssue.java', `
        public class MaximumCohesionIssue {
            private int a;
            private int b;
            private int c;
            private int d;
            private int e;
            
            public void useA() { this.a = 1; }
            public void useB() { this.b = 2; }
            public void useC() { this.c = 3; }
            public void useD() { this.d = 4; }
            public void useE() { this.e = 5; }
        }
    `);
    const result10 = calculateLCOM('MaximumCohesionIssue.java');
    console.log(`Test 10 - Maximum cohesion issue: LCOM = ${result10} (expected: 10)`);
    
    // Clean up test files
    const testFiles = [
        'PerfectCohesion.java',
        'NoCohesion.java',
        'PartialCohesion.java',
        'SingleMethod.java',
        'NoVariableUsage.java',
        'ComplexGroups.java',
        'MultipleVariableUsage.java',
        'InheritanceTest.java',
        'StaticMixed.java',
        'MaximumCohesionIssue.java'
    ];
    
    testFiles.forEach(file => {
        try {
            unlinkSync(file);
        } catch(e) {}
    });
    
    console.log('\nAll LCOM tests completed!');
}

// Run the tests
runLCOMTests();