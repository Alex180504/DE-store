// Loyalty Management JavaScript
const LOYALTY_API_BASE = '/api/loyalty';

// Tab switching
function showTab(tabName, event) {
    // Hide all tabs
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.style.display = 'none';
    });
    
    // Remove active class from all tab buttons
    document.querySelectorAll('.tab').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Show selected tab
    const tabMap = {
        'rules': 'rulesTab',
        'redemptions': 'redemptionsTab',
        'customers': 'customersTab'
    };
    
    const tabElement = document.getElementById(tabMap[tabName]);
    if (tabElement) {
        tabElement.style.display = 'block';
        if (event && event.target) {
            event.target.classList.add('active');
        }
    }
    
    // Load data for the tab
    if (tabName === 'rules') loadRules();
    else if (tabName === 'redemptions') loadRedemptions();
}

// Load product points rules
async function loadRules() {
    try {
        // Fetch non-deleted rules (includes both active and inactive)
        const response = await authenticatedFetch(`${LOYALTY_API_BASE}/rules/active`);
        if (!response.ok) throw new Error('Failed to fetch rules');
        
        const rules = await response.json();
        const tbody = document.getElementById('rulesTableBody');
        
        if (rules.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="8" class="info-message">No product points rules configured. Click "Add Rule" to create one.</td>
                </tr>
            `;
            return;
        }
        
        tbody.innerHTML = rules.map(rule => `
            <tr>
                <td>${rule.ruleId}</td>
                <td>${rule.itemId}</td>
                <td>${rule.pointsPerUnit || '-'}</td>
                <td>${rule.pointsPerPound || '-'}</td>
                <td>${formatDateTime(rule.validFrom)}</td>
                <td>${rule.validTo ? formatDateTime(rule.validTo) : 'Ongoing'}</td>
                <td>
                    <span class="badge ${rule.isActive ? 'badge-success' : 'badge-danger'}">
                        ${rule.isActive ? 'Active' : 'Inactive'}
                    </span>
                </td>
                <td>
                    <button onclick="editRule(${rule.ruleId})" class="btn btn-sm btn-secondary">Edit</button>
                    <button onclick="toggleRuleStatus(${rule.ruleId})" class="btn btn-sm ${rule.isActive ? 'btn-warning' : 'btn-success'}">
                        ${rule.isActive ? 'Deactivate' : 'Activate'}
                    </button>
                    <button onclick="deleteRule(${rule.ruleId})" class="btn btn-sm btn-danger">Delete</button>
                </td>
            </tr>
        `).join('');
    } catch (error) {
        showToast('Error loading rules: ' + error.message, 'error');
        document.getElementById('rulesTableBody').innerHTML = 
            '<tr><td colspan="8" class="error-message">Error loading rules. Please try again.</td></tr>';
    }
}

// ==================== REDEMPTION OFFERS ====================

// Load redemption offers
async function loadRedemptions() {
    try {
        const response = await authenticatedFetch(`${LOYALTY_API_BASE}/offers`);
        
        if (!response.ok) {
            throw new Error('Failed to load redemption offers');
        }
        
        const offers = await response.json();
        const tbody = document.getElementById('redemptionsTableBody');
        
        if (offers.length === 0) {
            tbody.innerHTML = '<tr><td colspan="9" class="info-message">No redemption offers configured</td></tr>';
            return;
        }
        
        tbody.innerHTML = offers.map(offer => `
            <tr>
                <td>${offer.redemptionId}</td>
                <td>${offer.itemId || 'Any'}</td>
                <td>${offer.offerName}</td>
                <td>${offer.discountPercentage}%</td>
                <td>${offer.pointsCost}</td>
                <td>${offer.maxUsesPerCustomer || '∞'}</td>
                <td>${offer.currentTotalUses}/${offer.maxTotalUses || '∞'}</td>
                <td><span class="badge ${offer.isActive ? 'badge-success' : 'badge-danger'}">
                    ${offer.isActive ? 'Active' : 'Inactive'}
                </span></td>
                <td>
                    <button class="btn btn-sm btn-secondary" onclick="editRedemption(${offer.redemptionId})" title="Edit">Edit</button>
                    <button class="btn btn-sm btn-danger" onclick="archiveRedemption(${offer.redemptionId})" title="Archive">Archive</button>
                </td>
            </tr>
        `).join('');
    } catch (error) {
        showToast('Error loading redemption offers: ' + error.message, 'error');
        document.getElementById('redemptionsTableBody').innerHTML = 
            '<tr><td colspan="10" class="error-message">Error loading offers</td></tr>';
    }
}

// Show add redemption modal
function showAddRedemptionModal() {
    document.getElementById('redemptionModalTitle').textContent = 'Add Redemption Offer';
    document.getElementById('redemptionForm').reset();
    document.getElementById('redemptionOfferId').value = '';
    
    // Set default valid from to now
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    document.getElementById('redemptionValidFrom').value = now.toISOString().slice(0, 16);
    
    document.getElementById('redemptionModal').style.display = 'block';
}

// Edit redemption
async function editRedemption(id) {
    try {
        const response = await authenticatedFetch(`${LOYALTY_API_BASE}/offers/${id}`);
        
        if (!response.ok) {
            throw new Error('Failed to fetch redemption offer');
        }
        
        const offer = await response.json();
        
        // Populate form
        document.getElementById('redemptionModalTitle').textContent = 'Edit Redemption Offer';
        document.getElementById('redemptionOfferId').value = offer.redemptionId;
        document.getElementById('redemptionItemId').value = offer.itemId || '';
        document.getElementById('redemptionOfferName').value = offer.offerName;
        document.getElementById('redemptionDescription').value = offer.description || '';
        document.getElementById('redemptionDiscount').value = offer.discountPercentage;
        document.getElementById('redemptionPoints').value = offer.pointsCost;
        document.getElementById('redemptionMaxPerCustomer').value = offer.maxUsesPerCustomer || '';
        document.getElementById('redemptionMaxTotal').value = offer.maxTotalUses || '';
        document.getElementById('redemptionValidFrom').value = formatDateTimeForInput(offer.validFrom);
        document.getElementById('redemptionValidTo').value = offer.validTo ? formatDateTimeForInput(offer.validTo) : '';
        document.getElementById('redemptionIsActive').checked = offer.isActive;
        
        document.getElementById('redemptionModal').style.display = 'block';
    } catch (error) {
        showToast('Error loading redemption: ' + error.message, 'error');
    }
}

// Close redemption modal
function closeRedemptionModal() {
    document.getElementById('redemptionModal').style.display = 'none';
}

// Save redemption offer
async function saveRedemptionOffer(event) {
    event.preventDefault();
    
    const id = document.getElementById('redemptionOfferId').value;
    const itemId = document.getElementById('redemptionItemId').value;
    const validTo = document.getElementById('redemptionValidTo').value;
    const maxPerCustomer = document.getElementById('redemptionMaxPerCustomer').value;
    const maxTotal = document.getElementById('redemptionMaxTotal').value;
    
    const data = {
        itemId: itemId ? parseInt(itemId) : null,
        offerName: document.getElementById('redemptionOfferName').value,
        description: document.getElementById('redemptionDescription').value,
        discountPercentage: parseFloat(document.getElementById('redemptionDiscount').value),
        pointsCost: parseFloat(document.getElementById('redemptionPoints').value),
        maxUsesPerCustomer: maxPerCustomer ? parseInt(maxPerCustomer) : null,
        maxTotalUses: maxTotal ? parseInt(maxTotal) : null,
        validFrom: document.getElementById('redemptionValidFrom').value,
        validTo: validTo || null,
        isActive: document.getElementById('redemptionIsActive').checked
    };
    
    try {
        const url = id ? `${LOYALTY_API_BASE}/offers/${id}` : `${LOYALTY_API_BASE}/offers`;
        const method = id ? 'PUT' : 'POST';
        
        const response = await authenticatedFetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        
        if (!response.ok) {
            throw new Error('Failed to save redemption offer');
        }
        
        showToast(id ? 'Redemption updated successfully!' : 'Redemption created successfully!', 'success');
        closeRedemptionModal();
        loadRedemptions();
    } catch (error) {
        showToast('Error saving redemption: ' + error.message, 'error');
    }
}

// Archive redemption (deactivate)
async function archiveRedemption(id) {
    if (!confirm('Are you sure you want to archive this redemption offer? It will be deactivated and no longer available to customers.')) {
        return;
    }
    
    try {
        // First get the current offer
        const getResponse = await authenticatedFetch(`${LOYALTY_API_BASE}/offers/${id}`);
        if (!getResponse.ok) {
            throw new Error('Failed to fetch redemption offer');
        }
        
        const offer = await getResponse.json();
        
        // Update it with isActive = false
        const updateResponse = await authenticatedFetch(`${LOYALTY_API_BASE}/offers/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                ...offer,
                isActive: false
            })
        });
        
        if (!updateResponse.ok) {
            throw new Error('Failed to archive redemption offer');
        }
        
        showToast('Redemption offer archived successfully!', 'success');
        loadRedemptions();
    } catch (error) {
        showToast('Error archiving redemption: ' + error.message, 'error');
    }
}

