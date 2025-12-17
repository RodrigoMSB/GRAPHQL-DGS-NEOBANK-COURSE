# Netflix DGS Framework - Explicación Completa

## 🎯 ¿Qué es DGS?

**DGS** = **D**omain **G**raph **S**ervice

Es el framework que Netflix creó internamente para construir sus servicios GraphQL, y lo liberaron como open source en 2021. Está construido sobre Spring Boot y graphql-java.

---

## 💡 Analogía: El Traductor Universal

Imagina que DGS es como un **traductor universal** entre dos mundos:

```
┌─────────────────┐                           ┌─────────────────┐
│   MUNDO DEL     │                           │   MUNDO DE      │
│   CLIENTE       │  ←── DGS traduce ───→     │   TU CÓDIGO     │
│   (GraphQL)     │                           │   (Java/Spring) │
└─────────────────┘                           └─────────────────┘

El cliente habla GraphQL          DGS            Tú escribes Java
query { user(id: "1") }    ──────────────→      public User user(String id)
```

---

## 🔧 ¿Qué hace DGS exactamente?

### 1️⃣ **Lee tu Schema GraphQL automáticamente**

Cuando arranca tu aplicación, DGS:

```
src/main/resources/schema/
└── cashback-service.graphqls   ← DGS lo encuentra automáticamente
```

```graphql
# DGS lee esto y entiende la estructura
type Query {
    user(id: ID!): User
    rewards: [Reward!]!
}

type User {
    id: ID!
    fullName: String!
    rewards: [Reward!]!    # ← DGS sabe que esto necesita un resolver
}
```

---

### 2️⃣ **Conecta tus métodos Java con el Schema**

Tú escribes métodos Java con anotaciones, y DGS los conecta automáticamente:

```java
@DgsComponent
public class QueryDataFetcher {
    
    // DGS ve @DgsQuery y lo conecta con "Query.user" del schema
    @DgsQuery
    public User user(@InputArgument String id) {
        return userRepository.findById(id);
    }
    
    // DGS ve @DgsQuery y lo conecta con "Query.rewards" del schema
    @DgsQuery
    public List<Reward> rewards() {
        return rewardRepository.findAll();
    }
}
```

**La magia:** DGS usa el **nombre del método** para encontrar el campo en el schema.
- Método `user()` → Campo `user` en `type Query`
- Método `rewards()` → Campo `rewards` en `type Query`

---

### 3️⃣ **Resuelve campos anidados**

Cuando el schema tiene relaciones:

```graphql
type User {
    id: ID!
    fullName: String!
    rewards: [Reward!]!    # ← Campo anidado
}
```

DGS necesita saber cómo obtener `rewards` cuando alguien pide:

```graphql
query {
    user(id: "1") {
        fullName
        rewards {       # ← ¿De dónde saco esto?
            amount
        }
    }
}
```

Tú le dices con `@DgsData`:

```java
@DgsComponent
public class NestedFieldDataFetcher {
    
    // "Para el campo 'rewards' del tipo 'User', usa este método"
    @DgsData(parentType = "User", field = "rewards")
    public List<Reward> userRewards(DgsDataFetchingEnvironment dfe) {
        User user = dfe.getSource();  // DGS te da el User padre
        return rewardRepository.findByUserId(user.getId());
    }
}
```

---

### 4️⃣ **Ejecuta la Query paso a paso**

Cuando llega una query como esta:

```graphql
query {
    user(id: "user-001") {
        fullName
        rewards {
            amount
            category
        }
    }
}
```

DGS ejecuta en este orden:

```
PASO 1: Resolver "user(id: user-001)"
        └── Llama a: QueryDataFetcher.user("user-001")
        └── Retorna: User { id: "user-001", fullName: "María García", ... }

PASO 2: Para el campo "fullName"
        └── DGS ve que User tiene getFullName()
        └── Llama automáticamente: user.getFullName()
        └── Retorna: "María García"

PASO 3: Para el campo "rewards"
        └── DGS busca un @DgsData(parentType="User", field="rewards")
        └── Llama a: NestedFieldDataFetcher.userRewards(dfe)
        └── Retorna: [Reward, Reward, Reward]

PASO 4: Para cada Reward, obtener "amount" y "category"
        └── Llama: reward.getAmount(), reward.getCategory()
```

