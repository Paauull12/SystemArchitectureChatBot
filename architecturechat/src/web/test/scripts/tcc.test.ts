import { writeFileSync, unlinkSync, mkdirSync, rmdirSync } from 'fs';
import * as path from 'path';
import { calculateTightClassCohesion } from '../../scripts/tcc';

const TEST_DIR = './test-tcc';

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

function runTCCTests(): void {
    console.log('Testing Tight Class Cohesion (TCC) Calculator\n');
    
    setupTestDirectory();
    
    // Test 1: Perfect cohesion - all methods access all variables
    createTestFile('PerfectCohesion.java', `
        public class PerfectCohesion {
            private int count;
            private String name;
            
            public void increment() {
                count++;
                name = "Count: " + count;
            }
            
            public void reset() {
                count = 0;
                name = "Reset";
            }
            
            public String getInfo() {
                return name + " - " + count;
            }
        }
    `);
    
    let result = calculateTightClassCohesion(path.join(TEST_DIR, 'PerfectCohesion.java'));
    console.log(`Test 1 - Perfect cohesion: TCC = ${result.toFixed(2)} (expected: 1.00)`);
    
    // Test 2: No cohesion - methods access different variables
    createTestFile('NoCohesion.java', `
        public class NoCohesion {
            private int x;
            private int y;
            private String label;
            
            public void setX(int value) {
                this.x = value;
            }
            
            public void setY(int value) {
                this.y = value;
            }
            
            public void setLabel(String text) {
                this.label = text;
            }
        }
    `);
    
    result = calculateTightClassCohesion(path.join(TEST_DIR, 'NoCohesion.java'));
    console.log(`Test 2 - No cohesion: TCC = ${result.toFixed(2)} (expected: 0.00)`);
    
    // Test 3: Partial cohesion
    createTestFile('PartialCohesion.java', `
        public class PartialCohesion {
            private int width;
            private int height;
            private String color;
            
            public int calculateArea() {
                return width * height;
            }
            
            public int calculatePerimeter() {
                return 2 * (width + height);
            }
            
            public void setColor(String c) {
                this.color = c;
            }
            
            public String getDescription() {
                return "Rectangle: " + width + "x" + height;
            }
        }
    `);
    
    result = calculateTightClassCohesion(path.join(TEST_DIR, 'PartialCohesion.java'));
    console.log(`Test 3 - Partial cohesion: TCC = ${result.toFixed(2)} (expected: 0.50)`);
    
    // Test 4: Two groups of methods
    createTestFile('TwoGroups.java', `
        public class TwoGroups {
            private String firstName;
            private String lastName;
            private int age;
            private double salary;
            
            // Group 1: Name methods
            public String getFullName() {
                return firstName + " " + lastName;
            }
            
            public void setName(String first, String last) {
                this.firstName = first;
                this.lastName = last;
            }
            
            // Group 2: Employee data methods
            public void updateEmployeeData(int newAge, double newSalary) {
                this.age = newAge;
                this.salary = newSalary;
            }
            
            public double calculateYearlyBonus() {
                return salary * 0.1 + (age > 50 ? 1000 : 0);
            }
        }
    `);
    
    result = calculateTightClassCohesion(path.join(TEST_DIR, 'TwoGroups.java'));
    console.log(`Test 4 - Two groups: TCC = ${result.toFixed(2)} (expected: 0.33)`);
    
    // Test 5: Methods with complex variable access
    createTestFile('ComplexAccess.java', `
        public class ComplexAccess {
            private List<String> items;
            private int currentIndex;
            private String status;
            
            public void addItem(String item) {
                if (items == null) {
                    items = new ArrayList<>();
                }
                items.add(item);
                currentIndex = items.size() - 1;
                status = "Added: " + item;
            }
            
            public String getCurrentItem() {
                if (items != null && currentIndex >= 0 && currentIndex < items.size()) {
                    return items.get(currentIndex);
                }
                return null;
            }
            
            public void nextItem() {
                if (items != null && currentIndex < items.size() - 1) {
                    currentIndex++;
                    status = "Moved to: " + currentIndex;
                }
            }
            
            public String getStatus() {
                return status + " (Index: " + currentIndex + ")";
            }
        }
    `);
    
    result = calculateTightClassCohesion(path.join(TEST_DIR, 'ComplexAccess.java'));
    console.log(`Test 5 - Complex access: TCC = ${result.toFixed(2)} (expected: 0.67)`);
    
    // Test 6: Single method class
    createTestFile('SingleMethod.java', `
        public class SingleMethod {
            private int value;
            private String label;
            
            public String process() {
                value = value * 2;
                label = "Processed: " + value;
                return label;
            }
        }
    `);
    
    result = calculateTightClassCohesion(path.join(TEST_DIR, 'SingleMethod.java'));
    console.log(`Test 6 - Single method: TCC = ${result.toFixed(2)} (expected: 0.00)`);
    
    // Test 7: Methods with no variable access
    createTestFile('NoVariableAccess.java', `
        public class NoVariableAccess {
            private int counter;
            private String message;
            
            public void printHello() {
                System.out.println("Hello");
            }
            
            public int getConstant() {
                return 42;
            }
            
            public String staticMessage() {
                return "Static message";
            }
        }
    `);
    
    result = calculateTightClassCohesion(path.join(TEST_DIR, 'NoVariableAccess.java'));
    console.log(`Test 7 - No variable access: TCC = ${result.toFixed(2)} (expected: 0.00)`);
    
   
    // Test 9: High cohesion with multiple variables
    createTestFile('HighCohesion.java', `
        public class HighCohesion {
            private double x;
            private double y;
            private double z;
            
            public double calculateMagnitude() {
                return Math.sqrt(x*x + y*y + z*z);
            }
            
            public void normalize() {
                double mag = Math.sqrt(x*x + y*y + z*z);
                if (mag > 0) {
                    x /= mag;
                    y /= mag;
                    z /= mag;
                }
            }
            
            public double dotProduct(double ox, double oy, double oz) {
                return x*ox + y*oy + z*oz;
            }
            
            public void scale(double factor) {
                x *= factor;
                y *= factor;
                z *= factor;
            }
        }
    `);
    
    result = calculateTightClassCohesion(path.join(TEST_DIR, 'HighCohesion.java'));
    console.log(`Test 9 - High cohesion: TCC = ${result.toFixed(2)} (expected: 1.00)`);
    
    // Test 10: Real-world example
    createTestFile('BankAccount.java', `
        public class BankAccount {
            private String accountNumber;
            private double balance;
            private String ownerName;
            private List<String> transactionHistory;
            
            public void deposit(double amount) {
                balance += amount;
                transactionHistory.add("Deposit: " + amount);
            }
            
            public void withdraw(double amount) {
                if (balance >= amount) {
                    balance -= amount;
                    transactionHistory.add("Withdrawal: " + amount);
                }
            }
            
            public double getBalance() {
                return balance;
            }
            
            public String getAccountInfo() {
                return ownerName + " - " + accountNumber + " - Balance: " + balance;
            }
            
            public List<String> getRecentTransactions() {
                return new ArrayList<>(transactionHistory);
            }
            
            public void updateOwnerName(String newName) {
                this.ownerName = newName;
                transactionHistory.add("Owner changed to: " + newName);
            }
        }
    `);
    
    result = calculateTightClassCohesion(path.join(TEST_DIR, 'BankAccount.java'));
    console.log(`Test 10 - Bank account: TCC = ${result.toFixed(2)}`);
    
    cleanupTestDirectory();
    console.log('All TCC tests completed!');
}

// Run the tests
runTCCTests();


