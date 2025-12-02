# 💰 Chapter 04: Smart Savings Goals

**Persistencia, Servicios y Performance**

> *"De Queries en Memoria a Persistencia Real con PostgreSQL"*

---

## 📋 Información del Capítulo

**Nombre:** Persistencia, Servicios y Performance  
**Duración:** 1.75 horas (4 secciones × 30 minutos)  
**Nivel:** Intermedio-Avanzado  
**Feature:** Smart Savings Goals (Metas de Ahorro Inteligentes)

---

## 🎯 Objetivos de Aprendizaje

Al completar este capítulo, los alumnos serán capaces de:

✅ Integrar GraphQL con bases de datos relacionales (PostgreSQL)  
✅ Usar Docker para gestionar dependencias de infraestructura  
✅ Implementar la arquitectura en capas (Resolver → Service → Repository)  
✅ Aplicar Spring Data JPA para persistencia  
✅ Manejar transacciones con `@Transactional`  
✅ Implementar mutations que modifican estado persistente  
✅ Calcular campos derivados en tiempo de ejecución  
✅ Gestionar errores y excepciones profesionalmente  

---

## 🏗️ Arquitectura del Proyecto

```
┌─────────────────────────────────────────────────────────────┐
│                    GraphQL Layer                            │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Resolvers (@DgsQuery, @DgsMutation)                 │   │
│  │  - SavingsGoalResolver                               │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                   Service Layer                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Business Logic (@Service, @Transactional)           │   │
│  │  - SavingsGoalService                                │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                  Repository Layer                           │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Data Access (@Repository, JpaRepository)            │   │
│  │  - SavingsGoalRepository                             │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                PostgreSQL Database                          │
│  Tables: savings_goals                                      │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 Stack Tecnológico

| Componente | Tecnología | Versión |
|------------|-----------|---------|
| **Backend** | Spring Boot | 3.2.0 |
| **GraphQL** | Netflix DGS | 8.2.0 |
| **Database** | PostgreSQL | 15-alpine |
| **ORM** | Spring Data JPA | 3.2.0 |
| **Container** | Docker Compose | Latest |
| **Build** | Maven | 3.9+ |
| **Java** | OpenJDK | 17+ |

---

## 🚀 Quick Start

### 1️⃣ Prerequisitos

```bash
# Verificar Java
java -version  # Debe ser 17+

# Verificar Maven
mvn -version

# Verificar Docker
docker --version
docker-compose --version
```

### 2️⃣ Levantar Base de Datos

```bash
# Iniciar PostgreSQL en Docker
docker-compose up -d

# Verificar que esté corriendo
docker ps | grep neobank-savings-db

# Ver logs (opcional)
docker-compose logs -f postgres
```

### 3️⃣ Ejecutar la Aplicación

```bash
# Compilar y ejecutar
mvn spring-boot:run

# La aplicación estará disponible en:
# http://localhost:8080
# GraphiQL: http://localhost:8080/graphiql
```

### 4️⃣ Ejecutar Tests

**Opción A: Script automatizado**
```bash
chmod +x test-chapter04.sh
./test-chapter04.sh
```

**Opción B: Postman Collection**
- Importar `CHAPTER_04_postman_collection.json`
- Run Collection (10 tests automáticos)

---

## 📁 Estructura del Proyecto

```
chapter-04-smart-savings-goals/
├── docker-compose.yml              # PostgreSQL configuration
├── pom.xml                         # Maven dependencies
├── README.md                       # Este archivo
├── TEORIA.md                       # Teoría del capítulo
├── test-chapter04.sh               # Script de testing
├── CHAPTER_04_postman_collection.json
│
└── src/main/
    ├── java/com/neobank/savings/
    │   ├── SavingsApplication.java
    │   │
    │   ├── model/
    │   │   └── SavingsGoalEntity.java    # JPA Entity
    │   │
    │   ├── repository/
    │   │   └── SavingsGoalRepository.java # Spring Data JPA
    │   │
    │   ├── service/
    │   │   └── SavingsGoalService.java    # Business Logic
    │   │
    │   ├── resolver/
    │   │   └── SavingsGoalResolver.java   # GraphQL Resolver
    │   │
    │   └── scalar/
    │       └── MoneyScalar.java           # Custom Scalar
    │
    └── resources/
        ├── application.yml          # Spring configuration
        ├── data.sql                 # Test data
        └── schema/
            └── savings-schema.graphqls  # GraphQL Schema
