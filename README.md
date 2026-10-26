# Campus Lost & Found Manager

A Java Swing desktop application that stores lost and found items in a MySQL database using JDBC.
It supports full CRUD: add, view, search (by name or ID), update / mark as returned, and delete.

**Technology:** Java 21 (Temurin JDK) · Java Swing · JDBC (MySQL Connector/J 9.1.0) · MySQL 8.4

---

## Project structure

```
lostfound-java/
├── src/
│   ├── LostFoundApp.java    Swing GUI (frontend) + main method  ← run this
│   ├── ItemDAO.java         All SQL: INSERT, SELECT, UPDATE, DELETE (backend)
│   ├── DBConnection.java    Opens the JDBC connection (URL, user, password); main = connection test
│   └── Item.java            Model class: one object = one row of the items table
├── lib/
│   └── mysql-connector-j-9.1.0.jar   MySQL JDBC driver
├── database.sql             Creates the database, table and 5 sample rows
├── start-mysql.bat          Starts the MySQL server
├── .vscode/settings.json    Tells VS Code where the source and the driver jar are
├── README.md                This file
└── VIVA_GUIDE.md            Likely viva questions and answers
```

## How the parts talk to each other

```
LostFoundApp (Swing)  -->  ItemDAO (SQL via PreparedStatement)  -->  DBConnection  -->  MySQL: lostfound_db.items
       ^                                   |
       +---------- List<Item> / rows affected <----- ResultSet / update count
```

1. The user clicks a button in **LostFoundApp**. Its ActionListener validates the input and calls an **ItemDAO** method.
2. **ItemDAO** gets a `Connection` from **DBConnection**, fills the `?` placeholders of a `PreparedStatement`, and executes it.
3. MySQL runs the SQL. A SELECT returns a `ResultSet`, which ItemDAO turns into a `List<Item>`. INSERT, UPDATE and DELETE return the number of rows changed.
4. try-with-resources closes the connection. The GUI shows a message and refreshes the `JTable`.

See `VIVA_GUIDE.md` for a step-by-step walkthrough and Q&A.

---

## What is installed on this laptop (already done)

| Item | Location |
|---|---|
| JDK 21 (Eclipse Temurin) | `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot` (on PATH, `JAVA_HOME` set) |
| MySQL Server 8.4.9 | `C:\Program Files\MySQL\MySQL Server 8.4` |
| MySQL config + data | `%LOCALAPPDATA%\MySQL84\my.ini` and `%LOCALAPPDATA%\MySQL84\Data` |
| MySQL login | user `root`, password `root123` (set in `src/DBConnection.java`) |
| VS Code extensions | Extension Pack for Java (already installed) |

MySQL is **not** installed as a Windows service, because setting that up needs admin rights. That means it does **not**
start automatically when Windows starts. Start it with `start-mysql.bat` (step 1 below).

---

## Running it in VS Code (demo day)

**Close and reopen VS Code once** so it picks up the new JDK 21. Then:

### 1. Start MySQL
In File Explorer, double-click `start-mysql.bat`. Or, in the VS Code terminal (`Ctrl + ` `):
```
.\start-mysql.bat
```
A minimised window called **"MySQL Server"** appears on the taskbar. Leave it open: closing it stops MySQL.
Wait about 5 seconds.

### 2. Test the database connection
Open `src/DBConnection.java` and click **Run** above `main` (or press `F5`). Expected output:
```
Connected to MySQL successfully!
Database: lostfound_db
MySQL version: 8.4.9
```

### 3. Run the application
Open `src/LostFoundApp.java` and click **Run** above `main`. The window opens and shows the items from the database.

### 4. (Optional) Reset the data to the 5 sample rows
This deletes all current items and recreates the sample data:
```
Get-Content database.sql | & "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -proot123
```

### Running from the terminal instead of the Run button
```
javac -cp lib\mysql-connector-j-9.1.0.jar -d out src\*.java
java -cp "out;lib\mysql-connector-j-9.1.0.jar" LostFoundApp
```

### Showing the data directly in MySQL (good for the demo)
After adding, updating or deleting an item in the app, prove the database changed:
```
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -proot123 -e "SELECT * FROM lostfound_db.items;"
```

---

## Suggested demo sequence (5 minutes)

1. Run `DBConnection` → "Connected to MySQL successfully!" (**database connectivity**)
2. Run `LostFoundApp` → the table shows the 5 sample rows (**SELECT**)
3. Add "Red Umbrella / Other / Block A / Found" → it appears at the top (**INSERT**)
4. Click "Add Item" with an empty name → validation warning (**input validation**)
5. Search `wal`, then search `2` (**SELECT with WHERE / LIKE**)
6. Click the umbrella row, change the location, click Update. Then click "Mark as Returned" (**UPDATE**)
7. Select the umbrella → Delete → Yes (**DELETE**)
8. Run the `mysql -e "SELECT ..."` command above to show the table really changed in MySQL

---

## Troubleshooting

| Problem | Fix |
|---|---|
| "Communications link failure" | MySQL isn't running. Run `start-mysql.bat` and wait 5 seconds |
| "Access denied for user 'root'" | The password in `DBConnection.java` doesn't match MySQL (it should be `root123`) |
| "Unknown database 'lostfound_db'" | Run `database.sql` (step 4) |
| "No suitable driver found" | The jar isn't on the classpath. Check `lib/` contains the jar; in VS Code check "Referenced Libraries" in the Java Projects panel |
| `javac` not recognized / VS Code uses Java 8 | Close and reopen VS Code (or the terminal) so it picks up JDK 21 |
| `start-mysql.bat` window closes at once | Port 3306 may already be in use (MySQL already running), which is fine. Run the `DBConnection` test |

## Setting this up on a different PC (summary)
1. Install a JDK (`winget install EclipseAdoptium.Temurin.21.JDK`) and MySQL Server (`winget install Oracle.MySQL`), then configure MySQL with MySQL Configurator, choosing a root password.
2. Put that password in `src/DBConnection.java`.
3. Run `database.sql` in MySQL, then run `LostFoundApp`.
