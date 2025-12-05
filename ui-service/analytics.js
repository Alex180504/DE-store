// Analytics Dashboard JavaScript
const API_BASE_URL = 'http://localhost/api';
let charts = {};
let currentStoreId = null;
let currentStartDate = null;
let currentEndDate = null;

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    initializeDates();
    loadStores();
    setupEventListeners();
});

function initializeDates() {
    const today = new Date();
    const ninetyDaysAgo = new Date(today);
    ninetyDaysAgo.setDate(today.getDate() - 90);
    
    document.getElementById('startDate').valueAsDate = ninetyDaysAgo;
    document.getElementById('endDate').valueAsDate = today;
}

function setupEventListeners() {
    document.getElementById('periodSelect').addEventListener('change', function() {
        const period = this.value;
        const today = new Date();
        let startDate = new Date(today);
        
        switch(period) {
            case 'today':
                startDate = new Date(today);
                break;
            case 'week':
                startDate.setDate(today.getDate() - 6);
                break;
            case 'month':
                startDate = new Date(today.getFullYear(), today.getMonth(), 1);
                break;
            case 'custom':
                return; // Don't update dates
        }
        
        document.getElementById('startDate').valueAsDate = startDate;
        document.getElementById('endDate').valueAsDate = today;
    });
}

async function loadStores() {
    try {
        const response = await authenticatedFetch(`${API_BASE_URL}/analytics/stores/list`);

        if (!response.ok) {
            throw new Error('Failed to load stores');
        }

        const stores = await response.json();
        const select = document.getElementById('storeSelect');
        
        select.innerHTML = '<option value="">Select a store...</option>';
        stores.forEach(store => {
            const option = document.createElement('option');
            option.value = store.id;
            option.textContent = `${store.name} - ${store.location}`;
            select.appendChild(option);
        });

        // Auto-select first store if available
        if (stores.length > 0) {
            select.value = stores[0].id;
        }
    } catch (error) {
        console.error('Error loading stores:', error);
        showError('Failed to load stores. Please refresh the page.');
    }
}

function switchTab(tabName) {
    // Update tab buttons
    document.querySelectorAll('.tab-button').forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active');
    
    // Update tab content
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
    document.getElementById(`${tabName}-tab`).classList.add('active');
}

async function loadAnalytics() {
    const storeId = document.getElementById('storeSelect').value;
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    if (!storeId) {
        alert('Please select a store');
        return;
    }

    if (!startDate || !endDate) {
        alert('Please select date range');
        return;
    }

    currentStoreId = storeId;
    currentStartDate = startDate;
    currentEndDate = endDate;

    // Load all analytics
    await Promise.all([
        loadRevenueAnalytics(),
        loadTransactionAnalytics(),
        loadProductAnalytics(),
        loadStoreComparison()
    ]);
}

async function loadRevenueAnalytics() {
    try {
        const url = `${API_BASE_URL}/analytics/revenue/summary?storeId=${currentStoreId}&startDate=${currentStartDate}&endDate=${currentEndDate}`;
        
        const response = await authenticatedFetch(url);

        if (!response.ok) throw new Error('Failed to load revenue analytics');

        const data = await response.json();
        
        // Update KPIs
        document.getElementById('revenue-total').textContent = formatCurrency(data.totalRevenue);
        document.getElementById('revenue-count').textContent = formatNumber(data.transactionCount);
        document.getElementById('revenue-avg').textContent = formatCurrency(data.averageTransactionValue);
        document.getElementById('revenue-range').textContent = 
            `${formatCurrency(data.minTransactionValue)} - ${formatCurrency(data.maxTransactionValue)}`;

        // Update revenue trend chart
        updateRevenueTrendChart(data.dailyBreakdown);
        
        // Update payment method chart
        updatePaymentMethodChart(data.paymentMethodBreakdown);

    } catch (error) {
        console.error('Error loading revenue analytics:', error);
        showError('Failed to load revenue analytics');
    }
}

async function loadTransactionAnalytics() {
    try {
        const url = `${API_BASE_URL}/analytics/transactions/summary?storeId=${currentStoreId}&startDate=${currentStartDate}&endDate=${currentEndDate}`;
        
        const response = await authenticatedFetch(url);

        if (!response.ok) throw new Error('Failed to load transaction analytics');

        const data = await response.json();
        
        // Update KPIs
        document.getElementById('trans-volume').textContent = formatCurrency(data.totalVolume);
        document.getElementById('trans-completed').textContent = formatNumber(data.completedCount);
        document.getElementById('trans-pending').textContent = formatNumber(data.pendingCount);
        document.getElementById('trans-issues').textContent = 
            formatNumber((data.cancelledCount || 0) + (data.refundedCount || 0));

        // Update charts
        updateTransactionStatusChart(data);
        updateHourlyDistributionChart(data.hourlyDistribution);

    } catch (error) {
        console.error('Error loading transaction analytics:', error);
        showError('Failed to load transaction analytics');
    }
}

