@echo off
REM Starts the MySQL server in its own minimised window (no admin rights needed).
REM Run this once before the demo. Closing the "MySQL Server" window stops MySQL.
start "MySQL Server" /MIN "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe" --defaults-file="%LOCALAPPDATA%\MySQL84\my.ini" --console
echo MySQL is starting on port 3306...
