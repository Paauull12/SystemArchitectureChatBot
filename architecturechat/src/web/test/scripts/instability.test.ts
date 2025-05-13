// test-instability.ts
import { writeFileSync, unlinkSync, mkdirSync, rmdirSync } from 'fs';
import { calculateInstabilityDirect } from '../../scripts/instability';
import * as path from 'path';

const TEST_DIR = './test-instability';

function setupTestDirectory(): void {
    try {
        mkdirSync(TEST_DIR);
    } catch (e) {
        // Directory already exists
    }
}

function cleanupTestDirectory(): void {
    const fs = require('fs');
    try {
        const files = fs.readdirSync(TEST_DIR);
        files.forEach((file: string) => {
            unlinkSync(path.join(TEST_DIR, file));
        });
        rmdirSync(TEST_DIR);
    } catch (e) {
        // Ignore errors during cleanup
    }
}

function createTestFile(filename: string, content: string): void {
    writeFileSync(path.join(TEST_DIR, filename), content, 'utf8');
}

function runInstabilityTests(): void {
    console.log('Testing Instability (I) Calculator\n');
    
    setupTestDirectory();
    
    // Test 1: Completely stable class (Ce=0, Ca>0)
    createTestFile('StableClass.java', `
        public class StableClass {
            private String data;
            
            public String getData() {
                return data;
            }
        }
    `);
    
    createTestFile('DependentClass1.java', `
        public class DependentClass1 {
            private StableClass stable;      // Ca+1 for StableClass
            
            public void useStable() {
                stable = new StableClass();
                stable.getData();
            }
        }
    `);
    
    createTestFile('DependentClass2.java', `
        public class DependentClass2 {
            public void process(StableClass obj) {    // Ca+1 for StableClass
                String data = obj.getData();
            }
        }
    `);
    
    let result = calculateInstabilityDirect(path.join(TEST_DIR, 'StableClass.java'));
    console.log(`Test 1 - Completely stable: I = ${result.toFixed(2)} (expected: 0.00)`);
    
    // Test 2: Completely unstable class (Ce>0, Ca=0)
    createTestFile('UnstableClass.java', `
        import com.example.ServiceA;
        import com.example.ServiceB;
        import com.example.ModelC;
        
        public class UnstableClass {
            private ServiceA serviceA;    // Ce+1
            private ServiceB serviceB;    // Ce+1
            private ModelC model;         // Ce+1
            
            public void execute() {
                serviceA = new ServiceA();
                serviceB = new ServiceB();
                model = new ModelC();
            }
        }
    `);
    
    result = calculateInstabilityDirect(path.join(TEST_DIR, 'UnstableClass.java'));
    console.log(`Test 2 - Completely unstable: I = ${result.toFixed(2)} (expected: 1.00)`);
    
    // Test 3: Balanced stability (Ce=Ca)
    createTestFile('BalancedClass.java', `
        import com.example.ExternalService;    // Ce+1
        
        public class BalancedClass {
            private ExternalService service;
            
            public void process() {
                service = new ExternalService();
            }
        }
    `);
    
    createTestFile('UserOfBalanced.java', `
        public class UserOfBalanced {
            private BalancedClass balanced;    // Ca+1 for BalancedClass
            
            public void use() {
                balanced = new BalancedClass();
                balanced.process();
            }
        }
    `);
    
    result = calculateInstabilityDirect(path.join(TEST_DIR, 'BalancedClass.java'));
    console.log(`Test 3 - Balanced: I = ${result.toFixed(2)} (expected: 0.50)`);
    
    // Test 4: Mostly stable (Ce=1, Ca=3)
    createTestFile('MostlyStableClass.java', `
        import com.example.SingleDependency;    // Ce+1
        
        public class MostlyStableClass {
            private SingleDependency dep;
            
            public void doSomething() {
                dep = new SingleDependency();
            }
        }
    `);
    
    createTestFile('User1.java', `
        public class User1 {
            private MostlyStableClass stable;    // Ca+1
            
            public void use() {
                stable = new MostlyStableClass();
            }
        }
    `);
    
    createTestFile('User2.java', `
        public class User2 extends MostlyStableClass {    // Ca+1
            @Override
            public void doSomething() {
                super.doSomething();
            }
        }
    `);
    
    createTestFile('User3.java', `
        public class User3 {
            public void process(MostlyStableClass obj) {    // Ca+1
                obj.doSomething();
            }
        }
    `);
    
    result = calculateInstabilityDirect(path.join(TEST_DIR, 'MostlyStableClass.java'));
    console.log(`Test 4 - Mostly stable: I = ${result.toFixed(2)} (expected: 0.25)`);
    
    // Test 5: Mostly unstable (Ce=3, Ca=1)
    createTestFile('MostlyUnstableClass.java', `
        import com.example.ServiceX;    // Ce+1
        import com.example.ServiceY;    // Ce+1
        import com.example.ServiceZ;    // Ce+1
        
        public class MostlyUnstableClass {
            private ServiceX serviceX;
            private ServiceY serviceY;
            private ServiceZ serviceZ;
            
            public void execute() {
                serviceX = new ServiceX();
                serviceY = new ServiceY();
                serviceZ = new ServiceZ();
            }
        }
    `);
    
    createTestFile('SingleUser.java', `
        public class SingleUser {
            private MostlyUnstableClass unstable;    // Ca+1
            
            public void use() {
                unstable = new MostlyUnstableClass();
            }
        }
    `);
    
    result = calculateInstabilityDirect(path.join(TEST_DIR, 'MostlyUnstableClass.java'));
    console.log(`Test 5 - Mostly unstable: I = ${result.toFixed(2)} (expected: 0.75)`);
    
    // Test 6: Isolated class (Ce=0, Ca=0)
    createTestFile('IsolatedClass.java', `
        public class IsolatedClass {
            private int value;
            
            public int calculate() {
                return value * 2;
            }
        }
    `);
    
    result = calculateInstabilityDirect(path.join(TEST_DIR, 'IsolatedClass.java'));
    console.log(`Test 6 - Isolated: I = ${result.toFixed(2)} (expected: 0.00)`);
    
    // Test 7: Interface with implementations
    createTestFile('StableInterface.java', `
        public interface StableInterface {
            void execute();
            String process();
        }
    `);
    
    createTestFile('Implementation1.java', `
        public class Implementation1 implements StableInterface {    // Ca+1
            @Override
            public void execute() {
                // implementation
            }
            
            @Override
            public String process() {
                return "result";
            }
        }
    `);
    
    createTestFile('Implementation2.java', `
        public class Implementation2 implements StableInterface {    // Ca+1
            @Override
            public void execute() {
                // implementation
            }
            
            @Override
            public String process() {
                return "result2";
            }
        }
    `);
    
    result = calculateInstabilityDirect(path.join(TEST_DIR, 'StableInterface.java'));
    console.log(`Test 7 - Interface: I = ${result.toFixed(2)} (expected: 0.00)`);
    
    // Test 8: Abstract class
    createTestFile('AbstractBase.java', `
        import com.example.CommonService;    // Ce+1
        
        public abstract class AbstractBase {
            protected CommonService service;
            
            public abstract void process();
            
            protected void init() {
                service = new CommonService();
            }
        }
    `);
    
    createTestFile('ConcreteImpl.java', `
        public class ConcreteImpl extends AbstractBase {    // Ca+1
            @Override
            public void process() {
                init();
                // implementation
            }
        }
    `);
    
    result = calculateInstabilityDirect(path.join(TEST_DIR, 'AbstractBase.java'));
    console.log(`Test 8 - Abstract class: I = ${result.toFixed(2)} (expected: 0.50)`);
    
    // Test 9: Complex dependencies
    createTestFile('ComplexClass.java', `
        import com.example.service.UserService;     // Ce+1
        import com.example.model.DataRepository;    // Ce+1
        import com.example.util.CacheManager;       // Ce+1
        import com.example.util.Logger;             // Ce+1
        
        public class ComplexClass extends BaseClass implements ServiceInterface {
            private UserService userService;
            private DataRepository repository;
            private CacheManager cache;
            private Logger logger;
            
            public Response process(Request request) {
                logger.info("Processing");
                User user = userService.find(request.getId());
                Data data = repository.get(user);
                cache.put(user.getId(), data);
                return new Response(data);
            }
        }
    `);
    
    createTestFile('ComplexUser1.java', `
        public class ComplexUser1 {
            private ComplexClass complex;    // Ca+1
            
            public void use() {
                complex = new ComplexClass();
            }
        }
    `);
    
    createTestFile('ComplexUser2.java', `
        public class ComplexUser2 {
            public void handle(ComplexClass obj) {    // Ca+1
                obj.process(new Request());
            }
        }
    `);
    
    result = calculateInstabilityDirect(path.join(TEST_DIR, 'ComplexClass.java'));
    console.log(`Test 9 - Complex class: I = ${result.toFixed(2)}`);
    
    // Test 10: Utility class
    createTestFile('UtilityClass.java', `
        public class UtilityClass {
            public static String format(String input) {
                return input.toUpperCase();
            }
            
            public static int calculate(int a, int b) {
                return a + b;
            }
        }
    `);
    
    createTestFile('UtilityUser1.java', `
        public class UtilityUser1 {
            public void process() {
                String result = UtilityClass.format("test");    // Ca+1
                int sum = UtilityClass.calculate(1, 2);
            }
        }
    `);
    
    createTestFile('UtilityUser2.java', `
        public class UtilityUser2 {
            public String transform(String input) {
                return UtilityClass.format(input);    // Ca+1
            }
        }
    `);
    
    result = calculateInstabilityDirect(path.join(TEST_DIR, 'UtilityClass.java'));
    console.log(`Test 10 - Utility class: I = ${result.toFixed(2)} (expected: 0.00)`);
    
    cleanupTestDirectory();
    console.log('All Instability tests completed!');
}

// Run the tests
runInstabilityTests();