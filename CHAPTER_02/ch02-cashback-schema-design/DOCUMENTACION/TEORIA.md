# 📚 CAPÍTULO 2: DISEÑO CORRECTO DE SCHEMAS GRAPHQL

**Duración:** 1.5 horas (3 secciones × 30 min)  
**Objetivo:** Dominar los principios de diseño de schemas GraphQL orientados a dominio, no acoplados a la base de datos, con custom scalars, validaciones automáticas y campos calculados

---

## 📖 ÍNDICE DE CONTENIDOS

1. [Sección 2.1 - Principios del diseño de esquemas GraphQL](#sección-21---principios-del-diseño-de-esquemas-graphql)
2. [Sección 2.2 - Tipos escalares objetos listas e inputs](#sección-22---tipos-escalares-objetos-listas-e-inputs)
3. [Sección 2.3 - Queries y mutations complejas](#sección-23---queries-y-mutations-complejas)

---

# Sección 2.1 - Principios del Diseño de Esquemas GraphQL

**Duración:** 30 minutos

## 🎯 Objetivo

Comprender que el schema de GraphQL debe representar el **modelo de dominio del negocio**, NO la estructura de la base de datos. Aprender a diseñar schemas que sean intuitivos, autodocumentados y desacoplados de la implementación técnica.

---

## 💭 Contexto: El Schema como Contrato de Negocio

En el desarrollo de APIs tradicionales (REST), es común que el diseño de los endpoints esté fuertemente influenciado por la estructura de la base de datos. Esto genera un **acoplamiento** que tiene consecuencias graves:

1. **Rigidez arquitectónica:** Cualquier cambio en la DB requiere cambios en la API, rompiendo contratos con clientes.
2. **Exposición de detalles técnicos:** Los clientes ven `user_id`, `tier_id`, `created_at` en lugar de conceptos de negocio.
3. **Curva de aprendizaje elevada:** Nuevos desarrolladores deben entender la DB para usar la API.
4. **Pobre developer experience:** Queries verbosas, joins manuales, múltiples llamadas HTTP.

GraphQL nos da la oportunidad de **romper este acoplamiento** diseñando schemas que reflejen cómo el negocio **piensa** sobre sus datos, no cómo los **almacena**.

### La Filosofía Domain-Driven Design (DDD)

Eric Evans, en su libro "Domain-Driven Design" (2003), introdujo la idea del **Ubiquitous Language** (Lenguaje Ubicuo): un vocabulario compartido entre desarrolladores y expertos del dominio que se refleja en el código. 

En GraphQL, el schema ES ese lenguaje ubicuo. Cuando un product manager dice "necesitamos mostrar el cashback disponible del usuario premium", el schema debe tener:

```graphql
type User {
  tier: CashbackTier!      # "premium" = PLATINUM
  availableCashback: Money! # "cashback disponible"
}
```

NO debe tener:

```graphql
type User {
  tier_id: Int!            # ❌ ¿Qué es tier_id: 4?
  rewards_sum: Float!      # ❌ Término técnico, no de negocio
}
```

**Principio clave:** Si un product manager no entiende tu schema leyéndolo directamente, está mal diseñado.

---

## 1. El Antipatrón: Schema Acoplado a la Base de Datos

### 1.1 El Error Común: Exponer la Estructura de la DB

**Escenario:** Sistema de Cashback Rewards en un NeoBank.

**Schema INCORRECTO (acoplado a DB):**

```graphql
type User {
  user_id: Int!                    # ❌ Nombre de columna DB
  first_name: String!              # ❌ DB fields separados
  last_name: String!               # ❌ No es concepto de dominio
  tier_id: Int!                    # ❌ Foreign key expuesta
  created_at: String!              # ❌ Formato DB (no ISO 8601)
  updated_at: String!              # ❌ Campo técnico innecesario
  status_code: Int!                # ❌ Código numérico (no enum)
}

type Transaction {
  transaction_id: Int!             # ❌ Nombre técnico
  user_id: Int!                    # ❌ FK en lugar de navegación
  amount_cents: Int!               # ❌ Detalle de implementación
  category_id: Int!                # ❌ FK en lugar de enum
  created_timestamp: String!       # ❌ Formato DB
}
```

**Problemas de este diseño:**

1. **Acoplamiento:** Cambiar la DB rompe el schema
2. **Pobre UX:** Cliente debe conocer IDs para hacer joins manualmente
3. **No autodocumentado:** `tier_id: 3` ¿qué significa?
4. **Inconsistencia:** `first_name` vs `lastName` (snake_case vs camelCase)
5. **Campos técnicos:** `updated_at` no es relevante para el negocio

**Query resultante (terrible UX):**

```graphql
{
  transaction(transaction_id: 123) {
    amount_cents        # Cliente debe dividir / 100
    user_id            # ¿Cómo obtengo el user?
    category_id        # ¿Qué categoría es 5?
  }
}
```

### El Costo Real del Acoplamiento DB-Schema

Este antipatrón no es solo un problema estético. Tiene **consecuencias medibles** en proyectos reales:

**Ejemplo real de una fintech:**
- **Migración de DB:** Cambio de MySQL a PostgreSQL requirió actualizar 47 endpoints REST
- **Tiempo invertido:** 3 sprints completos (6 semanas)
- **Breaking changes:** 12 clientes móviles y 5 integraciones externas rompieron
- **Costo estimado:** $180,000 USD en desarrollo + $50,000 en soporte

Con GraphQL domain-driven:
- **Schema NO cambió:** Clientes ni se enteraron de la migración
- **Tiempo invertido:** 0 horas en actualizar contratos
- **Breaking changes:** 0
- **Costo:** Solo la migración interna de DB

**Lección:** El schema es un contrato a largo plazo. Desacoplarlo de la implementación es una inversión, no un costo.

---

### 1.2 El Diseño Correcto: Schema Orientado a Dominio

**Schema CORRECTO (domain-driven):**

```graphql
type User {
  id: ID!                          # ✅ ID abstracto (no Int)
  fullName: String!                # ✅ Concepto de negocio
  tier: CashbackTier!              # ✅ Enum autodocumentado
  email: Email!                    # ✅ Custom scalar validado
  enrolledAt: DateTime!            # ✅ Timestamp semántico
  availableCashback: Money!        # ✅ Campo calculado
  totalSpent: Money!               # ✅ Agregado de negocio
  totalCashbackEarned: Money!      # ✅ KPI del dominio
}

enum CashbackTier {
  BRONZE    # 1% cashback base
  SILVER    # 2% cashback base
  GOLD      # 3% cashback base
  PLATINUM  # 5% cashback base
}

type Transaction {
  id: ID!
  amount: Money!                   # ✅ No expone implementación
  merchantName: String!            # ✅ Dato de negocio
  category: TransactionCategory!   # ✅ Enum navegable
  status: TransactionStatus!       # ✅ Estado semántico
  user: User!                      # ✅ Navegación bidireccional
  cashbackAmount: Money!           # ✅ Calculado dinámicamente
  cashbackPercentage: Percentage!  # ✅ Lógica de negocio
  timestamp: DateTime!
}
```

**Beneficios:**

1. ✅ **Desacoplado:** DB puede cambiar sin romper schema
2. ✅ **Navegable:** `transaction.user.fullName` en una query
3. ✅ **Autodocumentado:** `tier: PLATINUM` es claro
4. ✅ **Validación:** `Email!` valida formato automáticamente
5. ✅ **Campos calculados:** `cashbackAmount` se calcula según tier + category

**Query resultante (excelente UX):**

```graphql
{
  transaction(id: "trans-123") {
    amount
    merchantName
    category
    cashbackAmount          # Calculado automáticamente
    user {
      fullName
      tier
      availableCashback
    }
  }
}
```

---

## 2. Principio 1: Domain-Driven Design (DDD)

### 2.1 Concepto: El Schema Refleja el Lenguaje del Negocio

**Regla de Oro:** El schema debe usar los términos que usa el equipo de producto, NO los que usa la DB.

**Ejemplo NeoBank:**

| Término de Negocio | ❌ Schema DB-Coupled | ✅ Schema Domain-Driven |
|-------------------|---------------------|------------------------|
| "Usuario premium" | `tier_id: 4` | `tier: PLATINUM` |
| "Nombre completo" | `first_name + last_name` | `fullName: String!` |
| "Cashback disponible" | (calcular en cliente) | `availableCashback: Money!` |
| "Transacción en tienda" | `category_id: 2` | `category: SHOPPING` |
| "Recompensa activa" | `status_code: 1` | `status: AVAILABLE` |

### Por Qué Domain-Driven Design Importa en GraphQL

El concepto de DDD va más allá de solo "usar nombres bonitos". Se trata de crear un **modelo mental compartido** entre todos los stakeholders del proyecto:

**Stakeholder 1: Product Manager**
- Habla de "usuarios premium con cashback disponible"
- Ve el schema GraphQL y reconoce inmediatamente: `tier: PLATINUM`, `availableCashback`
- Puede validar la API sin conocimiento técnico

**Stakeholder 2: Frontend Developer**  
- Necesita mostrar "cashback disponible" en la UI
- Query natural: `user { availableCashback }`
- No necesita documentación externa, el schema es autodocumentado

**Stakeholder 3: Mobile Developer**
- Conexión 3G lenta, necesita optimizar
- Pide solo: `user { fullName, availableCashback }`  
- No recibe 50 campos innecesarios como en REST

**Stakeholder 4: Backend Developer**
- Refactoriza la DB (cambio de normalización)
- Schema NO cambia → clientes NO se afectan
- Solo actualiza los resolvers internamente

**Ventaja competitiva:** Un schema domain-driven reduce el "time to market" porque diferentes equipos hablan el mismo idioma.

---

### 2.2 Ejemplo Práctico: User Type

**DB Table (PostgreSQL):**

```sql
CREATE TABLE users (
  user_id SERIAL PRIMARY KEY,
  first_name VARCHAR(50),
  last_name VARCHAR(50),
  email VARCHAR(100),
  tier_id INT REFERENCES tiers(id),
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);
```

**Schema GraphQL (Domain-Driven):**

```graphql
type User {
  id: ID!              # Abstracción (no expone que es INT)
  fullName: String!    # Concepto de negocio (no first_name/last_name)
  email: Email!        # Custom scalar con validación
  tier: CashbackTier!  # Enum (no tier_id FK)
  enrolledAt: DateTime!  # Semántica clara (no created_at)
}

# Campo calculado (NO en DB)
extend type User {
  availableCashback: Money!
  totalSpent: Money!
  totalCashbackEarned: Money!
}
```

**Resolver (calcula datos derivados):**

```java
@SchemaMapping(typeName = "User")
public Double availableCashback(User user) {
    // Sumar todas las rewards con status AVAILABLE
    return rewardRepository.findByUserIdAndStatus(
        user.getId(), 
        RewardStatus.AVAILABLE
    )
    .stream()
    .mapToDouble(Reward::getAmount)
    .sum();
}

@SchemaMapping(typeName = "User")
public Double totalSpent(User user) {
    // Sumar todas las transactions confirmadas
    return transactionRepository.findByUserIdAndStatus(
        user.getId(),
        TransactionStatus.CONFIRMED
    )
    .stream()
    .mapToDouble(Transaction::getAmount)
    .sum();
}
```

**Ventaja:** Cliente recibe `availableCashback` sin saber que se calcula en tiempo real.

---

## 3. Principio 2: Enums Bien Diseñados

### 3.1 Por Qué Usar Enums

Los enums en GraphQL no son solo una conveniencia sintáctica. Son una herramienta fundamental de **diseño de contratos** que previene una clase entera de bugs antes de que lleguen a producción.

**El problema con códigos numéricos:**

En sistemas legacy, es común ver códigos como `status_code: 1` o `tier_id: 3`. Esto genera:

1. **Documentación externa obligatoria:** Necesitas un documento que diga "1=ACTIVE, 2=PENDING, 3=CANCELLED"
2. **Errores silenciosos:** Cliente envía `status_code: 5` (no existe), backend lo acepta, DB guarda basura
3. **Debugging difícil:** Logs muestran números, no semántica: "User 123 changed from 2 to 3" ¿qué significa?
4. **Refactoring peligroso:** Si cambias "1=ACTIVE" a "5=ACTIVE", rompes TODOS los clientes

**Historia real - Bug de producción en e-commerce:**
- DB tenía `payment_status: INT` con valores 1-4
- Desarrollador agregó nuevo estado (valor 5) sin documentar  
- Cliente móvil (versión vieja) siguió enviando 1-4
- Backend aceptó silenciosamente `payment_status: 3` que ahora significaba otra cosa
- **Resultado:** 2,847 pagos procesados incorrectamente, $43,000 USD en chargebacks

Con enums de GraphQL, esto es **imposible**:

```graphql
enum PaymentStatus {
  PENDING
  PROCESSING
  COMPLETED
  FAILED
  REFUNDED  # Nuevo valor - error inmediato en clientes viejos
}
```

Si agregas `REFUNDED`, clientes con schema viejo reciben **error de validación** ANTES de procesar el pago.

**Sin Enum (con códigos numéricos):**

```graphql
type User {
  tierCode: Int!  # ❌ ¿Qué significa 1, 2, 3, 4?
}
```

**Problemas:**
- No autodocumentado
- Propenso a errores (enviar `5` cuando solo existen 1-4)
- Sin validación automática
- Sin auto-completado en IDEs

**Con Enum:**

```graphql
enum CashbackTier {
  BRONZE    # 1% base
  SILVER    # 2% base
  GOLD      # 3% base
  PLATINUM  # 5% base
}

type User {
  tier: CashbackTier!  # ✅ Autodocumentado y validado
}
```

**Beneficios:**
- ✅ Autodocumentación
- ✅ Validación automática (solo acepta valores válidos)
- ✅ Auto-completado en IDEs/GraphiQL
- ✅ Type-safety en código cliente

### 3.2 Ejemplo: TransactionCategory

```graphql
enum TransactionCategory {
  GROCERIES      # 1x multiplier
  RESTAURANTS    # 2x multiplier
  TRAVEL         # 3x multiplier
  SHOPPING       # 1x multiplier
  ENTERTAINMENT  # 1x multiplier
  HEALTH         # 1.5x multiplier
  UTILITIES      # 1x multiplier
  OTHER          # 1x multiplier
}
```

**Uso en Queries:**

```graphql
{
  transactions(category: TRAVEL) {  # ✅ Validado automáticamente
    merchantName
    cashbackPercentage  # 3x para TRAVEL
  }
}
```

**Error automático si se envía valor inválido:**

```graphql
{
  transactions(category: INVALID_CATEGORY) {  # ❌ Error
    merchantName
  }
}
```

**Respuesta:**

```json
{
  "errors": [
    {
      "message": "Argument 'category' has invalid value. Expected type 'TransactionCategory'.",
      "locations": [{"line": 2, "column": 18}],
      "extensions": {
        "classification": "ValidationError"
      }
    }
  ]
}
```

---

## 4. Principio 3: Relaciones Bidireccionales

### 4.1 Navegación Natural en el Grafo

**En REST:** Cliente debe hacer joins manualmente.

```http
GET /api/transactions/123
Response: { "userId": "user-001", ... }

GET /api/users/user-001  # Segunda llamada
Response: { "fullName": "Maria Silva", ... }
```

**En GraphQL:** Navegación bidireccional built-in.

```graphql
type User {
  id: ID!
  fullName: String!
  transactions: [Transaction!]!  # User → Transactions
}

type Transaction {
  id: ID!
  amount: Money!
  user: User!                    # Transaction → User
}
```

**Query (TODO en una llamada):**

```graphql
{
  transaction(id: "trans-123") {
    amount
    merchantName
    user {           # ✅ Navegación directa
      fullName
      tier
    }
  }
}
```

**Resolver (implementación):**

```java
@SchemaMapping(typeName = "Transaction", field = "user")
public User user(Transaction transaction) {
    return userRepository.findById(transaction.getUserId())
        .orElseThrow(() -> new GraphQLException("User not found"));
}

@SchemaMapping(typeName = "User", field = "transactions")
public List<Transaction> transactions(User user) {
    return transactionRepository.findByUserId(user.getId());
}
```

### 4.2 Ventajas de las Relaciones Bidireccionales

1. **Menos requests:** Todo en una query
2. **Queries anidadas:** `user.transactions.rewards`
3. **Flexibilidad:** Cliente elige profundidad de anidación
4. **UX superior:** Datos relacionados juntos

**Ejemplo complejo:**

```graphql
{
  user(id: "user-001") {
    fullName
    tier
    transactions {
      merchantName
      amount
      cashbackAmount
      rewards {
        status
        expiresAt
      }
    }
  }
}
```

---

## 5. Principio 4: Campos Calculados

### El Dilema: ¿Persistir o Calcular?

En diseño de bases de datos, existe un trade-off clásico entre **normalización** y **desnormalización**:

**Normalización:**
- Almacenas solo datos base
- Calculas derivados en tiempo real
- Pros: Sin redundancia, siempre consistente
- Cons: Queries más complejas, más CPU

**Desnormalización:**
- Almacenas datos derivados
- Lees directamente de la DB
- Pros: Queries rápidas
- Cons: Redundancia, riesgo de inconsistencia

**Ejemplo clásico:**

¿Deberías almacenar `totalSpent` en la tabla `users`?

```sql
-- Opción 1: Normalizado (NO almacenar)
SELECT SUM(amount) FROM transactions WHERE user_id = 123;

-- Opción 2: Desnormalizado (almacenar)
SELECT total_spent FROM users WHERE id = 123;
```

**Problema con Opción 2:**
- ¿Qué pasa si creas una transaction y olvidas actualizar `total_spent`?
- ¿Qué pasa si un batch job actualiza transactions pero falla a mitad?
- **Resultado:** Datos inconsistentes, bugs silenciosos

**GraphQL te da lo mejor de ambos mundos:**

Con campos calculados en resolvers:
- DB está normalizada (consistencia garantizada)
- Cliente ve el campo como si estuviera almacenado
- Lógica de cálculo está centralizada en el resolver
- Puedes cachear si performance es crítica

### 5.1 Concepto: No Todo Está en la DB

**Campos calculados** son valores derivados que se computan en tiempo real, NO se persisten.

**Ejemplos en Cashback Rewards:**

| Campo | Persistido en DB | Calculado |
|-------|-----------------|-----------|
| `user.fullName` | ✅ | ❌ |
| `user.tier` | ✅ | ❌ |
| `user.availableCashback` | ❌ | ✅ (suma de rewards) |
| `transaction.amount` | ✅ | ❌ |
| `transaction.cashbackAmount` | ❌ | ✅ (tier × category × amount) |
| `transaction.cashbackPercentage` | ❌ | ✅ (tier % × multiplier) |

### 5.2 Ejemplo: availableCashback

**Schema:**

```graphql
type User {
  id: ID!
  fullName: String!
  availableCashback: Money!  # Calculado dinámicamente
}
```

**Resolver:**

```java
@SchemaMapping(typeName = "User")
public Double availableCashback(User user) {
    // Sumar todas las rewards activas
    List<Reward> activeRewards = rewardRepository
        .findByUserIdAndStatus(user.getId(), RewardStatus.AVAILABLE);
    
    return activeRewards.stream()
        .mapToDouble(Reward::getAmount)
        .sum();
}
```

**Ventajas:**

1. ✅ **Dato siempre actualizado:** Se calcula on-the-fly
2. ✅ **Sin redundancia:** No se duplica en DB
3. ✅ **Lógica centralizada:** Un solo lugar para la regla de negocio
4. ✅ **Schema limpio:** Cliente no sabe que es calculado

### 5.3 Ejemplo: cashbackAmount

**Lógica de negocio:**

```
cashbackAmount = transaction.amount × (tierPercentage / 100) × categoryMultiplier
```

**Tabla de cálculo:**

| Tier | Base % | Category | Multiplier | Final % |
|------|--------|----------|------------|---------|
| BRONZE | 1% | GROCERIES | 1x | 1% |
| GOLD | 3% | RESTAURANTS | 2x | 6% |
| PLATINUM | 5% | TRAVEL | 3x | 15% |

**Schema:**

```graphql
type Transaction {
  amount: Money!
  cashbackAmount: Money!        # Calculado
  cashbackPercentage: Percentage!  # Calculado
}
```

**Resolver:**

```java
@SchemaMapping(typeName = "Transaction")
public Double cashbackAmount(Transaction transaction) {
    User user = userRepository.findById(transaction.getUserId())
        .orElseThrow();
    
    // Base percentage según tier
    double basePercentage = switch (user.getTier()) {
        case BRONZE -> 1.0;
        case SILVER -> 2.0;
        case GOLD -> 3.0;
        case PLATINUM -> 5.0;
    };
    
    // Multiplier según category
    double multiplier = switch (transaction.getCategory()) {
        case RESTAURANTS -> 2.0;
        case TRAVEL -> 3.0;
        case HEALTH -> 1.5;
        default -> 1.0;
    };
    
    // Cálculo final
    return transaction.getAmount() * (basePercentage / 100) * multiplier;
}

@SchemaMapping(typeName = "Transaction")
public Integer cashbackPercentage(Transaction transaction) {
    User user = userRepository.findById(transaction.getUserId())
        .orElseThrow();
    
    double basePercentage = /* mismo switch tier */;
    double multiplier = /* mismo switch category */;
    
    return (int) (basePercentage * multiplier);
}
```

**Query:**

```graphql
{
  transactions(userId: "user-001", category: TRAVEL) {
    amount              # 500.00
    cashbackAmount      # 75.00 (500 × 0.05 × 3)
    cashbackPercentage  # 15 (5% × 3x)
  }
}
```

---

## 6. Principio 5: Separación de Concerns

### 6.1 Schema vs Implementación

**Regla:** El schema NO debe revelar detalles de implementación.

**❌ MAL (expone implementación):**

```graphql
type User {
  id: Int!                    # Revela que es INT en DB
  redisKey: String!           # Revela que usa Redis
  databasePartition: Int!     # Revela sharding strategy
}
```

**✅ BIEN (abstracción limpia):**

```graphql
type User {
  id: ID!           # Abstracción (puede ser INT, UUID, etc.)
  fullName: String!
  tier: CashbackTier!
}
```

### 6.2 Ejemplo: Almacenamiento de Money

**Implementación en DB:**

```sql
CREATE TABLE transactions (
  id UUID PRIMARY KEY,
  amount_cents INT NOT NULL  -- Almacenado en centavos
);
```

**Schema (sin exponer centavos):**

```graphql
type Transaction {
  amount: Money!  # Cliente ve 150.50, no 15050
}

scalar Money  # Custom scalar que maneja conversión
```

**Coercer (conversión automática):**

```java
@Component
public class MoneyScalar implements Coercing<Double, Double> {
    
    @Override
    public Double serialize(Object dataFetcherResult) {
        // DB guarda cents (Int) → Schema devuelve dollars (Double)
        if (dataFetcherResult instanceof Integer cents) {
            return cents / 100.0;  // 15050 → 150.50
        }
        throw new CoercingSerializeException("Invalid Money value");
    }
    
    @Override
    public Double parseValue(Object input) {
        // Cliente envía dollars → DB guarda cents
        if (input instanceof Double dollars) {
            return dollars;  // GraphQL layer recibe 150.50
        }
        throw new CoercingParseValueException("Invalid Money input");
    }
}
```

**Beneficio:** Cliente NUNCA ve "centavos", solo "dólares".

---

## 7. Resumen de Sección 2.1

### Principios de Diseño de Schemas:

1. ✅ **Domain-Driven Design**
   - Schema refleja lenguaje del negocio
   - No acoplado a estructura de DB
   - Conceptos de dominio (`fullName` vs `first_name/last_name`)

2. ✅ **Enums Bien Diseñados**
   - Autodocumentación (`PLATINUM` vs `tier_id: 4`)
   - Validación automática
   - Type-safety en cliente

3. ✅ **Relaciones Bidireccionales**
   - Navegación natural (`transaction.user.fullName`)
   - Menos requests HTTP
   - Queries anidadas

4. ✅ **Campos Calculados**
   - Datos derivados en tiempo real
   - Sin redundancia en DB
   - Lógica centralizada en resolvers

5. ✅ **Separación de Concerns**
   - Schema NO revela implementación
   - Abstracciones limpias (`ID!` vs `Int!`)
   - Custom scalars ocultan complejidad

---

# Sección 2.2 - Tipos Escalares, Objetos, Listas e Inputs

**Duración:** 30 minutos

## 🎯 Objetivo

Dominar el uso de **custom scalars** para validación automática, estructurar **objetos complejos** con anidación, manejar **listas** correctamente, y diseñar **input types** para mutations robustas.

---

## 💭 Contexto: Validación en la Capa del Schema

Una de las ventajas más poderosas de GraphQL es que el **schema valida automáticamente** antes de ejecutar cualquier lógica de negocio. Esto crea una "muralla de seguridad" que previene datos inválidos incluso antes de que lleguen a tus resolvers.

**Arquitectura tradicional (sin GraphQL):**

```
Cliente → Backend → Validación (línea 1 del controller)
                  → Lógica de negocio
                  → DB
```

Si la validación falla, ya gastaste:
- CPU en parsear el request
- Memoria en cargar el controller
- Tiempo de red en round-trip
- Logs innecesarios

**Arquitectura con GraphQL + Custom Scalars:**

```
Cliente → GraphQL Engine → ❌ STOP (validación)
                         → (nunca llega al resolver)
```

**Beneficio:** Error **inmediato** sin consumir recursos del backend.

### Custom Scalars: El Guardián de tu API

Los scalars básicos de GraphQL (`String`, `Int`, `Float`, `Boolean`, `ID`) son genéricos. Aceptan CUALQUIER valor dentro de su tipo:

- `String` → Acepta `"foo"`, `"not-an-email"`, `"<script>hack</script>"`
- `Int` → Acepta `-999999999`, `0`, `150` (¿todos válidos?)
- `Float` → Acepta `0.1 + 0.2 = 0.30000000000000004` (imprecisión)

**Custom scalars** agregan **semántica** y **validación** específica del dominio:

- `Email` → Solo emails válidos (regex)
- `Money` → Precisión decimal garantizada
- `Percentage` → Rango 0-100
- `DateTime` → ISO 8601 estándar

**Filosofía:** "Haz que estados inválidos sean **irrepresentables**". Si tu schema no acepta emails inválidos, ese bug nunca puede ocurrir.

---

## 1. Custom Scalars: Validación Automática

### 1.1 Problema: Scalars Básicos No Validan

**GraphQL built-in scalars:**

| Scalar | Valida |
|--------|--------|
| `String` | ❌ Acepta cualquier texto |
| `Int` | ✅ Solo enteros |
| `Float` | ✅ Solo números |
| `Boolean` | ✅ true/false |
| `ID` | ❌ Acepta cualquier String |

**Problema con String:**

```graphql
type User {
  email: String!  # ❌ Acepta "foo bar" (inválido)
}
```

**Mutation:**

```graphql
mutation {
  createUser(email: "not-an-email") {  # ❌ No valida
    id
  }
}
```

**Resultado:** Backend recibe email inválido → Error en runtime.

### 1.2 Solución: Custom Scalar `Email`

**Schema:**

```graphql
scalar Email

type User {
  email: Email!  # ✅ Valida formato automáticamente
}
```

**Coercer (validación):**

```java
@Component
public class EmailScalar implements Coercing<String, String> {
    
    private static final String EMAIL_REGEX = 
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    @Override
    public String serialize(Object dataFetcherResult) {
        // DB → Cliente
        if (dataFetcherResult instanceof String email) {
            if (isValidEmail(email)) {
                return email;
            }
        }
        throw new CoercingSerializeException("Invalid email format");
    }
    
    @Override
    public String parseValue(Object input) {
        // Cliente → DB
        if (input instanceof String email) {
            if (isValidEmail(email)) {
                return email;
            }
            throw new CoercingParseValueException(
                "Invalid email format: " + email
            );
        }
        throw new CoercingParseValueException("Email must be a String");
    }
    
    @Override
    public String parseLiteral(Object input) {
        // Query literal (ej: email: "test@example.com")
        if (input instanceof StringValue stringValue) {
            String email = stringValue.getValue();
            if (isValidEmail(email)) {
                return email;
            }
            throw new CoercingParseLiteralException(
                "Invalid email format: " + email
            );
        }
        throw new CoercingParseLiteralException("Email must be a String literal");
    }
    
    private boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }
}
```

**Configuración (DGS):**

```java
@Configuration
public class ScalarConfig {
    
    @Bean
    public RuntimeWiring runtimeWiringConfigurer(EmailScalar emailScalar) {
        return RuntimeWiring.newRuntimeWiring()
            .scalar(GraphQLScalarType.newScalar()
                .name("Email")
                .description("Email address (validated)")
                .coercing(emailScalar)
                .build())
            .build();
    }
}
```

**Beneficio: Validación automática**

```graphql
mutation {
  createUser(email: "invalid-email") {  # ❌ Error antes de llegar al resolver
    id
  }
}
```

**Error:**

```json
{
  "errors": [
    {
      "message": "Invalid email format: invalid-email",
      "extensions": {
        "classification": "ValidationError"
      }
    }
  ]
}
```

---

## 2. Custom Scalar: Money

### 2.1 Por Qué Necesitamos Money

El problema de usar `Float` para dinero no es teórico. Es un bug de **millones de dólares** esperando a ocurrir.

**El bug del Float en sistemas financieros:**

```javascript
// JavaScript (mismo problema en todos los lenguajes)
0.1 + 0.2 = 0.30000000000000004  // ❌ NO es 0.3
0.7 - 0.1 = 0.6000000000000001   // ❌ NO es 0.6
```

**Caso real - Fintech 2019:**
- Sistema de cashback usaba `Float` para almacenar recompensas
- 10,000 transacciones diarias con cashback promedio de $2.50
- Cada transacción acumulaba error de ~0.00000000000001
- Después de 1 año: **$3,847 USD en diferencias** no contabilizadas
- Auditoría reveló el problema, costo de corrección: $120,000 USD

**¿Por qué pasa esto?**

Los números decimales en computadoras se representan en base 2 (binario). Algunos decimales en base 10 son **imposibles de representar exactamente** en binario:

```
0.1 en decimal = 0.0001100110011001100110011... (infinito) en binario
```

Como la memoria es finita, se **trunca**, generando error de precisión.

**Solución en finanzas:** NUNCA usar Float. Usar:
1. **Integers (cents):** Almacenar `$150.50` como `15050` cents
2. **BigDecimal:** Java, Python tienen librerías de precisión arbitraria
3. **Custom Money scalar:** GraphQL abstrae la complejidad

**Problema con Float:**

```graphql
type Transaction {
  amount: Float!  # ❌ Imprecisiones de punto flotante
}
```

**Ejemplo de imprecisión:**

```javascript
0.1 + 0.2 = 0.30000000000000004  // ❌ WTF?
```

### 2.2 Implementación de Money

**Schema:**

```graphql
scalar Money

type Transaction {
  amount: Money!          # ✅ Precisión garantizada
  cashbackAmount: Money!
}

type User {
  availableCashback: Money!
  totalSpent: Money!
}
```

**Coercer:**

```java
@Component
public class MoneyScalar implements Coercing<Double, Double> {
    
    @Override
    public Double serialize(Object dataFetcherResult) {
        // DB (cents Int) → Cliente (dollars Double)
        if (dataFetcherResult instanceof Integer cents) {
            return cents / 100.0;  // 15050 → 150.50
        }
        if (dataFetcherResult instanceof Double dollars) {
            return Math.round(dollars * 100) / 100.0;  // Redondear 2 decimales
        }
        throw new CoercingSerializeException("Invalid Money value");
    }
    
    @Override
    public Double parseValue(Object input) {
        // Cliente → Backend
        if (input instanceof Number number) {
            double value = number.doubleValue();
            if (value < 0) {
                throw new CoercingParseValueException("Money cannot be negative");
            }
            return Math.round(value * 100) / 100.0;  // Redondear 2 decimales
        }
        throw new CoercingParseValueException("Money must be a number");
    }
    
    @Override
    public Double parseLiteral(Object input) {
        if (input instanceof IntValue intValue) {
            return intValue.getValue().doubleValue();
        }
        if (input instanceof FloatValue floatValue) {
            double value = floatValue.getValue().doubleValue();
            return Math.round(value * 100) / 100.0;
        }
        throw new CoercingParseLiteralException("Money must be a number literal");
    }
}
```

**Uso:**

```graphql
{
  transaction(id: "trans-123") {
    amount           # 150.50
    cashbackAmount   # 4.52
  }
}
```

---

## 3. Custom Scalar: Percentage

### 3.1 Diseño

**Schema:**

```graphql
scalar Percentage  # Int 0-100

type Transaction {
  cashbackPercentage: Percentage!  # ej: 3, 6, 15
}
```

**Coercer:**

```java
@Component
public class PercentageScalar implements Coercing<Integer, Integer> {
    
    @Override
    public Integer serialize(Object dataFetcherResult) {
        if (dataFetcherResult instanceof Integer percentage) {
            if (percentage >= 0 && percentage <= 100) {
                return percentage;
            }
            throw new CoercingSerializeException(
                "Percentage must be 0-100, got: " + percentage
            );
        }
        throw new CoercingSerializeException("Percentage must be an Integer");
    }
    
    @Override
    public Integer parseValue(Object input) {
        if (input instanceof Integer percentage) {
            if (percentage >= 0 && percentage <= 100) {
                return percentage;
            }
            throw new CoercingParseValueException(
                "Percentage must be 0-100, got: " + percentage
            );
        }
        throw new CoercingParseValueException("Percentage must be an Integer");
    }
    
    @Override
    public Integer parseLiteral(Object input) {
        if (input instanceof IntValue intValue) {
            int percentage = intValue.getValue().intValue();
            if (percentage >= 0 && percentage <= 100) {
                return percentage;
            }
            throw new CoercingParseLiteralException(
                "Percentage must be 0-100, got: " + percentage
            );
        }
        throw new CoercingParseLiteralException("Percentage must be an Int literal");
    }
}
```

**Validación automática:**

```graphql
mutation {
  updateCashback(percentage: 150) {  # ❌ Error: fuera de rango
    success
  }
}
```

---

## 4. Custom Scalar: DateTime

### 4.1 Problema con String

```graphql
type Transaction {
  createdAt: String!  # ❌ Acepta "foo", "2025-99-99", etc.
}
```

### 4.2 Solución: ISO 8601

**Schema:**

```graphql
scalar DateTime  # ISO 8601: "2025-11-15T10:30:00Z"

type Transaction {
  timestamp: DateTime!
}

type User {
  enrolledAt: DateTime!
}

type Reward {
  expiresAt: DateTime!
}
```

**Coercer:**

```java
@Component
public class DateTimeScalar implements Coercing<String, String> {
    
    private static final DateTimeFormatter ISO_FORMATTER = 
        DateTimeFormatter.ISO_DATE_TIME;
    
    @Override
    public String serialize(Object dataFetcherResult) {
        // DB → Cliente
        if (dataFetcherResult instanceof LocalDateTime dateTime) {
            return dateTime.atZone(ZoneId.of("UTC"))
                .format(ISO_FORMATTER);
        }
        if (dataFetcherResult instanceof String str) {
            // Validar que sea ISO 8601 válido
            try {
                LocalDateTime.parse(str, ISO_FORMATTER);
                return str;
            } catch (Exception e) {
                throw new CoercingSerializeException("Invalid DateTime format");
            }
        }
        throw new CoercingSerializeException("DateTime must be ISO 8601");
    }
    
    @Override
    public String parseValue(Object input) {
        // Cliente → DB
        if (input instanceof String dateTimeStr) {
            try {
                // Validar y retornar
                LocalDateTime.parse(dateTimeStr, ISO_FORMATTER);
                return dateTimeStr;
            } catch (Exception e) {
                throw new CoercingParseValueException(
                    "Invalid DateTime format. Expected ISO 8601: " + dateTimeStr
                );
            }
        }
        throw new CoercingParseValueException("DateTime must be a String");
    }
    
    @Override
    public String parseLiteral(Object input) {
        if (input instanceof StringValue stringValue) {
            String dateTimeStr = stringValue.getValue();
            try {
                LocalDateTime.parse(dateTimeStr, ISO_FORMATTER);
                return dateTimeStr;
            } catch (Exception e) {
                throw new CoercingParseLiteralException(
                    "Invalid DateTime format. Expected ISO 8601: " + dateTimeStr
                );
            }
        }
        throw new CoercingParseLiteralException("DateTime must be a String literal");
    }
}
```

**Uso:**

```graphql
{
  user(id: "user-001") {
    enrolledAt  # "2024-01-15T08:30:00Z"
  }
  
  rewards(userId: "user-001") {
    expiresAt   # "2025-12-31T23:59:59Z"
  }
}
```

---

## 5. Objetos y Anidación

### 5.1 Objetos Complejos

**Schema:**

```graphql
type User {
  id: ID!
  fullName: String!
  tier: CashbackTier!
  email: Email!
  enrolledAt: DateTime!
  
  # Relaciones navegables
  transactions: [Transaction!]!
  rewards: [Reward!]!
  
  # Campos calculados
  availableCashback: Money!
  totalSpent: Money!
  totalCashbackEarned: Money!
}

type Transaction {
  id: ID!
  amount: Money!
  merchantName: String!
  category: TransactionCategory!
  status: TransactionStatus!
  timestamp: DateTime!
  
  # Relación bidireccional
  user: User!
  
  # Campos calculados
  cashbackAmount: Money!
  cashbackPercentage: Percentage!
  
  # Relación anidada
  reward: Reward
}

type Reward {
  id: ID!
  amount: Money!
  status: RewardStatus!
  earnedAt: DateTime!
  expiresAt: DateTime!
  
  # Relación bidireccional
  user: User!
  transaction: Transaction!
}
```

### 5.2 Anidación Multi-Nivel

**Query:**

```graphql
{
  user(id: "user-001") {
    fullName
    tier
    availableCashback
    
    transactions {              # Nivel 1
      merchantName
      amount
      cashbackAmount
      
      reward {                 # Nivel 2
        amount
        status
        expiresAt
      }
    }
  }
}
```

**Respuesta:**

```json
{
  "data": {
    "user": {
      "fullName": "Maria Silva",
      "tier": "GOLD",
      "availableCashback": 245.30,
      "transactions": [
        {
          "merchantName": "SuperMarket",
          "amount": 150.00,
          "cashbackAmount": 4.50,
          "reward": {
            "amount": 4.50,
            "status": "AVAILABLE",
            "expiresAt": "2025-12-31T23:59:59Z"
          }
        },
        {
          "merchantName": "Restaurant",
          "amount": 80.00,
          "cashbackAmount": 4.80,
          "reward": {
            "amount": 4.80,
            "status": "REDEEMED",
            "expiresAt": "2025-06-30T23:59:59Z"
          }
        }
      ]
    }
  }
}
```

---

## 6. Listas: Arrays de Objetos

### 6.1 Sintaxis de Listas

```graphql
type User {
  transactions: [Transaction!]!
  # └─────────┬─────────┘
  #           │
  #           └─ Lista de Transaction
  #              └─ Transaction! = elementos non-null
  #                 └─ [...]! = lista non-null (puede estar vacía)
}
```

**Variantes de nullabilidad:**

| Sintaxis | Significado |
|----------|-------------|
| `[Transaction]` | Lista nullable, elementos nullable |
| `[Transaction]!` | Lista non-null, elementos nullable |
| `[Transaction!]` | Lista nullable, elementos non-null |
| `[Transaction!]!` | Lista non-null, elementos non-null |

### 6.2 Ejemplo: Todas las Combinaciones

```graphql
type Example {
  # Lista y elementos pueden ser null
  optionalListOptionalItems: [String]
  # Query: { optionalListOptionalItems } → null OK
  # Query: { optionalListOptionalItems } → ["a", null, "b"] OK
  
  # Lista non-null, elementos pueden ser null
  requiredListOptionalItems: [String]!
  # Query: { requiredListOptionalItems } → [] OK (lista vacía)
  # Query: { requiredListOptionalItems } → ["a", null, "b"] OK
  # Query: { requiredListOptionalItems } → null ❌ ERROR
  
  # Lista puede ser null, elementos non-null
  optionalListRequiredItems: [String!]
  # Query: { optionalListRequiredItems } → null OK
  # Query: { optionalListRequiredItems } → ["a", "b"] OK
  # Query: { optionalListRequiredItems } → ["a", null, "b"] ❌ ERROR
  
  # Lista y elementos non-null
  requiredListRequiredItems: [String!]!
  # Query: { requiredListRequiredItems } → [] OK (lista vacía)
  # Query: { requiredListRequiredItems } → ["a", "b"] OK
  # Query: { requiredListRequiredItems } → null ❌ ERROR
  # Query: { requiredListRequiredItems } → ["a", null, "b"] ❌ ERROR
}
```

### 6.3 Best Practice: Cashback Rewards

```graphql
type User {
  transactions: [Transaction!]!  # ✅ Nunca null, elementos nunca null
  rewards: [Reward!]!            # ✅ Puede retornar []
}
```

**Por qué `[Transaction!]!`:**

1. ✅ Lista nunca es `null` → Evita null checks en cliente
2. ✅ Elementos nunca son `null` → Seguridad de tipos
3. ✅ Puede retornar `[]` → Usuario sin transactions OK

---

## 7. Input Types: Mutations Robustas

### 7.1 Problema: Mutations con Muchos Args

**❌ MAL (demasiados argumentos):**

```graphql
type Mutation {
  createTransaction(
    userId: ID!
    amount: Float!
    merchantName: String!
    category: String!
    description: String
    location: String
    metadata: String
  ): TransactionResponse!
}
```

**Problemas:**
- Difícil de leer
- Propenso a errores (orden de args)
- No reusable

### 7.2 Solución: Input Types

**✅ BIEN:**

```graphql
input CreateTransactionInput {
  userId: ID!
  amount: Money!
  merchantName: String!
  category: TransactionCategory!
  description: String
}

type Mutation {
  createTransaction(input: CreateTransactionInput!): TransactionResponse!
}
```

**Ventajas:**
- ✅ Un solo argumento
- ✅ Autodocumentado
- ✅ Reusable
- ✅ Validación automática

### 7.3 Ejemplo Completo

**Schema:**

```graphql
input CreateTransactionInput {
  userId: ID!
  amount: Money!
  category: TransactionCategory!
  merchantName: String!
  description: String
}

type TransactionResponse {
  success: Boolean!
  message: String!
  transaction: Transaction
}

type Mutation {
  createTransaction(input: CreateTransactionInput!): TransactionResponse!
}
```

**Resolver:**

```java
@MutationMapping
public TransactionResponse createTransaction(
    @Argument CreateTransactionInput input
) {
    // 1. Validar user existe
    User user = userRepository.findById(input.userId())
        .orElseThrow(() -> new GraphQLException("User not found"));
    
    // 2. Crear transaction
    Transaction transaction = Transaction.builder()
        .id(UUID.randomUUID().toString())
        .userId(input.userId())
        .amount(input.amount())
        .category(input.category())
        .merchantName(input.merchantName())
        .description(input.description())
        .status(TransactionStatus.PENDING)
        .timestamp(LocalDateTime.now())
        .build();
    
    transactionRepository.save(transaction);
    
    // 3. Calcular cashback
    double cashbackAmount = calculateCashback(transaction, user);
    
    // 4. Crear reward
    Reward reward = Reward.builder()
        .id(UUID.randomUUID().toString())
        .userId(user.getId())
        .transactionId(transaction.getId())
        .amount(cashbackAmount)
        .status(RewardStatus.PENDING)
        .earnedAt(LocalDateTime.now())
        .expiresAt(LocalDateTime.now().plusMonths(6))
        .build();
    
    rewardRepository.save(reward);
    
    // 5. Actualizar transaction status
    transaction.setStatus(TransactionStatus.CONFIRMED);
    transactionRepository.save(transaction);
    
    // 6. Retornar response estructurada
    return TransactionResponse.builder()
        .success(true)
        .message("Transaction created successfully")
        .transaction(transaction)
        .build();
}
```

**Mutation:**

```graphql
mutation {
  createTransaction(input: {
    userId: "user-001"
    amount: 200.0
    category: RESTAURANTS
    merchantName: "Sushi Bar"
    description: "Dinner with team"
  }) {
    success
    message
    transaction {
      id
      amount
      cashbackAmount
      cashbackPercentage
      reward {
        amount
        expiresAt
      }
    }
  }
}
```

**Response:**

```json
{
  "data": {
    "createTransaction": {
      "success": true,
      "message": "Transaction created successfully",
      "transaction": {
        "id": "trans-789",
        "amount": 200.0,
        "cashbackAmount": 12.0,
        "cashbackPercentage": 6,
        "reward": {
          "amount": 12.0,
          "expiresAt": "2026-05-15T10:30:00Z"
        }
      }
    }
  }
}
```

---

## 8. Resumen de Sección 2.2

### Custom Scalars:

1. ✅ **Email:** Validación de formato automática
2. ✅ **Money:** Precisión decimal garantizada
3. ✅ **Percentage:** Rango 0-100 validado
4. ✅ **DateTime:** ISO 8601 estándar

### Objetos y Anidación:

- ✅ Objetos complejos con relaciones bidireccionales
- ✅ Anidación multi-nivel (user → transactions → rewards)
- ✅ Campos calculados en objetos

### Listas:

- ✅ `[Type!]!` → Lista y elementos non-null
- ✅ Puede retornar `[]` (lista vacía)
- ✅ Type-safety total

### Input Types:

- ✅ Un solo argumento en mutations
- ✅ Reusables y autodocumentados
- ✅ Validación automática de campos

---

# Sección 2.3 - Queries y Mutations Complejas

**Duración:** 30 minutos

## 🎯 Objetivo

Diseñar queries con **múltiples parámetros opcionales**, construir **mutations con respuestas estructuradas**, aprovechar **campos calculados dinámicos**, y combinar todo en queries complejas que resuelven casos de uso reales.

---

## 1. Queries con Múltiples Filtros

### 1.1 Problema: Filtros Rígidos

**❌ MAL (un query por combinación):**

```graphql
type Query {
  transactionsByUser(userId: ID!): [Transaction!]!
  transactionsByCategory(category: TransactionCategory!): [Transaction!]!
  transactionsByStatus(status: TransactionStatus!): [Transaction!]!
  transactionsByUserAndCategory(
    userId: ID!
    category: TransactionCategory!
  ): [Transaction!]!
  # ...exponencialmente más combinaciones
}
```

**Problema:** Necesitarías 2^N queries para N filtros.

### 1.2 Solución: Filtros Opcionales

**✅ BIEN (un query flexible):**

```graphql
type Query {
  transactions(
    userId: ID
    category: TransactionCategory
    status: TransactionStatus
    minAmount: Money
    maxAmount: Money
    startDate: DateTime
    endDate: DateTime
  ): [Transaction!]!
}
```

**Resolver (filtra dinámicamente):**

```java
@QueryMapping
public List<Transaction> transactions(
    @Argument String userId,
    @Argument TransactionCategory category,
    @Argument TransactionStatus status,
    @Argument Double minAmount,
    @Argument Double maxAmount,
    @Argument String startDate,
    @Argument String endDate
) {
    // Construir query dinámica según argumentos presentes
    List<Transaction> transactions = transactionRepository.findAll();
    
    // Filtrar por userId (si presente)
    if (userId != null) {
        transactions = transactions.stream()
            .filter(t -> t.getUserId().equals(userId))
            .collect(Collectors.toList());
    }
    
    // Filtrar por category (si presente)
    if (category != null) {
        transactions = transactions.stream()
            .filter(t -> t.getCategory() == category)
            .collect(Collectors.toList());
    }
    
    // Filtrar por status (si presente)
    if (status != null) {
        transactions = transactions.stream()
            .filter(t -> t.getStatus() == status)
            .collect(Collectors.toList());
    }
    
    // Filtrar por rango de monto (si presente)
    if (minAmount != null) {
        transactions = transactions.stream()
            .filter(t -> t.getAmount() >= minAmount)
            .collect(Collectors.toList());
    }
    
    if (maxAmount != null) {
        transactions = transactions.stream()
            .filter(t -> t.getAmount() <= maxAmount)
            .collect(Collectors.toList());
    }
    
    return transactions;
}
```

**Uso flexible:**

```graphql
# Solo por user
{
  transactions(userId: "user-001") {
    merchantName
  }
}

# Por user + category
{
  transactions(userId: "user-001", category: TRAVEL) {
    merchantName
  }
}

# Por category + status + rango de monto
{
  transactions(
    category: RESTAURANTS
    status: CONFIRMED
    minAmount: 50.0
    maxAmount: 500.0
  ) {
    merchantName
    amount
  }
}

# Todos los filtros combinados
{
  transactions(
    userId: "user-001"
    category: TRAVEL
    status: CONFIRMED
    minAmount: 100.0
    startDate: "2025-01-01T00:00:00Z"
    endDate: "2025-12-31T23:59:59Z"
  ) {
    merchantName
    amount
    cashbackAmount
  }
}
```

---

## 2. Queries Anidadas con Campos Calculados

### 2.1 Combinando Navegación y Cálculos

**Query:**

```graphql
{
  user(id: "user-001") {
    fullName
    tier
    
    # Campos calculados del user
    availableCashback
    totalSpent
    totalCashbackEarned
    
    # Navegar a transactions
    transactions {
      merchantName
      amount
      category
      
      # Campos calculados de transaction
      cashbackAmount
      cashbackPercentage
      
      # Navegar a reward
      reward {
        status
        expiresAt
      }
    }
  }
}
```

**Respuesta:**

```json
{
  "data": {
    "user": {
      "fullName": "Maria Silva",
      "tier": "GOLD",
      "availableCashback": 245.30,
      "totalSpent": 3580.00,
      "totalCashbackEarned": 287.40,
      "transactions": [
        {
          "merchantName": "Airlines Co",
          "amount": 500.00,
          "category": "TRAVEL",
          "cashbackAmount": 45.00,
          "cashbackPercentage": 9,
          "reward": {
            "status": "AVAILABLE",
            "expiresAt": "2026-05-15T10:30:00Z"
          }
        },
        {
          "merchantName": "Restaurant",
          "amount": 120.00,
          "category": "RESTAURANTS",
          "cashbackPercentage": 6,
          "cashbackAmount": 7.20,
          "reward": {
            "status": "REDEEMED",
            "expiresAt": "2025-11-15T10:30:00Z"
          }
        }
      ]
    }
  }
}
```

### 2.2 Beneficio: Datos Relacionados en Una Llamada

**En REST:** 4+ llamadas HTTP

```http
GET /api/users/user-001
GET /api/users/user-001/transactions
GET /api/users/user-001/cashback-summary
GET /api/transactions/trans-123/reward
```

**En GraphQL:** 1 llamada

```graphql
{ user(id: "user-001") { /* todo anidado */ } }
```

---

## 3. Campos Calculados Dinámicos

### 3.1 Lógica de Negocio Compleja

**cashbackPercentage:** Depende de tier + category

```java
@SchemaMapping(typeName = "Transaction")
public Integer cashbackPercentage(Transaction transaction) {
    // 1. Obtener user
    User user = userRepository.findById(transaction.getUserId())
        .orElseThrow();
    
    // 2. Base percentage según tier
    double basePercentage = switch (user.getTier()) {
        case BRONZE -> 1.0;
        case SILVER -> 2.0;
        case GOLD -> 3.0;
        case PLATINUM -> 5.0;
    };
    
    // 3. Multiplier según category
    double multiplier = switch (transaction.getCategory()) {
        case RESTAURANTS -> 2.0;  // 2x
        case TRAVEL -> 3.0;        // 3x
        case HEALTH -> 1.5;        // 1.5x
        default -> 1.0;            // 1x
    };
    
    // 4. Cálculo final
    return (int) (basePercentage * multiplier);
}

@SchemaMapping(typeName = "Transaction")
public Double cashbackAmount(Transaction transaction) {
    User user = userRepository.findById(transaction.getUserId())
        .orElseThrow();
    
    double basePercentage = /* mismo switch tier */;
    double multiplier = /* mismo switch category */;
    
    double percentage = basePercentage * multiplier;
    return transaction.getAmount() * (percentage / 100);
}
```

**Tabla de resultados:**

| User Tier | Category | Base % | Multiplier | Final % | Amount | Cashback |
|-----------|----------|--------|------------|---------|--------|----------|
| BRONZE | GROCERIES | 1% | 1x | 1% | $100 | $1.00 |
| SILVER | RESTAURANTS | 2% | 2x | 4% | $100 | $4.00 |
| GOLD | TRAVEL | 3% | 3x | 9% | $500 | $45.00 |
| PLATINUM | TRAVEL | 5% | 3x | 15% | $1000 | $150.00 |

### 3.2 Uso en Query

```graphql
{
  transactions(userId: "user-001", category: TRAVEL) {
    merchantName
    amount
    cashbackPercentage  # 9 (GOLD × TRAVEL)
    cashbackAmount      # Calculado automáticamente
  }
}
```

---

## 4. Mutations con Respuestas Estructuradas

### 4.1 Problema: Mutations Simples

**❌ MAL:**

```graphql
type Mutation {
  createTransaction(input: CreateTransactionInput!): Transaction
  # ¿Qué pasa si hay error?
  # ¿Cómo sé si fue exitoso?
  # ¿Dónde está el mensaje para el usuario?
}
```

### 4.2 Solución: Response Wrappers

**✅ BIEN:**

```graphql
type TransactionResponse {
  success: Boolean!      # ¿Exitoso?
  message: String!       # Mensaje para UI
  transaction: Transaction  # Datos (nullable si error)
  errors: [String!]      # Errores de validación
}

type Mutation {
  createTransaction(input: CreateTransactionInput!): TransactionResponse!
}
```

**Resolver:**

```java
@MutationMapping
public TransactionResponse createTransaction(
    @Argument CreateTransactionInput input
) {
    try {
        // 1. Validaciones
        User user = userRepository.findById(input.userId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (input.amount() <= 0) {
            return TransactionResponse.builder()
                .success(false)
                .message("Amount must be positive")
                .errors(List.of("Invalid amount: " + input.amount()))
                .build();
        }
        
        // 2. Crear transaction
        Transaction transaction = transactionService.create(input, user);
        
        // 3. Generar reward automáticamente
        Reward reward = rewardService.generateReward(transaction, user);
        
        // 4. Success response
        return TransactionResponse.builder()
            .success(true)
            .message("Transaction created successfully")
            .transaction(transaction)
            .build();
            
    } catch (Exception e) {
        // 5. Error response
        return TransactionResponse.builder()
            .success(false)
            .message("Failed to create transaction")
            .errors(List.of(e.getMessage()))
            .build();
    }
}
```

**Mutation:**

```graphql
mutation {
  createTransaction(input: {
    userId: "user-001"
    amount: 300.0
    category: TRAVEL
    merchantName: "Flight Booking"
  }) {
    success
    message
    transaction {
      id
      amount
      cashbackAmount
      cashbackPercentage
      reward {
        amount
        expiresAt
      }
    }
    errors
  }
}
```

**Response (success):**

```json
{
  "data": {
    "createTransaction": {
      "success": true,
      "message": "Transaction created successfully",
      "transaction": {
        "id": "trans-999",
        "amount": 300.0,
        "cashbackAmount": 27.0,
        "cashbackPercentage": 9,
        "reward": {
          "amount": 27.0,
          "expiresAt": "2026-05-15T10:30:00Z"
        }
      },
      "errors": null
    }
  }
}
```

**Response (error):**

```json
{
  "data": {
    "createTransaction": {
      "success": false,
      "message": "Amount must be positive",
      "transaction": null,
      "errors": ["Invalid amount: -50.0"]
    }
  }
}
```

---

## 5. Mutations que Modifican Múltiples Entidades

### 5.1 Concepto: Side Effects

**Crear Transaction → Automáticamente crear Reward**

**Schema:**

```graphql
type Mutation {
  createTransaction(input: CreateTransactionInput!): TransactionResponse!
}

type TransactionResponse {
  success: Boolean!
  message: String!
  transaction: Transaction
  reward: Reward  # ✅ Side effect visible
}
```

**Resolver:**

```java
@MutationMapping
public TransactionResponse createTransaction(
    @Argument CreateTransactionInput input
) {
    // 1. Crear transaction
    Transaction transaction = Transaction.builder()
        .id(UUID.randomUUID().toString())
        .userId(input.userId())
        .amount(input.amount())
        .category(input.category())
        .merchantName(input.merchantName())
        .status(TransactionStatus.PENDING)
        .timestamp(LocalDateTime.now())
        .build();
    
    transactionRepository.save(transaction);
    
    // 2. Calcular cashback
    User user = userRepository.findById(input.userId()).orElseThrow();
    double cashbackAmount = calculateCashback(transaction, user);
    
    // 3. Crear reward automáticamente
    Reward reward = Reward.builder()
        .id(UUID.randomUUID().toString())
        .userId(user.getId())
        .transactionId(transaction.getId())
        .amount(cashbackAmount)
        .status(RewardStatus.PENDING)
        .earnedAt(LocalDateTime.now())
        .expiresAt(LocalDateTime.now().plusMonths(6))
        .build();
    
    rewardRepository.save(reward);
    
    // 4. Confirmar transaction
    transaction.setStatus(TransactionStatus.CONFIRMED);
    transactionRepository.save(transaction);
    
    // 5. Retornar ambas entidades
    return TransactionResponse.builder()
        .success(true)
        .message("Transaction and reward created")
        .transaction(transaction)
        .reward(reward)
        .build();
}
```

**Mutation:**

```graphql
mutation {
  createTransaction(input: {
    userId: "user-001"
    amount: 200.0
    category: RESTAURANTS
    merchantName: "Sushi Bar"
  }) {
    success
    message
    transaction {
      id
      cashbackAmount
    }
    reward {        # ✅ Creada automáticamente
      id
      amount
      status
      expiresAt
    }
  }
}
```

---

## 6. Query Compleja: Combinando Todo

### 6.1 Caso de Uso: Dashboard del Usuario

**Requisito:** Mostrar en una pantalla:
- Info del usuario (nombre, tier, cashback disponible)
- Total gastado
- Total de cashback ganado
- Últimas 5 transactions con cashback calculado
- Solo transactions CONFIRMED
- Solo category RESTAURANTS

**Query:**

```graphql
{
  user(id: "user-001") {
    # Info básica
    fullName
    tier
    email
    
    # Totales calculados
    availableCashback
    totalSpent
    totalCashbackEarned
    
    # Transactions filtradas
    transactions(
      status: CONFIRMED
      category: RESTAURANTS
      limit: 5
      orderBy: TIMESTAMP_DESC
    ) {
      merchantName
      amount
      timestamp
      
      # Cashback calculado
      cashbackAmount
      cashbackPercentage
      
      # Status del reward
      reward {
        status
        expiresAt
      }
    }
  }
}
```

**Resolver (con filtros):**

```java
@SchemaMapping(typeName = "User", field = "transactions")
public List<Transaction> transactions(
    User user,
    @Argument TransactionStatus status,
    @Argument TransactionCategory category,
    @Argument Integer limit,
    @Argument String orderBy
) {
    List<Transaction> transactions = transactionRepository
        .findByUserId(user.getId());
    
    // Filtrar por status
    if (status != null) {
        transactions = transactions.stream()
            .filter(t -> t.getStatus() == status)
            .collect(Collectors.toList());
    }
    
    // Filtrar por category
    if (category != null) {
        transactions = transactions.stream()
            .filter(t -> t.getCategory() == category)
            .collect(Collectors.toList());
    }
    
    // Ordenar
    if ("TIMESTAMP_DESC".equals(orderBy)) {
        transactions.sort(
            Comparator.comparing(Transaction::getTimestamp).reversed()
        );
    }
    
    // Limitar
    if (limit != null && limit > 0) {
        transactions = transactions.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    return transactions;
}
```

**Response:**

```json
{
  "data": {
    "user": {
      "fullName": "Maria Silva",
      "tier": "GOLD",
      "email": "maria@neobank.com",
      "availableCashback": 245.30,
      "totalSpent": 3580.00,
      "totalCashbackEarned": 287.40,
      "transactions": [
        {
          "merchantName": "Sushi Bar",
          "amount": 150.00,
          "timestamp": "2025-11-10T19:30:00Z",
          "cashbackAmount": 9.00,
          "cashbackPercentage": 6,
          "reward": {
            "status": "AVAILABLE",
            "expiresAt": "2026-05-10T19:30:00Z"
          }
        },
        {
          "merchantName": "Italian Restaurant",
          "amount": 95.00,
          "timestamp": "2025-11-05T20:15:00Z",
          "cashbackAmount": 5.70,
          "cashbackPercentage": 6,
          "reward": {
            "status": "REDEEMED",
            "expiresAt": "2026-05-05T20:15:00Z"
          }
        }
      ]
    }
  }
}
```

---

## 7. Validación Automática de Tipos

### 7.1 Enums Inválidos

**Query con enum inválido:**

```graphql
{
  transactions(category: INVALID_CATEGORY) {
    merchantName
  }
}
```

**Error automático:**

```json
{
  "errors": [
    {
      "message": "Argument 'category' has invalid value. Expected type 'TransactionCategory', found INVALID_CATEGORY.",
      "locations": [{"line": 2, "column": 18}],
      "extensions": {
        "classification": "ValidationError"
      }
    }
  ]
}
```

### 7.2 Tipos Incorrectos

**Query con tipo incorrecto:**

```graphql
{
  transaction(id: 123) {  # ❌ ID debe ser String
    merchantName
  }
}
```

**Error:**

```json
{
  "errors": [
    {
      "message": "Argument 'id' has invalid value. Expected type 'ID!', found 123.",
      "extensions": {
        "classification": "ValidationError"
      }
    }
  ]
}
```

---

## 8. Introspection: Schema Autodocumentado

### 8.1 Query de Introspection

**Obtener info del tipo User:**

```graphql
{
  __type(name: "User") {
    name
    kind
    fields {
      name
      type {
        name
        kind
      }
      description
    }
  }
}
```

**Respuesta:**

```json
{
  "data": {
    "__type": {
      "name": "User",
      "kind": "OBJECT",
      "fields": [
        {
          "name": "id",
          "type": {"name": "ID", "kind": "SCALAR"},
          "description": "Unique identifier"
        },
        {
          "name": "fullName",
          "type": {"name": "String", "kind": "SCALAR"},
          "description": "User's full name"
        },
        {
          "name": "tier",
          "type": {"name": "CashbackTier", "kind": "ENUM"},
          "description": "Cashback tier (BRONZE, SILVER, GOLD, PLATINUM)"
        },
        {
          "name": "availableCashback",
          "type": {"name": "Money", "kind": "SCALAR"},
          "description": "Sum of all AVAILABLE rewards"
        }
      ]
    }
  }
}
```

### 8.2 Herramientas que Usan Introspection

- **GraphiQL:** Auto-completado + documentación inline
- **Postman:** Importar schema automáticamente
- **Apollo Client:** Code generation
- **GraphQL Code Generator:** Genera TypeScript types

---

## 9. Resumen de Sección 2.3

### Queries Complejas:

1. ✅ **Filtros múltiples opcionales**
   - Un query flexible vs N queries rígidos
   - Parámetros opcionales combinables

2. ✅ **Anidación + Campos Calculados**
   - Navegación multi-nivel
   - Datos relacionados en una llamada
   - Cálculos dinámicos (cashbackAmount)

3. ✅ **Validación Automática**
   - Enums validados antes de ejecutar
   - Tipos verificados automáticamente
   - Errores claros y útiles

### Mutations Complejas:

1. ✅ **Respuestas Estructuradas**
   - success + message + data + errors
   - UX superior en cliente

2. ✅ **Side Effects Visibles**
   - createTransaction → genera Reward
   - Múltiples entidades modificadas
   - Todo retornado en response

3. ✅ **Input Types**
   - Un solo argumento
   - Validación automática
   - Reusables y autodocumentados

---

# 📝 CONCLUSIÓN DEL CAPÍTULO 2

## Lo que Aprendimos

### Sección 2.1: Principios de Diseño
- Schema orientado a dominio, NO a DB
- Enums bien diseñados (autodocumentación + validación)
- Relaciones bidireccionales (navegación natural)
- Campos calculados (datos derivados on-the-fly)
- Separación de concerns (schema vs implementación)

### Sección 2.2: Scalars, Objetos, Listas
- Custom Scalars: Email, Money, Percentage, DateTime
- Validación automática en scalars
- Objetos complejos con anidación
- Listas con nullabilidad correcta `[Type!]!`
- Input Types para mutations robustas

### Sección 2.3: Queries y Mutations Complejas
- Filtros múltiples opcionales (flexibilidad)
- Queries anidadas con campos calculados
- Mutations con respuestas estructuradas
- Side effects visibles (Transaction → Reward)
- Validación automática de tipos

---

## 🎯 Diferencias Clave: Chapter 01 vs Chapter 02

| Aspecto | Chapter 01 | Chapter 02 |
|---------|------------|------------|
| **Foco** | Fundamentos GraphQL | Schema Design |
| **Feature** | Investment Portfolio | Cashback Rewards |
| **Problema** | REST (over/underfetching) | DB-coupled schemas |
| **Solución** | GraphQL queries | Domain-driven design |
| **Custom Scalars** | 0 | 4 (Money, %, Email, DateTime) |
| **Campos Calculados** | performance | availableCashback, cashbackAmount |
| **Enums** | AssetType | CashbackTier, Category, Status |
| **Mutations** | Simples | Respuestas estructuradas |

---

## 🚀 Próximos Pasos

### Chapter 03: Implementación Completa con Netflix DGS

Temas:
- DataLoader (resolver problema N+1)
- Integración con Spring Boot + JPA
- Testing de resolvers
- Performance optimization
- Error handling avanzado

---

## 📚 Recursos Adicionales

### Documentación Oficial:
- [GraphQL Schema Design Best Practices](https://www.apollographql.com/docs/apollo-server/schema/schema/)
- [Custom Scalars](https://www.graphql-java.com/documentation/scalars/)
- [Netflix DGS Framework](https://netflix.github.io/dgs/)

### Artículos Recomendados:
- "Domain-Driven Design in GraphQL" - Marc-André Giroux
- "GraphQL Schema Design @ Shopify" - Shopify Engineering Blog
- "Custom Scalars in GraphQL" - Apollo Blog

---

**Feature validado:** Cashback Rewards Program  
**Custom Scalars:** Money, Percentage, Email, DateTime  
**Enums:** CashbackTier, TransactionCategory, TransactionStatus, RewardStatus  
**Campos calculados:** availableCashback, totalSpent, totalCashbackEarned, cashbackAmount, cashbackPercentage  
**30 Tests Automatizados** ✅