// ==================== CUSTOMER POINTS ====================

// Search customer points
async function searchCustomerPoints() {
    const customerId = document.getElementById('customerIdSearch').value;
    
    if (!customerId) {
        showToast('Please enter a customer ID', 'warning');
        return;
    }
    
    try {
        const response = await authenticatedFetch(`${LOYALTY_API_BASE}/points/balance/${customerId}`);
        
        if (!response.ok) {
            throw new Error('Failed to fetch customer points');
        }
        
        const points = await response.json();
        
        // Display results
        document.getElementById('customerIdDisplay').textContent = customerId;
        document.getElementById('currentBalance').textContent = points.currentBalance.toFixed(2);
        document.getElementById('lifetimeEarned').textContent = points.lifetimeEarned.toFixed(2);
        document.getElementById('lifetimeRedeemed').textContent = points.lifetimeRedeemed.toFixed(2);
        document.getElementById('customerPointsResult').style.display = 'block';
        
    } catch (error) {
        showToast('Error fetching customer points: ' + error.message, 'error');
        document.getElementById('customerPointsResult').style.display = 'none';
    }
}

// Calculate customer points (trigger manual recalculation)
async function calculateCustomerPoints() {
    const customerId = document.getElementById('customerIdSearch').value;
    
    if (!customerId) {
        showToast('Please enter a customer ID', 'warning');
        return;
    }
    
    try {
        showToast('Calculating points... This may take a moment', 'info');
        
        const response = await authenticatedFetch(
            `${LOYALTY_API_BASE}/points/calculate/${customerId}`,
            { method: 'POST' }
        );
        
        if (!response.ok) {
            throw new Error('Failed to calculate points');
        }
        
        const points = await response.json();
        
        // Display results
        document.getElementById('customerIdDisplay').textContent = customerId;
        document.getElementById('currentBalance').textContent = points.currentBalance.toFixed(2);
        document.getElementById('lifetimeEarned').textContent = points.lifetimeEarned.toFixed(2);
        document.getElementById('lifetimeRedeemed').textContent = points.lifetimeRedeemed.toFixed(2);
        document.getElementById('customerPointsResult').style.display = 'block';
        
        showToast('Points calculated successfully!', 'success');
        
    } catch (error) {
        showToast('Error calculating points: ' + error.message, 'error');
    }
}

