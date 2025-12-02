# 🔗 Chapter 05: Apollo Federation with Netflix DGS

**Federation, Subgraphs & Distributed Architecture**

> *"De Monolito GraphQL a Arquitectura Federada con Microservicios"*

---

## 📋 Información del Capítulo

**Nombre:** Federación con Apollo Federation y DGS  
**Duración:** 1.75 horas (4 secciones × 30 minutos)  
**Nivel:** Avanzado  
**Feature:** P2P Lending Marketplace (Préstamos entre Usuarios)

---

## 🎯 Objetivos de Aprendizaje

Al completar este capítulo, los alumnos serán capaces de:

✅ Comprender arquitectura federada vs monolítica  
✅ Implementar subgrafos independientes con DGS  
✅ Usar directivas de Apollo Federation v2 (@key, @extends, @external, @requires, @provides)  
✅ Resolver entidades entre subgrafos  
✅ Diseñar bounded contexts claros  
✅ Aplicar ownership de datos  
✅ Entender el rol del Apollo Router/Gateway  
✅ Implementar entity references distribuidas  

---

## 🏗️ Arquitectura del Proyecto

```
┌─────────────────────────────────────────────────────────────┐
│                     CLIENTE (Frontend)                       │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                   APOLLO ROUTER (Puerto 8080)                │
│  - Unified GraphQL endpoint                                  │
│  - Query planning                                            │
│  - Entity resolution                                         │
│  - Composition de subgrafos                                  │
└─────────────────────────────────────────────────────────────┘
              ↓                              ↓
┌──────────────────────────┐    ┌──────────────────────────┐
│   USERS SERVICE          │    │   LOANS SERVICE          │
│   (Puerto 8081)          │    │   (Porto 8082)          │
│                          │    │                          │
│  Spring Boot + DGS       │    │  Spring Boot + DGS       │
│                          │    │                          │
│  Dominio: Users          │    │  Dominio: Loans          │
│  - User                  │    │  - Loan                  │
│  - LenderProfile         │    │  - extends User          │
│  - BorrowerProfile       │    │                          │
│                          │    │                          │
│  @key(fields: "id")      │    │  @extends User           │
└──────────────────────────┘    └──────────────────────────┘
```

---

## 📦 Stack Tecnológico

| Componente | Tecnología | Versión | Propósito |
|------------|-----------|---------|-----------|
| **Subgrafos** | Spring Boot + DGS | 3.2.0 / 8.2.0 | Servicios GraphQL |
| **Federation** | Apollo Federation | v2.3 | Composición de schemas |
| **Gateway** | Apollo Router | v1.37 | Orquestador de queries |
| **Build** | Maven | 3.9+ | Construcción |
| **Java** | OpenJDK | 17+ | Runtime |
| **Container** | Docker | Latest | Para Apollo Router |

---

## 🚀 Quick Start

### Pre-requisitos

```bash
# Java 17+
java -version

# Maven
mvn -version

# Docker (para Apollo Router - OPCIONAL)
docker --version
```

### Opción A: Sin Apollo Router (Desarrollo)

**Ejecutar cada servicio independientemente:**

```bash
# Terminal 1: Users Service
cd users-service
mvn spring-boot:run
# Disponible en http://localhost:8081/graphiql

# Terminal 2: Loans Service
cd loans-service
mvn spring-boot:run
# Disponible en http://localhost:8082/graphiql
```

**Probar cada subgrafo:**
- Users: http://localhost:8081/graphiql
- Loans: http://localhost:8082/graphiql

### Opción B: Con Apollo Router (Producción-like)

```bash
# 1. Compilar ambos servicios
cd users-service && mvn clean package
cd ../loans-service && mvn clean package

# 2. Ejecutar con Docker Compose
cd ..
docker-compose up -d

# 3. Unified endpoint disponible en:
# http://localhost:8080/graphql
```

---

## 📁 Estructura del Proyecto

