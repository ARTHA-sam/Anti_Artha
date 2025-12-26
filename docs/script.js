// Smooth scroll
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    });
});

// Mobile Menu Toggle
const mobileMenuToggle = document.querySelector('.mobile-menu-toggle');
const navMenu = document.querySelector('.nav-menu');
const mobileOverlay = document.querySelector('.mobile-overlay');
const navLinks = document.querySelectorAll('.nav-link, .nav-link-special');

function toggleMobileMenu() {
    mobileMenuToggle.classList.toggle('active');
    navMenu.classList.toggle('active');
    mobileOverlay.classList.toggle('active');
    document.body.style.overflow = navMenu.classList.contains('active') ? 'hidden' : '';
}

if (mobileMenuToggle) {
    mobileMenuToggle.addEventListener('click', toggleMobileMenu);
}

if (mobileOverlay) {
    mobileOverlay.addEventListener('click', toggleMobileMenu);
}

// Close mobile menu when clicking nav links
navLinks.forEach(link => {
    link.addEventListener('click', () => {
        if (navMenu.classList.contains('active')) {
            toggleMobileMenu();
        }
    });
});

// Example tabs
const showcaseTabs = document.querySelectorAll('.showcase-tab');
const exampleCards = document.querySelectorAll('.example-card');

showcaseTabs.forEach(tab => {
    tab.addEventListener('click', () => {
        const example = tab.getAttribute('data-example');

        showcaseTabs.forEach(t => t.classList.remove('active'));
        exampleCards.forEach(c => c.classList.remove('active'));

        tab.classList.add('active');
        document.querySelector(`.example-card[data-example="${example}"]`).classList.add('active');
    });
});

// Intersection Observer for scroll animations
const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -100px 0px'
};

const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.style.opacity = '1';
            entry.target.style.transform = 'translateY(0)';
        }
    });
}, observerOptions);

// Observe elements
document.querySelectorAll('.feature-glass-card, .step-card').forEach(el => {
    el.style.opacity = '0';
    el.style.transform = 'translateY(30px)';
    el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
    observer.observe(el);
});

// Navbar scroll effect
let lastScroll = 0;
const navbar = document.querySelector('.navbar');

window.addEventListener('scroll', () => {
    const currentScroll = window.pageYOffset;

    if (currentScroll > 100) {
        navbar.style.background = 'rgba(10, 10, 15, 0.95)';
        navbar.style.boxShadow = '0 10px 30px rgba(0, 0, 0, 0.5)';
    } else {
        navbar.style.background = 'rgba(10, 10, 15, 0.7)';
        navbar.style.boxShadow = 'none';
    }

    lastScroll = currentScroll;
});

// Parallax effect for orbs
window.addEventListener('mousemove', (e) => {
    const orbs = document.querySelectorAll('.gradient-orb');
    const x = e.clientX / window.innerWidth;
    const y = e.clientY / window.innerHeight;

    orbs.forEach((orb, index) => {
        const speed = (index + 1) * 10;
        orb.style.transform = `translate(${x * speed}px, ${y * speed}px)`;
    });
});

// Copy code functionality
document.addEventListener('DOMContentLoaded', () => {
    const codeBlocks = document.querySelectorAll('.terminal-body pre, .step-code pre');

    codeBlocks.forEach(block => {
        const container = block.closest('.terminal-body, .step-code');
        if (!container) return;

        // Create copy button
        const copyBtn = document.createElement('button');
        copyBtn.innerHTML = `
            <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path d="M8 16H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v2m-6 12h8a2 2 0 0 0 2-2v-8a2 2 0 0 0-2-2h-8a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2z"/>
            </svg>
        `;
        copyBtn.style.cssText = `
            position: absolute;
            top: 1rem;
            right: 1rem;
            background: rgba(255, 255, 255, 0.1);
            border: 1px solid rgba(255, 255, 255, 0.2);
            padding: 0.5rem;
            border-radius: 8px;
            cursor: pointer;
            color: rgba(255, 255, 255, 0.7);
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.3s;
            z-index: 10;
        `;

        copyBtn.addEventListener('click', async () => {
            const code = block.textContent;
            try {
                await navigator.clipboard.writeText(code);
                copyBtn.innerHTML = `
                    <svg width="18" height="18" fill="none" stroke="#50fa7b" stroke-width="2" viewBox="0 0 24 24">
                        <path d="M5 13l4 4L19 7"/>
                    </svg>
                `;
                setTimeout(() => {
                    copyBtn.innerHTML = `
                        <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                            <path d="M8 16H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v2m-6 12h8a2 2 0 0 0 2-2v-8a2 2 0 0 0-2-2h-8a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2z"/>
                        </svg>
                    `;
                }, 2000);
            } catch (err) {
                console.error('Failed to copy:', err);
            }
        });

        copyBtn.addEventListener('mouseenter', () => {
            copyBtn.style.background = 'rgba(255, 255, 255, 0.15)';
            copyBtn.style.color = 'rgba(255, 255, 255, 1)';
        });

        copyBtn.addEventListener('mouseleave', () => {
            copyBtn.style.background = 'rgba(255, 255, 255, 0.1)';
            copyBtn.style.color = 'rgba(255, 255, 255, 0.7)';
        });

        container.style.position = 'relative';
        container.appendChild(copyBtn);
    });
});

// Add loading animation
window.addEventListener('load', () => {
    document.body.style.opacity = '0';
    setTimeout(() => {
        document.body.style.transition = 'opacity 0.5s';
        document.body.style.opacity = '1';
    }, 100);
});