// ==================== PRODUCT POINTS RULES FUNCTIONS ====================

// Show add rule modal
function showAddRuleModal() {
    document.getElementById('ruleModalTitle').textContent = 'Add Product Points Rule';
    document.getElementById('ruleForm').reset();
    document.getElementById('ruleId').value = '';
    document.getElementById('ruleIsActive').checked = true;
    
    // Set default valid from to now
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    document.getElementById('ruleValidFrom').value = now.toISOString().slice(0, 16);
    
    document.getElementById('ruleModal').style.display = 'block';
}

// Close rule modal
function closeRuleModal() {
    document.getElementById('ruleModal').style.display = 'none';
}

// Edit existing rule
async function editRule(id) {
    try {
        const response = await authenticatedFetch(`${LOYALTY_API_BASE}/rules/${id}`);
        if (!response.ok) throw new Error('Failed to fetch rule');
        
        const rule = await response.json();
        
        document.getElementById('ruleModalTitle').textContent = 'Edit Product Points Rule';
        document.getElementById('ruleId').value = rule.ruleId;
        document.getElementById('ruleItemId').value = rule.itemId;
        document.getElementById('rulePointsPerUnit').value = rule.pointsPerUnit || '';
        document.getElementById('rulePointsPerPound').value = rule.pointsPerPound || '';
        
        // Format datetime for input
        if (rule.validFrom) {
            const validFrom = new Date(rule.validFrom);
            validFrom.setMinutes(validFrom.getMinutes() - validFrom.getTimezoneOffset());
            document.getElementById('ruleValidFrom').value = validFrom.toISOString().slice(0, 16);
        }
        
        if (rule.validTo) {
            const validTo = new Date(rule.validTo);
            validTo.setMinutes(validTo.getMinutes() - validTo.getTimezoneOffset());
            document.getElementById('ruleValidTo').value = validTo.toISOString().slice(0, 16);
        } else {
            document.getElementById('ruleValidTo').value = '';
        }
        
        document.getElementById('ruleIsActive').checked = rule.isActive;
        
        document.getElementById('ruleModal').style.display = 'block';
    } catch (error) {
        console.error('Error editing rule:', error);
        showToast('Failed to load rule details', 'error');
    }
}

