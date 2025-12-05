# TEORÍA: Apollo Federation con Netflix DGS

## 📚 Índice
1. Introducción a Arquitectura Federada
2. Problema del Monolito GraphQL
3. Conceptos Fundamentales de Federation
4. Directivas de Apollo Federation
5. Entity Resolution
6. Bounded Contexts (DDD)
7. Ventajas y Desventajas
8. Cuándo Usar Federation

---

## 1. Introducción a Arquitectura Federada

### ¿Qué es Apollo Federation?

Apollo Federation es un patrón arquitectónico que permite **dividir un schema GraphQL grande en múltiples servicios independientes** (subgrafos) que se comportan como un solo API unificado.

**Analogía:** 
- **Monolito GraphQL** = Un restaurante con un solo chef que cocina TODO
- **Federation** = Un food court con múltiples restaurantes especializados, pero un solo menú unificado

---

## 2. Problema del Monolito GraphQL

### ❌ Problemas Típicos:

**Problema 1: Acoplamiento**
```
┌─────────────────────────────────────┐
│     MONOLITO GRAPHQL                │
│                                     │
│  ┌─────────┐  ┌─────────┐         │
│  │ Users   │  │ Loans   │         │
│  │ Service │──│ Service │         │
│  └─────────┘  └─────────┘         │
│       │            │               │
│       └────┬───────┘               │
│            ▼                        │
│     ┌─────────────┐                │
│     │ UN SOLO     │                │
│     │ SCHEMA      │                │
│     └─────────────┘                │
└─────────────────────────────────────┘
```

**Consecuencias:**
- Deploy de Users afecta a Loans
- Un error en cualquier parte tumba TODO
- Equipos bloqueados esperando cambios de otros
- Difícil escalar componentes individuales

---

## 3. Conceptos Fundamentales de Federation

### 3.1 Subgrafos (Subgraphs)

**Definición:** Servicios GraphQL independientes que exponen parte del schema total.

**Ejemplo NeoBank:**
```
┌──────────────────┐         ┌──────────────────┐
│  USERS SUBGRAPH  │         │  LOANS SUBGRAPH  │
│                  │         │                  │
│  - User          │         │  - Loan          │
│  - LenderProfile │         │  - extends User  │
│  - BorrowerProf  │         │                  │
└──────────────────┘         └──────────────────┘
         │                            │
         └──────────┬─────────────────┘
                    ▼
         ┌────────────────────┐
         │   APOLLO ROUTER    │
         │  (Supergraph)      │
         └────────────────────┘
```

### 3.2 Entidades Federadas (Federated Entities)

**Definición:** Tipos que pueden ser **referenciados y extendidos** por múltiples subgrafos.

**Regla de Oro:** Un subgrafo es **owner** de la entidad, otros pueden **extenderla**.

---

## 4. Directivas de Apollo Federation

### 4.1 @key

**Propósito:** Marca un tipo como **entidad federada**.

**Sintaxis:**
```graphql
type User @key(fields: "id") {
  id: ID!
  email: String!
  fullName: String!
}
```

**Significado:**
- ✅ "User puede ser referenciado desde otros subgrafos usando solo su `id`"
- ✅ "Este subgrafo puede resolver un User dado su `id`"

**Compound Keys:**
```graphql
type Product @key(fields: "sku region") {
  sku: String!
  region: String!
  name: String!
}
```

---

### 4.2 @extends

**Propósito:** Indica que este subgrafo **agrega campos** a un tipo definido en otro lugar.

**Ejemplo:**
```graphql
# Loans Service extiende User
type User @key(fields: "id") @extends {
  id: ID! @external
  loansAsLender: [Loan!]!
  loansAsBorrower: [Loan!]!
}
```

**Significado:**
- ✅ "User está definido en otro subgrafo"
- ✅ "Yo (Loans) agrego los campos loansAsLender y loansAsBorrower"

---

### 4.3 @external

**Propósito:** Marca campos que están **definidos en otro subgrafo**.
```graphql
type User @extends {
  id: ID! @external        # Definido en Users Service
  email: String! @external # Definido en Users Service
  loansAsLender: [Loan!]!  # Definido AQUÍ en Loans
}
```

---

### 4.4 @requires

**Propósito:** Indica que un campo **necesita otros campos** para resolverse.
```graphql
type Product @key(fields: "id") @extends {
  id: ID! @external
  price: Float! @external
  tax: Float! @requires(fields: "price")
}
```

**Significado:** Para calcular `tax`, necesito `price` del otro subgrafo.

---

### 4.5 @provides

**Propósito:** **Optimización** - indica que este campo ya trae datos de otro subgrafo.
```graphql
type Review {
  product: Product! @provides(fields: "name")
}
```

**Significado:** "Cuando retorno product, ya incluyo product.name, no necesitas fetching adicional"

---

## 5. Entity Resolution

### ¿Qué es?

**Entity Resolution** es el proceso donde Apollo Router **reconstruye una entidad completa** consultando múltiples subgrafos.

### Flujo Completo

**Query del Cliente:**
```graphql
{
  user(id: "user-001") {
    fullName          # Users Service
    email             # Users Service
    loansAsLender {   # Loans Service
      amount
      status
    }
  }
}
```

**Query Plan (generado por Apollo Router):**
```
STEP 1: Fetch from users-service
  Query: { user(id: "user-001") { __typename id fullName email } }
  Result: { __typename: "User", id: "user-001", fullName: "Alice", email: "alice@..." }

STEP 2: Fetch from loans-service
  Query: { _entities(representations: [{ __typename: "User", id: "user-001" }]) {
            ... on User { loansAsLender { amount status } }
          }}
  Result: { loansAsLender: [{ amount: 25000, status: "ACTIVE" }] }

STEP 3: Merge results
  Final: {
    user: {
      fullName: "Alice",
      email: "alice@...",
      loansAsLender: [{ amount: 25000, status: "ACTIVE" }]
    }
  }
```