async function loadProductAnalytics() {
    try {
        const url = `${API_BASE_URL}/analytics/products/performance?storeId=${currentStoreId}&startDate=${currentStartDate}&endDate=${currentEndDate}&limit=10`;
        
        const response = await authenticatedFetch(url);

        if (!response.ok) throw new Error('Failed to load product analytics');

        const data = await response.json();
        
        // Update charts
        updateTopProductsChart(data.topProducts);
        updateCategoryChart(data.categoryPerformance);
        
        // Update table
        updateProductsTable(data.topProducts);

    } catch (error) {
        console.error('Error loading product analytics:', error);
        showError('Failed to load product analytics');
    }
}

async function loadStoreComparison() {
    try {
        const url = `${API_BASE_URL}/analytics/stores/comparison?startDate=${currentStartDate}&endDate=${currentEndDate}`;
        
        const response = await authenticatedFetch(url);

        if (!response.ok) throw new Error('Failed to load store comparison');

        const data = await response.json();
        
        // Update KPIs
        document.getElementById('network-revenue').textContent = formatCurrency(data.totalNetworkRevenue);
        document.getElementById('network-trans').textContent = formatNumber(data.totalNetworkTransactions);
        document.getElementById('network-avg').textContent = formatCurrency(data.averageRevenuePerStore);

        // Update chart and table
        updateStoreComparisonChart(data.stores);
        updateStoresTable(data.stores);

    } catch (error) {
        console.error('Error loading store comparison:', error);
        showError('Failed to load store comparison');
    }
}

// Chart update functions
function updateRevenueTrendChart(dailyData) {
    const ctx = document.getElementById('revenueTrendChart');
    
    if (charts.revenueTrend) {
        charts.revenueTrend.destroy();
    }

    charts.revenueTrend = new Chart(ctx, {
        type: 'line',
        data: {
            labels: dailyData.map(d => formatDate(d.date)),
            datasets: [{
                label: 'Revenue',
                data: dailyData.map(d => d.revenue),
                borderColor: '#667eea',
                backgroundColor: 'rgba(102, 126, 234, 0.1)',
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: context => `Revenue: ${formatCurrency(context.parsed.y)}`
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: value => formatCurrency(value)
                    }
                }
            }
        }
    });
}

function updatePaymentMethodChart(paymentData) {
    const ctx = document.getElementById('paymentMethodChart');
    
    if (charts.paymentMethod) {
        charts.paymentMethod.destroy();
    }

    charts.paymentMethod = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: paymentData.map(p => p.paymentMethod),
            datasets: [{
                data: paymentData.map(p => p.revenue),
                backgroundColor: [
                    '#667eea',
                    '#764ba2',
                    '#f093fb',
                    '#4facfe',
                    '#43e97b'
                ]
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom'
                },
                tooltip: {
                    callbacks: {
                        label: context => {
                            const label = context.label || '';
                            const value = formatCurrency(context.parsed);
                            const percentage = paymentData[context.dataIndex].percentage || 0;
                            return `${label}: ${value} (${percentage}%)`;
                        }
                    }
                }
            }
        }
    });
}

