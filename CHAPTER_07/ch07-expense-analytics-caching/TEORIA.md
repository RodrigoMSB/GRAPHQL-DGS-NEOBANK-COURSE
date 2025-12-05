# TEORIA - Chapter 07: Caching y Performance en GraphQL

## 📚 Índice

1. ¿Por qué Caching en GraphQL?
2. Per-Request Caching (DataLoader)
3. Resolver-Level Caching (Spring Cache)
4. Estrategias de Invalidación
5. Antipatrones y Mejores Prácticas

---

## 1. ¿Por qué Caching en GraphQL?

### El Problema

GraphQL permite queries flexibles, pero esto puede generar problemas de performance:

```graphql
query {
  expenses {
    id
    category {        # ← Request 1 a DB
      name
    }
  }
}
```

**Problema N+1:**
- 1 query para expenses
- N queries para categorías (una por expense)
- Total: 1 + N llamadas a DB

### La Solución: Caching

**2 niveles de caching:**

1. **Per-Request** (DataLoader) - Durante la misma petición HTTP
2. **Application-Level** (Spring Cache) - Entre peticiones, con TTL

---

## 2. Per-Request Caching (DataLoader)

### Concepto

**DataLoader** resuelve 2 problemas:

1. **Batching**: Agrupa múltiples requests en uno solo
2. **Caching**: Reutiliza datos durante la misma petición

### Ejemplo Sin DataLoader

```graphql
query {
  expense1: expense(id: "1") { category { name } }  # DB call 1
  expense2: expense(id: "2") { category { name } }  # DB call 2
  expense3: expense(id: "3") { category { name } }  # DB call 3
}
```

**3 llamadas a DB** ❌

### Ejemplo Con DataLoader

```graphql
query {
  expense1: expense(id: "1") { category { name } }
  expense2: expense(id: "2") { category { name } }
  expense3: expense(id: "3") { category { name } }
}
```

DataLoader:
1. **Agrupa** los 3 IDs: `[1, 2, 3]`
2. **1 llamada batch** a DB
3. **Cache** en memoria durante la petición

**1 llamada a DB** ✅

### Implementación

```java
@DgsDataLoader(name = "categoryExpenses")
public class CategoryDataLoader implements BatchLoader<CategoryKey, List<Expense>> {
    
    @Override
    public CompletionStage<List<List<Expense>>> load(List<CategoryKey> keys) {
        log.info("BATCH: Loading {} categories", keys.size());
        
        // Procesar TODAS las keys en una sola llamada
        List<List<Expense>> results = new ArrayList<>();
        
        for (CategoryKey key : keys) {
            results.add(expenseService.getByCategory(key));
        }
        
        return CompletableFuture.completedFuture(results);
    }
}
```

**Uso en Resolver:**

```java
@DgsQuery
public CompletableFuture<List<Expense>> expensesByCategory(
        @InputArgument String accountId,
        @InputArgument Category category,
        DgsDataFetchingEnvironment dfe) {
    
    DataLoader<CategoryKey, List<Expense>> loader = 
        dfe.getDataLoader("categoryExpenses");
    
    return loader.load(new CategoryKey(accountId, category));
}
```

### Ventajas

✅ Elimina problema N+1  
✅ Reduce llamadas a DB  
✅ Cache automático durante request  
✅ Transparente para el cliente

---

## 3. Resolver-Level Caching (Spring Cache)

### Concepto

Cachear resultados de **queries costosas** entre peticiones HTTP.

**Ejemplo:** Calcular analytics mensuales (agregaciones complejas)

### Sin Cache

```
Request 1: monthlyAnalytics → Cálculo costoso (1 segundo)
Request 2: monthlyAnalytics → Cálculo costoso (1 segundo) ❌
Request 3: monthlyAnalytics → Cálculo costoso (1 segundo) ❌
```

**3 segundos totales** para la misma data ❌

### Con Cache

```
Request 1: monthlyAnalytics → Cálculo (1 seg) → CACHE STORE
Request 2: monthlyAnalytics → CACHE HIT (<10ms) ✅
Request 3: monthlyAnalytics → CACHE HIT (<10ms) ✅
```

**Mejora: 100x más rápido** ✅

### Implementación

```java
@Service
public class AnalyticsService {
    
    @Cacheable(value = "monthlyAnalytics", key = "#accountId + '_' + #year + '_' + #month")
    public MonthlyAnalytics calculateMonthlyAnalytics(
            String accountId, int year, int month) {
        
        log.info("CACHE MISS - Calculating analytics");
        
        // Cálculo costoso
        Thread.sleep(1000); // Simula procesamiento pesado
        
        // ... agregaciones, cálculos ...
        
        return analytics;
    }
}
```

### TTL (Time To Live)

```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=300s  # 5 minutos
```

**Después de 5 minutos:** Cache expira automáticamente

---

## 4. Estrategias de Invalidación

### ¿Cuándo invalidar cache?

