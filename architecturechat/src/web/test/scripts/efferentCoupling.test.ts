// test-efferent-coupling.ts
import { writeFileSync, unlinkSync } from 'fs';
import { calculateEfferentCoupling } from '../../scripts/efferentCoupling';

function createTestFile(filename: string, content: string): void {
    writeFileSync(filename, content, 'utf8');
}

function runEfferentCouplingTests(): void {
    console.log('Testing Efferent Coupling (Ce) Calculator\n');
    
    // Test 1: No dependencies - isolated class
    createTestFile('noDependency.java', `
        public class NoDependency {
            private int value;
            private String name;
            
            public int getValue() {
                return value;
            }
            
            public void setValue(int v) {
                this.value = v;
            }
        }
    `);
    const result1 = calculateEfferentCoupling('noDependency.java');
    console.log(`Test 1 - No dependencies: Ce = ${result1} (expected: 0)`);
    
    // Test 2: Single custom dependency
    createTestFile('singleDependency.java', `
        import com.example.UserModel;
        
        public class SingleDependency {
            private UserModel user;    // Ce+1
            
            public void setUser(UserModel u) {
                this.user = u;
            }
        }
    `);
    const result2 = calculateEfferentCoupling('singleDependency.java');
    console.log(`Test 2 - Single dependency: Ce = ${result2} (expected: 1)`);
    
    // Test 3: Multiple custom imports
    createTestFile('multipleImports.java', `
        import com.example.UserModel;        // Ce+1
        import com.example.UserService;      // Ce+1
        import com.example.UserRepository;   // Ce+1
        import com.example.ValidationUtil;   // Ce+1
        
        public class MultipleImports {
            private UserService userService;
            private UserRepository userRepository;
            private UserModel currentUser;
            
            public boolean validate(UserModel user) {
                return ValidationUtil.isValid(user);
            }
        }
    `);
    const result3 = calculateEfferentCoupling('multipleImports.java');
    console.log(`Test 3 - Multiple imports: Ce = ${result3} (expected: 4)`);
    
    // Test 4: Extends and implements
    createTestFile('inheritance.java', `
        import com.example.BaseController;   // Ce+1
        import com.example.Validateable;     // Ce+1
        import com.example.Auditable;        // Ce+1
        
        public class InheritanceClass extends BaseController implements Validateable, Auditable {
            @Override
            public boolean validate() {
                return true;
            }
            
            @Override
            public void audit() {
                // audit implementation
            }
        }
    `);
    const result4 = calculateEfferentCoupling('inheritance.java');
    console.log(`Test 4 - Inheritance: Ce = ${result4} (expected: 3)`);
    
    // Test 5: Method parameters and return types
    createTestFile('methodDependencies.java', `
        import com.example.RequestModel;     // Ce+1
        import com.example.ResponseModel;    // Ce+1
        import com.example.ErrorModel;       // Ce+1
        import com.example.ProcessorService; // Ce+1
        
        public class MethodDependencies {
            private ProcessorService processor;
            
            public ResponseModel process(RequestModel request) {
                try {
                    return processor.execute(request);
                } catch (Exception e) {
                    return new ErrorModel(e.getMessage());
                }
            }
        }
    `);
    const result5 = calculateEfferentCoupling('methodDependencies.java');
    console.log(`Test 5 - Method dependencies: Ce = ${result5} (expected: 4)`);
    
    // Test 6: Generic types (ignoring java.* imports)
    createTestFile('genericTypes.java', `
        import java.util.List;               // Ignored (java.*)
        import java.util.Map;                // Ignored (java.*)
        import com.example.DataModel;        // Ce+1
        import com.example.Repository;       // Ce+1
        import com.example.PageRequest;      // Ce+1
        
        public class GenericTypes {
            private Repository<DataModel> repository;
            private List<DataModel> cache;
            private Map<String, DataModel> index;
            
            public List<DataModel> findAll(PageRequest pageRequest) {
                return repository.findAll(pageRequest);
            }
        }
    `);
    const result6 = calculateEfferentCoupling('genericTypes.java');
    console.log(`Test 6 - Generic types: Ce = ${result6} (expected: 3)`);
    
    // Test 7: Exception dependencies
    createTestFile('exceptionDependencies.java', `
        import com.example.DataNotFoundException;  // Ce+1
        import com.example.ValidationException;    // Ce+1
        import com.example.ProcessingException;    // Ce+1
        import com.example.DataModel;              // Ce+1
        import com.example.DataValidator;          // Ce+1
        
        public class ExceptionDependencies {
            private DataValidator validator;
            
            public DataModel process(String id) 
                throws DataNotFoundException, ValidationException {
                try {
                    DataModel data = findData(id);
                    validator.validate(data);
                    return data;
                } catch (ProcessingException e) {
                    throw new ValidationException("Failed", e);
                }
            }
        }
    `);
    const result7 = calculateEfferentCoupling('exceptionDependencies.java');
    console.log(`Test 7 - Exception dependencies: Ce = ${result7} (expected: 5)`);
    
    // Test 8: Static imports
    createTestFile('staticImports.java', `
        import static com.example.Constants.MAX_SIZE;    // Ce+1 (Constants)
        import static com.example.MathUtil.calculate;    // Ce+1 (MathUtil)
        import static com.example.StringUtil.*;          // Ce+1 (StringUtil)
        import com.example.DataModel;                    // Ce+1
        import com.example.ResultModel;                  // Ce+1
        
        public class StaticImports {
            public ResultModel process(DataModel data) {
                String normalized = normalize(data.getName());
                int result = calculate(data.getValue(), MAX_SIZE);
                return new ResultModel(result, normalized);
            }
        }
    `);
    const result8 = calculateEfferentCoupling('staticImports.java');
    console.log(`Test 8 - Static imports: Ce = ${result8} (expected: 5)`);
    
    // Test 9: Inner class dependencies
    createTestFile('innerClasses.java', `
        import com.example.Handler;    // Ce+1
        import com.example.Event;      // Ce+1
        import com.example.Listener;   // Ce+1
        import com.example.Callback;   // Ce+1
        
        public class InnerClasses {
            private Handler handler;
            
            public void setup() {
                handler = new Handler();
                handler.addListener(new Listener() {
                    @Override
                    public void onEvent(Event event) {
                        processEvent(event);
                    }
                });
                
                handler.setCallback(new Callback<Event>() {
                    @Override
                    public void execute(Event event) {
                        // handle callback
                    }
                });
            }
            
            private class InnerHandler extends Handler {
                private Event lastEvent;
            }
        }
    `);
    const result9 = calculateEfferentCoupling('innerClasses.java');
    console.log(`Test 9 - Inner classes: Ce = ${result9} (expected: 4)`);
    
    // Test 10: Complex dependencies
    createTestFile('complexDependencies.java', `
        import java.util.*;                        // Ignored (java.*)
        import com.example.UserService;            // Ce+1
        import com.example.OrderService;           // Ce+1
        import com.example.ProductService;         // Ce+1
        import com.example.UserModel;              // Ce+1
        import com.example.OrderModel;             // Ce+1
        import com.example.ProductModel;           // Ce+1
        import com.example.UserRepository;         // Ce+1
        import com.example.OrderRepository;        // Ce+1
        import com.example.ValidationUtil;         // Ce+1
        import com.example.DateUtil;               // Ce+1
        import com.example.ServiceException;       // Ce+1
        import com.example.ValidationException;    // Ce+1
        import com.example.RequestDTO;             // Ce+1
        import com.example.ResponseDTO;            // Ce+1
        import com.example.DtoMapper;              // Ce+1
        
        public class ComplexDependencies extends BaseService {
            private UserService userService;
            private OrderService orderService;
            private UserRepository userRepository;
            private OrderRepository orderRepository;
            private DtoMapper mapper;
            private Map<String, List<OrderModel>> orderCache;
            
            public ResponseDTO processOrder(RequestDTO request) 
                throws ServiceException, ValidationException {
                ValidationUtil.validate(request);
                UserModel user = userService.findById(request.getUserId());
                
                if (!DateUtil.isValidDate(request.getOrderDate())) {
                    throw new ValidationException("Invalid date");
                }
                
                List<OrderModel> orders = orderRepository.findByUser(user);
                ProductModel product = orderService.getProduct(request.getProductId());
                
                return mapper.toResponse(user, orders, product);
            }
        }
    `);
    const result10 = calculateEfferentCoupling('complexDependencies.java');
    console.log(`Test 10 - Complex dependencies: Ce = ${result10} (expected: 16)`);
    
    // Clean up test files
    const testFiles = [
        'noDependency.java',
        'singleDependency.java',
        'multipleImports.java',
        'inheritance.java',
        'methodDependencies.java',
        'genericTypes.java',
        'exceptionDependencies.java',
        'staticImports.java',
        'innerClasses.java',
        'complexDependencies.java'
    ];
    
    testFiles.forEach(file => {
        try {
            unlinkSync(file);
        } catch(e) {}
    });
    
    console.log('\nAll tests completed!');
}

// Run the tests
runEfferentCouplingTests();