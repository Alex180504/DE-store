// Inventory Monitoring UI JavaScript
const INVENTORY_API = '/api/inventory/admin';
let authToken = null;
let currentUser = null;

// Check authentication and ensure user is Network Manager
function checkAuth() {
    authToken = localStorage.getItem('authToken');
    const username = localStorage.getItem('username');
    const fullName = localStorage.getItem('fullName');
    const role = localStorage.getItem('role');

    if (!authToken || !username) {
        window.location.href = 'login.html';
        return false;
    }

    // Only Network Managers can access inventory monitoring
    if (role !== 'NETWORK_MANAGER') {
        alert('Access denied. Only Network Managers can access this page.');
        window.location.href = 'index.html';
        return false;
    }

    currentUser = { username, fullName, role };
    
    const userInfo = document.getElementById('userInfo');
    if (userInfo) {
        userInfo.innerHTML = `<strong>${fullName}</strong><br>${role}`;
    }

    return true;
}

// Logout function
function logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('username');
    localStorage.removeItem('fullName');
    localStorage.removeItem('role');
    localStorage.removeItem('storeId');
    window.location.href = 'login.html';
}

// Authenticated fetch helper
async function authenticatedFetch(url, options = {}) {
    const token = localStorage.getItem('authToken');
    
    if (!token) {
        window.location.href = 'login.html';
        throw new Error('Not authenticated');
    }

    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
        ...options.headers
    };

    const response = await fetch(url, { ...options, headers });

    if (response.status === 401 || response.status === 403) {
        logout();
        throw new Error('Session expired');
    }

    if (!response.ok) {
        const error = await response.json().catch(() => ({ message: 'Request failed' }));
        throw new Error(error.message || `HTTP ${response.status}`);
    }

    return response;
}

// Show message
function showMessage(message, type = 'success') {
    const container = document.getElementById('messageContainer');
    const className = type === 'success' ? 'success-message' : 'error-message';
    container.innerHTML = `<div class="${className}">${message}</div>`;
    
    setTimeout(() => {
        container.innerHTML = '';
    }, 5000);
}

// Load statistics and thresholds
async function loadStatistics() {
    try {
        const response = await authenticatedFetch(`${INVENTORY_API}/stats`);
        const stats = await response.json();
        
        document.getElementById('totalAlerts').textContent = stats.totalAlerts;
        document.getElementById('outOfStockCount').textContent = stats.outOfStock;
        document.getElementById('criticalCount').textContent = stats.critical;
        document.getElementById('lowCount').textContent = stats.low;
        
        // Update threshold form
        if (stats.thresholds) {
            document.getElementById('lowStock').value = stats.thresholds.lowStock;
            document.getElementById('criticalStock').value = stats.thresholds.criticalStock;
            document.getElementById('outOfStock').value = stats.thresholds.outOfStock;
        }
    } catch (error) {
        console.error('Error loading statistics:', error);
        showMessage('Failed to load statistics: ' + error.message, 'error');
    }
}

