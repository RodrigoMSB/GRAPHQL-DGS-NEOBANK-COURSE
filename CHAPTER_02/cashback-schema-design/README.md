# 📘 CHAPTER 02: Diseño Correcto de Schemas GraphQL

**Duración:** 1.5 horas (3 secciones × 30 min)  
**Nivel:** Desarrolladores backend con experiencia en GraphQL básico  
**Stack:** Spring Boot 3.4.5 + GraphQL Java 24.x + Java 17

---

## 📚 DOCUMENTACIÓN COMPLETA

- **[📖 TEORÍA COMPLETA](DOCUMENTACION/TEORIA.md)** - Conceptos detallados de cada sección
- **[🚀 GUÍA POSTMAN](DOCUMENTACION/POSTMAN_GUIDE.md)** - Cómo usar la collection de Postman

---

## 🎯 OBJETIVO DEL CAPÍTULO

Dominar los principios de diseño de schemas GraphQL **orientados a dominio** (NO acoplados a la base de datos), usando custom scalars para validación automática, campos calculados para lógica de negocio, y estructuras complejas anidadas. Los alumnos aprenderán a diseñar schemas **profesionales** y **mantenibles** usando un caso real de **Cashback Rewards Program** (NeoBank).

---

## 📋 CONTENIDO DEL CAPÍTULO

### Sección 2.1 — Principios del Diseño de Esquemas GraphQL (30 min)
**Conceptos clave:**
- ❌ **Antipatrón:** Schema acoplado a la base de datos (exponer FKs, nombres de columnas)
- ✅ **Domain-Driven Design:** Schema refleja el lenguaje del negocio
- ✅ **Enums bien diseñados:** `PLATINUM` vs `tier_id: 4`
- ✅ **Relaciones bidireccionales:** Navegación natural (`transaction.user.fullName`)
- ✅ **Campos calculados:** Datos derivados on-the-fly (no persistidos)
- ✅ **Separación de concerns:** Schema NO revela implementación técnica

**Ejemplo práctico:** Comparación lado a lado de schema DB-coupled vs domain-driven para Cashback Rewards.

---

### Sección 2.2 — Tipos Escalares, Objetos, Listas e Inputs (30 min)
**Los 4 custom scalars:**
1. **Money:** Precisión decimal garantizada (no errores de Float)
2. **Percentage:** Rango 0-100 validado automáticamente
3. **Email:** Validación de formato con regex
4. **DateTime:** ISO 8601 estándar (no String genérico)

**Conceptos adicionales:**
- Objetos complejos con anidación multi-nivel
- Listas con nullabilidad correcta: `[Transaction!]!`
- Input Types para mutations robustas
- Validación automática en todos los scalars

**Ejemplo práctico:** Schema completo de Cashback con 4 custom scalars + validación.

---

### Sección 2.3 — Queries y Mutations Complejas (30 min)
**Conceptos clave:**
- Queries con **múltiples filtros opcionales** (flexibilidad máxima)
- Queries anidadas con **campos calculados** dinámicos
- Mutations con **respuestas estructuradas** (success + message + data + errors)
- Mutations que modifican **múltiples entidades** (Transaction → Reward)
- Validación automática de tipos y enums
- Introspection para autodocumentación

**Ejemplo práctico:** Query compleja de dashboard combinando todo (filtros + anidación + cálculos).

---

## 🚀 QUICK START

### Prerrequisitos
```bash
- Java 17+ (LTS)
- Maven 3.8+
- Git
- curl (para testing)
- jq (opcional, para formatear JSON)
- Editor de código (IntelliJ IDEA, VS Code, etc.)
```

### 1. Clonar y posicionarse en el capítulo
```bash
git clone <repo-url>
cd GRAPHQL-DGS-NEOBANK-COURSE/CHAPTER_02/cashback-rewards-schema
```

### 2. Compilar el proyecto
```bash
mvn clean install
```

### 3. Ejecutar la aplicación
```bash
mvn spring-boot:run
```

**Salida esperada:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::               (v3.4.5)

