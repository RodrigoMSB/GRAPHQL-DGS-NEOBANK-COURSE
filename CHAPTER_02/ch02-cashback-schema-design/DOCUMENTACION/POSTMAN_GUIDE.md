# 🚀 POSTMAN COLLECTION - GUÍA DE USO

**GraphQL DGS NeoBank Course - Chapter 02**

---

## 🔥 IMPORTAR EN POSTMAN

### Paso 1: Importar Collection

1. Abre Postman
2. Click en **Import** (esquina superior izquierda)
3. Arrastra el archivo `CHAPTER_02_postman_collection.json`
4. Click **Import**

### Paso 2: Verificar Environment

1. En la esquina superior derecha, selecciona el dropdown de environments
2. Selecciona **GraphQL-DGS-NeoBank-Course** (el mismo del Chapter 01)
3. ✅ Listo, todas las variables están configuradas

> 💡 **Nota:** Este chapter usa el MISMO environment del Chapter 01. No necesitas importar nada nuevo.

---

## 🎯 ESTRUCTURA DE LA COLLECTION

```
CHAPTER 02: Schema Design - Cashback Rewards
│
├── 📁 Section 2.1 - Schema Design Principles (6 tests)
│   ├── 2.1.1 - Domain-Driven Design (User with fullName)
│   ├── 2.1.2 - Well-Designed Enums (CashbackTier)
│   ├── 2.1.3 - Bidirectional Relationships (Transaction → User)
│   ├── 2.1.4 - Calculated Fields (availableCashback)
│   ├── 2.1.5 - Domain Entities (User, Transaction, Reward)
│   └── 2.1.6 - Separation of Concerns (Schema vs DB)
│
├── 📁 Section 2.2 - Scalars, Objects, Lists & Inputs (8 tests)
│   ├── 2.2.1 - Custom Scalar: Money
│   ├── 2.2.2 - Custom Scalar: Percentage
│   ├── 2.2.3 - Custom Scalar: Email
│   ├── 2.2.4 - Custom Scalar: DateTime
│   ├── 2.2.5 - Objects & Nesting (Transaction → User)
│   ├── 2.2.6 - Lists (User → [Transaction])
│   ├── 2.2.7 - Input Types (CreateTransactionInput)
│   └── 2.2.8 - All Custom Scalars Together
│
├── 📁 Section 2.3 - Complex Queries & Mutations (10 tests)
│   ├── 2.3.1 - Query with Multiple Filters
│   ├── 2.3.2 - Nested Query with Calculated Fields
│   ├── 2.3.3 - Dynamic Calculated Fields (cashbackPercentage)
│   ├── 2.3.4 - Mutation with Structured Response
│   ├── 2.3.5 - Mutation Affects Multiple Entities
│   ├── 2.3.6 - Complex Query (User + Filtered Transactions)
│   ├── 2.3.7 - Type Validation (Enum)
│   ├── 2.3.8 - Schema Introspection
│   ├── 2.3.9 - List with Calculated Elements
│   └── 2.3.10 - Many-to-One Relationships
│
└── 📁 BONUS - Extra Design Validations (6 tests)
    ├── BONUS.1 - Filter by Category (TRAVEL)
    ├── BONUS.2 - Filter Users by Tier (PLATINUM)
    ├── BONUS.3 - Calculated Totals (totalSpent, totalCashbackEarned)
    ├── BONUS.4 - Category Multiplier (TRAVEL 3x)
    ├── BONUS.5 - PLATINUM User Higher Cashback
    └── BONUS.6 - All Users Query
```

**Total: 30 requests** (mapean 1:1 con los 30 tests del script bash)

---

## ▶️ CÓMO USAR

### Opción 1: Ejecutar Request Individual

1. Click en cualquier request de la lista
2. Click en **Send**
3. Ver la respuesta en el panel inferior
4. Los **Tests** se ejecutan automáticamente y muestran ✅ o ❌

### Opción 2: Ejecutar Toda una Sección

1. Click derecho en una carpeta (ej: "Section 2.1")
2. Click en **Run folder**
3. Se abre el Collection Runner
4. Click **Run** para ejecutar todos los tests de esa sección

### Opción 3: Ejecutar TODOS los Tests (30)

