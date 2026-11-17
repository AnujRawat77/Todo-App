# Basic Java Todo Application

A very simple console-based Todo application written in plain Java.

## Features
- Add a todo
- View all todos
- Mark a todo as completed
- Delete a todo
- Exit the application

Todos are stored in memory, so they are cleared when the application exits.

## Requirements
- JDK 8 or later

## Project structure

```text
BasicJavaTodoApp/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── todo/
│                   ├── Main.java
│                   └── Todo.java
└── README.md
```

## How to run

Open a terminal in the project folder and compile:

```bash
javac -d out src/main/java/com/todo/*.java
```

Run the application:

```bash
java -cp out com.todo.Main
```

No Spring Boot, database, API, or external dependency is required.