Started CashbackRewardsApplication in 2.234 seconds
```

### 4. Verificar que funciona
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ users { fullName tier } }"}'

# Debe responder con lista de usuarios
```

---

## 🧪 TESTING CON SCRIPT AUTOMATIZADO

### Ejecutar todos los tests (MODO CLASE)
```bash
# Modo interactivo (pausa entre tests para explicar)
./test-chapter02.sh
```

**Uso en clase:**
1. Ejecutas el script
2. Aparece el test #1 con su resultado
3. Presionas Enter
4. Explicas el concepto de diseño de schema
5. Presionas Enter para el siguiente test
6. Repites hasta completar las 3 secciones

### Ejecutar todos los tests (MODO RÁPIDO)
```bash
# Modo silencioso (sin pausas, para CI/CD)
./test-chapter02.sh -s
```

**Resultado esperado:**
```
================================================================================
📊 RESUMEN DE RESULTADOS
================================================================================
Total de tests ejecutados: 30
✅ Tests exitosos: 30
❌ Tests fallidos: 0

🎉 ¡TODOS LOS TESTS PASARON! Chapter 02 completo y funcional.

Cobertura del temario:
  ✅ Sección 2.1 - Principios de diseño (6 tests)
  ✅ Sección 2.2 - Scalars, objetos, listas (8 tests)
  ✅ Sección 2.3 - Queries y mutations complejas (10 tests)
  🎁 Bonus - Validaciones extra (6 tests)

TOTAL: 30 tests automatizados
Feature: Cashback Rewards Program
Custom Scalars: Money, Percentage, Email, DateTime
```

---

## 🎓 CÓMO USAR ESTE PROYECTO EN CLASE

### Metodología: "Show, Don't Build"

Este proyecto está diseñado para **DEMOSTRAR** schema design profesional, no para construir desde cero durante la clase.

**Flujo recomendado por sección:**

#### 1. Contextualización (5 min)
- Muestra un ejemplo de **schema mal diseñado** (DB-coupled)
- Explica por qué es un problema
- Introduce el diseño correcto (domain-driven)

#### 2. Demostración en vivo (15 min)
- Ejecuta `./test-chapter02.sh` en modo interactivo
- Para en cada test para explicar:
  - Qué principio de diseño valida
  - Por qué ese diseño es superior
  - Cómo impacta la UX del cliente

#### 3. Exploración GraphiQL (5 min)
- Abre http://localhost:8080/graphiql
- Muestra introspection del schema
- Demuestra validación automática de custom scalars
- Prueba queries anidadas con campos calculados

#### 4. Código deep-dive (5 min)
- Muestra un custom scalar (ej: `EmailScalar.java`)
- Explica cómo funciona la validación
- Muestra un resolver de campo calculado (ej: `availableCashback`)
- Responde preguntas

**Total:** 30 min por sección × 3 secciones = 1.5 horas (con buffer para preguntas)

---

## 🔍 ENDPOINTS DISPONIBLES

### GraphQL Endpoint
```
POST http://localhost:8080/graphql
Content-Type: application/json

{
  "query": "{ users { fullName tier availableCashback } }"
}
```

### GraphiQL Interface (Playground)
```
http://localhost:8080/graphiql
```

### Health Check
```
GET http://localhost:8080/actuator/health
```

---

## 📊 QUERIES DE EJEMPLO

### Query básica (User con campos de dominio)
```graphql
{
  user(id: "user-001") {
    id
    fullName          # ✅ Concepto de dominio (no first_name/last_name)
    tier              # ✅ Enum (no tier_id FK)
    email             # ✅ Custom scalar validado
    enrolledAt        # ✅ DateTime ISO 8601
  }
}
```

### Query con campos calculados
```graphql
{
  user(id: "user-001") {
    fullName
    tier
    
    # Campos calculados (NO en DB)
    availableCashback      # Suma de rewards AVAILABLE
    totalSpent            # Suma de transactions CONFIRMED
    totalCashbackEarned   # Total histórico
  }
}
```

