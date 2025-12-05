# Chapter 08: Carbon Footprint - Governance & Schema Evolution

## 🎯 Objetivo

Demostrar **gobernanza de schemas** en GraphQL:
- **Versionado semántico** (v2.0.0)
- **Deprecation de campos** sin romper clientes
- **Breaking changes** documentados
- **CHANGELOG** automático
- **Backward compatibility**

---

## 🚀 Ejecución

```bash
./mvnw spring-boot:run
```

**Abrir:** http://localhost:8080/graphiql

**Tests:**
```bash
chmod +x test-chapter08.sh
./test-chapter08.sh
```

---

## 📊 Conceptos de Gobernanza

### 1. Schema Versioning

**Versión actual:** 2.0.0

```graphql
{
  schemaVersion {
    version          # "2.0.0"
    lastUpdated      # "2024-12-05"
    deprecations {
      field
      reason
      removedInVersion
      alternative
    }
    breakingChanges
  }
}
```

### 2. Deprecation Strategy

**3 campos deprecados (remoción en v3.0.0):**

```graphql
type Transaction {
  # ❌ DEPRECATED
  category: String @deprecated(reason: "Use merchantCategory enum")
  hasOffset: Boolean @deprecated(reason: "Moved to carbonFootprint.offsetPurchased")
}

# ❌ DEPRECATED
mutation {
  buyOffset(transactionId: ID!): Boolean @deprecated(reason: "Use purchaseCarbonOffset")
}
```

**Migración:**
```graphql
# Antes (v1.x)
{ transaction { category hasOffset } }

# Ahora (v2.0+)
{ transaction { 
  merchantCategory 
  carbonFootprint { offsetPurchased } 
} }
```

### 3. Breaking Changes

**v2.0.0 introdujo:**
- ✅ `MerchantCategory` enum (type-safe)
- ✅ `CarbonBreakdown` type
- ✅ `PeriodComparison` type
- ✅ `SchemaVersionInfo` query

**Sin romper clientes existentes** - campos viejos siguen funcionando

---

## 📝 Queries de Ejemplo

### Schema Version Info
```graphql
{
  schemaVersion {
    version
    deprecations {
      field
      alternative
      removedInVersion
    }
  }
}
```

### Transactions con Carbon Footprint
```graphql
{
  transactions(accountId: "account-001") {
    merchantName
    merchantCategory  # Nuevo enum type-safe
    carbonFootprint {
      co2Kg
      impactLevel
      breakdown {
        transportationCO2
        productionCO2
      }
    }
  }
}
```

### Sustainability Report
```graphql
{
  sustainabilityReport(accountId: "account-001", year: 2024, month: 11) {
    totalCO2Kg
    totalTransactions
    recommendations
    highestImpactTransaction {
      merchantName
      carbonFootprint { co2Kg }
    }
  }
}
```

### Comparar Períodos
```graphql
{
  comparePeriods(
    accountId: "account-001"
    year1: 2024, month1: 11
    year2: 2024, month2: 12
  ) {
    co2Change
    percentageChange
    trend  # IMPROVING, STABLE, WORSENING
  }
}
```

### Crear Transaction (genera alert si CRITICAL)
```graphql
mutation {
  createTransaction(input: {
    accountId: "account-001"
    amount: 3000
    merchantName: "Lufthansa"
    merchantCategory: TRAVEL_AVIATION
    date: "2024-12-05"
  }) {
    success
    transaction {
      carbonFootprint {
        co2Kg
        impactLevel
      }
    }
    carbonAlert {  # Solo si impact = CRITICAL
      severity
      message
    }
  }
}
```

### Usar Endpoint Deprecated (genera WARNING)
```graphql
mutation {
  buyOffset(transactionId: "txn-xxx")  # ⚠️ DEPRECATED
}
```

**Log del servidor:**
```
WARN: DEPRECATED ENDPOINT USED: buyOffset - Use purchaseCarbonOffset instead
```

---

## 🔧 Governance Features

### CHANGELOG.md

Documenta todos los cambios:
- Added
- Deprecated
- Breaking Changes
- Migration Guides

### Schema Version Service

```java
@Service
public class SchemaVersionService {
    public Map<String, Object> getVersionInfo() {
        // Retorna version, deprecations, breaking changes
    }
}
```

### Deprecation Warnings

```java
@Deprecated
@DgsMutation
public Boolean buyOffset(@InputArgument String transactionId) {
    log.warn("⚠️ DEPRECATED ENDPOINT USED: buyOffset");
    // Still works for backward compatibility
}
```

---

## 📐 Carbon Footprint Calculation

### Factores de CO2 por Categoría

| Categoría | CO2 por $100 USD |
|-----------|------------------|
| TRAVEL_AVIATION | 120 kg |
| ENERGY | 45 kg |
| TRANSPORTATION | 35 kg |
| FASHION_RETAIL | 25 kg |
| FOOD_RETAIL | 8 kg |

### Impact Levels

| Level | CO2 Range |
|-------|-----------|
| LOW | < 5 kg |
| MEDIUM | 5-20 kg |
| HIGH | 20-50 kg |
| CRITICAL | > 50 kg |

---

## 🎓 Best Practices Demostradas

1. ✅ **Nunca remover campos** - Deprecar primero
2. ✅ **Avisar con tiempo** - 90 días antes de remover
3. ✅ **Documentar todo** - CHANGELOG + schema comments
4. ✅ **Proveer alternativas** - Siempre indicar el reemplazo
5. ✅ **Mantener backward compatibility** - Clientes viejos siguen funcionando
6. ✅ **Versionado semántico** - MAJOR.MINOR.PATCH

---

## 🔍 Verificar Governance

```bash
./test-chapter08.sh
```

**Verifica:**
- Schema version metadata
- Deprecation warnings en logs
- Backward compatibility
- Breaking changes documentados

---

**NeoBank - Chapter 08**  
*Schema Governance & Evolution*