// Load alerts
async function loadAlerts() {
    const container = document.getElementById('alertsContainer');
    container.innerHTML = '<div class="loading">Loading alerts...</div>';
    
    try {
        const response = await authenticatedFetch(`${INVENTORY_API}/alerts`);
        const alerts = await response.json();
        
        if (alerts.length === 0) {
            container.innerHTML = '<p style="text-align: center; padding: 40px; color: #666;">No stock alerts at this time. All inventory levels are sufficient.</p>';
            return;
        }
        
        // Group alerts by status
        const grouped = {
            OUT_OF_STOCK: alerts.filter(a => a.status === 'OUT_OF_STOCK'),
            CRITICAL: alerts.filter(a => a.status === 'CRITICAL'),
            LOW: alerts.filter(a => a.status === 'LOW')
        };
        
        let html = '';
        
        // Out of stock
        if (grouped.OUT_OF_STOCK.length > 0) {
            html += '<h3 style="color: #dc3545; margin-top: 20px;"><i class="fas fa-exclamation-triangle"></i> Out of Stock</h3>';
            html += createAlertsTable(grouped.OUT_OF_STOCK, 'OUT_OF_STOCK');
        }
        
        // Critical
        if (grouped.CRITICAL.length > 0) {
            html += '<h3 style="color: #fd7e14; margin-top: 20px;"><i class="fas fa-exclamation-circle"></i> Critical Stock</h3>';
            html += createAlertsTable(grouped.CRITICAL, 'CRITICAL');
        }
        
        // Low
        if (grouped.LOW.length > 0) {
            html += '<h3 style="color: #ffc107; margin-top: 20px;"><i class="fas fa-info-circle"></i> Low Stock</h3>';
            html += createAlertsTable(grouped.LOW, 'LOW');
        }
        
        container.innerHTML = html;
        
        // Update last update time
        document.getElementById('lastUpdate').textContent = 
            `Last updated: ${new Date().toLocaleString()}`;
        
    } catch (error) {
        console.error('Error loading alerts:', error);
        container.innerHTML = `<div class="error-message">Failed to load alerts: ${error.message}</div>`;
    }
}

// Create alerts table HTML
function createAlertsTable(alerts, status) {
    const badgeClass = status === 'OUT_OF_STOCK' ? 'badge-out' : 
                       status === 'CRITICAL' ? 'badge-critical' : 'badge-low';
    
    let html = '<table class="alerts-table">';
    html += '<thead><tr>';
    html += '<th>Item ID</th>';
    html += '<th>Item Name</th>';
    html += '<th>Category</th>';
    html += '<th>Current Stock</th>';
    html += '<th>Status</th>';
    html += '</tr></thead><tbody>';
    
    alerts.forEach(alert => {
        html += '<tr>';
        html += `<td>${alert.itemId}</td>`;
        html += `<td>${alert.itemName}</td>`;
        html += `<td>${alert.category}</td>`;
        html += `<td><strong>${alert.currentStock}</strong></td>`;
        html += `<td><span class="badge ${badgeClass}">${status.replace('_', ' ')}</span></td>`;
        html += '</tr>';
    });
    
    html += '</tbody></table>';
    return html;
}

// Trigger manual stock check
async function triggerManualCheck() {
    const btn = event.target;
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Checking...';
    
    try {
        const response = await authenticatedFetch(`${INVENTORY_API}/trigger-check`, {
            method: 'POST'
        });
        const result = await response.json();
        
        showMessage(result.message, 'success');
        
        // Reload data after a short delay
        setTimeout(() => {
            loadData();
        }, 2000);
        
    } catch (error) {
        console.error('Error triggering stock check:', error);
        showMessage('Failed to trigger stock check: ' + error.message, 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<i class="fas fa-sync"></i> Trigger Manual Stock Check';
    }
}

// Update thresholds
async function updateThresholds(event) {
    event.preventDefault();
    
    const lowStock = parseInt(document.getElementById('lowStock').value);
    const criticalStock = parseInt(document.getElementById('criticalStock').value);
    const outOfStock = parseInt(document.getElementById('outOfStock').value);
    
    // Validate
    if (criticalStock >= lowStock) {
        showMessage('Critical stock threshold must be less than low stock threshold', 'error');
        return;
    }
    
    try {
        const response = await authenticatedFetch(`${INVENTORY_API}/thresholds`, {
            method: 'PUT',
            body: JSON.stringify({ lowStock, criticalStock, outOfStock })
        });
        
        const result = await response.json();
        showMessage(result.message, 'success');
        
        // Reload data
        loadData();
        
    } catch (error) {
        console.error('Error updating thresholds:', error);
        showMessage('Failed to update thresholds: ' + error.message, 'error');
    }
}

// Load all data
async function loadData() {
    await Promise.all([
        loadStatistics(),
        loadAlerts()
    ]);
}

// Initialize on page load
if (checkAuth()) {
    loadData();
}