### Query anidada con relaciones bidireccionales
```graphql
{
  transaction(id: "trans-001") {
    merchantName
    amount
    category
    
    # Navegación a User
    user {
      fullName
      tier
    }
    
    # Campos calculados dinámicos
    cashbackAmount        # Calculado según tier + category
    cashbackPercentage    # GOLD × TRAVEL = 9%
  }
}
```

### Query compleja con filtros múltiples
```graphql
{
  user(id: "user-001") {
    fullName
    tier
    availableCashback
    
    # Transactions filtradas
    transactions(
      status: CONFIRMED
      category: RESTAURANTS
      minAmount: 50.0
      maxAmount: 500.0
    ) {
      merchantName
      amount
      cashbackAmount
      cashbackPercentage
      
      reward {
        status
        expiresAt
      }
    }
  }
}
```

### Query con variables tipadas
```graphql
query GetUserDashboard($userId: ID!, $category: TransactionCategory) {
  user(id: $userId) {
    fullName
    tier
    availableCashback
    
    transactions(category: $category, status: CONFIRMED) {
      merchantName
      amount
      cashbackAmount
    }
  }
}

# Variables:
{
  "userId": "user-001",
  "category": "TRAVEL"
}
```

### Mutation con Input Type
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
      cashbackAmount        # Calculado automáticamente
      cashbackPercentage    # GOLD × TRAVEL = 9%
      
      reward {              # ✅ Creada automáticamente
        amount
        status
        expiresAt
      }
    }
  }
}
```

### Query de Introspection (schema autodocumentado)
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

---

## 📁 ESTRUCTURA DEL PROYECTO

```
cashback-rewards-schema/
│
├── src/
│   ├── main/
│   │   ├── java/com/neobank/cashback/
│   │   │   ├── CashbackRewardsApplication.java
│   │   │   │
│   │   │   ├── config/                    # Configuración
│   │   │   │   ├── GraphQLConfig.java     # Config GraphQL + Scalars
│   │   │   │   └── DataInitializer.java   # Datos de ejemplo
│   │   │   │
│   │   │   ├── scalar/                    # Custom Scalars
│   │   │   │   ├── MoneyScalar.java       # Precisión decimal
│   │   │   │   ├── PercentageScalar.java  # Rango 0-100
│   │   │   │   ├── EmailScalar.java       # Validación formato
│   │   │   │   └── DateTimeScalar.java    # ISO 8601
│   │   │   │
│   │   │   ├── controller/                # GraphQL Resolvers
│   │   │   │   ├── UserQueryResolver.java
│   │   │   │   ├── TransactionQueryResolver.java
│   │   │   │   ├── TransactionMutationResolver.java
│   │   │   │   └── FieldResolvers.java    # Campos calculados
│   │   │   │
│   │   │   ├── model/                     # Domain models
│   │   │   │   ├── User.java
│   │   │   │   ├── Transaction.java
│   │   │   │   ├── Reward.java
│   │   │   │   │
│   │   │   │   ├── enums/                 # Enums bien diseñados
│   │   │   │   │   ├── CashbackTier.java  # BRONZE, SILVER, GOLD, PLATINUM
│   │   │   │   │   ├── TransactionCategory.java
│   │   │   │   │   ├── TransactionStatus.java
│   │   │   │   │   └── RewardStatus.java
│   │   │   │   │
│   │   │   │   └── input/                 # Input Types
│   │   │   │       └── CreateTransactionInput.java
│   │   │   │
│   │   │   ├── service/                   # Business logic
│   │   │   │   ├── UserService.java
│   │   │   │   ├── TransactionService.java
│   │   │   │   ├── RewardService.java
│   │   │   │   └── CashbackCalculator.java  # Lógica de cashback
│   │   │   │
│   │   │   ├── repository/                # Data access (in-memory)
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── TransactionRepository.java
│   │   │   │   └── RewardRepository.java
│   │   │   │
│   │   │   └── response/                  # Response wrappers
│   │   │       └── TransactionResponse.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties     # Configuración Spring
│   │       └── graphql/                   # GraphQL schemas
│   │           └── schema.graphqls        # Schema principal
│   │
│   └── test/
│       └── java/com/neobank/cashback/
│           └── CashbackRewardsApplicationTests.java
│
├── DOCUMENTACION/
│   ├── TEORIA.md                          # Teoría detallada
│   ├── POSTMAN_GUIDE.md                   # Guía de Postman
│   └── POSTMAN/
│       ├── CHAPTER_02_postman_collection.json
│       └── GraphQL-NeoBank-Course.postman_environment.json
│
├── pom.xml                                # Maven dependencies
├── test-chapter02.sh                      # Script de testing
└── README.md                              # Este archivo
```

---

## 🔧 TECNOLOGÍAS UTILIZADAS

### Core Stack
- **Java 17** (LTS hasta 2029)
- **Spring Boot 3.4.5** (última estable)
- **Spring for GraphQL 1.4.x** (integración oficial)
- **GraphQL Java 24.x** (motor GraphQL)

### Dependencias principales
```xml
<!-- GraphQL -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-graphql</artifactId>
</dependency>

