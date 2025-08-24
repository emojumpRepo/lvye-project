# Project Structure & Organization

## Root Level Structure

```
yudao/
├── yudao-dependencies/          # Maven dependency management (BOM)
├── yudao-framework/             # Core framework extensions and starters
├── yudao-server/               # Main application server (executable JAR)
├── yudao-module-*/             # Business modules
├── yudao-ui/                   # Frontend projects
├── sql/                        # Database scripts for different DB types
├── script/                     # Deployment and utility scripts
└── .image/                     # Documentation images
```

## Module Architecture

### Core Modules (Always Required)
- **yudao-module-system**: User management, roles, permissions, departments, tenants
- **yudao-module-infra**: Infrastructure services (file storage, code generation, monitoring)

### Business Modules (Optional)
- **yudao-module-psychology**: Student psychological assessment and management
- **yudao-module-bpm**: Workflow and business process management
- **yudao-module-pay**: Payment system integration
- **yudao-module-mall**: E-commerce functionality
- **yudao-module-crm**: Customer relationship management
- **yudao-module-erp**: Enterprise resource planning
- **yudao-module-ai**: AI and large language model integration
- **yudao-module-member**: Member/customer management
- **yudao-module-mp**: WeChat integration
- **yudao-module-report**: Reporting and dashboard
- **yudao-module-iot**: IoT device management

## Standard Module Structure

Each business module follows this consistent pattern:

```
yudao-module-{name}/
├── pom.xml                     # Module dependencies
└── src/main/java/cn/iocoder/yudao/module/{name}/
    ├── api/                    # External API interfaces (for other modules)
    ├── controller/             # REST API controllers
    │   ├── admin/              # Admin backend APIs (/admin-api/{name}/**)
    │   └── app/                # User/mobile APIs (/app-api/{name}/**)
    ├── service/                # Business logic services
    │   └── impl/               # Service implementations
    ├── dal/                    # Data Access Layer
    │   ├── dataobject/         # Database entities (DO)
    │   ├── mysql/              # MyBatis mappers
    │   └── redis/              # Redis operations
    ├── convert/                # MapStruct converters (DTO ↔ DO)
    ├── enums/                  # Enums and constants
    └── framework/              # Module-specific configurations
```

## Naming Conventions

### Package Structure
- Base package: `cn.iocoder.yudao.module.{moduleName}`
- Controllers: `controller.admin.{domain}` or `controller.app.{domain}`
- Services: `service.{domain}` with `impl` subpackage
- Data objects: `dal.dataobject.{domain}`
- Mappers: `dal.mysql.{domain}`

### Class Naming
- **Controllers**: `{Domain}Controller` (e.g., `UserController`)
- **Services**: `{Domain}Service` interface + `{Domain}ServiceImpl`
- **Data Objects**: `{Domain}DO` (e.g., `UserDO`)
- **Mappers**: `{Domain}Mapper` (e.g., `UserMapper`)
- **Converters**: `{Domain}Convert` (e.g., `UserConvert`)
- **Request VOs**: `{Domain}{Action}ReqVO` (e.g., `UserCreateReqVO`)
- **Response VOs**: `{Domain}RespVO` (e.g., `UserRespVO`)

### Database Conventions
- **Table Names**: `{module_prefix}_{domain}` (e.g., `psychology_student_profile`)
- **Primary Keys**: Always `id` (Long type)
- **Audit Fields**: `creator`, `create_time`, `updater`, `update_time`
- **Soft Delete**: `deleted` (Boolean, default false)
- **Multi-tenant**: `tenant_id` (Long)

## API Structure

### Admin APIs (Management Backend)
- **Base Path**: `/admin-api/{module}/**`
- **Authentication**: Admin user login required
- **Permissions**: Fine-grained permission checks with `@PreAuthorize`

### App APIs (User/Mobile)
- **Base Path**: `/app-api/{module}/**`
- **Authentication**: Member user login (where applicable)
- **Permissions**: User-based data access control

### Common Patterns
- **CRUD Operations**: `create`, `update`, `delete`, `get`, `page`
- **Batch Operations**: `batch-delete`, `batch-update`
- **Export**: `export-excel`
- **Simple Lists**: `list-all-simple` (for dropdowns)

## Configuration Files

### Module Configuration
- **application.yml**: Main configuration in yudao-server
- **Module-specific**: Each module can have its own configuration classes
- **Database**: SQL scripts in `/sql/{database_type}/`

### Frontend Integration
- **Vue3 + Element Plus**: Primary admin interface
- **Vue3 + Vben**: Alternative admin interface
- **UniApp**: Mobile and mini-program support

## Development Workflow

### Adding New Features
1. Create controller with proper REST endpoints
2. Implement service layer with business logic
3. Create data objects and mappers
4. Add converters for DTO transformations
5. Write comprehensive unit tests
6. Update API documentation

### Module Dependencies
- Modules can depend on `yudao-module-system` and `yudao-module-infra`
- Business modules should not depend on each other directly
- Use API interfaces for cross-module communication

### Code Quality Standards
- Follow Alibaba Java Development Guidelines
- Maintain high test coverage with JUnit + Mockito
- Use Lombok to reduce boilerplate code
- Comprehensive Swagger API documentation
- Detailed code comments (113,770 lines of Java, 42,462 lines of comments)