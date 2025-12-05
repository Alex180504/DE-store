# Loyalty Service

## Overview

The Loyalty Service manages the DE-Store loyalty points program at the network level. It handles points earning rules, bonus offers, redemption discounts, and customer points balances.

## Features

### Points Earning
- **Product Points Rules**: Configure points earned per product (e.g., 5 points per cordless drill)
- **Bonus Offers**: Award extra points for spending thresholds (e.g., 50 points for £100+ purchases)
- **Store-Specific Offers**: Global or store-level bonus configurations
- **Historical Accuracy**: Rules have validity periods for accurate retroactive calculation

### Points Redemption
- **Discount Offers**: Use points for percentage discounts on items
- **Usage Limits**: Per-customer and total usage caps
- **Real-time Validation**: Check points balance and offer eligibility at checkout

### Points Calculation
- **Batch Updates**: Automated recalculation every 6 hours
- **Incremental Processing**: Only process new transactions since last update
- **On-Demand API**: Update all customers or single customer via REST endpoint
- **Historical Matching**: Match transaction timestamps with active rules at purchase time

## Database Schema

### Core Tables

#### `product_points_rules`
Defines points earned per product purchase.
- Supports points per unit OR points per £1 spent
- Validity periods with start/end dates
- Soft delete (archived, not removed)

####  `bonus_offers`
Additional points for spending thresholds.
- Global or store-specific
- Minimum spend requirements
- Bonus points awarded
- Validity periods

#### `redemption_offers`
Point-based discounts on items.
- Discount percentage (5-100%)
- Points cost to redeem
- Per-customer usage limits
- Total usage tracking

#### `customer_points_balance`
Current points balance per customer.
- Current balance (earned - redeemed)
- Lifetime earned/redeemed tracking
- Version number for optimistic locking
- Last update timestamp for incremental calculation

#### `points_transactions`
Complete audit trail of all points activity.
- Types: EARNED, REDEEMED, BONUS, ADJUSTMENT, EXPIRED, REVERSED
- Links to accounting transactions
- Links to redemption offers
- Balance snapshots (before/after)

#### `redemption_usage`
Tracks customer usage of redemption offers.
- Enforces per-customer usage limits
- Usage count and timestamps

### Views

- **`active_product_points`**: Currently active product points rules
- **`active_bonus_offers`**: Currently active bonus offers
- **`active_redemption_offers`**: Currently active redemption offers with availability
- **`customer_points_summary`**: Comprehensive customer loyalty status

## Architecture

### Database Connections
- **Loyalty DB** (read/write): Primary loyalty database
- **Accounting DB** (read-only): Source of purchase transactions for points calculation

### Integration Points
1. **Accounting Service**: Read transaction history for points calculation
2. **Shopping Service**: Provide pricing with loyalty discounts applied
3. **Auth Service**: Validate network manager JWT tokens for rule management

### SAGA Pattern for Basket Checkout

The loyalty redemption uses SAGA pattern to ensure consistency:

1. **Validate**: Check customer has sufficient points
2. **Reserve**: Lock points using optimistic locking (version column)
3. **Calculate**: Apply discounts to basket items
4. **Commit**: Deduct points and create transaction record
5. **Compensate**: Release reserved points if checkout fails

### Edge Case Handling

- **Concurrent Redemptions**: Optimistic locking with version column
- **Insufficient Points**: Validation before reservation
- **Expired Offers**: Check validity dates in real-time
- **Usage Limit Exceeded**: Check current usage vs limits
- **Rule Changes Mid-Checkout**: Use snapshot of rules at basket creation time

## REST API Endpoints

### Loyalty Rules Management (Network Managers Only)

```
POST   /api/loyalty/rules/product-points      - Create product points rule
GET    /api/loyalty/rules/product-points      - List all product points rules
PUT    /api/loyalty/rules/product-points/{id} - Update product points rule
DELETE /api/loyalty/rules/product-points/{id} - Deactivate product points rule (soft delete)

POST   /api/loyalty/rules/bonus-offers         - Create bonus offer
GET    /api/loyalty/rules/bonus-offers         - List all bonus offers
PUT    /api/loyalty/rules/bonus-offers/{id}    - Update bonus offer
DELETE /api/loyalty/rules/bonus-offers/{id}    - Deactivate bonus offer

POST   /api/loyalty/rules/redemption-offers    - Create redemption offer
GET    /api/loyalty/rules/redemption-offers    - List all redemption offers
PUT    /api/loyalty/rules/redemption-offers/{id} - Update redemption offer
DELETE /api/loyalty/rules/redemption-offers/{id} - Deactivate redemption offer
```

