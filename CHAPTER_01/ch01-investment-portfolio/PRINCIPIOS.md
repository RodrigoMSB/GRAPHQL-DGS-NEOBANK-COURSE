# Capítulo 1: Fundamentos de GraphQL

## ¿Qué es GraphQL?

GraphQL es un lenguaje de consulta para APIs que permite al cliente pedir **exactamente** los datos que necesita.
```
┌─────────────────────────────────────────────────────────────┐
│  REST: El servidor decide qué datos enviar                  │
│  GraphQL: El cliente decide qué datos recibir               │
└─────────────────────────────────────────────────────────────┘
```

---

## REST vs GraphQL: El Problema

### Con REST (múltiples llamadas, datos de más)
```
GET /portfolios/001              → Portfolio completo (aunque solo quieras el nombre)
GET /portfolios/001/assets       → Segunda llamada para assets
GET /portfolios/001/performance  → Tercera llamada para performance
```

### Con GraphQL (una llamada, datos exactos)
```graphql
query {
  portfolio(id: "001") {
    name                    # Solo lo que necesito
    assets { symbol }       # Anidado en UNA llamada
    performance { totalReturn }
  }
}
```

**Resultado:** 1 request en lugar de 3, sin datos innecesarios.

---

## Los 5 Componentes de GraphQL

### 1. Schema (Contrato)
Define QUÉ datos existen y cómo se relacionan.
```graphql
type Portfolio {
  id: ID!
  name: String!
  assets: [Asset!]!      # Relación 1:N
  performance: Performance
}
```

### 2. Types (Estructura de datos)
Definen la forma de los objetos.
```graphql
type Asset {
  symbol: String!
  currentPrice: Float!
  totalValue: Float!      # Campo calculado
}
```

### 3. Queries (Lectura)
Operaciones para LEER datos (equivale a GET).
```graphql
type Query {
  portfolio(id: ID!): Portfolio
  myPortfolios: [Portfolio!]!
}
```

### 4. Mutations (Escritura)
Operaciones para MODIFICAR datos (equivale a POST/PUT/DELETE).
```graphql
type Mutation {
  createPortfolio(input: CreatePortfolioInput!): CreatePortfolioResponse!
  addAsset(input: AddAssetInput!): AddAssetResponse!
}
```

### 5. Resolvers (Implementación)
Código Java que ejecuta las operaciones.
```java
@Controller
public class PortfolioQueryResolver {
    
    @QueryMapping
    public Portfolio portfolio(@Argument String id) {
        return dataService.getPortfolioById(id);
    }
}
```

---

## Nullabilidad: `!` significa obligatorio
```graphql
name: String!      # NUNCA es null
description: String # Puede ser null
assets: [Asset!]!  # Lista obligatoria, elementos obligatorios
```

---

## Variables: Queries reutilizables

En lugar de hardcodear valores:
```graphql
# ❌ Hardcodeado
query {
  portfolio(id: "portfolio-001") { name }
}

# ✅ Con variable
query GetPortfolio($id: ID!) {
  portfolio(id: $id) { name }
}

# Variables JSON:
{ "id": "portfolio-001" }
```

---

## Paginación Cursor-Based (Patrón Relay)

### ¿Por qué cursors y no offset?

- **Offset:** "Dame items 20-30" → Problemas si se insertan/eliminan datos
- **Cursor:** "Dame 10 items después de X" → Siempre consistente

### Estructura de respuesta paginada
```graphql
query {
  assets(portfolioId: "001", pagination: { limit: 5 }) {
    totalCount
    edges {
      cursor              # Identificador único de posición
      node { symbol }     # El dato real
    }
    pageInfo {
      hasNextPage
      endCursor           # Usar para pedir siguiente página
    }
  }
}
```

### Siguiente página
```graphql
query {
  assets(portfolioId: "001", pagination: { limit: 5, after: "YXNzZXQtMDA1" }) {
    # ...
  }
}
```

---

## Filtros y Ordenamiento
```graphql
query {
  assets(
    portfolioId: "001"
    filter: { assetType: STOCK, minValue: 1000 }
    sort: { field: TOTAL_VALUE, direction: DESC }
  ) {
    edges { node { symbol totalValue } }
  }
}
```

---

## Patrón de Respuesta para Mutations

Siempre retornar estructura consistente:
```graphql
type CreatePortfolioResponse {
  success: Boolean!    # ¿Funcionó?
  message: String!     # Mensaje para el usuario
  portfolio: Portfolio # El objeto creado (null si falló)
}
```
```graphql
mutation {
  createPortfolio(input: { name: "Mi Portfolio" }) {
    success
    message
    portfolio { id name }
  }
}
```

---

## Resumen: GraphQL en 30 segundos

| Concepto | Qué es |
|----------|--------|
| **Schema** | Contrato de la API |
| **Type** | Estructura de datos |
| **Query** | Leer datos |
| **Mutation** | Modificar datos |
| **Resolver** | Código que ejecuta |
| **`!`** | Campo obligatorio |
| **Variables** | Parámetros reutilizables |
| **Cursor** | Paginación consistente |