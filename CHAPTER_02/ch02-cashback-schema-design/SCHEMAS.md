# Capítulo 2: Diseño de Schemas GraphQL

## ¿De qué trata este capítulo?

El Capítulo 2 se enfoca en **cómo diseñar un schema GraphQL profesional**. Usando el ejemplo de **Cashback Rewards Program**, aprenderás las buenas prácticas que distinguen un schema amateur de uno enterprise-ready.

---

## Las 8 Reglas de Oro del Diseño de Schemas

### 1. Domain-Driven Design
Los tipos representan **entidades reales del negocio**, no estructuras técnicas.
```graphql
# ✅ BIEN: Orientado al dominio
type User {
  tier: CashbackTier!
  rewards: [Reward!]!
}

type Transaction {
  cashbackAmount: Money!
}

# ❌ MAL: Orientado a la base de datos
type UserTable {
  tier_id: Int!
  reward_ids: [Int!]!
}
```

### 2. Separación Input/Output
Los tipos de entrada (mutations) son **diferentes** de los tipos de salida.
```graphql
# OUTPUT: Lo que retorna el servidor (tiene campos calculados, relaciones)
type Transaction {
  id: ID!
  user: User!                    # Relación navegable
  cashbackAmount: Money!         # Campo calculado
  cashbackPercentage: Percentage!
}

# INPUT: Lo que envía el cliente (solo datos necesarios para crear)
input CreateTransactionInput {
  userId: ID!                    # Solo el ID, no el objeto completo
  amount: Money!
  category: TransactionCategory!
  merchantName: String!
}
```

**¿Por qué?** Evolucionan independientemente. Puedes agregar campos al output sin afectar el input.

### 3. Custom Scalars
Tipos semánticos que **validan automáticamente** y documentan la intención.
```graphql
scalar DateTime      # ISO-8601: "2025-11-15T10:30:00Z"
scalar Money         # Decimal preciso: "125.50" (sin errores de flotantes)
scalar Percentage    # 0-100: "15.5"
scalar Email         # Valida formato: "user@example.com"
```
```graphql
# ❌ MAL: Sin semántica
type Transaction {
  amount: Float!        # ¿Pesos? ¿Dólares? ¿Centavos?
  date: String!         # ¿Qué formato?
}

# ✅ BIEN: Semántica clara
type Transaction {
  amount: Money!        # Obvio: es dinero
  date: DateTime!       # Obvio: ISO-8601 con timezone
}
```

### 4. Nullabilidad Inteligente
```graphql
# ! = NUNCA puede ser null
id: ID!                    # Siempre tiene ID
email: Email!              # Siempre tiene email

# Sin ! = PUEDE ser null
description: String        # Opcional
redeemedAt: DateTime       # Solo si fue canjeado

# [Type!]! = Lista obligatoria con elementos obligatorios
rewards: [Reward!]!        # Siempre retorna lista (puede ser vacía [])

# [Type] = Lista opcional con elementos opcionales
legacyRewards: [Reward]    # Puede ser null, elementos pueden ser null
```

### 5. Relaciones Bidireccionales
Navega en **ambas direcciones** entre entidades relacionadas.
```graphql
type User {
  id: ID!
  transactions: [Transaction!]!   # User → Transactions (1:N)
  rewards: [Reward!]!             # User → Rewards (1:N)
}

type Transaction {
  id: ID!
  user: User!                     # Transaction → User (N:1)
  reward: Reward                  # Transaction → Reward (1:1)
}

type Reward {
  id: ID!
  user: User!                     # Reward → User (N:1)
  transaction: Transaction!       # Reward → Transaction (1:1)
}
```

**Ventaja:** El cliente decide desde dónde navegar.
```graphql
# Desde Usuario
query { user(id: "1") { transactions { merchantName } } }

# Desde Transacción
query { transaction(id: "99") { user { fullName } } }
```

