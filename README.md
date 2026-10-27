# FileGuard

FileGuard is an entry-level cybersecurity project written in Java. Its goal is to detect unexpected file changes by calculating and comparing SHA-256 hashes. A changed hash can indicate that a file was edited, replaced, or corrupted.

This project is being developed incrementally as a seven-day learning project using object-oriented programming (OOP).

## Project status

Days 1, 2, 3, and 4 are complete.

- SHA-256 file hashing is implemented.
- File paths are validated before hashing.
- Core OOP models represent users, monitored folders, file baselines, and security events.
- Enums define event types, severity levels, and review statuses.
- Automated JUnit tests cover hashing and model behavior.
- The JavaFX interface, database, monitoring, alerts, and reports are planned but are not operational yet.

## Current features

- Calculate the SHA-256 hash of a file.
- Process files in buffered blocks instead of loading an entire file into memory.
- Reject null, missing, non-regular, and unreadable files.
- Activate and deactivate folder monitoring.
- Store and validate trusted SHA-256 baseline information.
- Create baselines for individual files or complete folders.
- Detect files that were modified or deleted after baselining.
- Record file-created, file-modified, and file-deleted security events.
- Move security events through open, acknowledged, and resolved states.
- Verify behavior with automated tests.

## Planned features

- Select folders to monitor.
- Store trusted file hashes as a baseline.
- Detect created, modified, and deleted files.
- Record security events in an SQLite database.
- Display alerts in a JavaFX interface.
- Generate file-integrity reports.
- Authenticate users with BCrypt password hashing.

## Technologies

- Java 17
- Maven
- JavaFX 21
- JUnit 5
- SQLite
- BCrypt
- Git and GitHub

## Requirements

Install the following software before running the project:

- Java Development Kit (JDK) 17 or newer
- Apache Maven
- Git, if you want to use version control

Confirm that Java and Maven are available:

```powershell
java -version
mvn -version
```

## Project structure

The current Day 1, Day 2, and Day 3 files are:

```text
CyberCheck/
|-- pom.xml
|-- README.md
`-- src/
    |-- main/java/com/fileguard/
    |   |-- model/
    |   |   |-- EventStatus.java
    |   |   |-- EventType.java
    |   |   |-- FileBaseline.java
    |   |   |-- MonitoredFolder.java
    |   |   |-- SecurityEvent.java
    |   |   |-- Severity.java
    |   |   `-- User.java
    |   `-- service/
    |       |-- BaselineService.java
    |       `-- HashService.java
    `-- test/java/com/fileguard/
        |-- model/
        |   `-- ModelClassesTest.java
        |-- BaselineServiceTest.java
        `-- HashServiceTest.java
```

Additional packages and resources will be added as the project develops.

## Running the tests

Open PowerShell and move to the project directory:

```powershell
cd C:\Users\MADALA\Documents\CyberCheck
```

Run every test:

```powershell
mvn clean test
```

Run only the hash-service tests:

```powershell
mvn "-Dtest=HashServiceTest" test
```

Run only the model tests:

```powershell
mvn "-Dtest=ModelClassesTest" test
```

Run only the baseline-service tests:

```powershell
mvn "-Dtest=BaselineServiceTest" test
```

A successful test run should report:

```text
Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
```

The messages about missing `src/main/resources` and `src/test/resources` directories are harmless at this stage because those resources have not been added yet.

## How SHA-256 checking works

1. FileGuard reads the selected file.
2. It calculates a 64-character SHA-256 hash representing the file's contents.
3. A future scan calculates the hash again.
4. If the new hash differs from the stored baseline, FileGuard records a possible file change.

SHA-256 identifies changes; it does not determine whether a file is malicious.

## Seven-day development plan

| Day | Goal | Status |
| --- | --- | --- |
| 1 | Implement and test SHA-256 hashing | Complete |
| 2 | Create the core model classes | Complete |
| 3 | Build baseline creation and comparison | Complete |
| 4 | Monitor folders and detect file changes | Complete |
| 5 | Store folders, baselines, and events in SQLite | Complete |
| 6 | Build the JavaFX dashboard and alerts | Planned |
| 7 | Generate reports, test the application, and document it | Planned |

## OOP design

FileGuard separates responsibilities into classes and packages:

- `model`: application data such as users, monitored folders, baselines, and events
- `service`: hashing, monitoring, authentication, alerts, baselines, and reports
- `repository`: database access
- `controller`: JavaFX screen behavior
- `database`: database connection and initialization
- `util`: reusable helper functions

This separation keeps the code easier to understand, test, and extend.

## Security and privacy notes

- Do not commit real passwords, private keys, or sensitive files.
- Do not commit the generated `target/` directory.
- Do not commit the local `data/fileguard.db` database.
- Store passwords as BCrypt hashes, never as plain text.
- Only monitor folders you own or have permission to access.

## Educational purpose

FileGuard is an educational project and is not a replacement for antivirus, endpoint detection, backups, or professional security monitoring software.

## License

This project is intended for educational use. A formal license can be added in the `LICENSE` file.
