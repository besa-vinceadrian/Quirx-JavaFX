# Quirx: Task Management System  
**"Get It Done, The Quirx Way"**

Quirx is a desktop-based task management application developed to help individuals and teams efficiently create, assign, prioritize, and track tasks. It features a drag-and-drop interface, email notifications, and task categorization, making it ideal for managing both personal workloads and collaborative team efforts.

## 🚀 Features

- ✅ Create, assign, and manage tasks
- 📌 Prioritize tasks based on levels: High, Medium, or Low
- 🗂 Monitor task statuses: Not Started, Ongoing, Done
- 📨 Email notifications for task assignments and deadlines (via Jakarta Mail)
- 📦 Local SQL database with remote access via **Zero Tier One**
- 🖱️ Drag-and-drop JavaFX interface built with **SceneBuilder**
- 🔐 Real-time error logging and basic validation

## 🎯 Project Objectives

- Deliver a reliable and user-friendly task management application
- Support task assignment and collaboration in team's workspaces
- Enable priority-based task tracking
- Provide email-based reminders
- Design a scalable structure for both personal and team usage

## 🛠️ Tech Stack

| Layer         | Tools & Technologies |
|---------------|----------------------|
| Language       | Java (Java 8+) |
| UI Framework   | JavaFX (Designed via SceneBuilder) |
| Database       | SQL Server |
| Database Access| JDBC + ZeroTier for remote access DB |
| Email Service  | Jakarta Mail (Jakarta EE JARs) |
| IDE            | Eclipse |
| Database Mgmt  | SQL Server Management Studio (SSMS) |
| Version Control| Git |

## 🧰 Development Tools

- **Java** – Primary programming language  
- **SceneBuilder** – For creating the GUI visually  
- **JDBC** – For Java-SQL Server connectivity  
- **ZeroTier** – Allowing remote access to the local SQL Server instance  
- **Jakarta Mail JARs** – For sending task assignment and deadline emails  
- **Git** – Version control using CLI and Eclipse integration  

## 📋 Reporting

- **Session Reports:** Track created, completed, and overdue tasks  
- **In-App Feedback:** Real-time notifications, error messages, and status updates  
- **Logged Errors:** Invalid inputs, assignment failures, and database issues  

## ⚠️ Limitations & Constraints

- No cloud-based syncing (desktop-local use only)  
- Local machine availability affects application access  
- Basic security (no role-based access or cloud backup)  
- Manual deployment only (no CI/CD pipeline)

## 👨‍💻 Team Members

| Role               | Name                    |
|--------------------|-------------------------|
| Team Lead          | Vince Adrian Besa       |
| Frontend Developer | King Andrei Gutteirez   |
| Backend Developer  | Bench Brian Bualat      |
| Document Analyst   | Karl Christian Caya     |
| Data Admin         | Norlan Arguelles        |
| Researcher         | Ciara Marie Condino     |
| QA Tester          | Michael Maestre         |
| Presentor          | Ma. Rose Tolentino      |

## 📦 Getting Started

1. **Clone the repository:**
   ```bash
   git clone git@github.com:besa-vinceadrian/Quirx-JavaFX.git
   cd Quirx-JavaFX
2. **Open the project in Eclipse**

3. **Install dependencies:**
- JavaFX
- JDBC driver for SQL Server
- Jakarta Mail libraries

4. **Database Setup:**
- Configure SQL Server and expose it using ngrok
- Update JDBC credentials in your source code

5. **Run the Application**

## 📄 License
This project was developed as part of the academic curriculum at Polytechnic University of the Philippines – Taguig Campus and is intended for educational purposes only.