```
chapter-05-p2p-lending-federation/
├── docker-compose.yml               # Orquestación Docker
├── router-config.yaml               # Configuración Apollo Router
├── supergraph-schema.graphql        # Schema compuesto
├── README.md                        # Este archivo
├── TEORIA.md                        # Teoría profunda
│
├── users-service/                   # SUBGRAFO 1
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/neobank/users/
│       │   ├── UsersServiceApplication.java
│       │   ├── model/
│       │   │   ├── User.java
│       │   │   ├── LenderProfile.java
│       │   │   └── BorrowerProfile.java
│       │   ├── service/
│       │   │   └── UsersService.java
│       │   ├── resolver/
│       │   │   └── UsersResolver.java
│       │   └── datafetcher/
│       │       └── UserEntityFetcher.java  # @key resolver
│       └── resources/
│           ├── application.yml
│           └── schema/
│               └── users-schema.graphqls   # Schema con @key
│
└── loans-service/                   # SUBGRAFO 2
    ├── pom.xml
    └── src/main/
        ├── java/com/neobank/loans/
        │   ├── LoansServiceApplication.java
        │   ├── model/
        │   │   └── Loan.java
        │   ├── service/
        │   │   └── LoansService.java
        │   ├── resolver/
        │   │   └── LoansResolver.java
        │   └── datafetcher/
        │       └── UserEntityFetcher.java  # Entity stub
        └── resources/
            ├── application.yml
            └── schema/
                └── loans-schema.graphqls   # Schema con @extends
```

---

## 🎓 Contenido por Sección

### **Sección 5.1: Introducción a la Arquitectura Federada** (30 min)

**Conceptos:**
- Monolito vs Federación
- Bounded contexts
- Domain ownership
- Ventajas y desventajas

**Comparación:**

| Aspecto | Monolito | Federación |
|---------|----------|-----------|
| **Schema** | Un solo schema grande | Múltiples schemas pequeños |
| **Equipos** | Un equipo central | Equipos por dominio |
| **Deploy** | Todo junto | Independiente por servicio |
| **Escalabilidad** | Vertical | Horizontal por dominio |
| **Complejidad** | Baja inicialmente | Alta, requiere orquestación |

**Cuándo usar Federación:**
- ✅ Equipos múltiples con dominios claros
- ✅ Escalamiento independiente necesario
- ✅ Ciclos de deploy diferentes
- ✅ Arquitectura de microservicios existente

---

### **Sección 5.2: Fundamentos de Apollo Federation** (30 min)

**Directivas de Federation v2:**

#### **@key** - Define entidad federada
```graphql
type User @key(fields: "id") {
  id: ID!
  email: String!
}
```
Marca `User` como entidad que puede ser referenciada desde otros subgrafos.

#### **@extends** - Extiende tipo de otro subgrafo
```graphql
type User @key(fields: "id") @extends {
  id: ID! @external
  loansAsBorrower: [Loan!]!
}
```
El subgrafo Loans agrega campos al `User` del subgrafo Users.

#### **@external** - Campo definido en otro subgrafo
```graphql
type User @extends {
  id: ID! @external  # Viene de users-service
}
```

#### **@requires** - Campo necesita otros campos
```graphql
type Product @key(fields: "id") {
  price: Float!
  weight: Float!
  shippingEstimate: String! @requires(fields: "price weight")
}
```

#### **@provides** - Optimización de fetching
```graphql
type Review {
  product: Product! @provides(fields: "name")
}
```

**Herramientas del Ecosistema:**
- **Apollo Router:** Gateway moderno en Rust
- **Apollo Gateway:** Gateway legacy en Node.js
- **Rover CLI:** Herramienta de línea de comandos
- **Apollo Studio:** Plataforma cloud de gestión

---

### **Sección 5.3: Creación de Subgrafos con DGS** (30 min)

#### **Subgrafo Users (owner de User)**

**Schema:**
```graphql
type User @key(fields: "id") {
  id: ID!
  email: String!
  fullName: String!
  userType: UserType!
  lenderProfile: LenderProfile
  borrowerProfile: BorrowerProfile
  reputation: Float!
}
```

**Entity Fetcher:**
```java
@DgsEntityFetcher(name = "User")
public User resolveUser(Map<String, Object> values) {
    String id = (String) values.get("id");
    return usersService.getUserById(id);
}
```
Este método permite que Apollo Router "resuelva" un User dado solo su ID.

#### **Subgrafo Loans (extiende User)**

**Schema:**
```graphql
type User @key(fields: "id") @extends {
  id: ID! @external
  loansAsLender: [Loan!]!
  loansAsBorrower: [Loan!]!
}

type Loan @key(fields: "id") {
  id: ID!
  amount: Float!
  lender: User!
  borrower: User!
}
```

**Resolver para User.loansAsBorrower:**
```java
@DgsData(parentType = "User", field = "loansAsBorrower")
public List<Loan> loansAsBorrower(Map<String, Object> user) {
    String userId = (String) user.get("id");
    return loansService.getLoansByBorrower(userId);
}
```

**Retornar referencia stub:**
```java
@DgsData(parentType = "Loan", field = "lender")
public Map<String, Object> lender(Loan loan) {
    Map<String, Object> userRef = new HashMap<>();
    userRef.put("__typename", "User");
    userRef.put("id", loan.getLenderId());
    return userRef;  // Apollo Router resolverá el resto
}
```