1. Click derecho en la collection raíz "CHAPTER 02"
2. Click en **Run collection**
3. Postman ejecuta los 30 tests en secuencia
4. Ver resultados: X/30 passed

---

## 🔧 VARIABLES DE ENTORNO

La collection usa estas variables (ya configuradas del Chapter 01):

| Variable | Valor | Descripción |
|----------|-------|-------------|
| `BASE_URL` | `http://localhost:8080` | URL base del servidor |
| `GRAPHQL_ENDPOINT` | `{{BASE_URL}}/graphql` | Endpoint GraphQL |

**⚠️ IMPORTANTE:** Asegúrate de tener el servidor corriendo:
```bash
cd CHAPTER_02/cashback-rewards-schema
mvn spring-boot:run
```

---

## 💡 FEATURES ESPECIALES

### 1. Tests Automáticos

Cada request tiene **tests automáticos** que validan:
- Status code 200
- Campos esperados en la respuesta
- Tipos de datos correctos (custom scalars)
- Validaciones de negocio (cashback calculations)
- Enums válidos (CashbackTier, TransactionCategory, TransactionStatus)

**Ver tests:**
1. Click en un request
2. Ve a la pestaña **Tests**
3. Verás el código JavaScript que valida la respuesta

### 2. Custom Scalars

Este chapter introduce **4 custom scalars** que aparecen en las respuestas:

| Scalar | Ejemplo | Validación |
|--------|---------|------------|
| `Money` | `150.50` | Float con 2 decimales |
| `Percentage` | `3` | Int entre 0-100 |
| `Email` | `maria@neobank.com` | Formato email válido |
| `DateTime` | `2024-11-15T10:30:00Z` | ISO 8601 format |

**Los tests validan automáticamente estos formatos.**

### 3. Campos Calculados

Varios campos son **calculados dinámicamente** (no están en la DB):

- `availableCashback`: Suma de rewards con status AVAILABLE
- `totalSpent`: Suma de todas las transactions confirmadas
- `totalCashbackEarned`: Suma de todo el cashback ganado
- `cashbackAmount`: Calculado según tier + category
- `cashbackPercentage`: Base tier % × category multiplier

**Los tests verifican que estos cálculos sean correctos.**

### 4. Documentación Inline

Cada request tiene **descripción** que explica:
- Qué principio de diseño demuestra
- Por qué es importante
- Cómo se relaciona con el schema design

**Ver documentación:**
- Click en un request
- Lee el panel derecho con la descripción

---

## 🎓 USO EN CLASE

### Para el Instructor:

**Modo Demo - Principios de Diseño:**
1. Ejecuta `2.1.1` - Muestra schema orientado a dominio
2. Ejecuta `2.1.2` - Explica enums bien diseñados
3. Ejecuta `2.1.4` - Demuestra campos calculados
4. Contrasta con diseño acoplado a DB

**Modo Demo - Custom Scalars:**
1. Ejecuta `2.2.1` (Money) → Muestra validación automática
2. Ejecuta `2.2.2` (Percentage) → Explica rangos
3. Ejecuta `2.2.8` → Muestra todos juntos

**Modo Demo - Queries Complejas:**
1. Ejecuta `2.3.1` → Múltiples filtros
2. Ejecuta `2.3.2` → Anidación + cálculos
3. Ejecuta `2.3.6` → Todo combinado

### Para el Alumno:

**Modo Exploración:**
1. Importa la collection
2. Ejecuta cada request de la sección 2.1
3. Observa cómo el schema está orientado a dominio
4. Modifica queries para agregar/quitar campos

**Modo Práctica:**
1. Intenta crear tus propias queries
2. Experimenta con filtros (userId, status, category)
3. Combina múltiples campos calculados
4. Valida con los tests automáticos

---

## 🧪 EJECUTAR TODOS LOS TESTS

### Usando Collection Runner:

1. Click en la collection "CHAPTER 02"
2. Click en **Run**
3. Configura:
   - ✅ Save responses
   - ✅ Keep variable values
   - Iterations: 1
4. Click **Run CHAPTER 02**
5. Espera ~25 segundos
6. Ver resultados: **30/30 passed** ✅

### Usando Newman (CLI):

