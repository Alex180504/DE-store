// Shared Navigation Component
// This file creates consistent navigation across all DE-Store pages

function createNavigation(activePage) {
    const user = JSON.parse(localStorage.getItem('user'));
    
    if (!user) {
        // Redirect to login if not authenticated
        if (window.location.pathname !== '/login.html' && window.location.pathname !== '/register.html') {
            window.location.href = '/login.html';
        }
        return;
    }

    const nav = document.createElement('nav');
    nav.className = 'navbar';
    
    const menuItems = [
        { id: 'home', label: '🏠 Home', href: '/index.html', roles: ['CUSTOMER', 'NETWORK_MANAGER'] },
        { id: 'inventory', label: '📦 Inventory', href: '/inventory.html', roles: ['CUSTOMER', 'NETWORK_MANAGER'] },
        { id: 'loyalty', label: '⭐ Loyalty', href: '/loyalty.html', roles: ['NETWORK_MANAGER'] },
        { id: 'analytics', label: '📊 Analytics', href: '/analytics.html', roles: ['NETWORK_MANAGER'] }
    ];

    // Filter menu items based on user role
    const userRole = user.isNetworkManager ? 'NETWORK_MANAGER' : 'CUSTOMER';
    const filteredItems = menuItems.filter(item => item.roles.includes(userRole));

    nav.innerHTML = `
        <div class="navbar-container">
            <a href="/index.html" class="navbar-brand">
                <span class="navbar-logo">🏪</span>
                <span class="navbar-title">DE-Store</span>
            </a>
            <ul class="navbar-menu">
                ${filteredItems.map(item => `
                    <li><a href="${item.href}" class="${item.id === activePage ? 'active' : ''}">${item.label}</a></li>
                `).join('')}
            </ul>
            <div class="navbar-user">
                <div class="user-info">
                    <span class="user-name">${user.username}</span>
                    <span class="user-role">${user.isNetworkManager ? '👑 Network Manager' : '👤 Customer'}</span>
                </div>
                <button class="btn btn-secondary btn-sm" onclick="logout()">Logout</button>
            </div>
        </div>
    `;

    document.body.insertBefore(nav, document.body.firstChild);
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login.html';
}

// Initialize navigation on page load
document.addEventListener('DOMContentLoaded', function() {
    // Get active page from body data attribute or filename
    const activePage = document.body.dataset.page || 
                      window.location.pathname.replace('/', '').replace('.html', '') || 
                      'home';
    createNavigation(activePage);
});