### 6. Campos Calculados
Campos que **no existen en la base de datos**, se calculan en el resolver.
```graphql
type User {
  # Campos de BD
  id: ID!
  email: Email!
  tier: CashbackTier!
  
  # CAMPOS CALCULADOS (se computan en Java)
  availableCashback: Money!      # SUM de rewards con status=AVAILABLE
  totalCashbackEarned: Money!    # SUM histórico de todos los rewards
  totalSpent: Money!             # SUM de todas las transacciones
}

type Transaction {
  amount: Money!
  category: TransactionCategory!
  
  # CALCULADOS según tier del usuario y categoría
  cashbackAmount: Money!         # amount × cashbackPercentage
  cashbackPercentage: Percentage! # tier.basePercentage × category.multiplier
}
```

### 7. Enums Descriptivos
Estados y categorías como **conjuntos cerrados**.
```graphql
enum CashbackTier {
  BRONZE      # 1% cashback
  SILVER      # 2% cashback
  GOLD        # 3% cashback
  PLATINUM    # 5% cashback
}

enum TransactionStatus {
  PENDING     # Esperando confirmación
  CONFIRMED   # Cashback calculado
  CANCELLED   # Sin cashback
  REFUNDED    # Cashback revertido
}

enum RewardStatus {
  PENDING     # 30 días de espera
  AVAILABLE   # Listo para canjear
  REDEEMED    # Ya canjeado
  EXPIRED     # Perdido (12 meses)
}
```

**Ventaja:** GraphQL rechaza automáticamente valores inválidos.

### 8. Responses Estructuradas
Todas las mutations retornan **el mismo patrón**.
```graphql
type TransactionResponse {
  success: Boolean!       # ¿Funcionó?
  message: String!        # Mensaje para UI
  transaction: Transaction # Objeto creado (null si falló)
}

type RedemptionResponse {
  success: Boolean!
  message: String!
  redeemedAmount: Money
  remainingCashback: Money
  rewards: [Reward!]      # Rewards afectados
}
```

---

## Modelo de Dominio: Cashback Rewards
```
┌─────────────┐       1:N        ┌─────────────────┐
│    User     │─────────────────→│   Transaction   │
│             │                  │                 │
│ - tier      │       1:N        │ - amount        │
│ - email     │─────────────────→│ - category      │
│             │                  │ - cashbackAmt   │
└─────────────┘                  └────────┬────────┘
                                          │ 1:1
                                          ▼
                                 ┌─────────────────┐
                                 │     Reward      │
                                 │                 │
                                 │ - amount        │
                                 │ - status        │
                                 │ - expiresAt     │
                                 └─────────────────┘
```

---

## Fórmula del Cashback
```
cashbackAmount = transactionAmount × (tierPercentage × categoryMultiplier)
```

| Tier     | Base % |
|----------|--------|
| BRONZE   | 1%     |
| SILVER   | 2%     |
| GOLD     | 3%     |
| PLATINUM | 5%     |

| Categoría    | Multiplier |
|--------------|------------|
| TRAVEL       | 3x         |
| RESTAURANTS  | 2x         |
| GROCERIES    | 1.5x       |
| Otras        | 1x         |

**Ejemplo:** Usuario GOLD compra $100 en RESTAURANTS:
```
$100 × (3% × 2) = $100 × 6% = $6.00 cashback
```

---

## Ciclo de Vida de un Reward
```
Transaction CONFIRMED
        │
        ▼
   ┌─────────┐   30 días   ┌───────────┐
   │ PENDING │────────────→│ AVAILABLE │
   └─────────┘             └─────┬─────┘
                                 │
              ┌──────────────────┼──────────────────┐
              ▼                  │                  ▼
        ┌──────────┐             │           ┌───────────┐
        │ REDEEMED │             │           │  EXPIRED  │
        └──────────┘             │           └───────────┘
         (canjeado)        12 meses sin       (se pierde)
                             canjear
```

---

## Resumen: Input vs Output vs Response

| Propósito | Tipo | Ejemplo |
|-----------|------|---------|
| **Lo que envía el cliente** | `input` | `CreateTransactionInput` |
| **Entidad del dominio** | `type` | `Transaction`, `User` |
| **Respuesta de mutation** | `type` | `TransactionResponse` |
```graphql
# El cliente ENVÍA input
mutation {
  createTransaction(input: CreateTransactionInput!) {
    # El servidor RETORNA response con type anidado
    success
    message
    transaction {  # ← type Transaction
      id
      cashbackAmount
      user { fullName }  # ← Navegación a type User
    }
  }
}
```