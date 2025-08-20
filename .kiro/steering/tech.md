# Technology Stack

## Build System & Runtime

- **Build Tool**: Maven 3.x with multi-module architecture
- **Java Version**: JDK 17+ (supports JDK 21)
- **Spring Boot**: 3.4.5
- **Package Manager**: Maven with Huawei/Aliyun mirrors for faster downloads

## Core Framework Stack

### Backend Framework
- **Spring Boot**: 3.4.5 - Main application framework
- **Spring MVC**: 6.1.10 - Web framework
- **Spring Security**: 6.3.1 - Security and authentication
- **Hibernate Validator**: 8.0.1 - Parameter validation

### Database & Persistence
- **MySQL**: 5.7/8.0+ - Primary database (also supports Oracle, PostgreSQL, SQL Server, MariaDB, DM, TiDB)
- **MyBatis Plus**: 3.5.7 - ORM framework with enhancements
- **Druid**: 1.2.23 - JDBC connection pool and monitoring
- **Dynamic Datasource**: 4.3.1 - Multi-datasource support

### Caching & Message Queue
- **Redis**: 5.0/6.0/7.0 - Caching and session storage
- **Redisson**: 3.32.0 - Redis client with distributed features
- **Built-in MQ**: Redis-based message queue (Stream for cluster, Pub/Sub for broadcast)

### Workflow & Scheduling
- **Flowable**: 7.0.0 - Workflow engine (BPMN support)
- **Quartz**: 2.3.2 - Job scheduling

### Documentation & Monitoring
- **Springdoc**: 2.3.0 - API documentation (Swagger)
- **SkyWalking**: 9.0.0 - Distributed tracing and logging
- **Spring Boot Admin**: 3.3.2 - Application monitoring

### Utilities & Code Generation
- **Jackson**: 2.17.1 - JSON processing
- **MapStruct**: 1.6.3 - Bean mapping
- **Lombok**: 1.18.34 - Code generation
- **JUnit**: 5.10.1 - Unit testing
- **Mockito**: 5.7.0 - Mocking framework

## Common Commands

### Development
```bash
# Build entire project
mvn clean compile

# Run tests
mvn test

# Package application
mvn clean package

# Skip tests during build
mvn clean package -DskipTests

# Run specific module tests
mvn test -pl yudao-module-psychology
```

### Running Application
```bash
# Start with embedded Redis (development)
java -jar yudao-server/target/yudao-server.jar

# Start without Redis mock (production)
java -Dyudao.redis.mock.enabled=false -jar yudao-server/target/yudao-server.jar

# Or use the provided batch script
start-without-redis-mock.bat
```

### Code Generation
- Use built-in code generator for CRUD operations
- Generates Java, Vue, SQL, and unit tests
- Supports single table, tree table, and master-detail tables

## Architecture Patterns

### Multi-Module Maven Structure
- Parent POM manages dependencies and versions
- Each business domain is a separate module
- Framework components are reusable across modules

### Layered Architecture
- **Controller Layer**: REST API endpoints with Swagger documentation
- **Service Layer**: Business logic implementation
- **Data Access Layer**: MyBatis Plus mappers and data objects
- **Convert Layer**: MapStruct converters for DTO/DO transformation

### Security & Permissions
- Spring Security with JWT tokens
- Multi-tenant data isolation
- Fine-grained permission control (menu, button level)
- SSO support with OAuth2

### Database Design
- Multi-tenant architecture with tenant_id isolation
- Soft delete pattern with deleted flag
- Audit fields (creator, create_time, updater, update_time)
- Optimistic locking with version field