```

---

## 🎓 Contenido por Sección

### **Sección 4.1: Conexión a BD y Modelo de Persistencia** (30 min)

**Conceptos:**
- PostgreSQL con Docker (zero instalación)
- Spring Data JPA configuration
- Entidades JPA vs GraphQL Types
- Mapeo objeto-relacional

**Código:**
- `docker-compose.yml` - Configuración PostgreSQL
- `SavingsGoalEntity.java` - Entidad JPA con anotaciones
- `application.yml` - Configuración datasource

**Resultado:**
- Base de datos funcionando en Docker
- Entidades mapeadas correctamente
- Datos de prueba cargados

---

### **Sección 4.2: Resolvers con Acceso a Datos Reales** (30 min)

**Conceptos:**
- Spring Data JPA Repositories
- Query methods (findByUserId, findByUserIdAndStatus)
- Service layer pattern
- Separation of concerns

**Código:**
- `SavingsGoalRepository.java` - Repository con queries
- `SavingsGoalService.java` - Business logic
- `SavingsGoalResolver.java` - GraphQL resolver

**Resultado:**
- Queries GraphQL que consultan PostgreSQL
- Filtrado por usuario y status
- Campos calculados (progressPercentage)

---

### **Sección 4.3: Mutations Persistentes con Transacciones** (30 min)

**Conceptos:**
- Mutations que modifican estado
- `@Transactional` para atomicidad
- Response types (success/message/goal)
- Side effects visibles

**Código:**
- `createSavingsGoal` mutation
- Transaction management
- Error handling

**Resultado:**
- Crear goals que persisten en DB
- Transacciones ACID garantizadas
- Respuestas estructuradas

---

### **Sección 4.4: Manejo de Errores y Excepciones** (30 min)

**Conceptos:**
- Custom exceptions (GoalNotFoundException)
- GraphQL error types
- Exception handlers
- Error responses profesionales

**Código:**
- Custom exception classes
- `GraphQLExceptionHandler`
- Error type mapping

**Resultado:**
- Errores controlados y legibles
- Información útil sin exponer detalles técnicos
- Experiencia de usuario mejorada

---

## 🧪 Testing

### Script Automatizado (Bash)

```bash
./test-chapter04.sh
```

**10 Tests incluidos:**
1. Get all savings goals for user 1
2. Get active savings goals only
3. Get specific goal by ID
4. Get goals for user 2
5. Create new savings goal (Tesla)
6. Verify created goal exists
7. Get user 3 goals (includes PAUSED)
8. Validate progress calculation (100%)
9. Verify active filtering
10. Create goal with minimal fields

**Características:**
- ✅ Interactivo (pausa entre tests)
- ✅ Genera archivo de resultados con timestamp
- ✅ Colores y formato visual
- ✅ REQUEST y RESPONSE claramente separados

### Postman Collection

**Importar:** `CHAPTER_04_postman_collection.json`

**Ventajas:**
- Validaciones automáticas con `pm.test()`
- Fácil de correr y compartir
- Exportable a Newman para CI/CD

---

## 💾 Base de Datos

### Configuración Docker

```yaml
services:
  postgres:
    image: postgres:15-alpine
    container_name: neobank-savings-db
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: savingsdb
      POSTGRES_USER: neobank
      POSTGRES_PASSWORD: neobank123
```

### Datos de Prueba

**User 1:**
- Emergency Fund: $5,000 / $15,000 (ACTIVE)
- Japan Vacation: $1,200 / $5,000 (ACTIVE)
- MacBook Pro: $3,500 / $3,500 (COMPLETED)

**User 2:**
- Home Down Payment: $25,000 / $80,000 (ACTIVE)
- Kids Education: $15,000 / $100,000 (ACTIVE)

**User 3:**
- Retirement: $75,000 / $500,000 (ACTIVE)
- Investment Portfolio: $12,000 / $50,000 (ACTIVE)
- Cruise Trip: $2,000 / $8,000 (PAUSED)

### Comandos Útiles

```bash
# Conectar a PostgreSQL
docker exec -it neobank-savings-db psql -U neobank -d savingsdb

# Ver datos
SELECT * FROM savings_goals;
SELECT * FROM savings_goals WHERE user_id = 1;

