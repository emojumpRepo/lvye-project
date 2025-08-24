# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is **芋道 RuoYi-Vue-Pro**, a comprehensive Java enterprise management system built on Spring Boot 3.4.5 + JDK 17. It's a multi-module Maven project that provides a complete business management platform with multiple frontend options.

### Architecture

- **Backend**: Spring Boot 3.4.5 with JDK 17, multi-module Maven architecture
- **Frontend**: Multiple UI options available in `yudao-ui/` directory:
  - Vue3 + Vben5 (Ant Design) - Main admin interface
  - Vue3 + Element Plus
  - Vue2 + Element UI
- **Database**: Supports MySQL, PostgreSQL, Oracle, SQL Server, and domestic databases
- **Message Queue**: Redis, RabbitMQ, Kafka, RocketMQ support
- **Cache**: Redis + Redisson
- **Workflow**: Flowable integration

## Development Commands

### Backend (Java/Maven)
```bash
# Build the entire project
mvn clean package -Dmaven.test.skip=true

# Run tests for specific module
mvn test -pl yudao-module-system

# Start the server (from yudao-server directory)
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring.profiles.active=local
```

### Frontend (Vue/Vben)
Navigate to `yudao-ui/yudao-ui-admin-vben/` and use:
```bash
# Install dependencies
pnpm install

# Development server
pnpm dev:antd    # Ant Design version
pnpm dev:ele     # Element Plus version

# Build for production
pnpm build:antd  # Ant Design version
pnpm build:ele   # Element Plus version

# Type checking
pnpm check:type

# Linting
pnpm lint

# Run tests
pnpm test:unit
pnpm test:e2e
```

## Module Architecture

The project follows a modular architecture with clear separation:

### Core Modules
- **yudao-dependencies**: Maven dependency management
- **yudao-framework**: Custom Spring Boot starters and framework extensions
- **yudao-server**: Main application server entry point
- **yudao-module-system**: Core system functionality (users, roles, permissions, tenants)
- **yudao-module-infra**: Infrastructure services (file storage, code generation, monitoring)

### Business Modules
- **yudao-module-bpm**: Workflow management (Flowable)
- **yudao-module-pay**: Payment system integration
- **yudao-module-member**: Member management
- **yudao-module-mall**: E-commerce functionality
- **yudao-module-crm**: Customer relationship management
- **yudao-module-erp**: Enterprise resource planning
- **yudao-module-ai**: AI integration features
- **yudao-module-iot**: IoT device management
- **yudao-module-mp**: WeChat Mini Program integration
- **yudao-module-psychology**: Psychology assessment system (custom module)

### Framework Components
Each `yudao-spring-boot-starter-*` provides specific functionality:
- `security`: Authentication and authorization
- `mybatis`: Database access layer
- `redis`: Cache management
- `web`: Web layer configuration
- `job`: Scheduled tasks
- `mq`: Message queue abstraction
- `monitor`: Application monitoring
- `websocket`: WebSocket support

## Key Configuration Files

- **Application Config**: `yudao-server/src/main/resources/application*.yaml`
- **Maven Parent**: Root `pom.xml` defines module structure
- **Frontend Config**: `yudao-ui/yudao-ui-admin-vben/package.json`
- **Workflow Config**: Flowable configuration in main application.yaml

## Database and Infrastructure

### Multi-Tenant Architecture
The system supports SaaS multi-tenancy with tenant isolation at the database level.

### Database Support
- Primary: MySQL 5.7/8.0+
- Supported: PostgreSQL, Oracle, SQL Server, MariaDB, DM (达梦), TiDB
- Schema initialization scripts available in `sql/` directory for each database type

### Cache Strategy
- Redis for session management, permissions, and business cache
- Redisson for distributed locks and advanced Redis features

## Development Practices

### Code Generation
The system includes a powerful code generator at `yudao-module-infra`:
- Generates Java backend code (Controller, Service, Mapper, DO)
- Generates Vue frontend code (forms, tables, CRUD operations)
- Generates SQL scripts and API documentation
- Templates available for different frontend frameworks

### Testing Strategy
- Unit tests using JUnit 5 + Mockito
- Test resources in `src/test/resources/` for each module
- Database test fixtures and cleanup scripts available

### API Documentation
- Swagger/OpenAPI 3.0 integration
- Automatic API documentation generation
- Accessible at `/swagger-ui` endpoint

## Multi-Frontend Support

### Admin Interfaces
1. **Vue3 + Vben5 + Ant Design** (Recommended)
   - Location: `yudao-ui/yudao-ui-admin-vben/`
   - Modern component library with rich features
   - Monorepo structure with multiple apps

2. **Vue3 + Element Plus**
   - Separate repository: `yudao-ui-admin-vue3`
   - Traditional single-page application structure

3. **Vue2 + Element UI**
   - Legacy support for older projects
   - Separate repository: `yudao-ui-admin-vue2`

### Mobile Support
- UniApp-based mobile admin interface
- Cross-platform support (iOS, Android, H5, Mini Programs)

## Security and Authentication

- Spring Security integration with JWT tokens
- Multi-terminal authentication support
- Role-based access control (RBAC) with data permissions
- SSO single sign-on support
- OAuth2 integration capabilities

## Special Features

### Psychology Module
A custom module (`yudao-module-psychology`) provides:
- Student psychological assessment management
- Multi-role permission system (administrators, teachers, counselors)
- Assessment task creation and management
- Timeline tracking for student records
- Quick reporting functionality for teachers

### Workflow Engine
Integrated Flowable BPMN engine with:
- Visual process designer (both simple and BPMN modes)
- Dynamic form generation
- Multi-signature approvals, delegation, and transfer
- Timeout handling and automatic reminders

### IoT Integration
Plugin-based IoT device management with:
- MQTT, HTTP, and EMQX protocol support
- Device lifecycle management
- Real-time data collection and processing

## Plugin Architecture

The system supports a plugin-based architecture for extending functionality:
- Plugin directory: `yudao-module-iot-plugins/`
- Hot-swappable plugin system
- Each plugin has its own configuration and assembly structure

## Common Troubleshooting

### Build Issues
- Ensure JDK 17 is configured correctly
- Run `mvn clean` before building if encountering dependency conflicts
- Check that all commented modules in root `pom.xml` are intentionally disabled

### Frontend Issues  
- Use `pnpm` package manager as specified
- Ensure Node.js version >= 20.10.0
- Run `pnpm reinstall` if encountering dependency issues

### Database Issues
- Verify database schema initialization scripts in `sql/` directory
- Check `application-local.yaml` for correct database configuration
- Ensure proper database permissions for schema creation