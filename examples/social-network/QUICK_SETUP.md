# Quick MySQL Setup Guide

## Step 1: Open MySQL Workbench

1. Open **MySQL Workbench**
2. Connect to your MySQL server:
   - Username: `root`
   - Password: `Sam@2006`

## Step 2: Run the SQL Script

1. Open `setup-mysql.sql` file
2. Click **Execute** (⚡ icon) or press `Ctrl+Shift+Enter`
3. Wait for "Database created successfully!" message

## Step 3: Verify

Run this query to check:
```sql
USE artha_social;
SHOW TABLES;
```

You should see:
- users
- posts
- likes  
- follows

## Step 4: Restart Server

```bash
cd c:\Users\samar\Downloads\artha\examples\social-network
artha dev
```

**That's it!** The database will connect! 🎉

## Troubleshooting

**If you see "Access denied":**
- Check password in `artha.json` matches MySQL password
- Make sure MySQL server is running

**If you see "Database not found":**
- Run the setup-mysql.sql script again

**If port 8080 is in use:**
```bash
# Kill process using port 8080
Get-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess | Stop-Process -Force
```
