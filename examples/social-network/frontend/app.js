const API_URL = 'http://localhost:8080/api';
let currentUser = null;
let token = localStorage.getItem('token');

// Check if logged in
if (token) {
    loadFeed();
}

//=============== AUTH FUNCTIONS ===============

function showLogin() {
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    event.target.classList.add('active');
    document.getElementById('login-form').style.display = 'flex';
    document.getElementById('register-form').style.display = 'none';
}

function showRegister() {
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    event.target.classList.add('active');
    document.getElementById('login-form').style.display = 'none';
    document.getElementById('register-form').style.display = 'flex';
}

document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;

    try {
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (response.ok) {
            token = data.token;
            currentUser = data.user;
            localStorage.setItem('token', token);
            loadFeed();
        } else {
            document.getElementById('auth-error').textContent = data.error || 'Login failed';
        }
    } catch (error) {
        document.getElementById('auth-error').textContent = 'Connection error';
    }
});

document.getElementById('register-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('reg-username').value;
    const email = document.getElementById('reg-email').value;
    const fullName = document.getElementById('reg-fullname').value;
    const password = document.getElementById('reg-password').value;

    try {
        const response = await fetch(`${API_URL}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, fullName, password })
        });

        const data = await response.json();

        if (response.ok) {
            token = data.token;
            currentUser = data.user;
            localStorage.setItem('token', token);
            loadFeed();
        } else {
            document.getElementById('auth-error').textContent = data.error || 'Registration failed';
        }
    } catch (error) {
        document.getElementById('auth-error').textContent = 'Connection error';
    }
});

function logout() {
    localStorage.removeItem('token');
    token = null;
    currentUser = null;
    document.getElementById('auth-page').classList.add('active');
    document.getElementById('feed-page').classList.remove('active');
}

//=============== FEED FUNCTIONS ===============

async function loadFeed() {
    document.getElementById('auth-page').classList.remove('active');
    document.getElementById('feed-page').classList.add('active');

    try {
        const response = await fetch(`${API_URL}/users/me/feed?limit=20`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        const data = await response.json();

        if (response.ok) {
            displayPosts(data.posts);
            loadCurrentUser();
        }
    } catch (error) {
        console.error('Failed to load feed:', error);
    }
}

async function loadCurrentUser() {
    try {
        const response = await fetch(`${API_URL}/users/me`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        const data = await response.json();

        if (response.ok) {
            currentUser = data.user;
            document.getElementById('current-user').textContent = `@${currentUser.username}`;
        }
    } catch (error) {
        console.error('Failed to load user:', error);
    }
}

function displayPosts(posts) {
    const container = document.getElementById('feed-container');
    container.innerHTML = '';

    posts.forEach(post => {
        const postCard = createPostCard(post);
        container.appendChild(postCard);
    });
}

function createPostCard(post) {
    const card = document.createElement('div');
    card.className = 'post-card';
    card.innerHTML = `
        <div class="post-header">
            <div>
                <div class="post-author">@${post.username}</div>
                <div class="post-date">${new Date(post.createdAt).toLocaleString()}</div>
            </div>
        </div>
        <div class="post-content">${post.content}</div>
        <div class="post-actions">
            <button class="action-btn ${post.likedByCurrentUser ? 'liked' : ''}" onclick="toggleLike(${post.id}, this)">
                ❤️ ${post.likesCount} Likes
            </button>
            <button class="action-btn" onclick="showComments(${post.id})">
                💬 ${post.commentsCount} Comments
            </button>
        </div>
        <div class="comment-section" id="comments-${post.id}" style="display: none;">
            <div id="comments-list-${post.id}"></div>
            <div class="comment-input-group">
                <input type="text" id="comment-input-${post.id}" placeholder="Add a comment...">
                <button class="btn-secondary" onclick="addComment(${post.id})">Post</button>
            </div>
        </div>
    `;
    return card;
}

//=============== POST FUNCTIONS ===============

async function createPost() {
    const content = document.getElementById('new-post-content').value.trim();

    if (!content) return;

    try {
        const response = await fetch(`${API_URL}/posts`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ content })
        });

        if (response.ok) {
            document.getElementById('new-post-content').value = '';
            loadFeed();
        }
    } catch (error) {
        console.error('Failed to create post:', error);
    }
}

async function toggleLike(postId, button) {
    const isLiked = button.classList.contains('liked');
    const method = isLiked ? 'DELETE' : 'POST';

    try {
        const response = await fetch(`${API_URL}/social/posts/${postId}/like`, {
            method,
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            loadFeed();
        }
    } catch (error) {
        console.error('Failed to toggle like:', error);
    }
}

async function showComments(postId) {
    const section = document.getElementById(`comments-${postId}`);

    if (section.style.display === 'block') {
        section.style.display = 'none';
        return;
    }

    section.style.display = 'block';

    try {
        const response = await fetch(`${API_URL}/social/posts/${postId}/comments`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        const data = await response.json();

        if (response.ok) {
            const commentsList = document.getElementById(`comments-list-${postId}`);
            commentsList.innerHTML = '';

            data.comments.forEach(comment => {
                const commentEl = document.createElement('div');
                commentEl.className = 'comment';
                commentEl.innerHTML = `
                    <div class="comment-author">@${comment.username}</div>
                    <div>${comment.content}</div>
                `;
                commentsList.appendChild(commentEl);
            });
        }
    } catch (error) {
        console.error('Failed to load comments:', error);
    }
}

async function addComment(postId) {
    const input = document.getElementById(`comment-input-${postId}`);
    const content = input.value.trim();

    if (!content) return;

    try {
        const response = await fetch(`${API_URL}/social/posts/${postId}/comments`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ content })
        });

        if (response.ok) {
            input.value = '';
            showComments(postId); // Reload comments
            loadFeed(); // Reload feed to update count
        }
    } catch (error) {
        console.error('Failed to add comment:', error);
    }
}
