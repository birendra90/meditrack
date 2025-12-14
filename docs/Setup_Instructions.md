# MediTrack Java Project Setup Instructions

## Prerequisites
- Operating System: macOS, Windows, or Linux
- Internet connection for downloading Java

## Step 1: Install Java Development Kit (JDK)

### For macOS:
1. **Download JDK:**
   - Visit [Oracle JDK Downloads](https://www.oracle.com/java/technologies/downloads/)
   - Download JDK 17 or later (LTS version recommended)
   - Alternatively, use Homebrew: `brew install openjdk@17`

2. **Install JDK:**
   - Open the downloaded `.dmg` file
   - Follow the installation wizard
   - Accept license agreements

3. **Verify Installation:**
   ```bash
   java -version
   javac -version
   ```
   
   Expected output should show Java version 17 or later.

### For Windows:
1. **Download JDK:**
   - Visit [Oracle JDK Downloads](https://www.oracle.com/java/technologies/downloads/)
   - Download Windows x64 Installer

2. **Install JDK:**
   - Run the downloaded `.exe` file
   - Follow installation wizard
   - Note the installation path (usually `C:\Program Files\Java\jdk-17`)

3. **Set Environment Variables:**
   - Right-click "This PC" → Properties → Advanced System Settings
   - Click "Environment Variables"
   - Add `JAVA_HOME` pointing to JDK installation directory
   - Add `%JAVA_HOME%\bin` to PATH variable

4. **Verify Installation:**
   ```cmd
   java -version
   javac -version
   ```

### For Linux (Ubuntu/Debian):
1. **Install JDK:**
   ```bash
   sudo apt update
   sudo apt install openjdk-17-jdk
   ```

2. **Set JAVA_HOME:**
   ```bash
   echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
   source ~/.bashrc
   ```

3. **Verify Installation:**
   ```bash
   java -version
   javac -version
   ```

## Step 2: IDE Setup (Optional but Recommended)

### Visual Studio Code:
1. Install VS Code from [https://code.visualstudio.com/](https://code.visualstudio.com/)
2. Install Java Extension Pack:
   - Open VS Code
   - Go to Extensions (Ctrl+Shift+X)
   - Search for "Extension Pack for Java"
   - Install it

### IntelliJ IDEA:
1. Download from [https://www.jetbrains.com/idea/](https://www.jetbrains.com/idea/)
2. Install Community Edition (free) or Ultimate Edition

## Step 3: Project Compilation and Execution

### Compile the project:
```bash
# Navigate to project root directory
cd /path/to/meditrack

# Compile all Java files
javac -cp . com/airtribe/meditrack/**/*.java

# Or compile specific main class
javac -cp . com/airtribe/meditrack/Main.java
```

### Run the project:
```bash
# Run the main application
java -cp . com.airtribe.meditrack.Main

# Run with command line arguments (for loading data)
java -cp . com.airtribe.meditrack.Main --loadData
```

### Generate JavaDoc:
```bash
javadoc -d docs/javadoc -cp . com.airtribe.meditrack/**/*.java
```

## Step 4: Project Structure Verification

Your project should have the following structure:
```
meditrack/
├── docs/
│   ├── Setup_Instructions.md
│   ├── JVM_Report.md
│   └── javadoc/
├── main/java/com/
│   └── airtribe/
│       └── meditrack/
│           ├── entity/
│           ├── service/
│           ├── util/
│           ├── exception/
│           ├── interface/
│           ├── constants/
│           ├── test/
│           └── Main.java
├── data/
│   ├── patients.csv
│   ├── doctors.csv
│   └── appointments.csv
└── README.md
```

## Troubleshooting

### Common Issues:

1. **"java: command not found"**
   - Ensure JAVA_HOME is set correctly
   - Verify PATH includes Java bin directory
   - Restart terminal/command prompt

2. **Compilation Errors**
   - Check Java version compatibility
   - Ensure all dependencies are in classpath
   - Verify file encoding (use UTF-8)

3. **ClassNotFoundException**
   - Verify classpath includes current directory (.)
   - Check package declarations match directory structure

## Additional Tools

### Recommended Utilities:
- **Git**: For version control
- **Maven/Gradle**: For dependency management (advanced)
- **JVisualVM**: For performance monitoring

### Installation verification screenshots should be taken showing:
1. Java version output in terminal
2. Successful compilation output
3. Application running with main menu
4. IDE setup (if applicable)

## Next Steps
1. Verify all components are working
2. Run the test suite: `java -cp . com.airtribe.meditrack.test.TestRunner`
3. Explore the application features through the console menu
4. Review generated JavaDoc documentation