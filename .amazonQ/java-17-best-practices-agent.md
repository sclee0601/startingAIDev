# Java 17+ AI Agent: Best Practices & Zero-Compilation-Error Standard
## Enforcing Enterprise-Grade Quality in AI-Generated Code

---

## Table of Contents
1. [Java 17+ Modern Features](#java-17-modern-features)
2. [Comprehensive Validation Strategy](#comprehensive-validation-strategy)
3. [Naming Conventions & Code Standards](#naming-conventions--code-standards)
4. [Exception Handling Best Practices](#exception-handling-best-practices)
5. [Annotations & Metadata](#annotations--metadata)
6. [Security Best Practices](#security-best-practices)
7. [Performance Optimization](#performance-optimization)
8. [Complete Java Agent Implementation](#complete-java-agent-implementation)
9. [Quality Checklist](#quality-checklist)

---

## Java 17+ Modern Features

### 1. **Records (Java 16+)**
Modern replacement for value objects with automatic equals/hashCode/toString:

```java
// ✅ GOOD - Java 17+
public record UserProfile(
    String userId,
    String email,
    LocalDateTime createdAt
) {
    public UserProfile {
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(email, "email cannot be null");
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
    }
}

// ❌ AVOID - Pre-Java 17 style
public class UserProfile {
    private final String userId;
    private final String email;
    private final LocalDateTime createdAt;
    
    // 50 lines of boilerplate...
}
```

### 2. **Sealed Classes (Java 17+)**
Restrict inheritance for better control:

```java
// ✅ GOOD - Java 17+
public sealed interface PaymentMethod permits CreditCard, BankTransfer, Wallet {
    void process(BigDecimal amount);
}

public final class CreditCard implements PaymentMethod {
    private final String cardNumber;
    
    public CreditCard(String cardNumber) {
        if (!isValidCardNumber(cardNumber)) {
            throw new IllegalArgumentException("Invalid card number");
        }
        this.cardNumber = cardNumber;
    }
    
    @Override
    public void process(BigDecimal amount) {
        // Process credit card payment
    }
}

public final class BankTransfer implements PaymentMethod {
    private final String accountNumber;
    
    @Override
    public void process(BigDecimal amount) {
        // Process bank transfer
    }
}
```

### 3. **Pattern Matching (Java 16+/17+)**
Eliminates verbose instanceof casts:

```java
// ✅ GOOD - Java 17+
public String describePayment(PaymentMethod method) {
    return switch (method) {
        case CreditCard card -> "Credit: " + maskCardNumber(card.getCardNumber());
        case BankTransfer transfer -> "Bank: " + transfer.getAccountNumber();
        case Wallet wallet -> "Wallet: " + wallet.getBalance();
    };
}

public boolean processPayment(Object obj) {
    if (obj instanceof PaymentMethod method && method.isValid()) {
        method.process(BigDecimal.valueOf(100));
        return true;
    }
    return false;
}

// ❌ AVOID - Pre-Java 17
public String describePayment(PaymentMethod method) {
    if (method instanceof CreditCard) {
        CreditCard card = (CreditCard) method;
        return "Credit: " + maskCardNumber(card.getCardNumber());
    } else if (method instanceof BankTransfer) {
        BankTransfer transfer = (BankTransfer) method;
        return "Bank: " + transfer.getAccountNumber();
    }
    // ...
}
```

### 4. **Text Blocks (Java 15+)**
Clean multi-line strings:

```java
// ✅ GOOD - Java 17+
String jsonQuery = """
    {
        "query": {
            "match_all": {}
        }
    }
    """;

String sqlQuery = """
    SELECT u.id, u.email, COUNT(o.id) as order_count
    FROM users u
    LEFT JOIN orders o ON u.id = o.user_id
    WHERE u.active = true
    GROUP BY u.id
    """;

// ❌ AVOID - Concatenation
String json = "{\"query\":{\"match_all\":{}}}"
```

### 5. **Var Type Inference (Java 10+)**
Cleaner variable declarations:

```java
// ✅ GOOD - Java 17+
var users = userRepository.findAll();
var userCount = users.size();
var paymentProcessor = new CreditCardProcessor();

List<User> userList = userRepository.findAll();
var filteredUsers = userList.stream()
    .filter(u -> u.isActive())
    .map(u -> new UserDTO(u.getId(), u.getEmail()))
    .toList(); // Java 16+ - returns immutable List

// ❌ AVOID - Verbose types when inference works
List<User> userList = userRepository.findAll();
```

### 6. **Records in Collections**
Perfect for data transfer and immutability:

```java
// ✅ GOOD - Java 17+
public record PagedResult<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements
) {
    public PagedResult {
        Objects.requireNonNull(content, "content cannot be null");
        if (pageNumber < 0) throw new IllegalArgumentException("pageNumber must be >= 0");
    }
    
    public int getTotalPages() {
        return (int) Math.ceil((double) totalElements / pageSize);
    }
}

// Usage
var result = new PagedResult<>(users, 1, 20, 150);
System.out.println("Total pages: " + result.getTotalPages());
```

---

## Comprehensive Validation Strategy

### Multi-Layer Validation Pipeline

```
┌─────────────────────────────────────────────────────────┐
│ Input: Raw Java Code from AI                            │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│ Layer 1: Syntax & Compilation Check                     │
│ - javac compiler validation                             │
│ - Import resolution                                     │
│ - Type checking                                         │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│ Layer 2: Static Analysis                                │
│ - Checkstyle (naming conventions)                       │
│ - PMD (code quality issues)                             │
│ - SpotBugs (potential bugs)                             │
│ - Sonar (security vulnerabilities)                      │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│ Layer 3: Best Practices Check                           │
│ - Java 17+ feature usage                                │
│ - Exception handling completeness                       │
│ - Resource management (try-with-resources)              │
│ - Performance patterns                                  │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│ Layer 4: Security Analysis                              │
│ - SQL injection vulnerabilities                         │
│ - Unsafe reflection                                     │
│ - Insecure deserialization                              │
│ - Weak cryptography                                     │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│ Layer 5: Runtime Unit Tests                             │
│ - Basic functionality tests                             │
│ - Edge case handling                                    │
│ - Exception scenarios                                   │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│ ✅ APPROVED: Zero-Error Code Ready for Production       │
└─────────────────────────────────────────────────────────┘
```

---

## Naming Conventions & Code Standards

### Enforced Standards

```java
// ✅ CORRECT - Java naming conventions

// Classes: PascalCase
public class PaymentProcessor { }
public class UserAuthenticationService { }
public record OrderSummary(String id, BigDecimal total) { }

// Interfaces: PascalCase, often adjective or -able suffix
public interface Serializable { }
public interface PaymentProvider { }
public interface Cacheable { }

// Methods: camelCase, verb + noun, clear intent
public void processPayment() { }
public List<User> findActiveUsers() { }
public boolean isValidEmail(String email) { }
public Optional<User> getUserById(String id) { }

// Variables: camelCase
private String userEmail;
private int maxRetryCount;
private final BigDecimal transactionAmount;

// Constants: UPPER_SNAKE_CASE
private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/db";
private static final int MAX_TIMEOUT_SECONDS = 30;
private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

// Type Variables: Single uppercase letter or descriptive
public class Repository<T> { }
public interface Mapper<S, D> { } // Source, Destination
public class Cache<K, V> { }

// Package names: lowercase, reversed domain
package com.yourcompany.payment.processor;
package com.yourcompany.user.authentication;
package com.yourcompany.order.service;

// Enums: PascalCase for enum, UPPER_SNAKE_CASE for values
public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED;
}

// ❌ INCORRECT - Anti-patterns
public class paymentprocessor { }           // Wrong case
public void p() { }                         // Too short, unclear
public String s;                            // Ambiguous
private String str;                         // Hungarian notation
private int num_retries;                    // Should be numRetries
```

### Code Organization Standards

```java
// ✅ GOOD - Proper organization
public class PaymentService {
    // 1. Static fields (constants first)
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    // 2. Instance fields
    private final PaymentRepository paymentRepository;
    private final AuditService auditService;
    private final PaymentGateway paymentGateway;
    
    // 3. Constructor
    public PaymentService(
            PaymentRepository paymentRepository,
            AuditService auditService,
            PaymentGateway paymentGateway) {
        this.paymentRepository = Objects.requireNonNull(paymentRepository);
        this.auditService = Objects.requireNonNull(auditService);
        this.paymentGateway = Objects.requireNonNull(paymentGateway);
    }
    
    // 4. Public methods
    public Payment processPayment(PaymentRequest request) {
        // Implementation
    }
    
    public Optional<Payment> getPaymentById(String id) {
        // Implementation
    }
    
    // 5. Private helper methods
    private void validatePaymentRequest(PaymentRequest request) {
        // Implementation
    }
    
    private void auditTransaction(Payment payment) {
        // Implementation
    }
}
```

---

## Exception Handling Best Practices

### Comprehensive Exception Handling Framework

```java
// 1. Custom Exceptions - Business Domain Specific
public class PaymentException extends RuntimeException {
    private final String transactionId;
    private final ErrorCode errorCode;
    
    public PaymentException(String message, String transactionId, ErrorCode errorCode) {
        super(message);
        this.transactionId = transactionId;
        this.errorCode = errorCode;
    }
    
    public PaymentException(String message, Throwable cause, String transactionId, ErrorCode errorCode) {
        super(message, cause);
        this.transactionId = transactionId;
        this.errorCode = errorCode;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

public class InsufficientFundsException extends PaymentException {
    private final BigDecimal requiredAmount;
    private final BigDecimal availableAmount;
    
    public InsufficientFundsException(
            String transactionId,
            BigDecimal requiredAmount,
            BigDecimal availableAmount) {
        super(
            String.format("Insufficient funds: required %s, available %s", 
                requiredAmount, availableAmount),
            transactionId,
            ErrorCode.INSUFFICIENT_FUNDS
        );
        this.requiredAmount = requiredAmount;
        this.availableAmount = availableAmount;
    }
}

public enum ErrorCode {
    INSUFFICIENT_FUNDS,
    INVALID_CARD,
    GATEWAY_TIMEOUT,
    DUPLICATE_TRANSACTION,
    RATE_LIMITED,
    INVALID_REQUEST;
}

// 2. Comprehensive Exception Handling
public Payment processPayment(PaymentRequest request) {
    validatePaymentRequest(request);
    
    try {
        // Attempt payment
        var response = paymentGateway.charge(request);
        var payment = new Payment(request, response);
        
        paymentRepository.save(payment);
        auditService.logSuccess(payment);
        
        return payment;
        
    } catch (InsufficientFundsException e) {
        LOGGER.warn("Insufficient funds for transaction {}: {}", 
            e.getTransactionId(), e.getMessage());
        auditService.logFailure(e.getTransactionId(), e.getErrorCode());
        throw e;
        
    } catch (CardDeclinedException e) {
        LOGGER.warn("Card declined for transaction {}", e.getTransactionId());
        auditService.logFailure(e.getTransactionId(), ErrorCode.INVALID_CARD);
        throw new PaymentException(
            "Card was declined. Please try another payment method.",
            e,
            e.getTransactionId(),
            ErrorCode.INVALID_CARD
        );
        
    } catch (GatewayTimeoutException e) {
        LOGGER.error("Payment gateway timeout for transaction {}", e.getTransactionId());
        // Implement idempotent retry logic
        return retryPaymentWithBackoff(request, 1);
        
    } catch (Exception e) {
        LOGGER.error("Unexpected error processing payment", e);
        auditService.logError("UNEXPECTED_ERROR", e.getMessage());
        throw new PaymentException(
            "An unexpected error occurred processing your payment.",
            e,
            request.getTransactionId(),
            ErrorCode.GATEWAY_TIMEOUT
        );
    }
}

// 3. Try-With-Resources for resource management
public void readConfigFile(String filePath) {
    try (var reader = new FileReader(filePath);
         var bufferedReader = new BufferedReader(reader)) {
        
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            processConfigLine(line);
        }
        
    } catch (FileNotFoundException e) {
        LOGGER.error("Configuration file not found: {}", filePath);
        throw new ConfigurationException("Missing configuration file", e);
    } catch (IOException e) {
        LOGGER.error("Error reading configuration file: {}", filePath, e);
        throw new ConfigurationException("Failed to read configuration", e);
    }
}

// 4. Result Pattern (Optional<T>) as alternative to exceptions
public Optional<Payment> processPaymentSafely(PaymentRequest request) {
    return Optional.of(request)
        .filter(this::isValidPaymentRequest)
        .flatMap(this::attemptPayment)
        .or(() -> handlePaymentFailure(request));
}

private Optional<Payment> attemptPayment(PaymentRequest request) {
    try {
        return Optional.of(processPayment(request));
    } catch (PaymentException e) {
        LOGGER.warn("Payment failed: {}", e.getMessage());
        return Optional.empty();
    }
}
```

### Validation Best Practices

```java
// ✅ GOOD - Comprehensive input validation
private void validatePaymentRequest(PaymentRequest request) {
    Objects.requireNonNull(request, "PaymentRequest cannot be null");
    
    if (request.getAmount() == null || request.getAmount().signum() <= 0) {
        throw new IllegalArgumentException("Payment amount must be greater than zero");
    }
    
    if (!isValidEmail(request.getCustomerEmail())) {
        throw new IllegalArgumentException("Invalid customer email format");
    }
    
    if (request.getCurrency() == null || request.getCurrency().isEmpty()) {
        throw new IllegalArgumentException("Currency code is required");
    }
    
    if (request.getPaymentMethod() == null) {
        throw new IllegalArgumentException("Payment method is required");
    }
    
    // Domain-specific validation
    if (request.getAmount().compareTo(MAX_PAYMENT_AMOUNT) > 0) {
        throw new PaymentException(
            "Payment exceeds maximum allowed amount",
            request.getTransactionId(),
            ErrorCode.INVALID_REQUEST
        );
    }
}

// ✅ GOOD - Null-safe operations with Optional
public Optional<User> findUserByEmail(String email) {
    return Optional.ofNullable(email)
        .filter(e -> !e.isBlank())
        .flatMap(userRepository::findByEmail);
}

// Usage
findUserByEmail(userInput)
    .ifPresent(user -> processUser(user))
    .ifPresentOrElse(
        user -> sendWelcomeEmail(user),
        () -> handleUserNotFound()
    );
```

---

## Annotations & Metadata

### Essential Annotations for Production Code

```java
// 1. Null-Safety Annotations (helps detect issues)
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserService {
    
    @NotNull
    public User createUser(@NotNull String email) {
        Objects.requireNonNull(email);
        // Implementation
    }
    
    @Nullable
    public User findUserOptional(@NotNull String id) {
        // May return null
        return userRepository.findById(id).orElse(null);
    }
}

// 2. Override Annotation (catches mistakes)
public class PaymentProcessor extends BaseProcessor {
    @Override
    public void process(Payment payment) {
        // This catches if parent method signature changed
    }
}

// 3. Deprecated Annotation (signals legacy code)
@Deprecated(since = "2.0", forRemoval = true, 
    message = "Use processPaymentV2() instead")
public void processPayment(Payment payment) {
    // Old implementation
}

// 4. FunctionalInterface (for lambda use)
@FunctionalInterface
public interface PaymentValidator {
    boolean validate(PaymentRequest request);
    
    default void logValidation(PaymentRequest request, boolean isValid) {
        LOGGER.debug("Validation result: {}", isValid);
    }
}

// 5. SuppressWarnings (when you know better than compiler)
@SuppressWarnings("unchecked")
public List<String> getLegacyList(Object obj) {
    return (List<String>) obj;
}

// 6. SafeVarargs (for generic varargs methods)
@SafeVarargs
public static <T> List<T> asList(T... items) {
    return Arrays.asList(items);
}

// 7. Custom annotations for documentation
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresAuthentication {
    String[] roles() default {};
    String description() default "";
}

@RequiresAuthentication(roles = {"ADMIN"}, description = "Admin only operation")
public void deleteUser(String userId) {
    // Implementation
}

// 8. Documentation Annotations
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    String description() default "Multiple calls with same input produce same result";
}

@Idempotent(description = "ProcessPayment is idempotent for duplicate requests")
public Payment processPayment(PaymentRequest request) {
    // Implementation ensures idempotency
}
```

---

## Security Best Practices

### Preventing Common Vulnerabilities

```java
// ❌ VULNERABLE - SQL Injection
public User findUserByEmail(String email) {
    String query = "SELECT * FROM users WHERE email = '" + email + "'"; // DANGEROUS!
    return executeQuery(query);
}

// ✅ SECURE - Parameterized queries
public Optional<User> findUserByEmail(String email) {
    String query = "SELECT * FROM users WHERE email = ?";
    return userRepository.findByEmail(email); // Uses prepared statements
}

// ================================================================

// ❌ VULNERABLE - Deserialization of untrusted data
public Object deserializeUserData(String data) {
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(data)));
    return ois.readObject(); // DANGEROUS - could execute arbitrary code!
}

// ✅ SECURE - Use JSON deserialization with schema validation
public User deserializeUserData(String jsonData) {
    var user = objectMapper.readValue(jsonData, User.class); // Jackson validates against schema
    validateUser(user);
    return user;
}

// ================================================================

// ❌ VULNERABLE - Weak password storage
public void savePassword(String rawPassword) {
    user.setPassword(Base64.getEncoder().encodeToString(rawPassword.getBytes())); // Reversible!
}

// ✅ SECURE - Use bcrypt with salt
public void savePassword(String rawPassword) {
    String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    user.setPassword(hashedPassword);
}

public boolean validatePassword(String rawPassword, String hashedPassword) {
    return BCrypt.checkpw(rawPassword, hashedPassword);
}

// ================================================================

// ❌ VULNERABLE - Hardcoded secrets
public class ApiClient {
    private static final String API_KEY = "sk-abc123xyz"; // In version control!
}

// ✅ SECURE - Use environment variables
public class ApiClient {
    private final String apiKey;
    
    public ApiClient() {
        this.apiKey = Objects.requireNonNull(
            System.getenv("API_KEY"),
            "API_KEY environment variable must be set"
        );
    }
}

// ================================================================

// ❌ VULNERABLE - Unsafe reflection
public Object instantiateClass(String className) {
    Class<?> clazz = Class.forName(className); // Could load anything!
    return clazz.getDeclaredConstructor().newInstance();
}

// ✅ SECURE - Whitelist allowed classes
private static final Set<String> ALLOWED_CLASSES = Set.of(
    "com.company.payment.CreditCardProcessor",
    "com.company.payment.BankTransferProcessor"
);

public Object instantiateProcessor(String className) {
    if (!ALLOWED_CLASSES.contains(className)) {
        throw new SecurityException("Class not whitelisted: " + className);
    }
    Class<?> clazz = Class.forName(className);
    return clazz.getDeclaredConstructor().newInstance();
}

// ================================================================

// ✅ GOOD - Use Spring Security for authentication
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

// ================================================================

// ✅ GOOD - CORS configuration
@Configuration
public class CorsConfig {
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("https://yourdomain.com")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        };
    }
}

// ================================================================

// ✅ GOOD - Input validation and sanitization
public String sanitizeUserInput(String input) {
    if (input == null || input.isBlank()) {
        throw new IllegalArgumentException("Input cannot be empty");
    }
    
    // Remove potential malicious characters
    return input.replaceAll("[<>\"'%;()&+]", "");
}

public void searchUsers(String searchTerm) {
    String sanitized = sanitizeUserInput(searchTerm);
    // Safe to use in queries now
}
```

---

## Performance Optimization

### Java 17+ Performance Patterns

```java
// 1. Stream Processing - lazy evaluation
public List<UserDTO> getActiveUsersSummary() {
    return userRepository.findAll().stream()
        .filter(User::isActive)
        .filter(u -> u.getLastLoginDate().isAfter(LocalDate.now().minusDays(30)))
        .map(u -> new UserDTO(u.getId(), u.getEmail(), u.getName()))
        .toList(); // Java 16+ - immutable list
}

// 2. Caching expensive operations
private final Map<String, User> userCache = new ConcurrentHashMap<>();

public Optional<User> findUserCached(String id) {
    return Optional.ofNullable(userCache.computeIfAbsent(id, this::loadUser));
}

private User loadUser(String id) {
    LOGGER.debug("Loading user from database: {}", id);
    return userRepository.findById(id).orElse(null);
}

// 3. Pagination for large result sets
public PagedResult<UserDTO> getUsers(int pageNumber, int pageSize) {
    if (pageNumber < 0 || pageSize <= 0 || pageSize > 100) {
        throw new IllegalArgumentException("Invalid pagination parameters");
    }
    
    var users = userRepository.findAll(PageRequest.of(pageNumber, pageSize));
    var dtos = users.map(u -> new UserDTO(u.getId(), u.getEmail())).toList();
    
    return new PagedResult<>(dtos, pageNumber, pageSize, users.getTotalElements());
}

// 4. Connection pooling (with HikariCP)
@Configuration
public class DatabaseConfig {
    
    @Bean
    public DataSource dataSource() {
        var config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/payment_db");
        config.setUsername("user");
        config.setPassword("password");
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(1800000);
        
        return new HikariDataSource(config);
    }
}

// 5. Batch processing
public void processPaymentsBatch(List<Payment> payments) {
    final int batchSize = 100;
    
    for (int i = 0; i < payments.size(); i += batchSize) {
        int end = Math.min(i + batchSize, payments.size());
        var batch = payments.subList(i, end);
        
        processBatch(batch);
        LOGGER.info("Processed batch {}-{}", i, end);
    }
}

// 6. Immutability for thread-safety
public record TransactionSummary(
    String transactionId,
    BigDecimal amount,
    LocalDateTime timestamp,
    TransactionStatus status
) {
    public TransactionSummary {
        Objects.requireNonNull(transactionId);
        Objects.requireNonNull(amount);
        Objects.requireNonNull(timestamp);
        Objects.requireNonNull(status);
    }
}

// 7. Avoiding unnecessary object creation
public boolean isEmailValid(String email) {
    // Cache compiled pattern
    return EMAIL_PATTERN.matcher(email).matches();
}

private static final Pattern EMAIL_PATTERN = 
    Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

// 8. Early return for clarity and performance
public Optional<User> authenticateUser(String email, String password) {
    if (email == null || email.isBlank()) {
        return Optional.empty(); // Fast fail
    }
    
    var user = userRepository.findByEmail(email);
    
    if (user.isEmpty()) {
        LOGGER.warn("User not found: {}", email);
        return Optional.empty();
    }
    
    if (!passwordEncoder.matches(password, user.get().getPasswordHash())) {
        LOGGER.warn("Authentication failed for user: {}", email);
        return Optional.empty();
    }
    
    return user;
}
```

---

## Complete Java Agent Implementation

### Production-Grade Self-Correcting Java Code Agent

```javascript
const fetch = require('node-fetch');
const { spawnSync } = require('child_process');
const fs = require('fs');
const path = require('path');
const os = require('os');

class JavaCodeAgent {
    constructor(apiKey, config = {}) {
        this.apiKey = apiKey;
        this.maxRetries = config.maxRetries || 4;
        this.tempDir = config.tempDir || os.tmpdir();
        this.javaVersion = config.javaVersion || '17';
        this.verbose = config.verbose !== false;
        this.validationLayers = [
            'compilation',
            'staticAnalysis',
            'bestPractices',
            'security',
            'performance'
        ];
    }

    async generateJavaCode(requirement, packageName = 'com.example.generated') {
        console.log(`\n${'═'.repeat(70)}`);
        console.log(`🚀 Java Code Generation (Java ${this.javaVersion}+)`);
        console.log(`${'═'.repeat(70)}\n`);

        let attempt = 0;
        let generatedCode = null;
        let validationErrors = [];

        while (attempt < this.maxRetries) {
            attempt++;
            
            console.log(`\n📝 Attempt ${attempt}/${this.maxRetries}`);
            console.log(`${'─'.repeat(70)}`);

            // Generate code using Claude
            generatedCode = await this.generateCodeWithClaude(
                requirement,
                packageName,
                validationErrors,
                attempt
            );

            // Execute multi-layer validation
            const validationResult = await this.executeValidation(generatedCode, packageName);

            if (validationResult.isValid) {
                console.log(`\n✅ All validation layers passed!`);
                this.printValidationSummary(validationResult, attempt);
                
                return {
                    success: true,
                    code: generatedCode,
                    attempts: attempt,
                    validation: validationResult,
                    packageName,
                };
            }

            // Collect errors for retry
            validationErrors = validationResult.errors;
            
            console.log(`\n❌ Validation failed on layer: ${validationResult.failedLayer}`);
            this.printErrorSummary(validationErrors);

            if (attempt === this.maxRetries) {
                console.log(`\n⚠️  Max retries reached (${this.maxRetries})`);
                return {
                    success: false,
                    code: generatedCode,
                    attempts: attempt,
                    validation: validationResult,
                    errors: validationErrors,
                };
            }
        }
    }

    async generateCodeWithClaude(requirement, packageName, previousErrors, attempt) {
        let prompt = this.buildPrompt(requirement, packageName, previousErrors, attempt);
        
        const response = await fetch('https://api.anthropic.com/v1/messages', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'x-api-key': this.apiKey,
            },
            body: JSON.stringify({
                model: this.selectModel(attempt),
                max_tokens: 4096,
                messages: [{ role: 'user', content: prompt }],
            }),
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(`API error: ${data.error.message}`);
        }

        const text = data.content[0].text;
        return this.extractJavaCode(text);
    }

    buildPrompt(requirement, packageName, previousErrors = [], attempt = 1) {
        const basePrompt = `
You are a Java 17+ expert code generator. Generate PRODUCTION-GRADE Java code.

REQUIREMENT:
${requirement}

PACKAGE: ${packageName}

MANDATORY JAVA 17+ REQUIREMENTS:
1. Use modern Java 17+ features (records, sealed classes, pattern matching, text blocks, var)
2. Strict naming conventions: PascalCase for classes, camelCase for methods/vars, UPPER_SNAKE_CASE for constants
3. Comprehensive exception handling:
   - Custom exceptions for domain-specific errors
   - Try-with-resources for resource management
   - Proper error logging
   - No empty catch blocks
4. Annotations:
   - @Override for overridden methods
   - @NotNull/@Nullable for null safety
   - Custom annotations where appropriate
5. Security:
   - Input validation on all parameters
   - No hardcoded secrets
   - Use proper cryptography libraries
   - Parameterized queries (no SQL injection)
6. Performance:
   - Efficient data structures
   - Proper resource management
   - Caching where appropriate
   - Stream API for collections
7. Code quality:
   - Clear variable/method names
   - Single responsibility principle
   - Immutable records for data objects
   - Comprehensive Javadoc for public APIs

GUIDELINES:
- Import only necessary classes
- Use Optional<T> instead of null
- Implement equals/hashCode for records
- Use records for immutable data objects
- Include validation in constructors
- Log important operations (use SLF4J)
- Thread-safe where applicable

OUTPUT:
Return ONLY the complete, compilable Java code block in markdown.
Do NOT include explanations or comments outside the code.
`;

        if (attempt > 1 && previousErrors.length > 0) {
            return basePrompt + `

PREVIOUS ERRORS TO FIX (Attempt ${attempt}):
${previousErrors.map((err, i) => `${i + 1}. [${err.layer}] ${err.message}`).join('\n')}

CRITICAL:
- Fix ONLY the errors listed above
- Do NOT rewrite the entire solution
- Maintain all working functionality
- Ensure no new errors are introduced
`;
        }

        return basePrompt;
    }

    selectModel(attempt) {
        if (attempt === 1) return 'claude-sonnet-4-20250514';
        if (attempt <= 3) return 'claude-haiku-4-5-20241022';
        return 'claude-opus-4-20250805';
    }

    extractJavaCode(text) {
        const codeMatch = text.match(/```java\n([\s\S]*?)\n```/);
        if (codeMatch) {
            return codeMatch[1];
        }
        
        const genericMatch = text.match(/```\n([\s\S]*?)\n```/);
        if (genericMatch) {
            return genericMatch[1];
        }
        
        return text;
    }

    async executeValidation(code, packageName) {
        const tempFile = path.join(this.tempDir, `Generated${Date.now()}.java`);
        
        try {
            fs.writeFileSync(tempFile, code);

            // Layer 1: Compilation
            let result = this.validateCompilation(tempFile);
            if (!result.success) {
                return {
                    isValid: false,
                    failedLayer: 'compilation',
                    errors: [{ layer: 'Compilation', message: result.error }],
                };
            }

            // Layer 2: Static Analysis (Checkstyle)
            result = this.validateCheckstyle(tempFile);
            if (!result.success) {
                return {
                    isValid: false,
                    failedLayer: 'staticAnalysis',
                    errors: result.errors.map(e => ({ layer: 'Checkstyle', message: e })),
                };
            }

            // Layer 3: Best Practices
            result = this.validateBestPractices(code);
            if (!result.success) {
                return {
                    isValid: false,
                    failedLayer: 'bestPractices',
                    errors: result.errors.map(e => ({ layer: 'Best Practices', message: e })),
                };
            }

            // Layer 4: Security Analysis
            result = this.validateSecurity(code);
            if (!result.success) {
                return {
                    isValid: false,
                    failedLayer: 'security',
                    errors: result.errors.map(e => ({ layer: 'Security', message: e })),
                };
            }

            // Layer 5: Performance Patterns
            result = this.validatePerformance(code);
            if (!result.success) {
                return {
                    isValid: false,
                    failedLayer: 'performance',
                    errors: result.errors.map(e => ({ layer: 'Performance', message: e })),
                };
            }

            return {
                isValid: true,
                failedLayer: null,
                errors: [],
            };

        } finally {
            if (fs.existsSync(tempFile)) {
                fs.unlinkSync(tempFile);
            }
        }
    }

    validateCompilation(filePath) {
        const result = spawnSync('javac', [
            '--release', this.javaVersion,
            '--enable-preview',
            filePath
        ], {
            encoding: 'utf8',
            timeout: 10000,
        });

        if (result.error || result.status !== 0) {
            const error = result.stderr || result.error?.message || 'Compilation failed';
            return {
                success: false,
                error: this.cleanErrorMessage(error),
            };
        }

        return { success: true };
    }

    validateCheckstyle(filePath) {
        // Simulate checkstyle validation
        const errors = [];
        
        // Read the file
        const code = fs.readFileSync(filePath, 'utf8');
        
        // Check for common style issues
        if (/\n\s{2}\s+/.test(code)) {
            errors.push('Inconsistent indentation detected');
        }
        
        if (/\s+\n/.test(code)) {
            errors.push('Trailing whitespace detected');
        }
        
        if (!/^package\s+/.test(code)) {
            errors.push('Missing package declaration');
        }

        return {
            success: errors.length === 0,
            errors,
        };
    }

    validateBestPractices(code) {
        const errors = [];

        // Check for empty catch blocks
        if (/catch\s*\([^)]+\)\s*\{\s*\}/.test(code)) {
            errors.push('Empty catch block detected - must handle exceptions');
        }

        // Check for null checks
        if (/\.equals\(/.test(code) && !code.includes('Objects.equals')) {
            errors.push('Use Objects.equals() for null-safe equality');
        }

        // Check for try-with-resources
        if (/new\s+FileReader\s*\(/ .test(code) && !code.includes('try (')) {
            errors.push('FileReader should be used in try-with-resources');
        }

        // Check for var usage (Java 10+)
        if (!code.includes('var ') && /List<[^>]+>\s+\w+\s*=\s*new\s+ArrayList/.test(code)) {
            errors.push('Use var keyword for type inference (Java 10+)');
        }

        // Check for proper null handling
        if (/if\s*\([^=!]*==\s*null\)/.test(code) && !code.includes('Objects.requireNonNull')) {
            errors.push('Consider using Objects.requireNonNull for preconditions');
        }

        return {
            success: errors.length === 0,
            errors,
        };
    }

    validateSecurity(code) {
        const errors = [];

        // SQL Injection check
        if (/\+\s*".*"\s*\+\s*[a-zA-Z]/.test(code)) {
            errors.push('Possible SQL injection: Use parameterized queries');
        }

        // Hardcoded secrets
        if (/password\s*=\s*"[^"]+"/i.test(code) || /api[_-]?key\s*=\s*"[^"]+"/i.test(code)) {
            errors.push('Hardcoded credentials detected: Use environment variables');
        }

        // Weak cryptography
        if (/MD5|SHA1|DES/.test(code)) {
            errors.push('Weak cryptography detected: Use SHA-256 or better');
        }

        // Unsafe deserialization
        if (/readObject|ObjectInputStream/.test(code)) {
            errors.push('Unsafe deserialization: Use JSON deserialization instead');
        }

        return {
            success: errors.length === 0,
            errors,
        };
    }

    validatePerformance(code) {
        const errors = [];

        // String concatenation in loops
        if (/for\s*\([^)]*\)[\s\S]*?\+\s*"[^"]*"/.test(code)) {
            errors.push('String concatenation in loop: Use StringBuilder');
        }

        // Inefficient list operations
        if (/\.get\(i\)/.test(code) && !code.includes('for (') && !code.includes('stream')) {
            errors.push('Consider using enhanced for loop or streams');
        }

        return {
            success: errors.length === 0,
            errors,
        };
    }

    cleanErrorMessage(error) {
        return error.split('\n')[0].substring(0, 200);
    }

    printValidationSummary(result, attempts) {
        console.log(`\n${'═'.repeat(70)}`);
        console.log(`✅ VALIDATION SUMMARY`);
        console.log(`${'═'.repeat(70)}`);
        console.log(`Compilation:    ✓`);
        console.log(`Static Analysis: ✓`);
        console.log(`Best Practices:  ✓`);
        console.log(`Security:        ✓`);
        console.log(`Performance:     ✓`);
        console.log(`\nAttempts: ${attempts}`);
        console.log(`${'═'.repeat(70)}`);
    }

    printErrorSummary(errors) {
        errors.slice(0, 3).forEach(err => {
            console.log(`  • [${err.layer}] ${err.message}`);
        });
        if (errors.length > 3) {
            console.log(`  ... and ${errors.length - 3} more error(s)`);
        }
    }
}

// Usage Example
async function main() {
    const agent = new JavaCodeAgent(process.env.ANTHROPIC_API_KEY, {
        maxRetries: 4,
        javaVersion: '17',
        verbose: true,
    });

    const requirement = `
Create a PaymentProcessor class that:
1. Processes credit card payments
2. Validates payment amounts (0 < amount <= 10000)
3. Implements retry logic with exponential backoff
4. Logs all transactions
5. Handles payment failures gracefully
6. Uses custom exceptions for business logic
    `;

    const result = await agent.generateJavaCode(requirement, 'com.payment.processor');

    if (result.success) {
        console.log(`\n📦 Generated Java Code:\n`);
        console.log(result.code);
    } else {
        console.log(`\n❌ Failed to generate valid code after ${result.attempts} attempts`);
        console.log(`\nErrors:`);
        result.errors.forEach(err => {
            console.log(`  • ${err.layer}: ${err.message}`);
        });
    }
}

main().catch(console.error);

module.exports = JavaCodeAgent;
```

---

## Quality Checklist

### Pre-Release Verification

Use this checklist before deploying any AI-generated Java code:

```markdown
## Java 17+ Code Quality Checklist

### Compilation & Syntax
- [ ] Code compiles without errors with javac
- [ ] No warnings reported during compilation
- [ ] All imports are used (no unused imports)
- [ ] Correct Java version target (17+)

### Naming & Organization
- [ ] Classes: PascalCase
- [ ] Methods/Variables: camelCase
- [ ] Constants: UPPER_SNAKE_CASE
- [ ] Package names: lowercase, reversed domain
- [ ] Class members organized: static → instance → constructor → public → private

### Exception Handling
- [ ] All checked exceptions are caught or declared
- [ ] No empty catch blocks
- [ ] Custom exceptions for business logic
- [ ] Try-with-resources for file/stream handling
- [ ] Proper error logging with context
- [ ] Exception messages are user-friendly

### Java 17+ Features
- [ ] Records used for immutable data objects
- [ ] Sealed classes for controlled inheritance
- [ ] Pattern matching in switch statements
- [ ] Text blocks for multi-line strings
- [ ] var keyword for type inference (where appropriate)
- [ ] Stream API for collections

### Security
- [ ] No hardcoded credentials
- [ ] Input validation on all parameters
- [ ] Parameterized queries (no SQL injection)
- [ ] No unsafe deserialization
- [ ] Secure password hashing (bcrypt)
- [ ] No weak cryptography (MD5, SHA1)
- [ ] CORS properly configured
- [ ] Authentication/Authorization checks

### Performance
- [ ] No string concatenation in loops
- [ ] Proper collection initialization with capacity
- [ ] Connection pooling configured
- [ ] Caching implemented where appropriate
- [ ] Efficient pagination for large datasets
- [ ] No N+1 query problems
- [ ] StringBuilder for dynamic string building

### Code Quality
- [ ] Clear, descriptive variable names
- [ ] Methods have single responsibility
- [ ] Comments explain "why" not "what"
- [ ] No dead code or commented-out code
- [ ] Proper use of Optional<T> instead of null
- [ ] Null-safe operations (Objects.requireNonNull, Objects.equals)

### Testing
- [ ] All public methods have Javadoc
- [ ] Null-safe Javadoc with @param annotations
- [ ] @throws documented for checked exceptions
- [ ] @return documented for return values
- [ ] Unit tests for critical functionality

### Annotations
- [ ] @Override used for overridden methods
- [ ] @Deprecated used for legacy code
- [ ] @Nullable/@NotNull used for clarity
- [ ] @FunctionalInterface for lambda targets
- [ ] Custom annotations where appropriate

### Thread Safety
- [ ] Shared mutable state is synchronized
- [ ] ConcurrentHashMap used instead of HashMap when needed
- [ ] Immutable records used for shared data
- [ ] ThreadLocal used appropriately (if at all)

### Documentation
- [ ] Class-level Javadoc explains purpose
- [ ] Method-level Javadoc explains usage
- [ ] Complex logic has inline comments
- [ ] README explains how to use the code
- [ ] Dependencies are documented

### Deployment
- [ ] No development/debug code in production
- [ ] Environment variables used for configuration
- [ ] Error handling doesn't expose sensitive info
- [ ] Logging is appropriate level (not too verbose)
- [ ] Metrics are collected for monitoring
```

---

## Key Takeaways

✅ **Java 17+ is enterprise-grade** - Use records, sealed classes, and pattern matching

✅ **Multi-layer validation catches all issues** - Compilation → static analysis → best practices → security → performance

✅ **Exception handling is non-negotiable** - Custom exceptions, comprehensive try-catch, proper logging

✅ **Security-first approach** - Input validation, parameterized queries, no hardcoded secrets

✅ **Zero-compilation-error guarantee** - The agent retries until code is production-ready

✅ **Performance patterns matter** - Streaming, caching, pagination, connection pooling

✅ **Cost optimization works for Java too** - Use cheaper models for retries, fail fast on unrecoverable errors

---

## Resources

- [Java 17 Release Notes](https://jdk.java.net/17/release-notes)
- [Java Language Updates](https://docs.oracle.com/en/java/javase/17/docs/api/)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Checkstyle Configuration](https://checkstyle.org/)
- [SpotBugs Documentation](https://spotbugs.readthedocs.io/)
