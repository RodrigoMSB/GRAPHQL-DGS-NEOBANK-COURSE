# TEORIA - Chapter 08: Gobernanza y Evolución de Schemas

## 📚 Índice

1. El Problema de Evolución de Schemas
2. Versionado Semántico en GraphQL
3. Estrategias de Deprecation
4. Breaking Changes vs Additive Changes
5. Federation y Gobernanza Distribuida
6. Tooling para Governance

---

## 1. El Problema de Evolución de Schemas

### El Desafío

En una organización real:
- **100+ desarrolladores** modificando el schema
- **50+ aplicaciones cliente** consumiendo el API
- **Millones de requests/día** en producción

**¿Cómo evolucionar sin romper nada?** 🤔

### Ejemplo Real: Refactoring Doloroso

**Escenario:** Queremos cambiar `category: String` → `merchantCategory: Enum`

**Enfoque MALO (breaking change):**
```graphql
# v1.0 → v2.0
type Transaction {
  # category: String  ← REMOVIDO
  merchantCategory: MerchantCategory!  # NUEVO
}
```

**Resultado:**
```
💥 200 aplicaciones rotas
💥 Millones de requests fallando
💥 Incidentes de producción
💥 Clientes enojados
```

**Enfoque BUENO (deprecation):**
```graphql
# v2.0
type Transaction {
  category: String @deprecated(reason: "Use merchantCategory")
  merchantCategory: MerchantCategory!
}
```

**Resultado:**
```
✅ Clientes viejos siguen funcionando
✅ Nuevos clientes usan el mejor campo
✅ Tiempo para migrar (90 días)
✅ Sin incidentes
```

---

## 2. Versionado Semántico en GraphQL

### MAJOR.MINOR.PATCH

**GraphQL NO usa /v1, /v2 en URLs** ❌

En REST:
```
/api/v1/users
/api/v2/users  ← Nueva versión, diferentes endpoints
```

En GraphQL:
```
/graphql  ← UN SOLO endpoint, evoluciona internamente
```

### Semantic Versioning

```
v2.1.3
│ │ │
│ │ └─ PATCH: Bug fixes, no cambios de schema
│ └─── MINOR: Nuevos campos/tipos (additive, no breaking)
└───── MAJOR: Breaking changes (remover campos deprecados)
```

**Ejemplos:**

| Cambio | Versión |
|--------|---------|
| Agregar campo nuevo | v2.1.0 → v2.2.0 (MINOR) |
| Deprecar campo | v2.2.0 → v2.3.0 (MINOR) |
| Remover campo deprecado | v2.3.0 → v3.0.0 (MAJOR) |
| Fix en resolver | v2.3.0 → v2.3.1 (PATCH) |

---

## 3. Estrategias de Deprecation

### @deprecated Directive

```graphql
type Transaction {
  """
  @deprecated Use merchantCategory instead
  Will be removed in v3.0.0 (Q2 2025)
  """
  category: String @deprecated(
    reason: "Use merchantCategory enum for type safety. Removal planned: v3.0.0"
  )
  
  merchantCategory: MerchantCategory!
}
```

### Timeline de Deprecation

```
┌─────────────────────────────────────────────────────────┐
│ v2.0.0          v2.1.0          v2.2.0          v3.0.0  │
│ Dec 2024        Jan 2025        Feb 2025        Mar 2025│
├─────────────────────────────────────────────────────────┤
│ Deprecar   →    Avisos      →   Último aviso → REMOVER │
│ campo           en logs          grave                  │
└─────────────────────────────────────────────────────────┘
     ↓                ↓                ↓              ↓
  90 días        60 días          30 días          0 días
```

**Best Practice:** Mínimo 90 días antes de remover

### Comunicación Interna