---

### **Sección 5.4: Buenas Prácticas y Gobernanza** (30 min)

#### **Ownership Claro**

**Principio:** Cada subgrafo es owner de su dominio

```
Users Service owns:
├── User (entidad base)
├── LenderProfile
└── BorrowerProfile

Loans Service owns:
├── Loan (entidad)
└── User.loansAsBorrower (campo extendido)
```

**❌ MAL - Ownership ambiguo:**
```graphql
# users-service
type User {
  activeLoans: Int!  # ❌ Loans data en Users service
}

# loans-service  
type User {
  email: String!  # ❌ Users data en Loans service
}
```

**✅ BIEN - Ownership claro:**
```graphql
# users-service (owner de User)
type User @key(fields: "id") {
  id: ID!
  email: String!
}

# loans-service (extiende User con su dominio)
type User @extends {
  id: ID! @external
  activeLoans: Int!  # Calculado desde Loans domain
}
```

#### **Naming Conventions**

```graphql
# Nombres descriptivos de dominio
type User              # ✅ Entidad principal
type LoanRequest       # ✅ Claro que es del dominio Loans
type UserLoanStats     # ✅ Stats de préstamos del usuario

# Evitar nombres genéricos
type Data              # ❌ Muy genérico
type Info              # ❌ Ambiguo
type Result            # ❌ No dice qué resultado
```

#### **Bounded Contexts (DDD)**

```
┌─────────────────────────────┐
│     USERS CONTEXT           │
│                             │
│  User                       │
│  ├─ Identity (email, name)  │
│  ├─ Profile (lender/borrower) │
│  └─ Reputation             │
└─────────────────────────────┘

┌─────────────────────────────┐
│     LOANS CONTEXT           │
│                             │
│  Loan                       │
│  ├─ Financial terms         │
│  ├─ Status                  │
│  └─ Parties (lender, borrower) │
└─────────────────────────────┘
```

#### **Evitar Dependencias Circulares**

**❌ MAL:**
```
users-service → HTTP call → loans-service
loans-service → HTTP call → users-service
```
Esto causa deadlocks y acoplamiento.

**✅ BIEN:**
```
users-service → No llama a loans
loans-service → No llama a users
Apollo Router → Orquesta ambos
```

#### **Versionado y Evolución**

**Estrategias:**
1. **Additive changes:** Agregar campos (sin romper)
2. **Deprecation:** Marcar como deprecated antes de eliminar
3. **Field aliasing:** Renombrar sin breaking changes

```graphql
type User {
  name: String! @deprecated(reason: "Use fullName")
  fullName: String!
}
```

#### **Schema Checks**

```bash
# Usar Rover para validar cambios
rover subgraph check my-graph@prod \
  --schema ./users-schema.graphqls \
  --name users
```

---

## 🧪 Queries de Ejemplo

### Query 1: Usuario simple (solo users-service)

```graphql
{
  user(id: "user-001") {
    id
    email
    fullName
    reputation
  }
}
```

### Query 2: Usuario con préstamos (federada)

```graphql
{
  user(id: "user-001") {
    id
    fullName
    
    # Campo del subgrafo Loans
    loansAsLender {
      id
      amount
      status
      
      # Referencia de vuelta a Users
      borrower {
        fullName
        creditScore
      }
    }
  }
}
```

**Flujo de resolución:**
```
1. Apollo Router recibe query
2. Envía { user(id: "user-001") } → users-service
3. users-service retorna: { id, fullName }
4. Apollo Router ve que necesita loansAsLender
5. Envía { User(id: "user-001") { loansAsLender } } → loans-service
6. loans-service retorna préstamos
7. Apollo Router ve referencias a borrower
8. Envía { User(id: "...") { fullName, creditScore } } → users-service
9. Compone resultado final
```

### Query 3: Préstamos disponibles

```graphql
{
  availableLoans {
    id
    amount
    interestRate
    term
    purpose
    
    borrower {
      fullName
      creditScore
      reputation
    }
  }
}
```

### Mutation: Crear solicitud de préstamo

```graphql
mutation {
  createLoanRequest(input: {
    borrowerId: "user-003"
    amount: 50000
    interestRate: 8.5
    term: 36
    purpose: "Business expansion"
  }) {
    success
    message
    loan {
      id
      monthlyPayment
      totalRepayment
    }
  }
}
```

---

## 🔍 Conceptos Clave de Federation

### Entity Resolution

**Problema:** ¿Cómo resuelve Apollo Router una entidad distribuida?

