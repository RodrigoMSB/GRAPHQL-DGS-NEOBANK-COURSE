# CHAPTER 03: Cashback Service with Netflix DGS Framework

## 📋 Descripción

Implementación completa del sistema de **Cashback Rewards** usando Netflix DGS Framework sobre Spring Boot.

Este capítulo cubre las 5 secciones del temario:
- ✅ **3.1**: Introducción al framework DGS y estructura de proyecto
- ✅ **3.2**: Definición del schema y generación automática de clases
- ✅ **3.3**: Implementación de resolvers con @DgsData
- ✅ **3.4**: Mutations y lógica de negocio integrada
- ✅ **3.5**: Optimización con DataLoader y prevención del problema N+1

## 🏗️ Estructura del Proyecto

```
dgs-cashback-service/
├── src/main/java/com/neobank/cashback/
│   ├── CashbackServiceDgsApplication.java   # Aplicación principal
│   ├── domain/                              # Modelos de dominio (POJOs)
│   │   ├── User.java
│   │   ├── Reward.java
│   │   ├── CashbackRule.java
│   │   ├── TierMultipliers.java
│   │   ├── RedemptionResult.java
│   │   ├── RewardStatus.java
│   │   ├── TransactionCategory.java
│   │   └── RewardTier.java
│   ├── repository/                          # Repositorios in-memory
│   │   ├── UserRepository.java
│   │   ├── RewardRepository.java
│   │   └── CashbackRuleRepository.java
│   ├── service/                             # Lógica de negocio
│   │   └── CashbackService.java
│   ├── datafetcher/                         # Resolvers GraphQL
│   │   ├── QueryDataFetcher.java           # Queries
│   │   ├── MutationDataFetcher.java        # Mutations
│   │   └── NestedFieldDataFetcher.java     # Campos anidados
│   └── dataloader/                          # DataLoaders para N+1
│       ├── UserDataLoader.java
│       └── RewardsDataLoader.java
└── src/main/resources/
    ├── schema/
    │   └── cashback-service.graphqls        # Schema GraphQL
    └── application.yml                      # Configuración
```

## 🚀 Cómo Ejecutar

### Prerrequisitos

- Java 17 o superior
- Maven 3.6+

### Pasos

1. **Compilar el proyecto:**
   ```bash
   mvn clean install
   ```

2. **Ejecutar la aplicación:**
   ```bash
   mvn spring-boot:run
   ```

3. **Acceder a GraphiQL:**
   ```
   http://localhost:8080/graphiql
   ```

4. **Endpoint GraphQL:**
   ```
   http://localhost:8080/graphql
   ```

## 📊 Queries de Ejemplo

### 1. Obtener usuario con sus rewards

```graphql
query {
  user(id: "user-001") {
    fullName
    tier
    availableCashback
    rewards {
      amount
      category
      status
      earnedAt
    }
  }
}
```

### 2. Filtrar rewards por categoría y estado

```graphql
query {
  rewards(filter: {
    category: TRAVEL
    status: ACTIVE
    minAmount: 50.00
  }) {
    id
    amount
    category
    user {
      fullName
      tier
    }
  }
}
```

### 3. Resumen de cashback de un usuario

```graphql
query {
  rewardsSummary(userId: "user-003") {
    totalEarned
    totalRedeemed
    availableBalance
    rewardsByCategory {
      category
      totalAmount
      count
    }
    rewardsByStatus {
      status
      totalAmount
      count
    }
  }
}
```

### 4. Calcular cashback estimado

```graphql
query {
  calculateCashback(
    userId: "user-004"
    transactionAmount: 1000.00
    category: TRAVEL
  )
}
```

### 5. Listar reglas de cashback

```graphql
query {
  cashbackRules {
    category
    basePercentage
    tierMultipliers {
      bronze
      silver
      gold
      platinum
    }
    maxCashbackPerTransaction
  }
}
```

## 🔄 Mutations de Ejemplo

### 1. Crear una nueva reward

```graphql
mutation {
  createReward(input: {
    userId: "user-001"
    transactionId: "txn-999"
    transactionAmount: 500.00
    category: GROCERIES
    description: "Compra en supermercado"
  }) {
    id
    amount
    status
    category
    user {
      fullName
      availableCashback
    }
  }
}
```

### 2. Redimir cashback

```graphql
mutation {
  redeemCashback(input: {
    userId: "user-002"
    amount: 100.00
    destinationAccount: "ACC-123456"
  }) {
    success
    message
    redeemedAmount
    newBalance
    transactionId
  }
}
```

### 3. Actualizar estado de reward

```graphql
mutation {
  updateRewardStatus(input: {
    rewardId: "reward-100"
    newStatus: CANCELLED
    reason: "Fraude detectado"
  }) {
    id
    status
    description
  }
}
```

### 4. Expirar rewards vencidas

```graphql
mutation {
  expireOldRewards
}
```

### 5. Upgrade de tier

```graphql
mutation {
  upgradeUserTier(
    userId: "user-001"
    newTier: GOLD
  ) {
    fullName
    tier
  }
}
```

## 🎯 Conceptos Clave Demostrados

### 1. Netflix DGS Framework
- Configuración con Spring Boot
- Anotaciones `@DgsQuery`, `@DgsMutation`, `@DgsData`
- Auto-discovery de schema GraphQL

### 2. Schema Design
- Custom scalars (Money, Date, DateTime)
- Enums para tipos cerrados
- Input types para mutations
- Nested types para grafos complejos

### 3. Resolvers
- Query resolvers para lecturas
- Mutation resolvers para escrituras
- Nested field resolvers para navegación

### 4. Lógica de Negocio
- Service layer con validaciones
- Cálculo de cashback según reglas y tier
- Operaciones atómicas (redención)

### 5. DataLoader (★ CLAVE)
- Solución al problema N+1
- Batch loading eficiente
- Per-request caching
- Comparación antes/después

## 📝 Notas Pedagógicas

### Usuarios Pre-cargados

El sistema viene con 5 usuarios de ejemplo:

1. **user-001**: María García (BRONZE) - $450.30 disponible
2. **user-002**: Carlos Rodríguez (SILVER) - $1,200.50 disponible
3. **user-003**: Ana Martínez (GOLD) - $2,850.00 disponible
4. **user-004**: Roberto López (PLATINUM) - $8,920.15 disponible
5. **user-005**: Laura Fernández (BRONZE) - $180.25 disponible

### Rewards Pre-cargadas

Cada usuario tiene múltiples rewards en diferentes estados (ACTIVE, REDEEMED, EXPIRED) para probar todas las queries y mutations.

### Reglas de Cashback

- **GROCERIES**: 2% base
- **RESTAURANTS**: 1.5% base
- **TRAVEL**: 3% base (el más alto)
- **UTILITIES**: 0.5% base (el más bajo)

Los porcentajes se multiplican según el tier:
- BRONZE: 1.0x
- SILVER: 1.5x
- GOLD: 2.0x
- PLATINUM: 3.0x

## 🔍 Para Profundizar

- Lee los comentarios en `UserDataLoader.java` para entender el problema N+1
- Compara `NestedFieldDataFetcher.java` antes/después de DataLoader
- Observa los logs en consola cuando ejecutes queries que usan DataLoader
- Experimenta deshabilitando DataLoader para ver la diferencia

---

**Desarrollado para:** El Mejor Curso de GraphQL del Mundo  
**Capítulo:** 03 - Implementación de GraphQL con DGS (Netflix Java)  
**Stack:** Java 17, Spring Boot 3.2, Netflix DGS 8.1