<!-- Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

## 📚 CONCEPTOS ENSEÑADOS

### ✅ Principios de Diseño (Sección 2.1)
- Domain-Driven Design (DDD)
- Schema desacoplado de la DB
- Enums bien diseñados vs códigos numéricos
- Relaciones bidireccionales
- Campos calculados vs persistidos
- Separación de concerns

### ✅ Custom Scalars (Sección 2.2)
- Money: Precisión decimal garantizada
- Percentage: Validación de rangos
- Email: Validación de formato
- DateTime: Estándar ISO 8601
- Coercers personalizados
- Validación automática pre-ejecución

### ✅ Queries y Mutations Complejas (Sección 2.3)
- Filtros múltiples opcionales
- Queries anidadas multi-nivel
- Campos calculados dinámicos
- Mutations con respuestas estructuradas
- Side effects visibles (Transaction → Reward)
- Introspection y autodocumentación

---

## 🎯 COBERTURA DE TESTS

### Sección 2.1 - Principios de Diseño (6 tests)
- ✅ Domain-Driven Design (User con fullName)
- ✅ Enums bien diseñados (CashbackTier)
- ✅ Relaciones bidireccionales (Transaction → User)
- ✅ Campos calculados (availableCashback)
- ✅ Entidades de dominio bien definidas
- ✅ Separación schema vs DB

### Sección 2.2 - Scalars, Objetos, Listas (8 tests)
- ✅ Custom Scalar: Money
- ✅ Custom Scalar: Percentage
- ✅ Custom Scalar: Email
- ✅ Custom Scalar: DateTime
- ✅ Objetos con anidación (Transaction → User)
- ✅ Listas (User → [Transaction])
- ✅ Input Types (CreateTransactionInput)
- ✅ Todos los scalars juntos

### Sección 2.3 - Queries y Mutations Complejas (10 tests)
- ✅ Query con múltiples filtros
- ✅ Query anidada con campos calculados
- ✅ Campos calculados dinámicos (cashbackPercentage)
- ✅ Mutation con respuesta estructurada
- ✅ Mutation que afecta múltiples entidades
- ✅ Query compleja (User + Transactions filtradas)
- ✅ Validación de tipos (Enum)
- ✅ Schema introspection
- ✅ Lista con elementos calculados
- ✅ Relaciones many-to-one

### Bonus - Validaciones Extra (6 tests)
- ✅ Filtrado por categoría
- ✅ Filtrado de usuarios por tier
- ✅ Totales calculados (totalSpent, totalCashbackEarned)
- ✅ Cashback con multiplicadores de categoría
- ✅ PLATINUM user con mayor cashback
- ✅ Query de todos los usuarios

**TOTAL: 30 tests automatizados** 🎉

---

## 💡 PRINCIPIOS DE SCHEMA DESIGN

### ❌ ANTI-PATRÓN: Schema DB-Coupled
```graphql
type User {
  user_id: Int!              # ❌ Nombre de columna DB
  first_name: String!        # ❌ No es concepto de dominio
  last_name: String!         # ❌ DB fields separados
  tier_id: Int!              # ❌ FK expuesta
  created_at: String!        # ❌ Formato DB
}
```

