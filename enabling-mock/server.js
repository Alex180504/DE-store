const express = require('express');
const bodyParser = require('body-parser');

const app = express();
const PORT = 8095;

app.use(bodyParser.json());

// Mock finance approval endpoint
app.post('/api/finance/approve', (req, res) => {
    const { customerId, firstName, lastName, phone, amount } = req.body;
    
    console.log(`Finance request received: ${firstName} ${lastName} (${customerId}) - £${amount}`);
    
    // 50/50 random approval
    const approved = Math.random() < 0.5;
    const reference = approved ? `EN${Date.now()}-${Math.floor(Math.random() * 10000)}` : null;
    
    const response = {
        approved,
        reference,
        customerId,
        amount,
        reason: approved ? 'Credit check passed' : 'Insufficient credit history',
        timestamp: new Date().toISOString()
    };
    
    console.log(`Response: ${approved ? 'APPROVED' : 'DENIED'} ${reference || ''}`);
    
    res.json(response);
});

// Health check
app.get('/health', (req, res) => {
    res.json({ status: 'UP', service: 'Enabling Finance Mock' });
});

app.listen(PORT, () => {
    console.log(`🏦 Enabling Finance Mock Service running on port ${PORT}`);
    console.log(`📊 Approval rate: 50/50 (random)`);
});
