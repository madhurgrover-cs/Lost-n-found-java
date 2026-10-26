# Viva Guide: Campus Lost & Found Manager

Short, accurate answers you can say in your own words. Read the "Architecture" section first.

---

## 1. Architecture: how the project fits together

```
 FRONTEND (Swing)            BACKEND (Java + JDBC)                 DATABASE (MySQL)
 LostFoundApp.java   --->    ItemDAO.java  --->  DBConnection.java  --->  lostfound_db.items
 (buttons, form, table)     (SQL with PreparedStatement)  (opens connection)
        ^                          |
        |    List<Item> / row count|
        +--------------------------+  <---  ResultSet / rows affected  <---
```

**Example flow: clicking "Add Item"**
1. `LostFoundApp.addItem()` runs because the button's ActionListener calls it.
2. `validateForm()` checks that Name and Location are filled and not too long.
3. It calls `dao.addItem(name, category, location, description, status)`.
4. `ItemDAO.addItem()` gets a connection from `DBConnection.getConnection()`.
5. It prepares `INSERT INTO items (...) VALUES (?, ?, ?, ?, ?)`, fills the `?` with `setString`, and calls `executeUpdate()`.
6. MySQL inserts the row and generates `item_id` automatically (AUTO_INCREMENT).
7. try-with-resources closes the statement and connection.
8. Back in the GUI: a success message appears and `loadAllItems()` runs a SELECT to refresh the table.

**Example flow: "Show All" (SELECT)**
GUI → `dao.getAllItems()` → `executeQuery()` returns a `ResultSet` → `readItems()` loops with `rs.next()` and creates one `Item` per row → `List<Item>` goes back to the GUI → `showItems()` adds each Item as a row in the `JTable`.

**Purpose of each file**

| File | Purpose |
|---|---|
| `database.sql` | Creates the database `lostfound_db`, the `items` table, and 5 sample rows |
| `DBConnection.java` | Stores URL/user/password and returns a `Connection`; its `main` tests the connection |
| `Item.java` | Model class: one object = one row of the table |
| `ItemDAO.java` | All SQL / CRUD methods (Data Access Object) |
| `LostFoundApp.java` | Swing GUI + `main` method (program entry point) |
| `lib/mysql-connector-j-9.1.0.jar` | The MySQL JDBC driver |
| `start-mysql.bat` | Starts the MySQL server (not part of the Java code) |

---

## 2. JDBC

**Q: What is JDBC?**
Java Database Connectivity. A standard Java API (in the `java.sql` package) that lets a Java program connect to a database, send SQL and read results.

**Q: What is a JDBC driver? Which one do you use?**
A library that implements JDBC for one particular database. I use MySQL Connector/J (`mysql-connector-j-9.1.0.jar`), which is in the `lib` folder and on the classpath.

**Q: What are the steps of JDBC?**
1. Load the driver (automatic since JDBC 4.0, so `Class.forName` is optional).
2. Open a connection: `DriverManager.getConnection(url, user, password)`.
3. Create a statement: `con.prepareStatement(sql)`.
4. Execute: `executeQuery()` for SELECT, `executeUpdate()` for INSERT/UPDATE/DELETE.
5. Process the `ResultSet` (for SELECT).
6. Close resources (I use try-with-resources).

**Q: Explain your connection URL.**
`jdbc:mysql://localhost:3306/lostfound_db` → protocol `jdbc`, sub-protocol `mysql`, server `localhost` (this computer), port `3306` (MySQL default), database `lostfound_db`.

**Q: Why don't you call `Class.forName("com.mysql.cj.jdbc.Driver")`?**
Since JDBC 4.0, drivers register themselves automatically when their jar is on the classpath (via the `META-INF/services` file in the jar). It still works if you add it.

**Q: What is `DriverManager`?**
A JDBC class that picks the right driver for a URL and opens the connection.

