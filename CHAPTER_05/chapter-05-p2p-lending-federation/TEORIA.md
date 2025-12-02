# 📚 TEORÍA - Chapter 05: Federación con Apollo Federation y DGS

**GraphQL con Netflix DGS Framework**  
**Capítulo 5:** De Monolito GraphQL a Arquitectura Federada

---

## 📖 Índice

1. [Introducción](#introducción)
2. [Sección 5.1: Introducción a la Arquitectura Federada](#sección-51-introducción-a-la-arquitectura-federada)
3. [Sección 5.2: Fundamentos de Apollo Federation](#sección-52-fundamentos-de-apollo-federation)
4. [Sección 5.3: Creación de Subgrafos con DGS](#sección-53-creación-de-subgrafos-con-dgs)
5. [Sección 5.4: Buenas Prácticas y Gobernanza](#sección-54-buenas-prácticas-y-gobernanza)
6. [Conceptos Avanzados](#conceptos-avanzados)
7. [Antipatrones](#antipatrones)
8. [Casos de Uso Reales](#casos-de-uso-reales)

---

## Introducción

### 🎯 El Problema del Monolito GraphQL

En los capítulos anteriores construimos servicios GraphQL como **monolitos**:

```
┌─────────────────────────────────────────┐
│        UN SOLO SERVICIO GRAPHQL         │
│                                         │
│  schema {                               │
│    - Users                              │
│    - Products                           │
│    - Orders                             │
│    - Payments                           │
│    - Notifications                      │
│    - Analytics                          │
│  }                                      │
│                                         │
│  Todo en un solo schema                │
│  Todo en un solo deploy                 │
│  Todo gestionado por un equipo         │
└─────────────────────────────────────────┘
```

**Problemas que surgen:**

1. **Escalabilidad del equipo:** 50 desarrolladores tocando el mismo schema = caos
2. **Deploy acoplado:** Cambio en Users requiere deploy de TODO
3. **Ownership difuso:** ¿Quién es responsable de qué?
4. **Testing complejo:** Probar todo el sistema para cambio pequeño
5. **Tecnología única:** Todo el stack debe ser igual

**Analogía:**
> Un monolito GraphQL es como un edificio de oficinas donde TODAS las empresas comparten un solo recepcionista. Si la recepcionista se enferma, TODO el edificio se detiene.

### 🌐 La Solución: Arquitectura Federada

**Apollo Federation** permite dividir un schema en **subgrafos independientes**:

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   USERS      │    │   PRODUCTS   │    │   ORDERS     │
│   subgraph   │    │   subgraph   │    │   subgraph   │
│              │    │              │    │              │
│  Team: Auth  │    │  Team: Cat.  │    │  Team: Sales │
│  Deploy: Ind.│    │  Deploy: Ind.│    │  Deploy: Ind.│
└──────────────┘    └──────────────┘    └──────────────┘
        ↓                   ↓                    ↓
        └───────────────────┴────────────────────┘
                           ↓
                  ┌─────────────────┐
                  │  APOLLO ROUTER  │
                  │   (Supergraph)  │
                  └─────────────────┘
                           ↓
                      CLIENTE
```

**Ventajas:**
- ✅ Equipos autónomos
- ✅ Deploy independiente
- ✅ Escalamiento por dominio
- ✅ Ownership claro
- ✅ Tecnología heterogénea (Java, Node, Go...)

---

## Sección 5.1: Introducción a la Arquitectura Federada

### 🏛️ Arquitectura Monolítica vs Federada

#### Monolito GraphQL

```graphql
# UN solo schema gigante
type Query {
  # Users domain
  user(id: ID!): User
  users: [User!]!
  
  # Products domain
  product(id: ID!): Product
  products: [Product!]!
  
  # Orders domain
  order(id: ID!): Order
  orders: [Order!]!
  
  # ... 50 queries más
}

type User {
  id: ID!
  email: String!
  orders: [Order!]!      # ¿De dónde viene esto?
  wishlist: [Product!]!  # ¿Quién lo mantiene?
}
```

**Problemas:**
- Schema de 5000+ líneas
- Conflictos de merge en Git
- Deploy de TODO por cambio mínimo
- Responsabilidades mezcladas

#### Arquitectura Federada

**Subgrafo Users:**
```graphql
type User @key(fields: "id") {
  id: ID!
  email: String!
  name: String!
}
```

**Subgrafo Orders:**
```graphql
type User @key(fields: "id") @extends {
  id: ID! @external
  orders: [Order!]!  # Agregado por Orders domain
}

type Order @key(fields: "id") {
  id: ID!
  total: Float!
  user: User!  # Referencia a Users subgraph
}
```

**Subgrafo Products:**
```graphql
type User @key(fields: "id") @extends {
  id: ID! @external
  wishlist: [Product!]!  # Agregado por Products domain
}
```

**Supergraph (compuesto):**
```graphql
type User {
  id: ID!
  email: String!
  name: String!
  orders: [Order!]!      # De Orders subgraph
  wishlist: [Product!]!  # De Products subgraph
}
```

### 📊 Comparación Detallada

| Aspecto | Monolito | Federación |
|---------|----------|-----------|
| **Schema** | Un archivo gigante | Múltiples schemas pequeños |
| **Equipos** | 1 equipo central | N equipos por dominio |
| **Deploy** | Todo junto | Independiente por subgrafo |
| **Escalabilidad** | Vertical (más RAM/CPU) | Horizontal (más instancias por dominio) |
| **Testing** | Todo el sistema | Solo el subgrafo |
| **Ownership** | Difuso | Claro (DDD) |
| **Latencia** | Baja (local) | Media (network hops) |
| **Complejidad** | Baja al inicio | Alta (orquestación) |
| **Costo Operacional** | 1 servicio | N servicios + Gateway |

### 🎯 ¿Cuándo Usar Federación?

**✅ Usa Federación cuando:**
- Tienes **3+ equipos** trabajando en el backend
- Necesitas **deploy independiente** por dominio
- Arquitectura de **microservicios** ya existe
- Dominios de negocio están **claramente separados**
- Escalamiento **no uniforme** (Users escala 10x más que Reports)

**❌ NO uses Federación cuando:**
- Equipo pequeño (< 10 personas)
- Producto en etapa MVP
- Dominios no están claros
- No tienes experiencia con microservicios
- Latencia es crítica (< 50ms)

**Analogía:**
> Federation es como tener restaurantes especializados (pizzería, sushi bar, parrilla) en lugar de un buffet gigante. Cada uno hace UNA cosa bien, pero necesitas un "menú unificado" para que el cliente no se confunda.

### 🏗️ Bounded Contexts (DDD)

**Domain-Driven Design** define **bounded contexts**:

```
┌────────────────────────────────────┐
│      USERS CONTEXT                 │
│                                    │
│  User                              │
│  ├─ Identity (email, password)     │
│  ├─ Profile (name, avatar)         │
│  └─ Preferences                    │
│                                    │
│  Responsibilities:                 │
│  - Authentication                  │
│  - Profile management              │
│  - User preferences                │
└────────────────────────────────────┘

┌────────────────────────────────────┐
│      LOANS CONTEXT                 │
│                                    │
│  Loan                              │
│  ├─ Financial terms                │
│  ├─ Status                         │
│  └─ Parties                        │
│                                    │
│  User (external reference)         │
│  └─ Loan-specific data only        │
│                                    │
│  Responsibilities:                 │
│  - Loan creation                   │
│  - Interest calculation            │
│  - Loan status management          │
└────────────────────────────────────┘
```

**Regla de Oro:** Cada subgrafo es **owner** de su bounded context.

---

## Sección 5.2: Fundamentos de Apollo Federation

### 🔑 Directivas de Federation v2

Apollo Federation v2 introduce directivas especiales para componer schemas.

#### **@key** - Marca Entidades Federadas

```graphql
type User @key(fields: "id") {
  id: ID!
  email: String!
}
```

**Significado:**
- "User es una entidad que puede ser referenciada desde otros subgrafos"
- "El campo `id` es suficiente para identificar un User único"
- "Este subgrafo puede resolver un User dado solo su `id`"

**Implementación en DGS:**
```java
@DgsEntityFetcher(name = "User")
public User resolveUser(Map<String, Object> values) {
    String id = (String) values.get("id");
    return usersService.getUserById(id);
}
```

**Compound Keys:**
```graphql
type Product @key(fields: "sku storeId") {
  sku: String!
  storeId: String!
  name: String!
}
```

#### **@extends** - Extiende Tipo de Otro Subgrafo

```graphql
type User @key(fields: "id") @extends {
  id: ID! @external
  orders: [Order!]!  # Nuevo campo agregado
}
```

**Significado:**
- "Este subgrafo NO es el owner de User"
- "Voy a AGREGAR campos al User definido en otro subgrafo"
- "El campo `id` viene de afuera (@external)"

**Ejemplo completo:**

**Subgrafo Users (owner):**
```graphql
type User @key(fields: "id") {
  id: ID!
  email: String!
  name: String!
}
```

**Subgrafo Orders (extiende):**
```graphql
type User @key(fields: "id") @extends {
  id: ID! @external
  orders: [Order!]!
}
```

**Resultado en Supergraph:**
```graphql
type User {
  id: ID!
  email: String!
  name: String!
  orders: [Order!]!  # Merged!
}
```

#### **@external** - Campo Definido Externamente

```graphql
type User @extends {
  id: ID! @external
  email: String! @external
}
```

**Significado:**
- "Este campo NO es resuelto por este subgrafo"
- "Lo uso como referencia, pero viene de otro lado"

**Regla:** Si usas `@extends`, DEBES marcar los campos clave como `@external`.

#### **@requires** - Campo Necesita Otros Campos

```graphql
type Product @key(fields: "id") {
  id: ID!
  price: Float!
  weight: Float!
  
  shippingEstimate: String! @requires(fields: "price weight")
}
```

**Significado:**
- "Para resolver `shippingEstimate`, necesito que me pasen `price` y `weight`"
- Apollo Router automáticamente fetcha esos campos primero

**Uso:**
```graphql
{
  product(id: "123") {
    shippingEstimate  # Router fetch price + weight automáticamente
  }
}
```

#### **@provides** - Optimización de Fetching

```graphql
type Review {
  product: Product! @provides(fields: "name price")
  rating: Int!
}
```

**Significado:**
- "Cuando retorno `product`, ya incluyo `name` y `price`"
- "Apollo Router NO necesita ir al Products subgraph para esos campos"

**Optimización:**
```graphql
{
  reviews {
    product {
      name   # ✅ Ya viene con Review, no hace extra fetch
      description  # ❌ Requiere fetch a Products subgraph
    }
  }
}
```

#### **@shareable** - Campo en Múltiples Subgrafos

```graphql
type Product @key(fields: "id") {
  id: ID!
  name: String! @shareable
}
```

**Significado:**
- "Este campo puede ser resuelto por múltiples subgrafos"
- Útil para datos replicados (caching, denormalización)

### 🛠️ Herramientas del Ecosistema Apollo

#### **Apollo Router**

**¿Qué es?**
- Gateway moderno escrito en **Rust**
- Orquesta queries entre subgrafos
- Compone resultados
- Cachea entity resolution

**Ventajas vs Apollo Gateway (Node.js):**
- ⚡ 10x más rápido
- 📦 Menor uso de memoria
- 🔒 Mejor seguridad
- 🚀 Startup más rápido

**Configuración:**
```yaml
# router-config.yaml
supergraph:
  introspection: true

subgraphs:
  users:
    routing_url: http://localhost:8081/graphql
  
  loans:
    routing_url: http://localhost:8082/graphql
```

#### **Rover CLI**

**¿Qué es?**
- Herramienta de línea de comandos
- Compone supergraph schema
- Valida cambios (schema checks)
- Publica subgrafos

**Comandos comunes:**
```bash
# Componer supergraph localmente
rover supergraph compose --config ./supergraph.yaml

# Validar cambios antes de deploy
rover subgraph check my-graph@prod \
  --schema ./users-schema.graphqls \
  --name users

# Publicar subgrafo
rover subgraph publish my-graph@prod \
  --name users \
  --schema ./users-schema.graphqls \
  --routing-url http://users-service:8081/graphql
```

#### **Apollo Studio**

**¿Qué es?**
- Plataforma cloud de Apollo
- Schema registry
- Analytics de queries
- Monitoreo de performance

**Features:**
- 📊 Query metrics (latency, errors)
- 🔍 Schema explorer
- 🚨 Schema checks en CI/CD
- 📈 Field usage analytics

### 🔄 Flujo de Query Federation

**Query del cliente:**
```graphql
{
  user(id: "user-001") {
    email          # users-service
    fullName       # users-service
    loansAsBorrower {  # loans-service
      amount
      lender {     # users-service again!
        fullName
      }
    }
  }
}
```

**Query Plan generado por Apollo Router:**

```
┌─────────────────────────────────────────────────────────┐
│ STEP 1: Fetch from users-service                       │
├─────────────────────────────────────────────────────────┤
│ Query:                                                  │
│   { user(id: "user-001") {                             │
│       __typename                                        │
│       id                                                │
│       email                                             │
│       fullName                                          │
│     }                                                   │
│   }                                                     │
│                                                         │
│ Response:                                               │
│   { __typename: "User",                                 │
│     id: "user-001",                                     │
│     email: "alice@...",                                 │
│     fullName: "Alice Thompson" }                        │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ STEP 2: Fetch from loans-service                       │
├─────────────────────────────────────────────────────────┤
│ Query:                                                  │
│   { _entities(representations: [                        │
│       { __typename: "User", id: "user-001" }           │
│     ]) {                                                │
│       ... on User {                                     │
│         loansAsBorrower {                               │
│           amount                                        │
│           lender { __typename id }                      │
│         }                                               │
│       }                                                 │
│     }                                                   │
│   }                                                     │
│                                                         │
│ Response:                                               │
│   { loansAsBorrower: [                                  │
│       { amount: 25000,                                  │
│         lender: { __typename: "User", id: "user-002" }}│
│     ]}                                                  │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ STEP 3: Fetch from users-service (lender)              │
├─────────────────────────────────────────────────────────┤
│ Query:                                                  │
│   { _entities(representations: [                        │
│       { __typename: "User", id: "user-002" }           │
│     ]) {                                                │
│       ... on User { fullName }                          │
│     }                                                   │
│   }                                                     │
│                                                         │
│ Response:                                               │
│   { fullName: "Bob Martinez" }                          │
└─────────────────────────────────────────────────────────┘
                         ↓
                 MERGE RESULTS
```

**Resultado final:**
```json
{
  "data": {
    "user": {
      "email": "alice@...",
      "fullName": "Alice Thompson",
      "loansAsBorrower": [
        {
          "amount": 25000,
          "lender": {
            "fullName": "Bob Martinez"
          }
        }
      ]
    }
  }
}
```

---

## Sección 5.3: Creación de Subgrafos con DGS

### 📦 Subgrafo Users (Owner de User)

#### Schema con @key

```graphql
extend schema 
  @link(url: "https://specs.apollo.dev/federation/v2.3", 
        import: ["@key", "@shareable"])

type User @key(fields: "id") {
  id: ID!
  email: String!
  fullName: String!
  userType: UserType!
  lenderProfile: LenderProfile
  borrowerProfile: BorrowerProfile
  reputation: Float!
}

type Query {
  user(id: ID!): User
  users: [User!]!
}
```

#### Entity Fetcher

**El corazón de Federation en DGS:**

```java
@DgsComponent
public class UserEntityFetcher {
    
    private final UsersService service;
    
    /**
     * Apollo Router llama este método para resolver User
     * dado solo su ID
     */
    @DgsEntityFetcher(name = "User")
    public User resolveUser(Map<String, Object> values) {
        String id = (String) values.get("id");
        return service.getUserById(id);
    }
}
```

**Flujo:**
```
1. Loans service retorna: { __typename: "User", id: "user-001" }
2. Apollo Router ve que necesita más campos de User
3. Apollo Router llama: _entities(representations: [{ __typename: "User", id: "user-001" }])
4. DGS enruta a UserEntityFetcher.resolveUser()
5. Service retorna User completo
6. Apollo Router compone resultado
```

### 📦 Subgrafo Loans (Extiende User)

#### Schema con @extends

```graphql
extend schema 
  @link(url: "https://specs.apollo.dev/federation/v2.3", 
        import: ["@key", "@external", "@extends"])

# Referencia a User del otro subgrafo
type User @key(fields: "id") @extends {
  id: ID! @external
  
  # Campos agregados por Loans domain
  loansAsLender: [Loan!]!
  loansAsBorrower: [Loan!]!
}

type Loan @key(fields: "id") {
  id: ID!
  amount: Float!
  lender: User!    # Referencia a Users subgraph
  borrower: User!  # Referencia a Users subgraph
}
```

#### Resolver para Campos Extendidos

```java
@DgsComponent
public class LoansResolver {
    
    private final LoansService service;
    
    /**
     * Resolver para User.loansAsBorrower
     * Agrega este campo al tipo User
     */
    @DgsData(parentType = "User", field = "loansAsBorrower")
    public List<Loan> loansAsBorrower(Map<String, Object> user) {
        String userId = (String) user.get("id");
        return service.getLoansByBorrower(userId);
    }
    
    /**
     * Resolver para User.loansAsLender
     */
    @DgsData(parentType = "User", field = "loansAsLender")
    public List<Loan> loansAsLender(Map<String, Object> user) {
        String userId = (String) user.get("id");
        return service.getLoansByLender(userId);
    }
}
```

#### Retornar Entity References

**Cuando Loan referencia a User:**

```java
@DgsData(parentType = "Loan", field = "lender")
public Map<String, Object> lender(Loan loan) {
    // Retornar STUB, no User completo
    Map<String, Object> userRef = new HashMap<>();
    userRef.put("__typename", "User");
    userRef.put("id", loan.getLenderId());
    return userRef;
    
    // Apollo Router resolverá el resto
}
```

**¿Por qué stub?**
- Loans service NO tiene datos completos de User
- Solo conoce el ID del lender
- Apollo Router llamará a Users service para resolver

---

## Sección 5.4: Buenas Prácticas y Gobernanza

### 👑 Ownership Claro

**Principio fundamental:** Un subgrafo es **owner** de sus entidades.

**✅ BIEN:**

```
Users Service:
├─ OWNS: User (entidad base)
├─ OWNS: LenderProfile
├─ OWNS: BorrowerProfile
└─ RESUELVE: user(id), users()

Loans Service:
├─ OWNS: Loan
├─ EXTIENDE: User.loansAsBorrower
├─ EXTIENDE: User.loansAsLender
└─ RESUELVE: loan(id), loans()
```

**❌ MAL - Ownership ambiguo:**

```graphql
# users-service (❌ NO debería tener loan data)
type User {
  activeLoansCount: Int!  # Esto es del dominio Loans
  totalBorrowed: Float!   # Esto es del dominio Loans
}

# loans-service (❌ NO debería tener user data)
type Loan {
  lenderEmail: String!  # Esto es del dominio Users
  lenderName: String!   # Esto es del dominio Users
}
```

**✅ BIEN - Ownership respetado:**

```graphql
# users-service
type User @key(fields: "id") {
  id: ID!
  email: String!
  name: String!
}

# loans-service
type User @extends {
  id: ID! @external
  activeLoansCount: Int!  # Calculado desde Loans domain
}

type Loan {
  lender: User!  # Referencia, no data inline
}
```

### 📛 Naming Conventions

**Para entidades:**
```graphql
User           # ✅ Sustantivo singular
Product        # ✅ Claro y conciso
LoanRequest    # ✅ Compuesto descriptivo

UserData       # ❌ Muy genérico
Info           # ❌ Ambiguo
Result         # ❌ No dice qué es
```

**Para campos extendidos:**
```graphql
type User @extends {
  # ✅ Nombres descriptivos de dominio
  loansAsBorrower: [Loan!]!
  savingsGoals: [SavingsGoal!]!
  transactionHistory: [Transaction!]!
  
  # ❌ Nombres genéricos
  data: [JSON!]!
  items: [Item!]!
}
```

### 🔒 Evitar Dependencias Circulares

**❌ ANTIPATRÓN - Dependencia HTTP:**

```java
// users-service
@Service
public class UsersService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public User getUserWithLoans(String id) {
        User user = repository.findById(id);
        
        // ❌ MAL: HTTP call a loans-service
        Loan[] loans = restTemplate.getForObject(
            "http://loans-service/loans?userId=" + id,
            Loan[].class
        );
        
        user.setLoans(loans);
        return user;
    }
}
```

**Problemas:**
- Acoplamiento fuerte
- Latencia acumulativa
- Cascading failures
- Circular dependency risk

**✅ PATRÓN CORRECTO - Federation:**

```java
// users-service: Solo retorna User base
@DgsQuery
public User user(@InputArgument String id) {
    return usersService.getUserById(id);
}

// loans-service: Extiende User con loans
@DgsData(parentType = "User", field = "loans")
public List<Loan> loans(Map<String, Object> user) {
    String userId = (String) user.get("id");
    return loansService.getLoansByUser(userId);
}

// Apollo Router: Orquesta ambos
```

### 📐 Bounded Contexts y DDD

**Domain-Driven Design** aplicado a Federation:

```
┌─────────────────────────────────────┐
│   IDENTITY & ACCESS CONTEXT         │
│   (users-service)                   │
│                                     │
│   User                              │
│   ├─ Authentication                 │
│   ├─ Profile                        │
│   └─ Permissions                    │
│                                     │
│   Ubiquitous Language:              │
│   - "authenticated user"            │
│   - "user profile"                  │
│   - "access control"                │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│   LENDING CONTEXT                   │
│   (loans-service)                   │
│                                     │
│   Loan                              │
│   ├─ Principal                      │
│   ├─ Interest Rate                  │
│   ├─ Term                           │
│   └─ Parties (Lender, Borrower)    │
│                                     │
│   User (external)                   │
│   └─ Only as party reference        │
│                                     │
│   Ubiquitous Language:              │
│   - "loan origination"              │
│   - "debt service"                  │
│   - "credit risk"                   │
└─────────────────────────────────────┘
```

**Regla:** Términos del dominio NO se mezclan.

### 🔄 Versionado y Evolución

**Estrategia 1: Additive Changes (sin breaking)**

```graphql
# v1
type User {
  name: String!
}

# v2 (✅ additive, no breaking)
type User {
  name: String!
  fullName: String!  # Nuevo campo
}
```

**Estrategia 2: Deprecation**

```graphql
type User {
  name: String! @deprecated(reason: "Use fullName instead")
  fullName: String!
}
```

**Estrategia 3: Field Aliasing**

```graphql
# Old schema
type Product {
  cost: Float!
}

# New schema (mantiene compatibilidad)
type Product {
  cost: Float! @deprecated(reason: "Use price")
  price: Float!  # Mismo valor, mejor nombre
}

# Resolver
@DgsData(parentType = "Product", field = "cost")
public Double cost(Product product) {
    return product.getPrice();  // Alias al nuevo campo
}
```

### 📊 Schema Checks en CI/CD

**Integración con Rover CLI:**

```yaml
# .github/workflows/schema-check.yml
name: Schema Check

on: [pull_request]

jobs:
  check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Install Rover
        run: |
          curl -sSL https://rover.apollo.dev/nix/latest | sh
      
      - name: Check Users Schema
        run: |
          rover subgraph check my-graph@prod \
            --name users \
            --schema ./users-service/src/main/resources/schema/users-schema.graphqls
      
      - name: Check Loans Schema
        run: |
          rover subgraph check my-graph@prod \
            --name loans \
            --schema ./loans-service/src/main/resources/schema/loans-schema.graphqls
```

**Beneficios:**
- ✅ Detecta breaking changes ANTES de producción
- ✅ Valida composición de supergraph
- ✅ Previene conflicts entre subgrafos

---

## Conceptos Avanzados

### 🧩 Entity Resolution Deep Dive

**¿Cómo resuelve Apollo Router una entidad?**

**Paso 1:** Cliente pide User con loans

```graphql
{
  user(id: "user-001") {
    email
    loansAsBorrower { amount }
  }
}
```

**Paso 2:** Apollo Router identifica que necesita:
- `email` → users-service
- `loansAsBorrower` → loans-service

**Paso 3:** Llama a users-service

```graphql
{
  user(id: "user-001") {
    __typename
    id
    email
  }
}
```

**Paso 4:** Llama a loans-service con entity representation

```graphql
{
  _entities(representations: [
    { __typename: "User", id: "user-001" }
  ]) {
    ... on User {
      loansAsBorrower { amount }
    }
  }
}
```

**Paso 5:** DGS enruta a Entity Fetcher

```java
@DgsEntityFetcher(name = "User")
public Map<String, Object> resolveUser(Map<String, Object> rep) {
    // rep = { __typename: "User", id: "user-001" }
    String id = (String) rep.get("id");
    
    // No retornamos User completo, solo lo necesario
    Map<String, Object> user = new HashMap<>();
    user.put("id", id);
    return user;
}
```

**Paso 6:** DGS resuelve loansAsBorrower

```java
@DgsData(parentType = "User", field = "loansAsBorrower")
public List<Loan> loansAsBorrower(Map<String, Object> user) {
    return loansService.getLoansByBorrower((String) user.get("id"));
}
```

**Paso 7:** Apollo Router merge todo

### 🚀 Performance y Caching

**Problema:** N+1 queries en Federation

```graphql
{
  loans {  # 10 loans
    borrower {  # 10 calls a users-service!
      fullName
    }
  }
}
```

**Solución 1: DataLoader en Router**

Apollo Router automáticamente batching:
```
❌ Sin batching:
  _entities({ __typename: "User", id: "1" })
  _entities({ __typename: "User", id: "2" })
  _entities({ __typename: "User", id: "3" })
  ... 10 llamadas

✅ Con batching:
  _entities([
    { __typename: "User", id: "1" },
    { __typename: "User", id: "2" },
    { __typename: "User", id: "3" },
    ...
  ])  # 1 llamada!
```

**Solución 2: @provides**

```graphql
type Loan {
  borrower: User! @provides(fields: "fullName")
}
```

Loans service ya tiene el nombre, no fetcha Users service.

---

## Antipatrones

### ❌ Antipatrón 1: "God Subgraph"

**Problema:**
```
users-service:
├─ Users
├─ Orders
├─ Products
├─ Payments
└─ Analytics  # 90% del schema aquí
```

**Solución:** Dividir por bounded contexts reales.

### ❌ Antipatrón 2: Chatty Federation

**Problema:**
```graphql
type Order {
  items: [OrderItem!]!
}

type OrderItem {
  product: Product!  # N fetches a products-service
}
```

Con 100 items = 100 llamadas a products-service.

**Solución:** Usar @provides o denormalizar datos críticos.

### ❌ Antipatrón 3: Shared Database

**Problema:**
```
users-service  ─┐
                ├─→ SAME DATABASE
loans-service  ─┘
```

Esto NO es microservicios ni federation real.

**Solución:** Database per service.

---

## Casos de Uso Reales

### 🏦 Banking Federation

```
identity-service: Authentication, KYC
accounts-service: Checking, Savings accounts
cards-service: Credit/Debit cards
loans-service: Mortgages, Personal loans
payments-service: Transfers, Bill pay
```

### 🛒 E-commerce Federation

```
catalog-service: Products, Categories
inventory-service: Stock levels
pricing-service: Prices, Discounts
cart-service: Shopping cart
checkout-service: Payment, Shipping
```

---

## 🎓 Resumen

**Conceptos Clave:**

1. **Federation** = Schema distribuido en subgrafos
2. **@key** = Marca entidades federadas
3. **@extends** = Agrega campos a entidades de otros subgrafos
4. **Apollo Router** = Orquesta queries entre subgrafos
5. **Ownership** = Cada subgrafo es dueño de su dominio
6. **Bounded Contexts** = Separación por dominio (DDD)

**Cuándo usar:**
- ✅ Equipos múltiples
- ✅ Microservicios
- ✅ Deploy independiente

**Cuándo NO usar:**
- ❌ Equipo pequeño
- ❌ MVP
- ❌ Latencia crítica

---

**Curso:** GraphQL con Netflix DGS y Apollo Federation  
**Capítulo:** 5 - Arquitectura Federada  
**Feature:** P2P Lending Marketplace