// Save rule (create or update)
async function saveRule(event) {
    event.preventDefault();
    
    const ruleId = document.getElementById('ruleId').value;
    const itemId = document.getElementById('ruleItemId').value;
    const pointsPerUnit = document.getElementById('rulePointsPerUnit').value;
    const pointsPerPound = document.getElementById('rulePointsPerPound').value;
    const validFrom = document.getElementById('ruleValidFrom').value;
    const validTo = document.getElementById('ruleValidTo').value;
    const isActive = document.getElementById('ruleIsActive').checked;
    
    // Validation: must have either pointsPerUnit or pointsPerPound
    if (!pointsPerUnit && !pointsPerPound) {
        showToast('Please specify either Points Per Unit or Points Per £1', 'error');
        return;
    }
    
    const data = {
        itemId: parseInt(itemId),
        pointsPerUnit: pointsPerUnit ? parseInt(pointsPerUnit) : null,
        pointsPerPound: pointsPerPound ? parseFloat(pointsPerPound) : null,
        validFrom: new Date(validFrom).toISOString(),
        validTo: validTo ? new Date(validTo).toISOString() : null,
        isActive: isActive
    };
    
    try {
        const url = ruleId 
            ? `${LOYALTY_API_BASE}/rules/${ruleId}`
            : `${LOYALTY_API_BASE}/rules`;
        
        const response = await authenticatedFetch(url, {
            method: ruleId ? 'PUT' : 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        
        if (!response.ok) throw new Error('Failed to save rule');
        
        showToast(ruleId ? 'Rule updated successfully' : 'Rule created successfully', 'success');
        closeRuleModal();
        loadRules();
    } catch (error) {
        console.error('Error saving rule:', error);
        showToast('Failed to save rule', 'error');
    }
}

// Delete rule
async function deleteRule(id) {
    if (!confirm('Are you sure you want to delete this rule?')) {
        return;
    }
    
    try {
        const response = await authenticatedFetch(`${LOYALTY_API_BASE}/rules/${id}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) throw new Error('Failed to delete rule');
        
        showToast('Rule deleted successfully', 'success');
        loadRules();
    } catch (error) {
        console.error('Error deleting rule:', error);
        showToast('Failed to delete rule', 'error');
    }
}

// Toggle rule status
async function toggleRuleStatus(id) {
    try {
        const response = await authenticatedFetch(`${LOYALTY_API_BASE}/rules/${id}/toggle`, {
            method: 'PATCH'
        });
        
        if (!response.ok) throw new Error('Failed to toggle rule status');
        
        showToast('Rule status updated', 'success');
        loadRules();
    } catch (error) {
        console.error('Error toggling rule status:', error);
        showToast('Failed to update rule status', 'error');
    }
}

// ==================== UTILITY FUNCTIONS ====================

// Format datetime for display
function formatDateTime(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleString('en-GB', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Format datetime for input field
function formatDateTimeForInput(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    const offset = date.getTimezoneOffset();
    date.setMinutes(date.getMinutes() - offset);
    return date.toISOString().slice(0, 16);
}

// Toast notification
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast toast-${type} show`;
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// Close modals when clicking outside
window.onclick = function(event) {
    const ruleModal = document.getElementById('ruleModal');
    const redemptionModal = document.getElementById('redemptionModal');
    
    if (event.target == ruleModal) {
        closeRuleModal();
    }
    if (event.target == redemptionModal) {
        closeRedemptionModal();
    }
}

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    // Check authentication first
    if (!checkAuth()) {
        return; // checkAuth will redirect to login if needed
    }
    
    // Set up form event handlers
    const ruleForm = document.getElementById('ruleForm');
    if (ruleForm) {
        ruleForm.addEventListener('submit', saveRule);
    }
    
    const redemptionForm = document.getElementById('redemptionForm');
    if (redemptionForm) {
        redemptionForm.addEventListener('submit', saveRedemptionOffer);
    }
    
    // Load initial tab
    showTab('rules');
});
