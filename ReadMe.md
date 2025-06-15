# 📄 Document Ingestion System

> A Spring Boot 3.5.0 application built with Java 17 for secure document ingestion and management. This project provides APIs for user authentication, role-based access, and file operations (upload, delete, update, view) with PostgreSQL as the database.

---

## 🚀 Features

### 🔐 Authentication & Authorization
- User Registration & Login
- JWT-based Authentication
- Role-based Authorization: `ADMIN`, `EDITOR`, `VIEWER`

### 👤 User Management
- Register and login users
- Admin APIs to:
    - Delete users
    - Modify user roles

### 📁 File Management
- Upload, update, and delete documents
- View documents by file ID
- Retrieve metadata by:
    - `fileId`
    - `editorId`
    - `keyword`
    - `fileType`

### 🧠 Question and Answer
- Ask a question to retrieve the most relevant document(s)

### 🛢️ Persistence
- PostgreSQL for storing users and files
- Redis Server for caching

---

## 🧰 Tech Stack

- Java 17
- Spring Boot 3.5.0
- Spring Security
- JWT (JSON Web Token)
- PostgreSQL
- Maven
- Redis Server

---

## 🛠️ Getting Started

### Prerequisites

- Java 17
- Maven
- PostgreSQL
- Redis Server

### Database Setup

Create a PostgreSQL database and update your `application.properties`:

```properties
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://localhost:5432/documentqaapp
spring.datasource.username=aman.sharma@mheducation.com
spring.datasource.password=12345
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.auto-commit=true
spring.datasource.hikari.pool-name=DocumentQAHikariPool

spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
```

### Redis Setup

Create a Redis and update your `application.properties`:

```properties
# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=foobared
spring.data.redis.timeout=60000ms
spring.data.redis.database=0

spring.data.redis.lettuce.pool.max-active=8
spring.data.redis.lettuce.pool.max-wait=-1ms
spring.data.redis.lettuce.pool.max-idle=8
spring.data.redis.lettuce.pool.min-idle=0

# Cache Configuration
spring.cache.type=redis
spring.cache.redis.time-to-live=600000ms
```

## Running the Application

```bash
git clone https://github.com/your-username/document-ingestion-system.git
cd document-qa-app
mvn clean install
mvn spring-boot:run
```
---

## 📑 API Documentation

All API endpoints are documented and can be accessed via the **Swagger UI**:

👉 [Swagger UI](http://localhost:8080/swagger-ui/index.html)

> Replace `localhost:8080` with your server URL and port if different.


## 🔑 Authentication

For all protected endpoints, include the JWT token in the request header:
> Authorization: Bearer <your_jwt_token>

## 👥 Roles & Permissions

| Role   | Permissions                          |
|--------|------------------------------------|
| ADMIN  | Full access, user and file management |
| EDITOR | Upload and edit files               |
| VIEWER | View documents and fetch metadata  |


## 🧪 Testing

Run tests using:

```bash
mvn test
```

## 📬 Postman Collection

A sample Postman collection is available in the `docs/` folder for testing the APIs.

--- 

## 🐳 Dockerizing the Spring Boot Application with PostgreSQL and Redis Server

### 1. Update application.properties for Docker
```properties
spring.datasource.url=jdbc:postgresql://${DB_HOST:postgres}:${DB_PORT:5432}/${DB_NAME:docdb}
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD}
spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}
```

### 2. Build the Application JAR
```bash
mvn clean package
```
Make sure the .jar file exists in the target/ folder before continuing.

### 3. Build and Run the Docker Containers
```bash
docker-compose up --build
```
This will:
- Start a PostgreSQL container
- Start a Redis server container
- Build and run your Spring Boot app container
- Bind app to http://localhost:8080

---

## 🚀 Future Deployment to AWS EC2, RDS and Redis

Follow these steps to deploy your Spring Boot Document Ingestion application on AWS:

### 1. Prepare the AWS RDS (PostgreSQL) Database

- Go to AWS Management Console → RDS → Create database
- Choose **PostgreSQL** as the engine
- Select instance class, storage, and other settings as needed
- Set master username & password
- Configure VPC and security groups (allow your EC2 instance IP or `0.0.0.0/0` for testing)
- Create the database and note the endpoint, port, username, and password

### 2. Configure Spring Boot Application

- Update`application.properties` with RDS connection details:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://<rds-endpoint>:5432/<database-name>
    username: <your-rds-username>
    password: <your-rds-password>
  jpa:
    hibernate:
      ddl-auto: update
    database-platform: org.hibernate.dialect.PostgreSQLDialect
  redis:
    host: <redis-endpoint>
    port: 6379
```

- Build your Spring Boot fat jar:
```bash
mvn clean package
```
### 3. Launch an EC2 Instance

- Go to AWS Management Console → EC2 → Launch Instance.
- Choose Amazon Linux 2 or Ubuntu AMI.
- Select instance type (e.g., `t2.micro` for testing).
- Configure security group to allow:
  - Inbound port **8080** (or your app port).
  - SSH port **22** from your IP.
  - Optionally **6379** (Redis, only if using self-managed Redis)
- Launch the instance and note its public IP or DNS.

### 4. Connect to EC2 and Setup Environment

- SSH into the EC2 instance:

  ```bash
  ssh -i /path/to/your-key.pem ec2-user@<ec2-public-ip>
  ```
- Install Java 17 (Amazon Linux 2 example):
```bash
sudo amazon-linux-extras enable corretto17
sudo yum install -y java-17-amazon-corretto-devel
java -version
```

- Transfer your jar file to EC2:
```bash
scp -i /path/to/your-key.pem target/document-ingestion-system.jar ec2-user@<ec2-public-ip>:~
```

### 5. Run Your Spring Boot Application on EC2
- Start the app
```bash
  java -jar document-ingestion-system.jar
```

### 6. Access Your Application
- Visit:
```bash
http://<ec2-public-ip>:8080/
```
