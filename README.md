# Fintech Service Backend

A Java-based backend service for financial technology applications, providing secure user management, loan processing, and transaction handling capabilities.

## Features

### User Account Management
- CRUD operations for user accounts
- Email and phone validation
- Secure password hashing with BCrypt

### Loan Management
- Loan application processing
- Status tracking (approved/rejected/completed/pending)
- Dynamic interest rate calculation
- User loan history

### Transaction Processing
- Transaction recording and validation
- Balance management
- Statement generation

### Security
- JWT authentication
- Role-based access control (Admin/User)
- Rate limiting
- Input validation

### Monitoring
- Audit logging for critical operations
- Health check endpoints
- Performance metrics

## Tech Stack

- Java 17
- Spring Boot
- PostgreSQL
- Spring Security
- JUnit 5
- Docker

## Prerequisites

- JDK 17
- PostgreSQL 14+
- Maven 3.8+
- Docker (optional)

## Setup

1. Clone the repository:
```bash
git clone https://github.com/johnDevALX/service-backend-fintech
cd fintech-service
```

2. Configure database:
```bash
cp src/main/resources/application.properties
# Update database credentials in application.yml
```

3. Build and run:
```bash
mvn clean install
java -jar target/fintech-service.jar
```

## API Documentation

Access Swagger UI through any of these URLs:
- `http://localhost:8080/` (redirects to Swagger)
- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/swagger-ui.html`

## Testing

Run tests:
```bash
mvn test                 # Unit tests
mvn verify              
```

## Database Schema

Key entities:
- Users
- Loans
- Transactions
- Audit_Logs

## Security Considerations

- All endpoints except /v1/user/** require authentication
- Passwords are hashed using BCrypt
- API rate limiting: 5 requests/minute
- Role-based endpoint restrictions

## Monitoring

Health check endpoint: `http://localhost:8080/actuator/health`

## License

MIT