---

## 🆚 Comparación: Spring GraphQL vs Netflix DGS

| Aspecto | Spring GraphQL (Cap. 2) | Netflix DGS (Cap. 3) |
|---------|------------------------|----------------------|
| **Anotación para Query** | `@QueryMapping` | `@DgsQuery` |
| **Anotación para Mutation** | `@MutationMapping` | `@DgsMutation` |
| **Anotación para campo anidado** | `@SchemaMapping` | `@DgsData` |
| **Clase de componente** | `@Controller` | `@DgsComponent` |
| **DataLoader** | Configuración manual | `@DgsDataLoader` (más fácil) |
| **Code generation** | No incluido | Incluido (genera clases desde schema) |
| **Quién lo mantiene** | Spring Team (VMware) | Netflix |

---

## 🔥 El Problema N+1 y DataLoader

### El problema:

```graphql
query {
    users {           # 1 query: obtener 100 usuarios
        fullName
        rewards {     # 100 queries: una por cada usuario 😱
            amount
        }
    }
}
# Total: 101 queries a la base de datos
```

### La solución de DGS: DataLoader

```java
@DgsDataLoader(name = "rewards")
public class RewardsDataLoader implements BatchLoader<String, List<Reward>> {
    
    @Override
    public CompletionStage<List<List<Reward>>> load(List<String> userIds) {
        // DGS acumula todos los userIds y llama UNA SOLA VEZ
        // userIds = ["user-001", "user-002", ..., "user-100"]
        
        System.out.println("🔥 Batch loading para " + userIds.size() + " usuarios");
        
        // 1 sola query para todos
        List<Reward> allRewards = rewardRepository.findByUserIdIn(userIds);
        
        // Agrupar por userId y retornar
        Map<String, List<Reward>> grouped = allRewards.stream()
            .collect(Collectors.groupingBy(Reward::getUserId));
        
        return CompletableFuture.completedFuture(
            userIds.stream()
                .map(id -> grouped.getOrDefault(id, List.of()))
                .toList()
        );
    }
}
```

**Resultado:** De 101 queries a solo 2 queries 🚀

---

## 📊 Flujo completo de una Request

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENTE                                     │
│  POST /graphql                                                      │
│  { "query": "{ user(id: \"1\") { fullName rewards { amount } } }" } │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      SPRING BOOT + DGS                              │
│  1. Endpoint /graphql recibe la request                             │
│  2. DGS parsea el query string                                      │
│  3. DGS valida contra el schema                                     │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      DGS EXECUTION ENGINE                           │
│  4. Busca @DgsQuery para "user"  →  QueryDataFetcher.user()         │
│  5. Ejecuta y obtiene User                                          │
│  6. Para "fullName" → user.getFullName()                            │
│  7. Para "rewards" → busca @DgsData o DataLoader                    │
│  8. Ejecuta DataLoader en batch                                     │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      RESPUESTA JSON                                 │
│  {                                                                  │
│    "data": {                                                        │
│      "user": {                                                      │
│        "fullName": "María García",                                  │
│        "rewards": [                                                 │
│          { "amount": 15.50 },                                       │
│          { "amount": 8.20 }                                         │
│        ]                                                            │
│      }                                                              │
│    }                                                                │
│  }                                                                  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🎓 Resumen: ¿Por qué usar DGS?

| Ventaja | Explicación |
|---------|-------------|
| **Menos código** | DGS conecta automáticamente métodos con el schema |
| **DataLoader integrado** | Solución elegante al problema N+1 |
| **Probado en producción** | Netflix lo usa para millones de requests |
| **Spring Boot nativo** | Se integra perfectamente con el ecosistema Spring |
| **Code generation** | Puede generar clases Java desde el schema |
| **Testing utilities** | Incluye herramientas para tests de integración |