**Q: Difference between `executeQuery()` and `executeUpdate()`?**
`executeQuery()` is for SELECT and returns a `ResultSet`. `executeUpdate()` is for INSERT, UPDATE and DELETE and returns an `int`: the number of rows affected.

**Q: What is a `ResultSet`?**
A table of results from a SELECT. It has a cursor that starts *before* the first row; `rs.next()` moves to the next row and returns `false` when there are no more. Values are read with `rs.getInt("item_id")`, `rs.getString("item_name")`, etc.

**Q: What happens if MySQL is not running?**
`getConnection()` throws an `SQLException` ("Communications link failure"). My code catches it and shows a "Database error" dialog instead of crashing.

**Q: What is `SQLException`?**
A checked exception thrown when a database operation fails (wrong password, server down, bad SQL, constraint violation). The DAO methods declare `throws SQLException`, and the GUI catches it and shows the message.

**Q: What is try-with-resources and why use it?**
`try (Connection con = ...; PreparedStatement ps = ...) { }` automatically closes the resources at the end, even if an exception happens. This prevents connection leaks. It works because these classes implement `AutoCloseable`.

**Q: Why open a new connection for each operation?**
It's simple and safe for a small desktop app: each operation opens, uses and closes its own connection. Large applications use a *connection pool* to reuse connections for better performance.

---

## 3. PreparedStatement

**Q: What is a PreparedStatement?**
A precompiled SQL statement with `?` placeholders. Values are supplied separately with `setString(index, value)`, `setInt(...)`. Indexes start at 1.