# Limpiar datos
TRUNCATE savings_goals RESTART IDENTITY CASCADE;

# Salir
\q
```

---

## 🔧 Gestión Docker

```bash
# Iniciar
docker-compose up -d

# Ver logs
docker-compose logs -f postgres

# Detener
docker-compose down

# Reset completo (borra datos)
docker-compose down -v
docker-compose up -d
```

---

## 📊 Queries GraphQL de Ejemplo

### Query: Obtener goals de un usuario

```graphql
{
  savingsGoals(userId: "1") {
    id
    name
    targetAmount
    currentAmount
    progressPercentage
    category
    status
  }
}
```

### Query: Solo goals activos

```graphql
{
  activeSavingsGoals(userId: "1") {
    name
    currentAmount
    progressPercentage
  }
}
```

### Mutation: Crear un goal

```graphql
mutation {
  createSavingsGoal(input: {
    userId: "1"
    name: "Tesla Model 3"
    description: "Electric car savings"
    targetAmount: 50000
    category: OTHER
  }) {
    success
    message
    goal {
      id
      name
      status
    }
  }
}
```

---

## 🎯 Diferencias vs Capítulos Anteriores

| Aspecto | Capítulo 3 | Capítulo 4 |
|---------|-----------|-----------|
| **Datos** | En memoria (HashMap) | PostgreSQL persistente |
| **Dependencias** | Solo Spring Boot + DGS | + PostgreSQL + Docker |
| **Complejidad** | Resolvers simples | Arquitectura en capas |
| **Transacciones** | No aplica | @Transactional |
| **Testing** | Queries básicas | Mutations + persistencia |

---

## 🚨 Troubleshooting

### Error: "Connection refused" al iniciar

**Causa:** PostgreSQL no está corriendo  
**Solución:**
```bash
docker-compose up -d
# Esperar 5 segundos y reintentar
```

### Error: "Port 5432 already in use"

**Causa:** Ya hay un PostgreSQL corriendo  
**Solución:**
```bash
# Detener PostgreSQL local
brew services stop postgresql  # Mac
# O cambiar puerto en docker-compose.yml a 5433:5432
```

### Error: "Table 'savings_goals' doesn't exist"

**Causa:** `defer-datasource-initialization` no configurado  
**Solución:** Verificar `application.yml`:
```yaml
spring:
  jpa:
    defer-datasource-initialization: true
```

---

## 📚 Recursos Adicionales

- **Teoría:** Ver `TEORIA.md` para conceptos detallados
- **Spring Data JPA:** https://spring.io/projects/spring-data-jpa
- **PostgreSQL Docs:** https://www.postgresql.org/docs/15/
- **Docker Compose:** https://docs.docker.com/compose/

---

## 👨‍🏫 Para el Instructor

### Preparación de Clase (15 min antes)

1. ✅ Clonar repositorio
2. ✅ `docker-compose up -d`
3. ✅ `mvn spring-boot:run`
4. ✅ Verificar http://localhost:8080/graphiql
5. ✅ Ejecutar test-chapter04.sh una vez

### Demos en Vivo Recomendadas

1. **Demo 1:** Mostrar Docker Desktop con PostgreSQL corriendo
2. **Demo 2:** Conectar con `psql` y mostrar tablas
3. **Demo 3:** Ejecutar mutation y mostrar cambio en DB
4. **Demo 4:** Mostrar rollback con error en transacción

### Puntos Clave a Enfatizar

- 🎯 Separación de responsabilidades (layers)
- 🎯 Importancia de transacciones
- 🎯 Diferencia entre Entity y GraphQL Type
- 🎯 Docker para simplificar dependencias

---

## 🎓 Próximos Pasos

**Capítulo 5:** Apollo Federation (Arquitectura de Microservicios)

Temas que se cubrirán:
- Subgraphs y Supergraph
- Federación de schemas
- Entity references
- Queries distribuidas

---

## 📝 Notas de Versión

**v1.0.0** - Versión inicial
- Persistencia con PostgreSQL
- Docker Compose setup
- 10 tests automatizados
- Postman collection completa

---

**Feature:** Smart Savings Goals  
**Database:** PostgreSQL 15 (Docker)  
**Status:** ✅ Production Ready  
**Curso:** GraphQL con Netflix DGS Framework
