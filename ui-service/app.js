// DE-Store Pricing Management UI
// API Gateway endpoint - use relative URL to avoid CORS
const API_BASE = '/api/pricing';
const AUTH_BASE = '/api/auth';

// State
let stores = [];
let pricingRules = [];
let authToken = null;
let currentUser = null;

// Check authentication on page load
function checkAuth() {
    authToken = localStorage.getItem('authToken');
    const username = localStorage.getItem('username');
    const fullName = localStorage.getItem('fullName');
    const role = localStorage.getItem('role');
    const storeId = localStorage.getItem('storeId');

    if (!authToken || !username) {
        // Redirect to login if not authenticated
        window.location.href = 'login.html';
        return false;
    }

    currentUser = { 
        username, 
        fullName, 
        role, 
        storeId: storeId ? parseInt(storeId) : null,
        isNetworkManager: role === 'NETWORK_MANAGER',
        isStoreManager: role === 'STORE_MANAGER'
    };
    
    // Display user info in header
    const userInfo = document.getElementById('userInfo');
    if (userInfo) {
        userInfo.innerHTML = `<strong>${fullName}</strong><br>${role}${storeId ? ` (Store ${storeId})` : ''}`;
    }

    // Apply role-based UI restrictions
    applyRoleBasedUI();

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

// Helper function to make authenticated API calls
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

// Apply role-based UI restrictions
function applyRoleBasedUI() {
    if (!currentUser) return;

    // Network managers have access to loyalty management
    if (currentUser.isNetworkManager) {
        const loyaltyTab = document.getElementById('loyaltyTab');
        if (loyaltyTab) {
            loyaltyTab.style.display = 'inline-block';
        }
    }

    // Store managers cannot create global rules
    if (currentUser.isStoreManager) {
        // Hide global checkbox and related help text
        const isGlobalCheckbox = document.getElementById('isGlobal');
        const isGlobalGroup = isGlobalCheckbox?.closest('.form-group');
        if (isGlobalGroup) {
            isGlobalGroup.style.display = 'none';
        }

        // Always show store field for store managers
        const storeIdGroup = document.getElementById('storeIdGroup');
        if (storeIdGroup) {
            storeIdGroup.style.display = 'block';
        }

        // Pre-select and lock store manager's store
        const storeSelect = document.getElementById('storeId');
        if (storeSelect && currentUser.storeId) {
            storeSelect.value = currentUser.storeId;
            storeSelect.disabled = true;
            storeSelect.required = true;
        }

        // Add informational message to Create Rule tab
        const createTab = document.getElementById('create-tab');
        const createCard = createTab?.querySelector('.card');
        if (createCard && !document.getElementById('store-manager-notice')) {
            const notice = document.createElement('div');
            notice.id = 'store-manager-notice';
            notice.className = 'alert alert-info';
            notice.innerHTML = `
                <strong>ℹ️ Store Manager Permissions</strong><br>
                As a store manager, you can only create and manage pricing rules for your assigned store (Store ${currentUser.storeId}).
                You cannot modify global (network-wide) rules.
            `;
            createCard.insertBefore(notice, createCard.querySelector('form'));
        }
    }
}

// Check if user can edit/delete a specific rule
function canModifyRule(rule) {
    if (!currentUser) return false;

    // Network managers can modify any rule
    if (currentUser.isNetworkManager) {
        return true;
    }

    // Store managers cannot modify global rules
    if (currentUser.isStoreManager) {
        if (rule.isGlobal) {
            return false;
        }
        // Can only modify rules for their own store
        return rule.storeId === currentUser.storeId;
    }

    return false;
}

// Initialize app
document.addEventListener('DOMContentLoaded', () => {
    if (!checkAuth()) return; // Stop if not authenticated
    
    loadStores();
    loadPricingRules();
    setupEventListeners();
});

// Setup event listeners
function setupEventListeners() {
    document.getElementById('create-rule-form').addEventListener('submit', handleCreateRule);
    document.getElementById('calculator-form').addEventListener('submit', handleCalculatePrice);
    document.getElementById('edit-rule-form').addEventListener('submit', handleEditRule);
    
    // Close modal when clicking outside
    window.onclick = function(event) {
        const modal = document.getElementById('edit-modal');
        if (event.target === modal) {
            closeEditModal();
        }
    };
}

// Tab switching
function switchTab(tabName) {
    // Update tab buttons
    document.querySelectorAll('.tab').forEach(tab => tab.classList.remove('active'));
    event.target.classList.add('active');

    // Update tab content
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
    document.getElementById(`${tabName}-tab`).classList.add('active');

    // Reload data if switching to rules tab
    if (tabName === 'rules') {
        loadPricingRules();
    }
}

// Load stores from API
async function loadStores() {
    try {
        // Fetch stores from the dedicated store-service
        const response = await authenticatedFetch('/api/stores?activeOnly=true');
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        const storesData = await response.json();
        
        // Map store data to match expected format
        stores = storesData.map(store => ({
            storeId: store.storeId,
            storeCode: store.storeCode,
            storeName: store.storeName
        }));
        
        // Populate store dropdowns
        populateStoreDropdowns();
    } catch (error) {
        console.error('Failed to load stores:', error);
        // Fallback: Use hardcoded store data from store-service init script
        stores = [
            { storeId: 1, storeCode: 'LON-001', storeName: 'London Central' },
            { storeId: 2, storeCode: 'MAN-001', storeName: 'Manchester Store' },
            { storeId: 3, storeCode: 'BIR-001', storeName: 'Birmingham Store' },
            { storeId: 4, storeCode: 'GLA-001', storeName: 'Glasgow Store' },
            { storeId: 5, storeCode: 'EDI-001', storeName: 'Edinburgh Store' }
        ];
        populateStoreDropdowns();
    }
}

// Populate store dropdown selects
function populateStoreDropdowns() {
    const storeSelects = ['storeId', 'calcStoreId', 'edit-store-id'];
    
    storeSelects.forEach(selectId => {
        const select = document.getElementById(selectId);
        
        // Different default text for edit modal
        if (selectId === 'edit-store-id') {
            select.innerHTML = '<option value="">Global Rule</option>';
        } else {
            select.innerHTML = '<option value="">Select Store</option>';
        }
        
        stores.forEach(store => {
            const option = document.createElement('option');
            option.value = store.storeId;
            option.textContent = `${store.storeName} (${store.storeCode})`;
            select.appendChild(option);
        });
    });
}

// Load pricing rules
async function loadPricingRules() {
    const loadingDiv = document.getElementById('rules-loading');
    const tableContainer = document.getElementById('rules-table-container');
    const alertDiv = document.getElementById('rules-alert');
    
    loadingDiv.style.display = 'block';
    tableContainer.innerHTML = '';
    alertDiv.innerHTML = '';

    try {
        const response = await authenticatedFetch(`${API_BASE}/rules`);
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        pricingRules = await response.json();
        loadingDiv.style.display = 'none';
        
        if (pricingRules.length === 0) {
            alertDiv.innerHTML = '<div class="alert alert-info">No active pricing rules found. Create your first rule!</div>';
            return;
        }
        
        renderPricingRulesTable(pricingRules);
    } catch (error) {
        loadingDiv.style.display = 'none';
        alertDiv.innerHTML = `<div class="alert alert-error">Failed to load pricing rules: ${error.message}</div>`;
        console.error('Error loading pricing rules:', error);
    }
}

// Render pricing rules table
function renderPricingRulesTable(rules) {
    const tableContainer = document.getElementById('rules-table-container');
    
    const table = `
        <table>
            <thead>
                <tr>
                    <th>Rule ID</th>
                    <th>Item ID</th>
                    <th>Store</th>
                    <th>Scope</th>
                    <th>Price</th>
                    <th>Promotion</th>
                    <th>Valid From</th>
                    <th>Valid To</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                ${rules.map(rule => {
                    const canModify = canModifyRule(rule);
                    return `
                    <tr ${!canModify ? 'style="opacity: 0.6;"' : ''}>
                        <td>${rule.ruleId}</td>
                        <td>${rule.itemId}</td>
                        <td>${rule.storeName || '-'}</td>
                        <td>
                            <span class="badge ${rule.isGlobal ? 'badge-info' : 'badge-warning'}">
                                ${rule.isGlobal ? '🌐 Global' : '🏪 Store-Specific'}
                            </span>
                        </td>
                        <td>£${rule.price.toFixed(2)}</td>
                        <td>
                            ${formatPromotion(rule.promotion, rule.promotionValue)}
                        </td>
                        <td>${formatDateTime(rule.validFrom)}</td>
                        <td>${rule.validTo ? formatDateTime(rule.validTo) : 'No expiry'}</td>
                        <td>
                            ${canModify ? `
                            <div class="action-buttons">
                                <button class="btn btn-primary" onclick="editRule(${rule.ruleId})">✏️ Edit</button>
                                <button class="btn btn-danger" onclick="deleteRule(${rule.ruleId})">🗑️ Delete</button>
                            </div>
                            ` : `
                            <span class="badge badge-secondary" title="You don't have permission to modify this rule">🔒 Read-only</span>
                            `}
                        </td>
                    </tr>
                `;
                }).join('')}
            </tbody>
        </table>
    `;
    
    tableContainer.innerHTML = table;
}

// Format promotion display
function formatPromotion(type, value) {
    switch(type) {
        case 'NONE':
            return '<span class="badge badge-secondary">No Promotion</span>';
        case 'THREE_FOR_TWO':
            return '<span class="badge badge-success">3 for 2</span>';
        case 'BOGOF':
            return '<span class="badge badge-success">BOGOF</span>';
        case 'FREE_DELIVERY':
            return '<span class="badge badge-success">Free Delivery</span>';
        case 'PERCENTAGE_OFF':
            return `<span class="badge badge-success">${value}% Off</span>`;
        case 'FIXED_DISCOUNT':
            return `<span class="badge badge-success">£${value} Off</span>`;
        default:
            return type;
    }
}

// Format date time
function formatDateTime(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-GB', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Toggle promotion value field based on promotion type
function togglePromotionValue() {
    const promotion = document.getElementById('promotion').value;
    const valueGroup = document.getElementById('promotionValueGroup');
    const valueInput = document.getElementById('promotionValue');
    const helpText = document.getElementById('promotionValueHelp');
    
    if (promotion === 'PERCENTAGE_OFF') {
        valueGroup.style.display = 'block';
        valueInput.required = true;
        valueInput.min = '0';
        valueInput.max = '100';
        valueInput.step = '0.01';
        helpText.textContent = 'Enter percentage (0-100)';
    } else if (promotion === 'FIXED_DISCOUNT') {
        valueGroup.style.display = 'block';
        valueInput.required = true;
        valueInput.min = '0';
        valueInput.max = '';
        valueInput.step = '0.01';
        helpText.textContent = 'Enter discount amount in £';
    } else {
        valueGroup.style.display = 'none';
        valueInput.required = false;
        valueInput.value = '';
    }
}

// Toggle store field based on global checkbox
function toggleStoreField() {
    // Store managers cannot use this - their store is locked
    if (currentUser?.isStoreManager) {
        return;
    }

    const isGlobal = document.getElementById('isGlobal').checked;
    const storeGroup = document.getElementById('storeIdGroup');
    const storeSelect = document.getElementById('storeId');
    
    if (isGlobal) {
        storeGroup.style.display = 'none';
        storeSelect.required = false;
        storeSelect.value = '';
    } else {
        storeGroup.style.display = 'block';
        storeSelect.required = true;
    }
}

// Handle create rule form submission
async function handleCreateRule(e) {
    e.preventDefault();
    
    const alertDiv = document.getElementById('create-alert');
    alertDiv.innerHTML = '';
    
    const formData = {
        itemId: parseInt(document.getElementById('itemId').value),
        price: parseFloat(document.getElementById('price').value),
        promotion: document.getElementById('promotion').value,
        promotionValue: document.getElementById('promotionValue').value ? 
            parseFloat(document.getElementById('promotionValue').value) : null,
        isGlobal: currentUser?.isStoreManager ? false : document.getElementById('isGlobal').checked,
        storeId: document.getElementById('storeId').value ? 
            parseInt(document.getElementById('storeId').value) : null,
        validFrom: document.getElementById('validFrom').value || null,
        validTo: document.getElementById('validTo').value || null,
        createdBy: document.getElementById('createdBy').value || currentUser?.fullName || 'UI User'
    };
    
    try {
        const response = await authenticatedFetch(`${API_BASE}/rules`, {
            method: 'POST',
            body: JSON.stringify(formData)
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || `HTTP ${response.status}`);
        }
        
        const result = await response.json();
        alertDiv.innerHTML = `<div class="alert alert-success">Pricing rule created successfully! Rule ID: ${result.ruleId}</div>`;
        
        // Reset form
        document.getElementById('create-rule-form').reset();
        
        // Reload rules if on rules tab
        loadPricingRules();
    } catch (error) {
        alertDiv.innerHTML = `<div class="alert alert-error">Failed to create rule: ${error.message}</div>`;
        console.error('Error creating rule:', error);
    }
}

// Handle calculate price form submission
async function handleCalculatePrice(e) {
    e.preventDefault();
    
    const alertDiv = document.getElementById('calc-alert');
    const resultDiv = document.getElementById('price-result');
    const breakdownDiv = document.getElementById('price-breakdown');
    
    alertDiv.innerHTML = '';
    resultDiv.style.display = 'none';
    
    const requestData = {
        itemId: parseInt(document.getElementById('calcItemId').value),
        storeId: parseInt(document.getElementById('calcStoreId').value),
        quantity: parseInt(document.getElementById('calcQuantity').value)
    };
    
    try {
        const response = await authenticatedFetch(`${API_BASE}/calculate`, {
            method: 'POST',
            body: JSON.stringify(requestData)
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || `HTTP ${response.status}`);
        }
        
        const result = await response.json();
        
        // Display result
        breakdownDiv.innerHTML = `
            <div class="price-row">
                <span>Item ID:</span>
                <span>${result.itemId}</span>
            </div>
            <div class="price-row">
                <span>Item Name:</span>
                <span>${result.itemName}</span>
            </div>
            <div class="price-row">
                <span>Unit Price:</span>
                <span>£${result.unitPrice.toFixed(2)}</span>
            </div>
            <div class="price-row">
                <span>Quantity:</span>
                <span>${result.quantity}</span>
            </div>
            <div class="price-row">
                <span>Subtotal:</span>
                <span>£${result.subtotal.toFixed(2)}</span>
            </div>
            ${result.promotionApplied !== 'NONE' ? `
                <div class="price-row">
                    <span>Promotion:</span>
                    <span>${formatPromotion(result.promotionApplied, result.promotionValue)}</span>
                </div>
                <div class="price-row">
                    <span>Discount:</span>
                    <span style="color: #28a745;">-£${result.discount.toFixed(2)}</span>
                </div>
            ` : ''}
            <div class="price-row">
                <span>Final Total:</span>
                <span>£${result.finalPrice.toFixed(2)}</span>
            </div>
            ${result.ruleSource ? `
                <div class="price-row">
                    <span>Rule Source:</span>
                    <span class="badge ${result.ruleSource === 'GLOBAL' ? 'badge-info' : 'badge-warning'}">
                        ${result.ruleSource}
                    </span>
                </div>
            ` : ''}
        `;
        
        resultDiv.style.display = 'block';
    } catch (error) {
        alertDiv.innerHTML = `<div class="alert alert-error">Failed to calculate price: ${error.message}</div>`;
        console.error('Error calculating price:', error);
    }
}

// Edit a pricing rule
async function editRule(ruleId) {
    const alertDiv = document.getElementById('rules-alert');
    alertDiv.innerHTML = '';
    
    try {
        // Fetch the rule details
        const response = await authenticatedFetch(`${API_BASE}/rules/${ruleId}`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        
        const rule = await response.json();

        // Check permission before allowing edit
        if (!canModifyRule(rule)) {
            alertDiv.innerHTML = `<div class="alert alert-error">⛔ You don't have permission to edit this rule.</div>`;
            return;
        }
        
        // Populate the edit modal
        document.getElementById('edit-rule-id').value = rule.ruleId;
        document.getElementById('edit-item-id').value = rule.itemId;
        document.getElementById('edit-store-id').value = rule.storeId || '';
        document.getElementById('edit-price').value = rule.price;
        document.getElementById('edit-promotion').value = rule.promotion;
        document.getElementById('edit-promotion-value').value = rule.promotionValue || '';
        document.getElementById('edit-is-global').checked = rule.isGlobal;

        // Store managers cannot change scope or store
        if (currentUser?.isStoreManager) {
            document.getElementById('edit-is-global').disabled = true;
            document.getElementById('edit-store-id').disabled = true;
        }
        
        // Format dates for datetime-local input
        if (rule.validFrom) {
            document.getElementById('edit-valid-from').value = formatDateTimeForInput(rule.validFrom);
        }
        if (rule.validTo) {
            document.getElementById('edit-valid-to').value = formatDateTimeForInput(rule.validTo);
        }
        
        // Show the modal
        document.getElementById('edit-modal').style.display = 'block';
        
    } catch (error) {
        alertDiv.innerHTML = `<div class="alert alert-error">Failed to load rule: ${error.message}</div>`;
        console.error('Error loading rule:', error);
    }
}

// Close edit modal
function closeEditModal() {
    document.getElementById('edit-modal').style.display = 'none';
    document.getElementById('edit-rule-form').reset();
}

// Handle edit form submission
async function handleEditRule(event) {
    event.preventDefault();
    
    const ruleId = document.getElementById('edit-rule-id').value;
    const alertDiv = document.getElementById('rules-alert');
    alertDiv.innerHTML = '';
    
    const formData = {
        itemId: parseInt(document.getElementById('edit-item-id').value),
        storeId: document.getElementById('edit-store-id').value ? parseInt(document.getElementById('edit-store-id').value) : null,
        price: parseFloat(document.getElementById('edit-price').value),
        promotion: document.getElementById('edit-promotion').value,
        promotionValue: document.getElementById('edit-promotion-value').value ? parseFloat(document.getElementById('edit-promotion-value').value) : null,
        isGlobal: document.getElementById('edit-is-global').checked,
        validFrom: document.getElementById('edit-valid-from').value || null,
        validTo: document.getElementById('edit-valid-to').value || null
    };
    
    try {
        const response = await authenticatedFetch(`${API_BASE}/rules/${ruleId}`, {
            method: 'PUT',
            body: JSON.stringify(formData)
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || `HTTP ${response.status}`);
        }
        
        closeEditModal();
        alertDiv.innerHTML = `<div class="alert alert-success">Rule ${ruleId} updated successfully!</div>`;
        
        // Reload rules
        setTimeout(() => {
            loadPricingRules();
        }, 1500);
        
    } catch (error) {
        alertDiv.innerHTML = `<div class="alert alert-error">Failed to update rule: ${error.message}</div>`;
        console.error('Error updating rule:', error);
    }
}

// Delete a pricing rule
async function deleteRule(ruleId) {
    // First check permission by fetching the rule
    const alertDiv = document.getElementById('rules-alert');
    alertDiv.innerHTML = '';

    try {
        const response = await authenticatedFetch(`${API_BASE}/rules/${ruleId}`);
        if (!response.ok) {
            throw new Error(`Failed to load rule ${ruleId}`);
        }
        const rule = await response.json();

        // Check permission
        if (!canModifyRule(rule)) {
            alertDiv.innerHTML = `<div class="alert alert-error">⛔ You don't have permission to delete this rule.</div>`;
            return;
        }
    } catch (error) {
        alertDiv.innerHTML = `<div class="alert alert-error">Failed to verify permissions: ${error.message}</div>`;
        return;
    }

    if (!confirm(`Are you sure you want to permanently delete rule ${ruleId}? This action cannot be undone.`)) {
        return;
    }
    
    try {
        const response = await authenticatedFetch(`${API_BASE}/rules/${ruleId}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || `HTTP ${response.status}`);
        }
        
        alertDiv.innerHTML = `<div class="alert alert-success">Rule ${ruleId} deleted successfully!</div>`;
        
        // Reload rules
        setTimeout(() => {
            loadPricingRules();
        }, 1500);
    } catch (error) {
        alertDiv.innerHTML = `<div class="alert alert-error">Failed to delete rule: ${error.message}</div>`;
        console.error('Error deleting rule:', error);
    }
}

// Format datetime for input field (YYYY-MM-DDTHH:MM)
function formatDateTimeForInput(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
}
