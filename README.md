# Bug Tracking System

A comprehensive Java full-stack bug tracking system built with Spring Boot and React.

## Features

### Core Features
- **User Management**: Role-based access control (Admin, Developer, Tester)
- **Project Management**: Create and manage projects
- **Bug Tracking**: Create, assign, and track bugs through their lifecycle
- **Comment System**: Communication between testers and developers
- **Dashboard**: Real-time statistics and charts
- **Search & Filter**: Advanced bug search and filtering capabilities

### Advanced Features
- **JWT Authentication**: Secure token-based authentication
- **Role-based Security**: Different permissions for different user roles
- **RESTful APIs**: Clean and well-documented API endpoints
- **Modern UI**: Responsive React frontend with Ant Design
- **Data Visualization**: Charts showing bug statistics

## Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security**
- **Spring Data JPA**
- **MySQL Database**
- **JWT Authentication**

### Frontend
- **React 18**
- **Ant Design**
- **React Router**
- **Axios**
- **Recharts** (for charts)

## Database Schema

### Users Table
- id (Primary Key)
- name
- email (Unique)
- password (Encrypted)
- role (ADMIN/DEVELOPER/TESTER)
- created_date

### Projects Table
- id (Primary Key)
- name
- description
- created_date

### Bugs Table
- id (Primary Key)
- title
- description
- severity (LOW/MEDIUM/HIGH/CRITICAL)
- priority (LOW/MEDIUM/HIGH/URGENT)
- status (OPEN/ASSIGNED/IN_PROGRESS/RESOLVED/CLOSED/REOPENED/REJECTED)
- project_id (Foreign Key)
- created_by (Foreign Key to Users)
- assigned_to (Foreign Key to Users)
- created_date
- updated_date

### Comments Table
- id (Primary Key)
- comment_text
- bug_id (Foreign Key)
- user_id (Foreign Key)
- created_date

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Node.js 16 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher

### Database Setup
1. Create a MySQL database named `bug_tracker`
2. Update the database credentials in `src/main/resources/application.properties`
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bug_tracker?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Backend Setup
1. Navigate to the project root directory
2. Build the project using Maven:
```bash
mvn clean install
```
3. Run the Spring Boot application:
```bash
mvn spring-boot:run
```
The backend will start on `http://localhost:8080`

### Frontend Setup
1. Navigate to the frontend directory:
```bash
cd frontend
```
2. Install dependencies:
```bash
npm install
```
3. Start the React development server:
```bash
npm start
```
The frontend will start on `http://localhost:3000`

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login

### Users
- `GET /api/users` - Get all users (Admin only)
- `POST /api/users/register` - Register new user
- `GET /api/users/{id}` - Get user by ID (Admin only)
- `PUT /api/users/{id}` - Update user (Admin only)
- `DELETE /api/users/{id}` - Delete user (Admin only)

### Projects
- `GET /api/projects` - Get all projects
- `POST /api/projects` - Create project (Admin only)
- `GET /api/projects/{id}` - Get project by ID
- `PUT /api/projects/{id}` - Update project (Admin only)
- `DELETE /api/projects/{id}` - Delete project (Admin only)

### Bugs
- `GET /api/bugs` - Get all bugs
- `POST /api/bugs` - Create bug (Tester/Admin only)
- `GET /api/bugs/{id}` - Get bug by ID
- `PUT /api/bugs/{id}` - Update bug (Developer/Admin only)
- `DELETE /api/bugs/{id}` - Delete bug (Admin only)
- `PUT /api/bugs/{id}/assign` - Assign bug to developer
- `PUT /api/bugs/{id}/status` - Update bug status
- `GET /api/bugs/search` - Search bugs with filters

### Comments
- `GET /api/comments/bug/{bugId}` - Get comments for a bug
- `POST /api/comments` - Add comment to bug
- `PUT /api/comments/{id}` - Update comment
- `DELETE /api/comments/{id}` - Delete comment

### Dashboard
- `GET /api/dashboard/stats` - Get dashboard statistics
- `GET /api/dashboard/developer-stats` - Get developer statistics

## User Roles and Permissions

### Admin
- Manage all users
- Create and manage projects
- Full access to all bugs
- View dashboard statistics

### Developer
- View assigned bugs
- Update bug status
- Add comments to bugs
- View dashboard statistics

### Tester
- Create new bugs
- View created bugs
- Add comments to bugs
- View dashboard statistics

## Default Users

After starting the application, you can create users through the registration endpoint or directly in the database. Here are some example users you might want to create:

**Admin User:**
- Email: admin@example.com
- Password: admin123
- Role: ADMIN

**Developer User:**
- Email: dev@example.com
- Password: dev123
- Role: DEVELOPER

**Tester User:**
- Email: tester@example.com
- Password: tester123
- Role: TESTER

## Running the Application

1. Start the MySQL database
2. Run the Spring Boot backend application
3. Start the React frontend application
4. Open `http://localhost:3000` in your browser
5. Login with your credentials or create a new user

## Screenshots

The application includes:
- Login page with secure authentication
- Dashboard with real-time statistics and charts
- Bug management with search and filtering
- Project management interface
- User management (Admin only)
- Detailed bug view with comments

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.
