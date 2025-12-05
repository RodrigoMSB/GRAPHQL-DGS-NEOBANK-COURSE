# CHANGELOG - Carbon Footprint Service

## [2.0.0] - 2024-12-05

### 🚀 Added
- **CarbonBreakdown type** - Detailed CO2 calculation breakdown
- **PeriodComparison type** - Compare sustainability between periods
- **MerchantCategory enum** - Type-safe merchant categorization
- **SchemaVersionInfo query** - Schema governance metadata
- **purchaseCarbonOffset mutation** - Enhanced offset purchase with detailed response
- **Trend enum** - IMPROVING, STABLE, WORSENING for period comparison

### ⚠️ Deprecated
- `Transaction.category` (String) → Use `merchantCategory` (enum) instead
  - **Removal planned:** v3.0.0 (Q2 2025)
  - **Reason:** Type safety and validation
  
- `Transaction.hasOffset` (Boolean) → Use `carbonFootprint.offsetPurchased` instead
  - **Removal planned:** v3.0.0
  - **Reason:** Better data organization
  
- `Mutation.buyOffset` → Use `purchaseCarbonOffset` instead
  - **Removal planned:** v3.0.0
  - **Reason:** Enhanced response with certificate ID and details

### 🔧 Migration Guide

#### Before (v1.x):
```graphql
{
  transaction(id: "123") {
    category  # String
    hasOffset # Boolean
  }
}

mutation {
  buyOffset(transactionId: "123")  # Returns Boolean
}
```

#### After (v2.0):
```graphql
{
  transaction(id: "123") {
    merchantCategory  # Enum (type-safe)
    carbonFootprint {
      offsetPurchased  # Organized under carbonFootprint
    }
  }
}

mutation {
  purchaseCarbonOffset(transactionId: "123") {
    success
    offsetCost
    certificateId
  }
}
```

---

## [1.0.0] - 2024-10-01

### 🚀 Initial Release
- Basic carbon footprint calculation
- Transaction tracking
- ESG scoring
- Sustainability reports
- Carbon alerts
