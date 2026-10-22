# Secure Banking Application

A secure, console-based Java banking application designed for university study, demonstrating object-oriented programming, authentication, account management, transactions, file storage, and defensive coding practices.

Student name: [Christwish]
Registration number: [H250574B]

## Project Objective

This project aims to implement a simple but realistic banking workflow in Java 17+ using only standard library components. It focuses on secure authentication, account ownership rules, transaction validation, and file-based persistence in a format that is easy for students to read and explain.

## Features Implemented

- User registration and login
- PBKDF2WithHmacSHA256 password hashing with unique salt per user
- Duplicate username prevention
- Secure account creation tied to authenticated users
- Deposit and withdrawal operations using BigDecimal
- Transaction logging with balance snapshots
- File-based persistence for users, accounts, and transactions
- Input validation for usernames, passwords, amounts, and menu choices
- Protection against unauthorized account access
- Error handling for file and input problems
- Console dashboard workflow

## Technologies Used

- Java 17+
- VS Code
- Standard Java libraries only
- UTF-8 text-file storage
- Git and GitHub for version control

## Project Folder Structure

```text
SecureBankApp/
├── .gitignore
├── README.md
├── data/
│   ├── accounts.txt
│   ├── transactions.txt
│   └── users.txt
├── src/
│   ├── Main.java
│   ├── securebankapp/
│   │   ├── models/
│   │   │   ├── BankAccount.java
│   │   │   ├── Transaction.java
│   │   │   └── User.java
│   │   ├── security/
│   │   │   └── PasswordSecurity.java
│   │   ├── services/
│   │   │   ├── AuthenticationService.java
│   │   │   └── BankingService.java
│   │   ├── storage/
│   │   │   └── FileStorageService.java
│   │   ├── tests/
│   │   │   └── SecureBankAppTests.java
│   │   └── utils/
│   │       └── InputValidator.java
│   └── ...
├── frontend/
│   ├── index.html
│   ├── login.html
│   ├── register.html
│   ├── dashboard.html
│   ├── accounts.html
│   ├── transactions.html
│   ├── styles/
│   │   ├── style.css
│   │   └── auth.css
│   └── scripts/
│       ├── app.js
│       ├── auth.js
│       └── dashboard.js
├── out/
└── .git/
```

## Frontend Interface

A separate static frontend layer was added under the `frontend/` folder to provide a modern banking dashboard experience without modifying the Java backend. This keeps the existing console-based Java project intact while giving the project a professional UI shell.

### Current frontend status

- The frontend is fully implemented as static HTML, CSS, and JavaScript.
- The UI is responsive and styled with a dark navy, white, and blue design system.
- Login, registration, dashboard, accounts, and transaction pages are included.
- The app includes form validation, password visibility toggles, loading states, modal dialogs, filters, and responsive navigation.
- The frontend currently uses demonstration data because the Java project is still a console application and does not yet expose an HTTP API.

### Important backend integration note

HTML and JavaScript running in the browser cannot directly call Java methods or read `users.txt`, `accounts.txt`, or `transactions.txt` files from the local file system. A real browser-based banking system requires a backend API layer, such as a lightweight Java HTTP server or framework, to enforce authentication, ownership checks, and financial validation on the server.

## OOP Principles Demonstrated

- Encapsulation: private fields with public access methods
- Abstraction: service classes separate business logic from persistence
- Separation of concerns: models, services, security, storage, utilities, and UI
- Polymorphism: method behavior is reused across cases where appropriate
- Meaningful class responsibilities: each class has one main purpose
- No unnecessary inheritance used only to satisfy a concept

## Security Measures Implemented

- Passwords are never stored in plaintext
- PBKDF2WithHmacSHA256 hashing with a unique random salt per user
- SecureRandom used for cryptographic-quality salt generation
- BigDecimal protects monetary calculations from floating-point errors
- Ownership checks enforce that a user can only access their own bank accounts
- Input validation blocks malformed usernames, passwords, and numeric values
- Temporary files are used before overwriting storage files to reduce corruption risk
- Sensitive information is not printed to the console
- Hardcoded admin passwords are not used

## Instructions for Compiling and Running in VS Code

### Java backend

1. Open the project folder in VS Code.
2. Open a terminal in the project root.
3. Compile the project:

```powershell
$files = Get-ChildItem -Path src -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
javac -d out $files
```

4. Run the application:

```powershell
java -cp out Main
```

5. Run the test suite:

```powershell
java -cp out securebankapp.tests.SecureBankAppTests
```

### Frontend preview

1. Open the `frontend/` folder in VS Code.
2. Open any HTML file, such as `frontend/login.html`.
3. Use a simple local static server or VS Code Live Server to preview it.

Example with Python:

```powershell
cd "frontend"
python -m http.server 8000
```

Then open:

```text
http://localhost:8000/login.html
```

This preview is for the interface layer only. It does not yet connect to the Java backend because no API layer exists yet.


1. Open the project folder in VS Code.
2. Open a terminal in the project root.
3. Compile the project:

```powershell
$files = Get-ChildItem -Path src -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
javac -d out $files
```

4. Run the application:

```powershell
java -cp out Main
```

5. Run the test suite:

```powershell
java -cp out securebankapp.tests.SecureBankAppTests
```

## Text-File Storage Format

The project uses UTF-8 text files with a pipe-delimited format to keep the data easy to inspect and maintain.

### User records

```text
username|passwordHash|salt|iterations
```

- username: unique account name chosen by the user
- passwordHash: PBKDF2 output in hexadecimal form
- salt: Base64-encoded random salt
- iterations: PBKDF2 iteration count

### Account records

```text
accountNumber|username|balance|currency|createdAt
```

### Transaction records

```text
transactionId|accountNumber|transactionType|amount|timestamp|balanceAfterTransaction
```

The application validates records before accepting them and ignores malformed entries rather than corrupting the whole file.

## Example Application Workflow

1. Launch the application.
2. Choose Register.
3. Enter a username and password.
4. Log in to the dashboard.
5. Create a bank account.
6. Deposit or withdraw funds.
7. Check your balance and transaction history.
8. Log out to return to the main menu.

## Testing Instructions

The project includes a test class with unit-style checks covering:

- Successful registration
- Duplicate username rejection
- Successful authentication
- Incorrect password rejection
- Unique salts
- Account ownership enforcement
- Successful deposit
- Successful withdrawal
- Negative deposits and withdrawals rejected
- Insufficient funds rejection
- Balance preservation after failed transactions
- Transaction history persistence
- Data reload after application restart
- Malformed file record handling

To run the tests:

```powershell
cd "SecureBankApp"
$files = Get-ChildItem -Path src -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
javac -d out $files
java -cp out securebankapp.tests.SecureBankAppTests
```

## Known Limitations

- Text files are not suitable for production-grade concurrent banking workloads.
- All data is stored in plain text files with a simple delimiter format, which is easier to read but less secure than a database.
- The application is console-based and designed for educational use, not for real-world financial deployment.
- File locking and multi-user concurrency controls are limited compared with real banking systems.

## Future Improvements

- Add encrypted file storage or database-backed persistence
- Add transaction categories and audit trails
- Add role-based access or admin features
- Improve concurrency safety and file locking
- Add a richer CLI or GUI layer
- Introduce stronger validation and endpoint-level authorization if extended

## Security Note

This application demonstrates secure coding practices for a university assignment. It is not production-ready and should not be used for real financial operations without more extensive security review, compliance evaluation, and operational controls.