### ✅ PATRÓN CORRECTO: Domain-Driven
```graphql
scalar Email
scalar Money
scalar DateTime

enum CashbackTier {
  BRONZE    # 1% base
  SILVER    # 2% base
  GOLD      # 3% base
  PLATINUM  # 5% base
}

type User {
  id: ID!                    # ✅ ID abstracto
  fullName: String!          # ✅ Concepto de negocio
  tier: CashbackTier!        # ✅ Enum autodocumentado
  email: Email!              # ✅ Custom scalar validado
  enrolledAt: DateTime!      # ✅ Timestamp semántico
  
  # Campos calculados (NO en DB)
  availableCashback: Money!  # ✅ Suma de rewards AVAILABLE
  totalSpent: Money!         # ✅ Agregado de negocio
}
```

---

## 🧮 LÓGICA DE CASHBACK

### Fórmula
```
cashbackAmount = amount × (tierPercentage / 100) × categoryMultiplier
```

### Tabla de Cálculo

| User Tier | Base % | Category | Multiplier | Final % | Amount | Cashback |
|-----------|--------|----------|------------|---------|--------|----------|
| BRONZE | 1% | GROCERIES | 1x | 1% | $100 | $1.00 |
| SILVER | 2% | RESTAURANTS | 2x | 4% | $100 | $4.00 |
| GOLD | 3% | TRAVEL | 3x | 9% | $500 | $45.00 |
| PLATINUM | 5% | TRAVEL | 3x | 15% | $1000 | $150.00 |

### Implementación
```java
@SchemaMapping(typeName = "Transaction")
public Double cashbackAmount(Transaction transaction) {
    User user = userService.findById(transaction.getUserId());
    
    // Base % según tier
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
    
    return transaction.getAmount() * (basePercentage / 100) * multiplier;
}
```

---

## 🐛 TROUBLESHOOTING

### Problema: Puerto 8080 ya en uso
```bash
# Encuentra y mata el proceso
lsof -i :8080
kill -9 <PID>

# O cambia el puerto en application.properties
server.port=8081
```

### Problema: Custom scalars no validan
```bash
# Verifica que los scalars estén registrados en GraphQLConfig.java
# Debe haber un @Bean para cada scalar:
@Bean
public RuntimeWiring runtimeWiringConfigurer() {
    return RuntimeWiring.newRuntimeWiring()
        .scalar(moneyScalar)
        .scalar(percentageScalar)
        .scalar(emailScalar)
        .scalar(dateTimeScalar)
        .build();
}
```

### Problema: Campo calculado retorna null
```bash
# Verifica que el @SchemaMapping apunte al tipo correcto
@SchemaMapping(typeName = "User", field = "availableCashback")
public Double availableCashback(User user) {
    // Tu lógica aquí
}
```

### Problema: Tests fallan en Windows
```bash
# Asegúrate de usar GitBash, no CMD
# En GitBash:
bash test-chapter02.sh -s
```

### Problema: Script .sh no ejecutable
```bash
# Dar permisos de ejecución
chmod +x test-chapter02.sh
```

### Problema: jq no instalado (opcional)
```bash
# Mac:
brew install jq

# Linux:
sudo apt-get install jq

# Windows GitBash:
# Descarga desde https://stedolan.github.io/jq/download/
```

---

## 📝 NOTAS PEDAGÓGICAS

### Para el instructor:

1. **Empieza con el antipatrón:** Muestra PRIMERO un schema mal diseñado. Los alumnos aprenden más viendo errores comunes.

2. **Usa analogías:** "Schema DB-coupled es como exponer tu contraseña de DB en la API"

3. **Compara lado a lado:** Muestra DB-coupled vs Domain-driven en pantalla dividida.

4. **Demo de validación:** Intenta enviar un email inválido para que vean el error automático.

5. **Explica los cálculos:** La lógica de cashback es compleja. Usa la tabla de cálculo.