Cuando los datos **cambian**:
- Usuario crea expense → Invalidar summary
- Usuario edita expense → Invalidar analytics
- Operación batch → Invalidar todo

### Invalidación Manual

```java
@Service
public class AnalyticsService {
    
    @CacheEvict(value = {
        "expenseSummary",
        "monthlyAnalytics",
        "topMerchants"
    }, key = "#accountId")
    public void invalidateCacheForAccount(String accountId) {
        log.warn("CACHE INVALIDATED for account: {}", accountId);
    }
}
```

### Invalidación Automática en Mutation

```java
@DgsMutation
public ExpenseResponse createExpense(@InputArgument ExpenseInput input) {
    
    // 1. Crear expense
    Expense expense = expenseService.create(input);
    
    // 2. Invalidar cache automáticamente
    analyticsService.invalidateCacheForAccount(input.getAccountId());
    
    return new ExpenseResponse(true, "Created", expense);
}
```

### Estrategias de Invalidación

| Estrategia | Cuándo Usar | Trade-off |
|------------|-------------|-----------|
| **Time-based** (TTL) | Datos que cambian poco | Simple, puede quedar stale |
| **Event-based** | Cambios conocidos | Preciso, requiere lógica |
| **Manual** | Cache crítico | Control total, más código |

---

## 5. Antipatrones y Mejores Prácticas

### ❌ Antipatrón 1: Cachear TODO

```java
// MAL
@Cacheable("everything")
public String getTimestamp() {
    return LocalDateTime.now().toString();  // ← Cambia constantemente
}
```

**Problema:** Datos que cambian constantemente no deben cachearse

### ❌ Antipatrón 2: TTL Muy Largo

```yaml
# MAL
caffeine:
  spec: expireAfterWrite=86400s  # 24 horas
```

**Problema:** Datos stale por mucho tiempo

### ❌ Antipatrón 3: No Invalidar Cache

```java
@DgsMutation
public Expense createExpense(ExpenseInput input) {
    return expenseService.create(input);
    // ← FALTA invalidar cache
}
```

**Problema:** Summary muestra datos viejos

### ✅ Mejores Prácticas

#### 1. Cachear Solo Queries Costosas

```java
// SÍ: Cálculo costoso (agregaciones, joins)
@Cacheable("monthlyAnalytics")
public MonthlyAnalytics calculate(...) { }

// NO: Query simple (ya es rápida)
@Cacheable("expense")  // ← Innecesario
public Expense getById(String id) { }
```

#### 2. TTL Apropiado

```
Datos en tiempo real (stocks):     TTL = 1-5 seg
Datos frecuentes (analytics):      TTL = 5-15 min
Datos estables (configuración):    TTL = 1-24 horas
```

#### 3. Logging de Cache

```java
@Cacheable("expenseSummary")
public ExpenseSummary calculate(String accountId) {
    log.info("🔄 CACHE MISS - accountId: {}", accountId);
    // ... cálculo ...
    log.info("✅ CACHE STORED");
    return summary;
}
```

**Monitorear:**
- % de cache hits vs misses
- Latencia con/sin cache
- Tamaño de cache

#### 4. Cache Keys Únicos

```java
// MAL: Key no única
@Cacheable(value = "summary", key = "#accountId")
public Summary getByMonth(String accountId, int month) { }
// ← Sobrescribe entre meses

// BIEN: Key compuesta
@Cacheable(value = "summary", key = "#accountId + '_' + #month")
public Summary getByMonth(String accountId, int month) { }
```

---

## 📊 Comparación Final

| Aspecto | DataLoader | Spring Cache |
|---------|-----------|--------------|
| **Scope** | Request único | Aplicación |
| **TTL** | Duración del request | Configurable (5 min) |
| **Uso** | Evitar N+1 | Queries costosas |
| **Invalidación** | Automática | Manual/Event-based |
| **Overhead** | Bajo | Medio |

---

## 🎓 Cuándo Usar Cada Uno

### Usa DataLoader cuando:
- Tienes problema N+1
- Necesitas batching
- Cache solo durante el request

### Usa Spring Cache cuando:
- Query tarda >500ms
- Datos cambian poco
- Múltiples usuarios piden lo mismo

### Usa Ambos cuando:
- Queries costosas CON N+1
- Máxima optimización

---

## 🔍 Caso Real: Expense Analytics

**Sin optimización:**
```
Query monthlyAnalytics:
- 1 query para expenses del mes
- N queries para categorías (N+1)
- Agregaciones en memoria
Total: ~2 segundos ❌
```

**Con DataLoader:**
```
- 1 query batch para expenses
- 1 query batch para categorías
- Agregaciones en memoria
Total: ~1 segundo (50% mejor) ✅
```

**Con DataLoader + Spring Cache:**
```
Primera vez: ~1 segundo (CACHE MISS)
Siguientes: <100ms (CACHE HIT)
Total: 10x más rápido ✅✅
```

---

**NeoBank - Expense Analytics**  
*Chapter 07 - Caching Strategies*