```bash
# Instalar Newman (si no lo tienes)
npm install -g newman

# Ejecutar collection
newman run CHAPTER_02_postman_collection.json \
  -e GraphQL-NeoBank-Course.postman_environment.json

# Con reporte HTML
newman run CHAPTER_02_postman_collection.json \
  -e GraphQL-NeoBank-Course.postman_environment.json \
  -r html
```

---

## 📊 COMPARACIÓN CON CHAPTER 01

| Aspecto | Chapter 01 | Chapter 02 |
|---------|------------|------------|
| **Feature** | Investment Portfolio | Cashback Rewards |
| **Tests** | 34 requests | 30 requests |
| **Foco** | Fundamentos GraphQL | Schema Design |
| **Custom Scalars** | 0 | 4 (Money, %, Email, DateTime) |
| **Enums** | AssetType | CashbackTier, TransactionCategory, Status |
| **Campos Calculados** | performance | availableCashback, cashbackAmount, totales |
| **Mutations** | Simples | Con respuestas estructuradas |

---

## 🔍 TROUBLESHOOTING

### Problema: Tests fallan con "Error: connect ECONNREFUSED"

**Solución:** El servidor NO está corriendo.
```bash
# Inicia el servidor
cd CHAPTER_02/cashback-rewards-schema
mvn spring-boot:run
```

### Problema: Custom scalars no se validan correctamente

**Solución:** Verifica que el servidor tenga los coercers implementados:
- `MoneyScalar.java`
- `PercentageScalar.java`
- `EmailScalar.java`
- `DateTimeScalar.java`

### Problema: "Field 'availableCashback' returned null"

**Solución:** Asegúrate que hay rewards con status AVAILABLE en los datos iniciales (DataInitializer.java).

### Problema: Cashback calculations son incorrectos

**Verifica:**
1. Tier del usuario (BRONZE=1%, SILVER=2%, GOLD=3%, PLATINUM=5%)
2. Category multiplier (RESTAURANTS=2x, TRAVEL=3x, otros=1x)
3. Fórmula: `cashbackPercentage = baseTier% × categoryMultiplier`

### Problema: Postman dice "Could not get any response"

**Soluciones:**
1. Verifica que el servidor está corriendo en puerto 8080
2. Prueba abrir `http://localhost:8080/graphql` en el navegador
3. Revisa que no haya firewall bloqueando
4. Asegúrate que el proyecto compila sin errores

---

## 📚 CONCEPTOS CLAVE DEL CHAPTER 02

### 1. Schema Design Principles

- ✅ **Domain-Driven:** Schema refleja el negocio, no la DB
- ✅ **Separation of Concerns:** API desacoplada de implementación
- ✅ **Calculated Fields:** Datos derivados no persistidos
- ✅ **Bidirectional Relations:** Navegación en ambas direcciones

### 2. Custom Scalars

- `Money`: Validación de formatos monetarios
- `Percentage`: Rangos acotados (0-100)
- `Email`: Validación de formato email
- `DateTime`: ISO 8601 standard

### 3. Complex Queries

- Filtros múltiples opcionales
- Anidación de objetos
- Campos calculados dinámicos
- Listas con elementos calculados

### 4. Structured Mutations

- Input types bien diseñados
- Responses estructuradas (success + message + data)
- Side effects (crear Transaction → generar Reward)

---

## 🎯 FLUJO DE APRENDIZAJE RECOMENDADO

### Paso 1: Principios (Section 2.1 - 15 min)
Ejecuta los 6 tests de principios de diseño para entender cómo un schema debe estar orientado a dominio.

### Paso 2: Scalars (Section 2.2 - 15 min)
Ejecuta los 8 tests de custom scalars para ver validaciones automáticas.

### Paso 3: Queries Complejas (Section 2.3 - 20 min)
Ejecuta los 10 tests de queries/mutations para ver el poder de GraphQL.

### Paso 4: Bonus (10 min)
Ejecuta los 6 tests bonus para cobertura completa.

### Paso 5: Experimentación (30 min)
Modifica queries, combina campos, crea tus propios requests.

---

## 💻 EJEMPLOS DE QUERIES INTERESANTES

### Query Simple (solo nombres)
```graphql
{
  users {
    fullName
    tier
  }
}
```

