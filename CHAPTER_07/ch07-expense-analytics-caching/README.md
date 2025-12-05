# Chapter 07: Expense Analytics - Caching & Performance

## 🎯 Objetivo

Implementar **caching strategies** en GraphQL:
- **Per-request caching** con DataLoader (elimina N+1)
- **Resolver-level caching** con Spring Cache (Caffeine)

---

## 🚀 Ejecución

```bash
./mvnw spring-boot:run
```

**Abrir:** http://localhost:8080/graphiql

**Tests:**
```bash
chmod +x test-chapter07.sh
./test-chapter07.sh
```

---

## 📊 Conceptos Clave

### 1. Per-Request Caching (DataLoader)

**Problema N+1:** Query pide 10 categorías → 10 llamadas DB

**Solución:** DataLoader → 1 batch + cache en memoria

```java
@DgsDataLoader(name = "categoryExpenses")
public class CategoryDataLoader implements BatchLoader<CategoryKey, List<Expense>> {
    
    @Override
    public CompletionStage<List<List<Expense>>> load(List<CategoryKey> keys) {
        // Batching: Procesa todas las keys juntas
        // Caching: Durante la misma petición HTTP
    }
}
```

### 2. Resolver-Level Caching (Spring Cache)

**Para queries costosas:**

```java
@Cacheable(value = "expenseSummary", key = "#accountId")
public ExpenseSummary calculateExpenseSummary(String accountId) {
    // Primera vez: CACHE MISS (~500ms)
    // Siguientes: CACHE HIT (<100ms)
}
```

**Invalidación:**
```java
@CacheEvict(value = "expenseSummary", key = "#accountId")
public void invalidateCacheForAccount(String accountId) {
    // Al crear/modificar expense
}
```

---

## 📝 Queries de Ejemplo

### Summary (CACHE TEST)
```graphql
{
  expenseSummary(accountId: "account-001") {
    totalAmount
    averageAmount
    topMerchants {
      merchantName
      totalSpent
    }
  }
}
```
**Ejecuta 2 veces:** Segunda es instantánea ⚡

### Monthly Analytics
```graphql
{
  monthlyAnalytics(accountId: "account-001", year: 2024, month: 11) {
    month
    totalSpent
    byCategory {
      category
      amount
      percentage
    }
  }
}
```

### Crear Expense (Invalida cache)
```graphql
mutation {
  createExpense(input: {
    accountId: "account-001"
    amount: 299.99
    merchantName: "Apple Store"
    category: SHOPPING
    date: "2024-12-05"
  }) {
    success
  }
}
```

---

## 🔧 Configuración Clave

```yaml
# application.yml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=300s

dgs:
  graphql:
    graphiql:
      enabled: true
      path: /graphiql  # ← Activa interfaz automática
```

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.netflix.graphql.dgs</groupId>
    <artifactId>graphql-dgs-spring-boot-starter</artifactId>
</dependency>
```

---

## ⚡ Performance

| Query | Sin Cache | Con Cache | Mejora |
|-------|-----------|-----------|--------|
| expenseSummary | ~500ms | <100ms | 5x |
| monthlyAnalytics | ~1000ms | <100ms | 10x |

---

## 🎓 Cuándo Usar

| Estrategia | Cuándo | TTL | Scope |
|------------|--------|-----|-------|
| **DataLoader** | Evitar N+1 | Request | Request |
| **Spring Cache** | Queries costosas | 5 min | App |

---

**NeoBank - Chapter 07**