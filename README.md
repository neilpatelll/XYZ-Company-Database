# XYZ Company Database Management Application

## Project Overview
This Java-based desktop application provides a complete CRUD (Create, Read, Update, Delete) interface for interacting with a MySQL database named `xyz_company_db`. It leverages JDBC for database connectivity and Java Swing for the graphical user interface.

The application supports the following operations:
- Browse and display table contents with a row limit for performance
- Dynamically generate input forms for selected tables
- Insert new records into the selected table
- Update existing records using a primary key reference
- Delete records from the selected table

---

## How to Run the Project

### Prerequisites
- Java JDK 8 or higher
- MySQL Server with a database named `xyz_company_db`
- MySQL JDBC Driver (e.g., `mysql-connector-java-8.0.xx.jar`)
- IDE such as IntelliJ IDEA, Eclipse, or command-line tools

### Setup Instructions
1. Ensure the MySQL database `xyz_company_db` exists and contains at least one table.
2. Modify the database credentials in the `App.java` source file:
```java
private static final String URL = "jdbc:mysql://127.0.0.1:3306/xyz_company_db";
private static final String USER = "root";
private static final String PASSWORD = "your_password";
```
3. Add the JDBC driver to your project's classpath.

### Running the Application
**Using the command line:**
```bash
javac -cp ".:mysql-connector-java-8.0.xx.jar" App.java
java -cp ".:mysql-connector-java-8.0.xx.jar" App
```

**Using an IDE:**
1. Import the project and add the JDBC driver to the library dependencies.
2. Compile and run `App.java`.

---

## Application Features
- **Browse Tables**: Select a table to load and display its contents (up to 500 rows).
- **Insert Records**: Dynamically generated form based on table metadata for inserting new rows.
- **Update Records**: Edit an existing record by specifying the primary key value.
- **Delete Records**: Remove a record using the primary key, with confirmation dialog.
- **Status Bar**: Displays operation results and error messages.

---

## Project Structure
```
├── App.java                      // Main Java application
├── README.md                     // Project documentation
└── lib/
    └── mysql-connector-java.jar  // MySQL JDBC driver
```

---

## Project Deliverables
This section is reserved for including project-related artifacts. Replace the placeholder references below with actual documents or image paths.

### Entity-Relationship Diagram
![er diagram](https://github.com/user-attachments/assets/32d73022-e74c-49d5-b269-322692678843)


### Logical Schema Diagram
![logical diagram](https://github.com/user-attachments/assets/a25097f8-ba71-4e5c-a495-7f99a6469586)


### Normalized Table Structures (3NF)
- Department(DeptID, DeptName)
- Employee(EmpID, Name, DeptID, Title)
- Project(ProjectID, Name, Budget, DeptID)

### Sample SQL Queries
```sql
SELECT DeptName, COUNT(*)
FROM Employee
JOIN Department ON Employee.DeptID = Department.DeptID
GROUP BY DeptName;
```

---

## Technologies Used
- Java (Swing GUI Framework)
- JDBC (Java Database Connectivity)
- MySQL (Relational Database Management System)
- Development Environment: IntelliJ IDEA / Eclipse