**Q: Why PreparedStatement instead of Statement?**
1. **Prevents SQL injection**: user input is always treated as data, never as part of the SQL command.
2. Handles quotes automatically (e.g. a name like `O'Brien` doesn't break the query).
3. Can be faster when executed repeatedly, because the database can reuse the compiled query.

**Q: What is SQL injection? Give an example.**
An attack where user input changes the meaning of a query. If I built SQL by joining strings:
`"DELETE FROM items WHERE item_name = '" + input + "'"` and the user typed `x' OR '1'='1`, the query would become `... WHERE item_name = 'x' OR '1'='1'`, which is true for every row, so the whole table would be deleted. With `?` placeholders, that input is just compared as a plain string.

**Q: How does your search work?**
`SELECT * FROM items WHERE item_name LIKE ? OR item_id = ?`. The first `?` gets `"%" + keyword + "%"` (partial name match). If the keyword is a number, the second `?` gets that number, otherwise `-1`, which matches no row. So "wal" finds "Black Wallet" and "3" finds item 3 (and any names containing "3").

---

## 4. SQL and the database

**Q: What is SQL?**
Structured Query Language: the standard language to create, read, update and delete data in relational databases.

**Q: DDL vs DML?**
DDL (Data Definition Language) defines structure: `CREATE`, `DROP`, `ALTER`. DML (Data Manipulation Language) works with data: `INSERT`, `SELECT`, `UPDATE`, `DELETE`.

**Q: Explain your table.**
`items(item_id, item_name, category, location, description, status, reported_on)`.
- `item_id INT AUTO_INCREMENT PRIMARY KEY`: unique ID that MySQL generates.
- `NOT NULL` on name, category, location, status: these are required.
- `VARCHAR(n)`: text up to n characters.
- `reported_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP`: filled in automatically when the row is inserted.
- `CHECK (status IN ('Lost','Found','Returned'))`: the database rejects any other status.

**Q: What is a primary key?**
A column that uniquely identifies each row. It cannot be NULL or duplicated. Mine is `item_id`.

**Q: What does AUTO_INCREMENT do?**
MySQL automatically gives each new row the next number, so the user never types the ID. That's why the ID field in the form is read-only.

**Q: Why does the UPDATE/DELETE use `WHERE item_id = ?`?**
To affect only that one item. Without a WHERE clause, UPDATE or DELETE would change every row in the table.

**Q: What does `LIKE '%wal%'` mean?**
`%` matches any number of characters, so it finds any name containing "wal".

**Q: Why validate in both Java and the database?**
Java validation gives the user a friendly message before anything is sent. Database constraints (`NOT NULL`, `CHECK`, `VARCHAR` length) are a final safety net that protects the data even if another program uses the database.

**Q: What is a database vs a table?**
A database (`lostfound_db`) is a container for related tables. A table (`items`) stores rows (records) and columns (fields).

---

## 5. CRUD

**Q: What is CRUD? Where is each in your project?**

| Operation | SQL | ItemDAO method | GUI button |
|---|---|---|---|
| Create | `INSERT` | `addItem` | Add Item |
| Read | `SELECT` | `getAllItems`, `searchItems` | Show All, Search (and on start-up) |
| Update | `UPDATE` | `updateItem`, `markReturned` | Update Item, Mark as Returned |
| Delete | `DELETE` | `deleteItem` | Delete Item |

**Q: How do you know an update/delete worked?**
`executeUpdate()` returns the number of rows affected. If it is 0, no item had that ID, so I show "No item found". If it's 1, I show success.

**Q: What is "mark as returned"?**
A specific UPDATE: `UPDATE items SET status = 'Returned' WHERE item_id = ?`.

---

## 6. Java Swing (frontend)

**Q: What is Swing?**
Java's built-in library (`javax.swing`) for desktop GUIs: windows, buttons, text fields, tables.

**Q: Which components do you use?**
`JFrame` (window), `JPanel` (container), `JLabel`, `JTextField`, `JComboBox` (drop-down for category/status), `JButton`, `JTable` inside a `JScrollPane`, and `JOptionPane` for messages and the delete confirmation.

**Q: How does a button do something?**
Event handling. I register an `ActionListener` using a lambda: `addButton.addActionListener(e -> addItem());`. When the button is clicked, Swing calls `addItem()`.

**Q: What is `DefaultTableModel`?**
The object that holds the table's data. The `JTable` only displays it. I clear it with `setRowCount(0)` and add rows with `addRow(...)`. I overrode `isCellEditable` to return `false` so users edit through the form, not the table.

**Q: How does clicking a row fill the form?**
A `ListSelectionListener` on the table runs `fillFormFromSelectedRow()`, which reads the values with `tableModel.getValueAt(row, column)` and puts them into the fields.

**Q: What layouts do you use?**
`BorderLayout` for the main window (search bar NORTH, form WEST, table CENTER), `GridLayout` for the form and buttons, `FlowLayout` for the search bar.

**Q: Why `SwingUtilities.invokeLater` in main?**
Swing is not thread-safe. All GUI work should happen on the Event Dispatch Thread, and `invokeLater` runs the window creation on that thread.

---

## 7. Design / general questions

**Q: Why separate the code into these classes?**
Separation of concerns: the GUI doesn't contain SQL, and the SQL doesn't contain GUI code. If the database changes, I only change `ItemDAO`/`DBConnection`. If the screen changes, I only change `LostFoundApp`.

**Q: What is a DAO?**
Data Access Object: a class that hides all database access behind simple methods like `addItem()` and `deleteItem()`.

**Q: What is the `Item` class for?**
It's a model (POJO) that carries one row of data between the DAO and the GUI, instead of passing seven separate values around.

**Q: Is storing the password in the code safe?**
It's acceptable for a local university demo. In a real application I'd keep it in a configuration file or environment variable and use a MySQL user with limited permissions instead of `root`.

**Q: What would you improve?**
Connection pooling, a login system, a date picker for the date lost/found, a separate MySQL user instead of root, storing the password outside the code, and running database calls in a background thread (`SwingWorker`) so the window never freezes.

**Q: How do you compile and run it without an IDE?**
```
javac -cp lib\mysql-connector-j-9.1.0.jar -d out src\*.java
java -cp "out;lib\mysql-connector-j-9.1.0.jar" LostFoundApp
```
`-cp` (classpath) tells Java where to find classes and the driver; `-d out` puts the compiled `.class` files in `out`. On Windows the classpath separator is `;`.