function updateTransactionStatusChart(data) {
    const ctx = document.getElementById('transactionStatusChart');
    
    if (charts.transactionStatus) {
        charts.transactionStatus.destroy();
    }

    charts.transactionStatus = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Completed', 'Pending', 'Cancelled', 'Refunded'],
            datasets: [{
                label: 'Count',
                data: [
                    data.completedCount || 0,
                    data.pendingCount || 0,
                    data.cancelledCount || 0,
                    data.refundedCount || 0
                ],
                backgroundColor: [
                    '#43e97b',
                    '#f093fb',
                    '#fa709a',
                    '#feca57'
                ]
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

function updateHourlyDistributionChart(hourlyData) {
    const ctx = document.getElementById('hourlyDistChart');
    
    if (charts.hourlyDist) {
        charts.hourlyDist.destroy();
    }

    // Fill in missing hours with 0
    const hours = Array.from({length: 24}, (_, i) => i);
    const hourlyMap = new Map(hourlyData.map(h => [h.hour, h]));
    const completeData = hours.map(hour => ({
        hour,
        transactionCount: hourlyMap.get(hour)?.transactionCount || 0
    }));

    charts.hourlyDist = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: hours.map(h => `${h}:00`),
            datasets: [{
                label: 'Transactions',
                data: completeData.map(d => d.transactionCount),
                backgroundColor: '#667eea'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

function updateTopProductsChart(products) {
    const ctx = document.getElementById('topProductsChart');
    
    if (charts.topProducts) {
        charts.topProducts.destroy();
    }

    charts.topProducts = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: products.map(p => p.itemName),
            datasets: [{
                label: 'Revenue',
                data: products.map(p => p.totalRevenue),
                backgroundColor: '#764ba2'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            indexAxis: 'y',
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: context => `Revenue: ${formatCurrency(context.parsed.x)}`
                    }
                }
            },
            scales: {
                x: {
                    beginAtZero: true,
                    ticks: {
                        callback: value => formatCurrency(value)
                    }
                }
            }
        }
    });
}

function updateCategoryChart(categories) {
    const ctx = document.getElementById('categoryChart');
    
    if (charts.category) {
        charts.category.destroy();
    }

    charts.category = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: categories.map(c => c.category),
            datasets: [{
                data: categories.map(c => c.totalRevenue),
                backgroundColor: [
                    '#667eea',
                    '#764ba2',
                    '#f093fb',
                    '#4facfe',
                    '#43e97b',
                    '#feca57'
                ]
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom'
                },
                tooltip: {
                    callbacks: {
                        label: context => {
                            const label = context.label || '';
                            const value = formatCurrency(context.parsed);
                            const percentage = categories[context.dataIndex].percentageOfTotal || 0;
                            return `${label}: ${value} (${percentage.toFixed(1)}%)`;
                        }
                    }
                }
            }
        }
    });
}

function updateStoreComparisonChart(stores) {
    const ctx = document.getElementById('storeComparisonChart');
    
    if (charts.storeComparison) {
        charts.storeComparison.destroy();
    }

    charts.storeComparison = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: stores.map(s => s.storeName),
            datasets: [{
                label: 'Revenue',
                data: stores.map(s => s.revenue),
                backgroundColor: '#667eea'
            }, {
                label: 'Transactions',
                data: stores.map(s => s.transactionCount),
                backgroundColor: '#764ba2',
                yAxisID: 'y1'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    type: 'linear',
                    display: true,
                    position: 'left',
                    ticks: {
                        callback: value => formatCurrency(value)
                    }
                },
                y1: {
                    type: 'linear',
                    display: true,
                    position: 'right',
                    grid: {
                        drawOnChartArea: false
                    }
                }
            }
        }
    });
}

// Table update functions
function updateProductsTable(products) {
    const tbody = document.querySelector('#productsTable tbody');
    tbody.innerHTML = '';

    products.forEach(product => {
        const row = tbody.insertRow();
        row.innerHTML = `
            <td>${product.itemName}</td>
            <td>${product.category}</td>
            <td>${formatNumber(product.quantitySold)}</td>
            <td>${formatCurrency(product.totalRevenue)}</td>
        `;
    });
}

function updateStoresTable(stores) {
    const tbody = document.querySelector('#storesTable tbody');
    tbody.innerHTML = '';

    stores.forEach(store => {
        const row = tbody.insertRow();
        const growthClass = (store.growthRate || 0) >= 0 ? 'positive' : 'negative';
        row.innerHTML = `
            <td>${store.rank || '-'}</td>
            <td>${store.storeName}</td>
            <td>${store.location}</td>
            <td>${formatCurrency(store.revenue)}</td>
            <td>${formatNumber(store.transactionCount)}</td>
            <td>${formatCurrency(store.averageTransactionValue)}</td>
            <td style="color: ${(store.growthRate || 0) >= 0 ? 'green' : 'red'}">
                ${store.growthRate ? store.growthRate.toFixed(1) + '%' : 'N/A'}
            </td>
        `;
    });
}

// Utility functions
function formatCurrency(value) {
    if (value === null || value === undefined) return '$0.00';
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD'
    }).format(value);
}

function formatNumber(value) {
    if (value === null || value === undefined) return '0';
    return new Intl.NumberFormat('en-US').format(value);
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

function showError(message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error';
    errorDiv.textContent = message;
    document.querySelector('.container').insertBefore(errorDiv, document.querySelector('.tabs'));
    
    setTimeout(() => errorDiv.remove(), 5000);
}