### Points Calculation

```
POST   /api/loyalty/points/calculate           - Calculate points for all customers
POST   /api/loyalty/points/calculate/{customerId} - Calculate points for specific customer
GET    /api/loyalty/points/balance/{customerId}   - Get customer points balance
GET    /api/loyalty/points/history/{customerId}   - Get customer points history
```

### Shopping Integration

```
GET    /api/loyalty/offers/active              - Get all active redemption offers
POST   /api/loyalty/basket/validate            - Validate basket with selected offers
POST   /api/loyalty/basket/checkout            - Apply loyalty discounts and deduct points
POST   /api/loyalty/basket/rollback            - Rollback points deduction (compensate)
```

## Scheduled Tasks

### Points Calculation Job
- **Schedule**: Every 6 hours (0:00, 6:00, 12:00, 18:00)
- **Cron Expression**: `0 0 */6 * * *`
- **Process**:
  1. Query all customers
  2. For each customer, find transactions since `last_calculated_at`
  3. Match transactions with rules active at transaction timestamp
  4. Calculate earned points (product points + bonus offers)
  5. Update `customer_points_balance`
  6. Create `points_transactions` audit records

### Optimization Strategies
- Batch processing with pagination (100 customers per batch)
- JDBC batch inserts for transaction records
- Cache active rules in memory
- Parallel processing for independent customer calculations

## Connection Details

- **Host**: `loyalty-db` (Docker) or `localhost:5436` (external)
- **Database**: `loyalty`
- **User**: `loyalty_user`
- **Password**: `loyalty_pass`

## Sample Data

The database is pre-populated with:
- **18 product points rules** covering all warehouse items
- **6 bonus offers** (4 global, 2 store-specific)
- **11 redemption offers** covering power tools, paint, garden equipment
- **20 customer balance records** (initialized to 0, will be calculated)

### Example Rules

**Product Points**:
- Cordless Drill: 5 points per unit
- Lawn Mower: 10 points per unit
- Paint: 0.5 points per £1 spent

**Bonus Offers**:
- £50+: 20 bonus points
- £100+: 50 bonus points
- £500+: 300 bonus points

**Redemption Offers**:
- 10% off Cordless Drill for 50 points
- 25% off Lawn Mower for 200 points
- 20% off Paint for 30 points

## Implementation Notes

### Rule Archival Strategy
Rules are never deleted from the database. Instead:
- `is_active` set to FALSE
- `deactivated_at` timestamp recorded
- `valid_to` set to deactivation date
- Historical calculations remain accurate

### Incremental Calculation
Each customer balance tracks:
- `last_calculated_at`: Timestamp of last points calculation
- `last_transaction_processed_id`: Last accounting transaction processed

This enables efficient incremental updates without reprocessing entire transaction history.

### Concurrency Control
Optimistic locking prevents double-spending:
```sql
UPDATE customer_points_balance 
SET current_balance = current_balance - :points,
    version = version + 1
WHERE customer_id = :customerId 
  AND version = :expectedVersion
  AND current_balance >= :points
```

If version doesn't match or balance insufficient, update fails and transaction rolls back.

## Future Enhancements

- Points expiration (e.g., expire after 12 months)
- Tiered loyalty levels (Bronze, Silver, Gold, Platinum)
- Birthday bonus points
- Referral bonuses
- Points transfer between customers
- Loyalty card physical/digital integration
- Machine learning for personalized offers
- Real-time points balance webhooks

## Testing

Use the shopping-mock-service to test:
1. View active offers
2. Create a basket with items
3. Select loyalty offers to apply
4. Submit for checkout
5. Verify points deducted and prices calculated correctly

Monitor points_transactions table for audit trail.
