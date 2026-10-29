# FileGuard User Manual

## 1. Overview

FileGuard is a Java cybersecurity application that monitors files for unauthorized changes.

It uses SHA-256 hashing to create a baseline for files in a selected folder. Later scans compare the current hashes with the baseline and detect:

- Created files
- Modified files
- Deleted files

FileGuard also displays security alerts and generates text reports.

## 2. Requirements

Install the following software before running FileGuard:

- Java Development Kit 17 or newer
- Apache Maven
- IntelliJ IDEA or another Java IDE
- Windows, Linux, or macOS

Confirm Java and Maven are available:

```powershell
java -version
mvn -version