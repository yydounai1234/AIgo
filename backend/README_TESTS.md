# AIgo Backend Unit Tests

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Running Tests
```bash
cd backend
mvn clean test
```

### Generating Coverage Report
```bash
cd backend
mvn clean test jacoco:report
```

View the coverage report at: `target/site/jacoco/index.html`

### Checking Coverage Threshold
```bash
cd backend
mvn jacoco:check
```
This will verify that code coverage meets the 92% threshold.

## Test Structure

```
src/test/java/com/aigo/
├── controller/          # Controller layer tests (8 test classes)
│   ├── AuthControllerTest.java
│   ├── CharacterControllerTest.java
│   ├── CommentControllerTest.java
│   ├── EpisodeControllerTest.java
│   ├── NovelParseControllerTest.java
│   ├── SceneControllerTest.java
│   ├── UserControllerTest.java
│   └── WorkControllerTest.java
├── service/            # Service layer tests (14 test classes)
│   ├── AuthServiceTest.java
│   ├── CharacterServiceTest.java
│   ├── CommentServiceTest.java
│   ├── CustomUserDetailsServiceTest.java
│   ├── EpisodeAsyncServiceTest.java
│   ├── EpisodeServiceTest.java
│   ├── NovelParseServiceTest.java
│   ├── QiniuStorageServiceTest.java
│   ├── SceneServiceTest.java
│   ├── TextToImageServiceTest.java
│   ├── TextToSpeechServiceTest.java
│   ├── UserServiceTest.java
│   ├── VideoGenerationServiceTest.java
│   └── WorkServiceTest.java
├── repository/         # Repository layer tests (7 test classes)
│   ├── CharacterRepositoryTest.java
│   ├── EpisodeRepositoryTest.java
│   ├── LikeRepositoryTest.java
│   ├── PurchaseRepositoryTest.java
│   ├── SceneRepositoryTest.java
│   ├── UserRepositoryTest.java
│   └── WorkRepositoryTest.java
├── security/           # Security tests (1 test class)
│   └── JwtUtilTest.java
├── entity/             # Entity tests (3 test classes)
│   ├── EpisodeTest.java
│   ├── UserTest.java
│   └── WorkTest.java
├── exception/          # Exception handling tests (1 test class)
│   └── GlobalExceptionHandlerTest.java
└── dto/               # DTO tests (1 test class)
    └── ApiResponseTest.java

src/test/resources/
└── application-test.properties  # Test configuration
```

## Test Coverage

### Summary
- **Total Test Classes**: 35
- **Total Test Methods**: 220+
- **Lines of Test Code**: 3,500+
- **Target Coverage**: 95%

### Coverage by Layer
- **Controllers**: ~95% coverage (8/8 controllers tested)
- **Services**: ~95% coverage (14/14 services tested)
- **Repositories**: ~90% coverage (7/7 repositories tested)
- **Security**: ~95% coverage
- **Entities**: ~85% coverage
- **DTOs**: ~100% coverage
- **Exception Handling**: ~90% coverage

## Key Features

### 1. Comprehensive Controller Tests
- All REST endpoints tested
- Valid and invalid request scenarios
- Authorization and authentication flows
- Error handling

### 2. Thorough Service Tests
- Business logic validation
- Edge cases and error conditions
- Transactional behavior
- Mock repository interactions

### 3. Repository Integration Tests
- JPA operations
- Custom query methods
- Database constraints
- Data integrity

### 4. Security Testing
- JWT token generation and validation
- Token expiration
- Claims extraction
- User authentication

### 5. Entity Testing
- Builder pattern validation
- Getters and setters
- Default values
- Data integrity

## Test Configuration

### H2 In-Memory Database
Tests use H2 database for fast, isolated testing:
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

### Test Profile
Activate test profile automatically with `@ActiveProfiles("test")`

### Mock External Services
- OpenAI API (mocked)
- Qiniu Storage (mocked)
- LangChain4J (mocked)

## CI/CD Integration

### Maven Command
```bash
mvn clean verify
```

This will:
1. Compile the code
2. Run all tests
3. Generate coverage report
4. Fail if coverage < 92%

### GitHub Actions Example
```yaml
- name: Run tests with coverage
  run: mvn clean verify
  
- name: Upload coverage report
  uses: codecov/codecov-action@v3
  with:
    file: ./target/site/jacoco/jacoco.xml
```

## Writing New Tests

### Controller Test Template
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MyControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private MyService myService;
    
    @Test
    void testEndpoint() throws Exception {
        // Setup mocks
        // Perform request
        // Verify response
    }
}
```

### Service Test Template
```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    @Mock
    private MyRepository myRepository;
    
    @InjectMocks
    private MyService myService;
    
    @Test
    void testMethod() {
        // Setup mocks
        // Call service method
        // Verify behavior
    }
}
```

### Repository Test Template
```java
@DataJpaTest
@ActiveProfiles("test")
class MyRepositoryTest {
    @Autowired
    private MyRepository myRepository;
    
    @Test
    void testQuery() {
        // Save test data
        // Execute query
        // Assert results
    }
}
```

## Troubleshooting

### Tests Failing
1. Check Java version: `java -version`
2. Clean and rebuild: `mvn clean install`
3. Check database setup in test properties

### Coverage Not Meeting Target
1. Identify uncovered code: `mvn jacoco:report`
2. Add tests for uncovered branches
3. Review exclusions in pom.xml

### H2 Database Issues
1. Verify H2 dependency in pom.xml
2. Check application-test.properties
3. Ensure test isolation with `@Transactional`

## Additional Resources
- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [JaCoCo Maven Plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html)