```yaml
# PR de Schema Change
Title: "[DEPRECATION] Transaction.category → merchantCategory"

Body:
- What: Deprecating Transaction.category field
- Why: Type safety with enum
- When: Removal in v3.0.0 (March 2025)
- Migration: Use merchantCategory instead
- Impact: 45 known clients (notified via email)
```

### Logs de Deprecation

```java
@Deprecated
@DgsMutation
public Boolean buyOffset(@InputArgument String transactionId) {
    log.warn("⚠️ DEPRECATED ENDPOINT USED: buyOffset");
    log.warn("   Client: {}", getClientInfo());
    log.warn("   Use: purchaseCarbonOffset instead");
    log.warn("   Removal: v3.0.0 (Q2 2025)");
    
    // Analytics para saber quién usa el endpoint viejo
    metricsService.recordDeprecatedUsage("buyOffset", getClientInfo());
    
    return transactionService.purchaseOffset(transactionId);
}
```

---

## 4. Breaking Changes vs Additive Changes

### Additive Changes (Safe) ✅

**Agregar campos nuevos:**
```graphql
type Transaction {
  id: ID!
  amount: Float!
  # NUEVO en v2.1.0
  esgScore: ESGScore  # ← Safe, clientes viejos lo ignoran
}
```

**Agregar tipos nuevos:**
```graphql
# NUEVO en v2.0.0
type CarbonBreakdown {
  transportationCO2: Float!
  productionCO2: Float!
}
```

**Agregar queries/mutations:**
```graphql
type Query {
  transactions: [Transaction!]!
  # NUEVO en v2.2.0
  sustainabilityReport: Report  # ← Safe
}
```

### Breaking Changes (Dangerous) ❌

**Remover campos:**
```graphql
type Transaction {
  # category: String  ← BREAKING! Clientes esperan esto
  merchantCategory: MerchantCategory!
}
```

**Cambiar tipos:**
```graphql
type Transaction {
  # amount: Float!  ← BREAKING!
  amount: Int!  # Cambió de Float a Int
}
```

**Hacer campos non-nullable:**
```graphql
type Transaction {
  # esgScore: ESGScore  ← BREAKING!
  esgScore: ESGScore!  # Ahora es requerido
}
```

### Migration Path

**NUNCA hacer esto:**
```
v2.0 → v3.0 (breaking change inmediato) ❌
```

**SIEMPRE hacer esto:**
```
v2.0 → v2.1 (deprecar) → v2.2 (avisos) → v3.0 (remover) ✅
        90 días          60 días          30 días
```

---

## 5. Federation y Gobernanza Distribuida

### El Problema de Múltiples Equipos

**Escenario real:**
```
NeoBank tiene 8 equipos:
- Team Accounts    → Subgraph: accounts-service
- Team Payments    → Subgraph: payments-service
- Team Carbon      → Subgraph: carbon-service
- Team Fraud       → Subgraph: fraud-service
...
```

**Cada equipo es dueño de su subgraph, pero todos comparten el supergraph**

### Contratos Compartidos

**Problema:** ¿Quién define el tipo `User`?

```graphql
# Team Accounts
type User @key(fields: "id") {
  id: ID!
  email: String!
}

# Team Carbon (extiende User)
extend type User @key(fields: "id") {
  id: ID! @external
  carbonFootprint: CarbonFootprint!
}
```

### Gobernanza de Entities

**Rules:**
1. **Owner único** - Un equipo es dueño de cada entity
2. **Extensiones permitidas** - Otros equipos pueden extender
3. **Schema registry** - Cambios validados antes de merge
4. **Composition checks** - Supergraph no puede romperse

### Ejemplo: Change Review Process

```yaml
# carbon-service PR #123
changes:
  - added: Transaction.carbonBreakdown
  - deprecated: Transaction.hasOffset

checks:
  - schema_composition: ✅ PASS
  - breaking_changes: ⚠️ WARNING (deprecation)
  - dependent_services: 
      - accounts-service: ✅ Compatible
      - payments-service: ✅ Compatible
  - approval_required: true (deprecation)

reviewers:
  - @carbon-team-lead
  - @platform-governance
```