**Solución:** Entity fetcher con @key

```java
// users-service
@DgsEntityFetcher(name = "User")
public User resolveUser(Map<String, Object> values) {
    return usersService.getUserById((String) values.get("id"));
}
```

Apollo Router puede:
1. Pedir solo `{ id }` al subgrafo que tiene la referencia
2. Usar ese `id` para pedir datos completos al owner

### Query Planning

Apollo Router analiza la query y crea un **query plan**:

```
Query: { user { email loansAsBorrower { amount } } }

Plan:
Step 1: Fetch → users-service
  { user(id: "...") { __typename id email } }

Step 2: Fetch → loans-service
  { _entities(representations: [{ __typename: "User", id: "..." }]) {
      loansAsBorrower { amount }
    }
  }

Step 3: Merge results
```

### Composition

Apollo Router (o Rover CLI) **compone** los schemas:

```graphql
# users-schema.graphqls
type User @key(fields: "id") {
  id: ID!
  email: String!
}

# loans-schema.graphqls
type User @extends {
  id: ID! @external
  loans: [Loan!]!
}

# ↓ Compuesto en supergraph ↓

type User {
  id: ID!
  email: String!
  loans: [Loan!]!
}
```

---

## 🎯 Diferencias vs Capítulo 4

| Aspecto | Capítulo 4 | Capítulo 5 |
|---------|-----------|-----------|
| **Arquitectura** | Monolito | Distribuida (2 servicios) |
| **Schema** | Un solo schema | Múltiples schemas federados |
| **Entidades** | Locales | Distribuidas con @key |
| **Queries** | Un servicio | Orquestadas por Router |
| **Complejidad** | Baja | Alta (network, latency) |
| **Escalabilidad** | Vertical | Horizontal por dominio |

---

## 🚨 Troubleshooting

### Error: "Cannot find User entity fetcher"

**Causa:** Falta `@DgsEntityFetcher` en el subgrafo owner

**Solución:**
```java
@DgsEntityFetcher(name = "User")
public User resolveUser(Map<String, Object> values) {
    // ...
}
```

### Error: "Field loansAsBorrower not found"

**Causa:** Falta resolver para campo extendido

**Solución:**
```java
@DgsData(parentType = "User", field = "loansAsBorrower")
public List<Loan> loansAsBorrower(Map<String, Object> user) {
    // ...
}
```

### Error: Apollo Router connection refused

**Causa:** Servicios no corriendo o puertos incorrectos

**Solución:**
```bash
# Verificar servicios
curl http://localhost:8081/graphql
curl http://localhost:8082/graphql
```

---

## 📚 Recursos Adicionales

- **Teoría:** Ver `TEORIA.md` para conceptos profundos
- **Apollo Federation:** https://www.apollographql.com/docs/federation/
- **DGS Federation:** https://netflix.github.io/dgs/federation/
- **Rover CLI:** https://www.apollographql.com/docs/rover/

---

## 👨‍🏫 Para el Instructor

### Preparación (20 min antes)

1. ✅ Compilar ambos servicios
2. ✅ Ejecutar users-service (puerto 8081)
3. ✅ Ejecutar loans-service (puerto 8082)
4. ✅ Verificar GraphiQL en ambos

### Demos Recomendadas

**Demo 1:** Mostrar users-service standalone
- Query simple de usuario
- Mostrar que NO tiene información de loans

**Demo 2:** Mostrar loans-service standalone
- Query de préstamos
- Mostrar stub de User (solo ID)

**Demo 3:** Explicar cómo Apollo Router los uniría
- Dibujar query plan en pizarra
- Mostrar flujo de entity resolution

**Demo 4:** Schemas federados
- Comparar users-schema.graphqls vs loans-schema.graphqls
- Explicar @key, @extends, @external

### Puntos Clave a Enfatizar

- 🎯 Federation NO es obligatoria (monolito está bien para empezar)
- 🎯 Ownership claro es CRÍTICO
- 🎯 @key marca entidades federadas
- 🎯 @extends agrega campos de otros dominios
- 🎯 Apollo Router orquesta, no modifica data

---

## 🎓 Próximos Pasos

**Capítulo 6:** Subscriptions y Tiempo Real

Temas:
- WebSockets con GraphQL
- @DgsSubscription
- Real-time notifications
- Publisher/Subscriber pattern

---

**Feature:** P2P Lending Marketplace  
**Architecture:** Federated (2 subgraphs)  
**Status:** ✅ Conceptual Implementation  
**Curso:** GraphQL con Netflix DGS y Apollo Federation

---

*Creado con ❤️ para NeoBank Corporate Training Program*
