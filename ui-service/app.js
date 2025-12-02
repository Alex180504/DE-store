// DE-Store Pricing Management UI
// API Gateway endpoint - use relative URL to avoid CORS
const API_BASE = '/api/pricing';

// State
let stores = [];
let pricingRules = [];

// Initialize app
document.addEventListener('DOMContentLoaded', () => {
    loadStores();
    loadPricingRules();
    setupEventListeners();
});

// Setup event listeners
function setupEventListeners() {
    document.getElementById('create-rule-form').addEventListener('submit', handleCreateRule);
    document.getElementById('calculator-form').addEventListener('submit', handleCalculatePrice);
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
        const response = await fetch(`${API_BASE}/rules`);
        const rules = await response.json();
        
        // Extract unique stores from rules
        const storeSet = new Map();
        rules.forEach(rule => {
            if (rule.storeId && rule.storeCode) {
                storeSet.set(rule.storeId, {
                    storeId: rule.storeId,
                    storeCode: rule.storeCode,
                    storeName: rule.storeName
                });
            }
        });
        
        stores = Array.from(storeSet.values());
        
        // Populate store dropdowns
        populateStoreDropdowns();
    } catch (error) {
        console.error('Failed to load stores:', error);
        // Fallback: Use hardcoded store data from DB init script
        stores = [
            { storeId: 1, storeCode: 'LON001', storeName: 'London Central' },
            { storeId: 2, storeCode: 'MAN001', storeName: 'Manchester Store' },
            { storeId: 3, storeCode: 'BIR001', storeName: 'Birmingham Store' },
            { storeId: 4, storeCode: 'LEE001', storeName: 'Leeds Store' },
            { storeId: 5, storeCode: 'GLW001', storeName: 'Glasgow Store' }
        ];
        populateStoreDropdowns();
    }
}

// Populate store dropdown selects
function populateStoreDropdowns() {
    const storeSelects = ['storeId', 'calcStoreId'];
    
    storeSelects.forEach(selectId => {
        const select = document.getElementById(selectId);
        select.innerHTML = '<option value="">Select Store</option>';
        
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
        const response = await fetch(`${API_BASE}/rules`);
        
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
                ${rules.map(rule => `
                    <tr>
                        <td>${rule.ruleId}</td>
                        <td>${rule.itemId}</td>
                        <td>${rule.storeName || '-'}</td>
                        <td>
                            <span class="badge ${rule.isGlobal ? 'badge-info' : 'badge-warning'}">
                                ${rule.isGlobal ? 'Global' : 'Store-Specific'}
                            </span>
                        </td>
                        <td>£${rule.price.toFixed(2)}</td>
                        <td>
                            ${formatPromotion(rule.promotion, rule.promotionValue)}
                        </td>
                        <td>${formatDateTime(rule.validFrom)}</td>
                        <td>${rule.validTo ? formatDateTime(rule.validTo) : 'No expiry'}</td>
                        <td>
                            <div class="action-buttons">
                                <button class="btn btn-danger" onclick="deactivateRule(${rule.ruleId})">Deactivate</button>
                            </div>
                        </td>
                    </tr>
                `).join('')}
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
        isGlobal: document.getElementById('isGlobal').checked,
        storeId: document.getElementById('storeId').value ? 
            parseInt(document.getElementById('storeId').value) : null,
        validFrom: document.getElementById('validFrom').value || null,
        validTo: document.getElementById('validTo').value || null,
        createdBy: document.getElementById('createdBy').value || 'UI User'
    };
    
    try {
        const response = await fetch(`${API_BASE}/rules`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
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
        const response = await fetch(`${API_BASE}/calculate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
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

// Deactivate a pricing rule
async function deactivateRule(ruleId) {
    if (!confirm(`Are you sure you want to deactivate rule ${ruleId}?`)) {
        return;
    }
    
    const alertDiv = document.getElementById('rules-alert');
    alertDiv.innerHTML = '';
    
    try {
        const response = await fetch(`${API_BASE}/rules/${ruleId}/deactivate`, {
            method: 'POST'
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || `HTTP ${response.status}`);
        }
        
        alertDiv.innerHTML = `<div class="alert alert-success">Rule ${ruleId} deactivated successfully!</div>`;
        
        // Reload rules
        setTimeout(() => {
            loadPricingRules();
        }, 1500);
    } catch (error) {
        alertDiv.innerHTML = `<div class="alert alert-error">Failed to deactivate rule: ${error.message}</div>`;
        console.error('Error deactivating rule:', error);
    }
}
