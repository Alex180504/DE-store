// Loyalty Management JavaScript
const LOYALTY_API_BASE = '/api/loyalty';

// Tab switching
function showTab(tabName) {
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
        'bonuses': 'bonusesTab',
        'redemptions': 'redemptionsTab',
        'customers': 'customersTab'
    };
    
    const tabElement = document.getElementById(tabMap[tabName]);
    if (tabElement) {
        tabElement.style.display = 'block';
        event.target.classList.add('active');
    }
    
    // Load data for the tab
    if (tabName === 'rules') loadRules();
    else if (tabName === 'bonuses') loadBonuses();
    else if (tabName === 'redemptions') loadRedemptions();
}

// Load product points rules
async function loadRules() {
    try {
        // For now, display placeholder since we haven't implemented admin CRUD endpoints
        const tbody = document.getElementById('rulesTableBody');
        tbody.innerHTML = `
            <tr>
                <td colspan="9" class="info-message">
                    Product points rules are configured in the database.<br>
                    Use the scheduled calculator (runs every 6 hours) or trigger manual calculation in Customer Points tab.
                </td>
            </tr>
        `;
    } catch (error) {
        showToast('Error loading rules: ' + error.message, 'error');
    }
}

// Load bonus offers
async function loadBonuses() {
    try {
        const tbody = document.getElementById('bonusesTableBody');
        tbody.innerHTML = `
            <tr>
                <td colspan="9" class="info-message">
                    Bonus offers are configured in the database.<br>
                    Check loyalty-service/db/init/02-sample-data.sql for current offers.
                </td>
            </tr>
        `;
    } catch (error) {
        showToast('Error loading bonuses: ' + error.message, 'error');
    }
}

// Load redemption offers
async function loadRedemptions() {
    try {
        const response = await authenticatedFetch(`${LOYALTY_API_BASE}/offers/active`);
        
        if (!response.ok) {
            throw new Error('Failed to load redemption offers');
        }
        
        const offers = await response.json();
        const tbody = document.getElementById('redemptionsTableBody');
        
        if (offers.length === 0) {
            tbody.innerHTML = '<tr><td colspan="10" class="info-message">No active redemption offers</td></tr>';
            return;
        }
        
        tbody.innerHTML = offers.map(offer => `
            <tr>
                <td>${offer.redemptionId}</td>
                <td>${offer.storeId || 'Global'}</td>
                <td>${offer.itemId || 'Any'}</td>
                <td>${offer.offerName}</td>
                <td>${offer.discountPercentage}%</td>
                <td>${offer.pointsCost}</td>
                <td>${offer.maxUsesPerCustomer || '∞'}</td>
                <td>${offer.currentTotalUses}/${offer.maxTotalUses || '∞'}</td>
                <td><span class="badge ${offer.available ? 'badge-success' : 'badge-danger'}">
                    ${offer.available ? 'Active' : 'Unavailable'}
                </span></td>
                <td>
                    <button class="btn-icon" onclick="viewOffer(${offer.redemptionId})" title="View Details">👁️</button>
                </td>
            </tr>
        `).join('');
    } catch (error) {
        showToast('Error loading redemption offers: ' + error.message, 'error');
        document.getElementById('redemptionsTableBody').innerHTML = 
            '<tr><td colspan="10" class="error-message">Error loading offers</td></tr>';
    }
}

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

// View offer details
function viewOffer(offerId) {
    showToast(`Viewing offer ${offerId}`, 'info');
    // Future: Show modal with detailed offer information
}

// Placeholder functions for add modals
function showAddRuleModal() {
    showToast('Add rule functionality - configure in database for now', 'info');
}

function showAddBonusModal() {
    showToast('Add bonus functionality - configure in database for now', 'info');
}

function showAddRedemptionModal() {
    showToast('Add redemption functionality - configure in database for now', 'info');
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

// Initialize page
if (typeof checkAuth === 'function' && checkAuth()) {
    // Load initial tab
    loadRedemptions();
}