---

## 6. Tooling para Governance

### Schema Registry (Apollo Studio / GraphOS)

**Función:** Centro de control para todos los schemas

```bash
# Publicar nuevo schema
rover subgraph publish my-graph@prod \
  --schema ./schema.graphqls \
  --name carbon-service

# Validar cambios
rover subgraph check my-graph@prod \
  --schema ./schema.graphqls \
  --name carbon-service
```

**Output:**
```
✅ No breaking changes detected
⚠️  1 deprecation added: Transaction.category
📊 Estimated impact: 45 operations (12 clients)
```

### CI/CD Integration

```yaml
# .github/workflows/schema-check.yml
name: Schema Governance
on: [pull_request]

jobs:
  schema-check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Schema Validation
        run: |
          rover subgraph check prod \
            --schema ./schema.graphqls
      
      - name: Breaking Change Detection
        run: |
          if [[ $BREAKING_CHANGES == "true" ]]; then
            echo "❌ Breaking changes require approval"
            exit 1
          fi
```

### Automatic CHANGELOG

```javascript
// generate-changelog.js
const schema = loadSchema('./schema.graphqls');
const previous = loadSchema('./previous-schema.graphqls');

const changes = diffSchemas(previous, schema);

changes.forEach(change => {
  if (change.type === 'DEPRECATION') {
    changelog.add({
      version: '2.1.0',
      type: 'Deprecated',
      description: change.description,
      migrationGuide: change.alternative
    });
  }
});
```

### Deprecation Dashboard

```
┌─────────────────────────────────────────────┐
│ DEPRECATED ENDPOINTS - Usage Last 30 Days  │
├─────────────────────────────────────────────┤
│ buyOffset                    1.2M requests  │
│   Clients: mobile-app-v2, web-dashboard    │
│   Removal: v3.0.0 (45 days)                │
│   Status: ⚠️ HIGH USAGE                     │
│                                             │
│ Transaction.category         850K requests  │
│   Clients: analytics-service               │
│   Removal: v3.0.0 (45 days)                │
│   Status: ⚠️ MEDIUM USAGE                   │
└─────────────────────────────────────────────┘
```

---

## 📊 Caso Real: NeoBank Carbon Service

### Timeline de Evolución

**v1.0.0 (Oct 2024)** - MVP
```graphql
type Transaction {
  category: String
  hasOffset: Boolean
}
```

**v2.0.0 (Dec 2024)** - Mejoras + Deprecations
```graphql
type Transaction {
  category: String @deprecated
  hasOffset: Boolean @deprecated
  merchantCategory: MerchantCategory!  # Type-safe
  carbonFootprint: CarbonFootprint!    # Mejor organización
}
```

**v3.0.0 (Mar 2025)** - Cleanup
```graphql
type Transaction {
  # category REMOVED
  # hasOffset REMOVED
  merchantCategory: MerchantCategory!
  carbonFootprint: CarbonFootprint!
}
```

### Métricas de Éxito

```
Deprecation de Transaction.category:
- Anunciado: Dec 1, 2024
- Emails enviados: 45 equipos
- Migraciones completadas: 43/45 (95%)
- Deadline: Mar 1, 2025
- Incidentes: 0
```

---

## 🎓 Best Practices Summary

1. ✅ **Nunca breaking changes sin avisar** - Mínimo 90 días
2. ✅ **Documentar TODO** - CHANGELOG + deprecation reasons
3. ✅ **Monitorear uso** - Saber quién usa qué
4. ✅ **Automatizar checks** - CI/CD valida schemas
5. ✅ **Schema registry** - Source of truth centralizado
6. ✅ **Comunicación clara** - PRs, emails, dashboards

---

**NeoBank - Carbon Footprint Service**  
*Chapter 08 - Schema Governance*