### Implementación en DGS

**Users Service (owner de User):**
```java
@DgsEntityFetcher(name = "User")
public User resolveUser(Map<String, Object> values) {
    String id = (String) values.get("id");
    return usersService.getUserById(id);  // Fetch completo
}
```

**Loans Service (extiende User):**
```java
@DgsData(parentType = "User", field = "loansAsLender")
public List<Loan> loansAsLender(DataFetchingEnvironment dfe) {
    Map<String, Object> user = dfe.getSource();
    String userId = (String) user.get("id");
    return loansService.getLoansByLender(userId);
}
```

---

## 6. Bounded Contexts (DDD)

### Definición

Un **Bounded Context** es una frontera explícita dentro de la cual un modelo de dominio tiene significado específico.

### Ejemplo NeoBank
```
┌─────────────────────────────┐  ┌─────────────────────────────┐
│  USERS BOUNDED CONTEXT      │  │  LOANS BOUNDED CONTEXT      │
│                             │  │                             │
│  Responsabilidades:         │  │  Responsabilidades:         │
│  - Autenticación            │  │  - Creación de préstamos    │
│  - Perfiles KYC             │  │  - Match lender-borrower    │
│  - Verificación             │  │  - Cálculo de intereses     │
│  - Reputación               │  │  - Estado de préstamos      │
│                             │  │                             │
│  Entidades:                 │  │  Entidades:                 │
│  - User (OWNER)             │  │  - Loan (OWNER)             │
│  - LenderProfile            │  │  - User (REFERENCE)         │
│  - BorrowerProfile          │  │                             │
└─────────────────────────────┘  └─────────────────────────────┘
```

### Regla de Ownership

**✅ BIEN:**
```graphql
# Users Service (OWNER)
type User @key(fields: "id") {
  id: ID!
  email: String!
  fullName: String!
}

# Loans Service (CONSUMER)
type User @extends {
  id: ID! @external
  loansAsLender: [Loan!]!  # Solo agrega campos
}
```

**❌ MAL:**
```graphql
# Loans Service intentando modificar campos core
type User @extends {
  email: String!  # ❌ No puedes redefinir campos del owner
}
```

---

## 7. Ventajas y Desventajas

### ✅ Ventajas

**1. Separación de Equipos**
- Equipo A: Users Service
- Equipo B: Loans Service
- Trabajan **independientemente**

**2. Escalabilidad Granular**
```
Users Service: 2 instancias (bajo tráfico)
Loans Service: 10 instancias (alto tráfico de préstamos)
```

**3. Despliegue Independiente**
- Deploy de Loans NO afecta Users
- Rollback granular

**4. Ownership Claro**
- Users team es responsable de User
- Loans team es responsable de Loan

**5. Performance**
- Cache específico por subgrafo
- Optimización independiente

---

### ❌ Desventajas

**1. Complejidad Operacional**
- Más servicios = más infraestructura
- Monitoreo distribuido

**2. Latencia**
- Query federada = múltiples network calls
- Monolito: 1 hop | Federation: 2-3 hops

**3. Debugging Complejo**
- Error puede estar en cualquier subgrafo
- Query planning puede fallar

**4. Transacciones Distribuidas**
- No hay transacciones ACID nativas
- Saga pattern o eventual consistency

**5. Schema Governance**
- Breaking changes afectan múltiples equipos
- Necesita schema registry

---

## 8. Cuándo Usar Federation

### ✅ Usar Federation cuando:

1. **Múltiples equipos independientes**
   - Diferentes squads por dominio
   - Ownership claro de entidades

2. **Escalabilidad diferenciada**
   - Algunos dominios requieren más recursos
   - Patrones de tráfico distintos

3. **Dominios bien definidos**
   - Bounded contexts claros (DDD)
   - Baja cohesión entre dominios

4. **Autonomía de deploy**
   - Equipos necesitan desplegar sin coordinación
   - Diferentes velocidades de cambio

5. **Organizaciones grandes**
   - +50 desarrolladores
   - +10 equipos

---

### ❌ NO usar Federation cuando:

1. **Equipo pequeño (<5 personas)**
   - Overhead no justificado
   - Monolito modular es suficiente

2. **Dominios fuertemente acoplados**
   - Alto nivel de interdependencia
   - Muchas queries atraviesan TODO

3. **Queries críticas de latencia**
   - Tiempo de respuesta ultra-bajo requerido
   - Federation agrega overhead

4. **Startup temprano**
   - Prioridad: velocidad de desarrollo
   - Dominios aún no claros

5. **Sin experiencia en microservicios**
   - Curva de aprendizaje alta
   - Mejor empezar simple

---

## 🎯 Resumen Ejecutivo

### Conceptos Clave

1. **Subgrafos** = Servicios GraphQL independientes
2. **@key** = Marca entidades federadas
3. **@extends** = Agrega campos a tipos de otros subgrafos
4. **Entity Resolution** = Reconstrucción automática por Apollo Router
5. **Bounded Contexts** = Dominios con responsabilidades claras

### Golden Rules

✅ Un subgrafo **es owner** de sus entidades  
✅ Otros subgrafos solo **referencian y extienden**  
✅ No compartir bases de datos entre subgrafos  
✅ Comunicación SOLO vía GraphQL (no HTTP directo)  
✅ Schema registry obligatorio en producción  

---

**NeoBank - Arquitectura Federada con Apollo & DGS**