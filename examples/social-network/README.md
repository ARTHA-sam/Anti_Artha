# ARTHA Social Network - **WORKING VERSION!** ✅

## It's Running! 🚀

The server is live at **http://localhost:8080**

## What Works

**Backend:**
- ✅ User Registration & Login
- ✅ Create Posts
- ✅ Like Posts  
- ✅ Feed (posts from followed users)
- ✅ Authentication Middleware (@Before)
- ✅ Config Injection (@ConfigValue)
- ✅ Scheduled Cleanup (@Scheduled)
- ✅ Exception Handling (@ExceptionHandler)

## Test the API

### 1. Register User
```bash
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d "{\"username\":\"john\",\"email\":\"john@test.com\",\"password\":\"pass123\",\"fullName\":\"John Doe\"}"
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"john\",\"password\":\"pass123\"}"
```

**Copy the token from response!**

### 3. Create Post
```bash
curl -X POST http://localhost:8080/api/posts -H "Authorization: Bearer user_1_token" -H "Content-Type: application/json" -d "{\"content\":\"Hello from ARTHA!\"}"
```

### 4. Get Feed
```bash
curl http://localhost:8080/api/posts/feed -H "Authorization: Bearer user_1_token"
```

### 5. Like Post
```bash
curl -X POST http://localhost:8080/api/posts/1/like -H "Authorization: Bearer user_1_token"
```

## Frontend

Open `frontend/index.html` in your browser and test:
1. Register account
2. Login
3. Create posts
4. Like posts
5. View feed

## Files Created

| File | Purpose |
|------|---------|
| `User.java` | User model |
| `Post.java` | Post model |
| `AuthController.java` | Register/Login endpoints |
| `PostController.java` | Posts, Feed, Like endpoints |
| `AuthMiddleware.java` | Authentication check |
| `CleanupTask.java` | Scheduled background tasks |

## All ARTHA Features Used

- `@Step` - Routing
- `@Inject` - Dependency Injection (Database)
- `@ConfigValue` - Config from artha.json
- `@Before` - Middleware
- `@ExceptionHandler` - Error handling
- `@Scheduled` - Background tasks
- QueryBuilder & execute() - Database operations

**Everything works perfectly!** 🎉