### Query Compleja (todo junto)
```graphql
{
  user(id: "user-001") {
    fullName
    tier
    email
    availableCashback
    totalSpent
    totalCashbackEarned
  }
  
  transactions(userId: "user-001", category: TRAVEL) {
    amount
    merchantName
    cashbackAmount
    cashbackPercentage
    status
    user {
      fullName
    }
  }
}
```

### Mutation con Input Type
```graphql
mutation {
  createTransaction(input: {
    userId: "user-001"
    amount: 500.0
    category: TRAVEL
    merchantName: "Airlines Co"
  }) {
    success
    message
    transaction {
      id
      amount
      cashbackAmount
      cashbackPercentage
    }
  }
}
```

---

## 📊 VALIDACIONES AUTOMÁTICAS

Los tests de Postman validan automáticamente:

### ✅ Validaciones de Estructura
- Status code 200
- Campos obligatorios presentes
- No hay campos extra inesperados

### ✅ Validaciones de Tipos
- Money es float con 2 decimales
- Percentage es int 0-100
- Email tiene formato válido
- DateTime es ISO 8601

### ✅ Validaciones de Negocio
- Enums solo tienen valores válidos
- Cashback calculations son correctos
- Relaciones bidireccionales funcionan
- Campos calculados tienen valores coherentes

### ✅ Validaciones de Schema
- Introspection funciona
- Type system está correctamente definido
- Input types tienen las validaciones esperadas

---

## 🎁 BONUS: COMPARACIÓN VISUAL

### Bad Schema (DB-Coupled) ❌
```graphql
type User {
  user_id: Int!
  first_name: String!
  last_name: String!
  tier_id: Int!
}
```

### Good Schema (Domain-Driven) ✅
```graphql
type User {
  id: ID!
  fullName: String!
  tier: CashbackTier!
  availableCashback: Money!
}
```

**Diferencias:**
- `fullName` vs `first_name + last_name` → Concepto de dominio
- `tier: Enum` vs `tier_id: Int` → Tipado fuerte
- `availableCashback` → Campo calculado (no en DB)
- `ID` vs `Int` → Abstracción de identidad

---

## 🚀 PRÓXIMOS PASOS

### Después de completar Chapter 02:

1. ✅ Ejecuta todos los 30 tests
2. 📝 Experimenta modificando queries
3. 🎨 Crea tus propios requests basados en el schema
4. 📚 Prepárate para Chapter 03 (implementación con DGS + DataLoader)

### Lo que viene en Chapter 03:

- Implementación completa con Netflix DGS Framework
- DataLoader para resolver el problema N+1
- Integración con base de datos
- Testing avanzado

---

## 📖 RECURSOS ADICIONALES

### GraphQL Schema Design:
- [GraphQL Schema Design Best Practices](https://www.apollographql.com/docs/apollo-server/schema/schema/)
- [Custom Scalars](https://www.graphql-java.com/documentation/scalars/)

### Postman & GraphQL:
- [Postman GraphQL Docs](https://learning.postman.com/docs/sending-requests/graphql/graphql-overview/)
- [Writing Tests in Postman](https://learning.postman.com/docs/writing-scripts/test-scripts/)

### Newman CLI:
- [Newman Documentation](https://learning.postman.com/docs/collections/using-newman-cli/command-line-integration-with-newman/)

---

## ✨ NOTAS FINALES

### Diferencias con Chapter 01:
- **Chapter 01:** Fundamentos (REST vs GraphQL, sintaxis básica)
- **Chapter 02:** Diseño (custom scalars, schema principles, cálculos)

### Mismo Environment:
Ambos chapters comparten el mismo environment porque:
- Usan el mismo puerto (8080)
- Usan el mismo endpoint GraphQL (/graphql)
- Son parte del mismo curso progresivo

### Datos Precargados:
El servidor viene con datos de ejemplo:
- 2 usuarios (user-001 GOLD, user-002 PLATINUM)
- 5 transacciones con diferentes categorías
- 5 rewards en diferentes estados

---

**¡Disfruta explorando el Schema Design de GraphQL! 🎉**

*Feature: Cashback Rewards Program*  
*Custom Scalars: Money, Percentage, Email, DateTime*  
*30 Tests Automatizados*  
*100% Coverage del Temario*

