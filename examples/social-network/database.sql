-- ====================================
-- ARTHA SOCIAL NETWORK DATABASE SCHEMA
-- MySQL Workbench
-- ====================================

-- 1. CREATE DATABASE
DROP DATABASE IF EXISTS artha_social;
CREATE DATABASE artha_social CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE artha_social;

-- 2. USERS TABLE
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    bio TEXT,
    profile_pic VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. POSTS TABLE
CREATE TABLE posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    content TEXT NOT NULL,
    image_url VARCHAR(255),
    likes_count INT DEFAULT 0,
    comments_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. LIKES TABLE
CREATE TABLE likes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    post_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_post (user_id, post_id),
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. FOLLOWS TABLE
CREATE TABLE follows (
    id INT AUTO_INCREMENT PRIMARY KEY,
    follower_id INT NOT NULL,
    following_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_follow (follower_id, following_id),
    INDEX idx_follower (follower_id),
    INDEX idx_following (following_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. COMMENTS TABLE
CREATE TABLE comments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    post_id INT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. INSERT SAMPLE DATA FOR TESTING
INSERT INTO users (username, email, password, full_name, bio) VALUES
('john_doe', 'john@example.com', '$2a$10$abcdefghijklmnopqrstuv', 'John Doe', 'Software developer and coffee enthusiast'),
('jane_smith', 'jane@example.com', '$2a$10$abcdefghijklmnopqrstuv', 'Jane Smith', 'Designer | Traveler | Photographer'),
('mike_wilson', 'mike@example.com', '$2a$10$abcdefghijklmnopqrstuv', 'Mike Wilson', 'Tech blogger'),
('sarah_jones', 'sarah@example.com', '$2a$10$abcdefghijklmnopqrstuv', 'Sarah Jones', 'Fitness coach');

INSERT INTO posts (user_id, content) VALUES
(1, 'Just deployed my first microservice! 🚀'),
(2, 'Beautiful sunset at the beach today 🌅'),
(1, 'Learning ARTHA framework - it''s amazing!'),
(3, 'New blog post: "10 Tips for Better Code"'),
(4, 'Morning workout done! 💪'),
(2, 'Coffee and code - perfect Sunday!');

INSERT INTO follows (follower_id, following_id) VALUES
(1, 2), (1, 3), (2, 1), (2, 4), (3, 1), (3, 2), (4, 2);

INSERT INTO likes (user_id, post_id) VALUES
(2, 1), (3, 1), (1, 2), (4, 2), (2, 3), (1, 4), (2, 5);

INSERT INTO comments (user_id, post_id, content) VALUES
(2, 1, 'Congratulations! 🎉'),
(3, 1, 'Well done!'),
(1, 2, 'Stunning photo!'),
(2, 4, 'Great tips, thanks for sharing');

-- 8. USEFUL QUERIES FOR TESTING

-- Get user feed (posts from people they follow)
SELECT p.*, u.username, u.full_name, u.profile_pic
FROM posts p
JOIN users u ON p.user_id = u.id
WHERE p.user_id IN (
    SELECT following_id FROM follows WHERE follower_id = 1
)
ORDER BY p.created_at DESC
LIMIT 20;

-- Get post with likes and comments count
SELECT 
    p.*,
    u.username,
    u.full_name,
    COUNT(DISTINCT l.id) as likes_count,
    COUNT(DISTINCT c.id) as comments_count
FROM posts p
JOIN users u ON p.user_id = u.id
LEFT JOIN likes l ON p.id = l.post_id
LEFT JOIN comments c ON p.id = c.post_id
WHERE p.id = 1
GROUP BY p.id;

-- Get user profile with stats
SELECT 
    u.*,
    (SELECT COUNT(*) FROM posts WHERE user_id = u.id) as posts_count,
    (SELECT COUNT(*) FROM follows WHERE following_id = u.id) as followers_count,
    (SELECT COUNT(*) FROM follows WHERE follower_id = u.id) as following_count
FROM users u
WHERE u.id = 1;

-- Check if user likes a post
SELECT EXISTS(
    SELECT 1 FROM likes 
    WHERE user_id = 1 AND post_id = 1
) as is_liked;

-- Get comments for a post
SELECT c.*, u.username, u.full_name, u.profile_pic
FROM comments c
JOIN users u ON c.user_id = u.id
WHERE c.post_id = 1
ORDER BY c.created_at ASC;

-- ====================================
-- DONE! Run these queries in MySQL Workbench
-- ====================================