6. **Tiempo real para preguntas:** Schema design es conceptual. Deja que discutan.

### Para el alumno:

1. **Lee TEORIA.md antes de clase:** Llega preparado con los conceptos.

2. **Experimenta con custom scalars:** Intenta romper la validación de Email, Percentage.

3. **Compara con tus proyectos:** ¿Tienes schemas DB-coupled en producción?

4. **Pregunta sobre DDD:** Es un tema profundo. No hay preguntas tontas.

5. **Juega con los cálculos:** Cambia tiers y categories para ver cómo cambia el cashback.

---

## 🚀 PRÓXIMOS PASOS

Después de completar este capítulo, los alumnos estarán listos para:

- **CHAPTER_03:** Cashback Service con DGS (implementación completa + DataLoader)
- **CHAPTER_04:** Smart Savings Goals con DB (persistencia JPA + transacciones)
- **CHAPTER_05:** P2P Lending federado (Apollo Federation conceptual)
- **CHAPTER_06:** Fraud Detection (subscriptions en tiempo real)

---

## 📖 RECURSOS ADICIONALES

### Documentación oficial
- [GraphQL Schema Design Best Practices](https://www.apollographql.com/docs/apollo-server/schema/schema/)
- [Custom Scalars](https://www.graphql-java.com/documentation/scalars/)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)

### Herramientas recomendadas
- [GraphiQL](http://localhost:8080/graphiql) - Playground incluido
- [Postman](https://www.postman.com/) - Testing de APIs (incluye collection)
- [GraphQL Voyager](https://github.com/APIs-guru/graphql-voyager) - Visualizar schema como grafo

### Lecturas complementarias
- "Production Ready GraphQL" - Marc-André Giroux
- "GraphQL Schema Design @ Shopify" - Shopify Engineering Blog
- [GraphQL Patterns](https://graphql-patterns.com/)

---

## 🎯 COMPARACIÓN: CHAPTER 01 vs CHAPTER 02

| Aspecto | Chapter 01 | Chapter 02 |
|---------|------------|------------|
| **Foco** | Fundamentos GraphQL | Schema Design |
| **Feature** | Investment Portfolio | Cashback Rewards |
| **Duración** | 2.5 horas (5 secciones) | 1.5 horas (3 secciones) |
| **Tests** | 34 tests | 30 tests |
| **Problema** | REST (over/underfetching) | DB-coupled schemas |
| **Solución** | GraphQL queries | Domain-driven design |
| **Custom Scalars** | 0 | 4 (Money, %, Email, DateTime) |
| **Campos Calculados** | performance | availableCashback, cashbackAmount |
| **Enums** | AssetType | CashbackTier, Category, Status |
| **Mutations** | Simples | Respuestas estructuradas + side effects |

---

## 👥 CONTRIBUCIONES

Este proyecto es parte del curso **"El Mejor Curso de GraphQL del Mundo"**.

**Instructor:** [Tu nombre]  
**Versión:** 1.0.0  
**Última actualización:** Noviembre 2025

---

## 📄 LICENCIA

Este material es de uso educativo exclusivo para el curso de GraphQL.

---

## ✨ CHANGELOG

### v1.0.0 (2025-11-16)
- ✅ Stack actualizado a Spring Boot 3.4.5 + GraphQL Java 24.x
- ✅ 30 tests automatizados funcionando
- ✅ 4 custom scalars con validación completa
- ✅ Schema domain-driven con campos calculados
- ✅ Script portable Mac/Linux/Windows
- ✅ Cobertura completa del temario (3 secciones + bonus)
- ✅ README completo con instrucciones detalladas
- ✅ Documentación pedagógica extensa (TEORIA.md + POSTMAN_GUIDE.md)

---

**Feature:** Cashback Rewards Program  
**Custom Scalars:** Money, Percentage, Email, DateTime  
**Enums:** CashbackTier, TransactionCategory, TransactionStatus, RewardStatus  
**Principio clave:** Schema orientado a dominio, NO a base de datos 🎯