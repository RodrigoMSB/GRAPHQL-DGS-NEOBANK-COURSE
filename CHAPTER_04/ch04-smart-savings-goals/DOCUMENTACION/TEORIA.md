# 📚 TEORÍA - Chapter 04: Persistencia, Servicios y Performance

**GraphQL con Netflix DGS Framework**  
**Capítulo 4:** De Queries en Memoria a Persistencia Real

---

## 📖 Índice

1. [Introducción](#introducción)
2. [Sección 4.1: Conexión a BD y Modelo de Persistencia](#sección-41-conexión-a-bd-y-modelo-de-persistencia)
3. [Sección 4.2: Resolvers con Acceso a Datos Reales](#sección-42-resolvers-con-acceso-a-datos-reales)
4. [Sección 4.3: Mutations Persistentes con Transacciones](#sección-43-mutations-persistentes-con-transacciones)
5. [Sección 4.4: Manejo de Errores y Excepciones](#sección-44-manejo-de-errores-y-excepciones)
6. [Conceptos Avanzados](#conceptos-avanzados)
7. [Antipatrones y Mejores Prácticas](#antipatrones-y-mejores-prácticas)
8. [Casos de Uso Reales](#casos-de-uso-reales)

---

## Introducción

### 🎯 ¿Por qué necesitamos persistencia?

En los capítulos anteriores trabajamos con datos en memoria:

```java
// Capítulo 3: Datos en memoria
private static final Map<String, User> USERS = new HashMap<>();
```

**Problemas:**
- ❌ Los datos se pierden al reiniciar la aplicación
- ❌ No hay concurrencia real (múltiples usuarios)
- ❌ No se pueden hacer transacciones ACID
- ❌ No hay backup ni recovery
- ❌ Limitado por la RAM del servidor

**Solución:** Integrar con una base de datos relacional (PostgreSQL)

### 🏗️ Evolución de la Arquitectura

**Antes (Capítulo 3):**
```
GraphQL Resolver → HashMap en memoria
```

**Ahora (Capítulo 4):**
```
GraphQL Resolver → Service Layer → Repository → PostgreSQL
```

### 📦 Stack Tecnológico

| Componente | Propósito | Alternativas |
|------------|-----------|--------------|
| **PostgreSQL** | Base de datos relacional | MySQL, MariaDB, Oracle |
| **Spring Data JPA** | ORM (Object-Relational Mapping) | Hibernate directo, MyBatis |
| **Docker** | Containerización | Podman, LXC |
| **HikariCP** | Connection pooling | Apache DBCP, C3P0 |

---

## Sección 4.1: Conexión a BD y Modelo de Persistencia

### 🐘 PostgreSQL con Docker

#### ¿Por qué Docker?

**Ventajas:**
1. ✅ **Zero Installation:** No instalar PostgreSQL en la máquina
2. ✅ **Portabilidad:** Mismo setup en dev, staging, prod
3. ✅ **Aislamiento:** No conflictos con otras apps
4. ✅ **Versionamiento:** Imagen específica (postgres:15-alpine)
5. ✅ **Reproducibilidad:** `docker-compose up` y listo

**Analogía:**
> Docker es como tener una máquina virtual ultraligera. En lugar de instalar PostgreSQL en tu Mac, lo "alquilas" en un contenedor aislado que se destruye y recrea fácilmente.

#### Configuración Docker Compose

```yaml
services:
  postgres:
    image: postgres:15-alpine        # Imagen oficial
    container_name: neobank-savings-db
    environment:
      POSTGRES_DB: savingsdb         # Nombre de la BD
      POSTGRES_USER: neobank         # Usuario
      POSTGRES_PASSWORD: neobank123  # Password
    ports:
      - "5432:5432"                  # Puerto host:container
    volumes:
      - postgres_data:/var/lib/postgresql/data  # Persistencia
```

**Explicación de Volúmenes:**

Sin volumen:
```
┌──────────────┐
│  Container   │  ← Datos aquí
└──────────────┘
     ↓ docker-compose down
   💥 Datos perdidos
```

Con volumen:
```
┌──────────────┐      ┌─────────────┐
│  Container   │ ←──→ │   Volume    │
└──────────────┘      │ (persiste)  │
                      └─────────────┘
```

### 🗄️ Spring Data JPA

#### ¿Qué es JPA?

**JPA (Java Persistence API)** es una especificación para mapear objetos Java a tablas SQL.

**Sin JPA:**
```java
// Código manual horrible
String sql = "INSERT INTO savings_goals (user_id, name, target_amount) VALUES (?, ?, ?)";
PreparedStatement stmt = connection.prepareStatement(sql);
stmt.setLong(1, goal.getUserId());
stmt.setString(2, goal.getName());
stmt.setBigDecimal(3, goal.getTargetAmount());
stmt.executeUpdate();
```

**Con JPA:**
```java
// Limpio y simple
savingsGoalRepository.save(goal);
```

#### Configuración Spring Boot

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/savingsdb
    username: neobank
    password: neobank123
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: create-drop  # Crea tablas al iniciar
    defer-datasource-initialization: true  # IMPORTANTE
    show-sql: true  # Ver SQL en logs
```

**⚠️ IMPORTANTE: `defer-datasource-initialization`**

**Problema:**
```
1. Spring Boot inicia
2. ❌ data.sql se ejecuta (tablas no existen aún)
3. ✅ Hibernate crea tablas
4. 💥 ERROR: relation "savings_goals" does not exist
```

**Solución:**
```yaml
defer-datasource-initialization: true
```

**Ahora:**
```
1. Spring Boot inicia
2. ✅ Hibernate crea tablas
3. ✅ data.sql se ejecuta (tablas ya existen)
4. ✅ Datos insertados correctamente
```

### 📊 Entidades JPA

#### Entidad vs GraphQL Type

**Concepto clave:** Son cosas DIFERENTES

**Entidad JPA (SavingsGoalEntity.java):**
```java
@Entity
@Table(name = "savings_goals")
public class SavingsGoalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long goalId;  // PK en base de datos
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal targetAmount;
    
    @Enumerated(EnumType.STRING)
    private GoalStatus status;
}
```

**GraphQL Type (savings-schema.graphqls):**
```graphql
type SavingsGoal {
  id: ID!              # String en GraphQL
  userId: ID!
  targetAmount: Money!
  status: GoalStatus!
  progressPercentage: Float!  # ← Calculado, NO en DB
}
```

#### ¿Por qué separar?

**Razones:**

1. **Campos calculados:** `progressPercentage` no existe en DB
2. **Tipos diferentes:** DB usa `Long`, GraphQL usa `ID` (String)
3. **Seguridad:** No exponer todos los campos de la BD
4. **Evolución:** Cambiar schema sin cambiar DB

**Analogía:**
> La entidad JPA es como un "contrato con la base de datos" (estructura física).  
> El GraphQL Type es como un "contrato con el cliente" (API pública).

#### Anotaciones JPA Importantes

```java
@Entity                     // Marca la clase como entidad JPA
@Table(name = "...")        // Nombre de la tabla
@Id                         // Primary key
@GeneratedValue(...)        // Auto-increment
@Column(nullable = false)   // NOT NULL en SQL
@Enumerated(EnumType.STRING)// Guardar enum como texto
@OneToMany                  // Relación 1 a N
@ManyToOne                  // Relación N a 1
```

#### Enums en JPA

**IMPORTANTE:** Usar `EnumType.STRING`

```java
@Enumerated(EnumType.STRING)  // ✅ Guarda "ACTIVE", "PAUSED"
private GoalStatus status;

// ❌ MAL: EnumType.ORDINAL guarda 0, 1, 2...
// Si cambias el orden del enum, ¡se corrompen los datos!
```

**Ejemplo:**
```java
enum GoalStatus {
    ACTIVE,    // ORDINAL = 0
    PAUSED,    // ORDINAL = 1
    COMPLETED  // ORDINAL = 2
}

// DB con ORDINAL:
// goal_1: status = 0 (ACTIVE)

// Refactorizas el enum:
enum GoalStatus {
    DRAFT,      // ORDINAL = 0  ← ¡Cambió!
    ACTIVE,     // ORDINAL = 1
    PAUSED,
    COMPLETED
}

// Ahora goal_1 tiene status = 0 = DRAFT 💥
```

### 🔄 Mapeo Entity ↔ GraphQL

**En el Resolver:**

```java
private Map<String, Object> mapToGraphQL(SavingsGoalEntity entity) {
    Map<String, Object> map = new HashMap<>();
    
    // Campos directos
    map.put("id", entity.getGoalId().toString());  // Long → String
    map.put("name", entity.getName());
    map.put("targetAmount", entity.getTargetAmount());
    map.put("currentAmount", entity.getCurrentAmount());
    
    // Enums
    map.put("status", entity.getStatus().name());  // ACTIVE → "ACTIVE"
    map.put("category", entity.getCategory().name());
    
    // Campos calculados
    map.put("progressPercentage", calculateProgress(entity));
    
    return map;
}

private double calculateProgress(SavingsGoalEntity entity) {
    if (entity.getTargetAmount().compareTo(BigDecimal.ZERO) == 0) {
        return 0.0;
    }
    return entity.getCurrentAmount()
        .divide(entity.getTargetAmount(), 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100))
        .doubleValue();
}
```

**¿Por qué `Map<String, Object>`?**

**Ventajas:**
- ✅ Flexibilidad: Agregar campos sin regenerar código
- ✅ Campos calculados fáciles
- ✅ No depender de codegen de DGS

**Desventaja:**
- ❌ Type safety: No hay verificación de tipos en compile-time

**Alternativa:** Usar codegen de DGS (veremos en capítulos avanzados)

---

## Sección 4.2: Resolvers con Acceso a Datos Reales

### 🏛️ Arquitectura en Capas

**Principio de Separación de Responsabilidades:**

```
┌────────────────────────────────────────────────────────────┐
│  Resolver Layer                                            │
│  Responsabilidad: Transformar entre GraphQL y Java         │
│  - @DgsQuery, @DgsMutation                                 │
│  - InputArgument parsing                                   │
│  - Mapeo Entity → GraphQL Type                             │
└────────────────────────────────────────────────────────────┘
                           ↓
┌────────────────────────────────────────────────────────────┐
│  Service Layer                                             │
│  Responsabilidad: Lógica de negocio                        │
│  - Validaciones                                            │
│  - Reglas de negocio                                       │
│  - Orquestación                                            │
│  - @Transactional                                          │
└────────────────────────────────────────────────────────────┘
                           ↓
┌────────────────────────────────────────────────────────────┐
│  Repository Layer                                          │
│  Responsabilidad: Acceso a datos                           │
│  - Queries SQL                                             │
│  - CRUD operations                                         │
│  - Spring Data JPA                                         │
└────────────────────────────────────────────────────────────┘
                           ↓
┌────────────────────────────────────────────────────────────┐
│  Database                                                  │
│  PostgreSQL                                                │
└────────────────────────────────────────────────────────────┘
```

**¿Por qué capas?**

1. **Mantenibilidad:** Cambiar una capa sin afectar otras
2. **Testabilidad:** Mockear fácilmente cada capa
3. **Reutilización:** Service puede usarse desde REST, GraphQL, etc.
4. **Separación de concerns:** Cada capa hace UNA cosa bien

### 📦 Spring Data JPA Repositories

#### Interface Repository

```java
@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoalEntity, Long> {
    
    // Query methods (Spring genera SQL automáticamente)
    List<SavingsGoalEntity> findByUserId(Long userId);
    
    List<SavingsGoalEntity> findByUserIdAndStatus(Long userId, GoalStatus status);
}
```

**Magia de Spring Data:**

Spring lee el nombre del método y genera SQL:

```java
findByUserId(Long userId)
↓
SELECT * FROM savings_goals WHERE user_id = ?

findByUserIdAndStatus(Long userId, GoalStatus status)
↓
SELECT * FROM savings_goals WHERE user_id = ? AND status = ?
```

#### Query Methods Patterns

| Método | SQL Generado |
|--------|--------------|
| `findByName` | `WHERE name = ?` |
| `findByNameAndCategory` | `WHERE name = ? AND category = ?` |
| `findByNameContaining` | `WHERE name LIKE %?%` |
| `findByTargetAmountGreaterThan` | `WHERE target_amount > ?` |
| `findByCreatedAtBetween` | `WHERE created_at BETWEEN ? AND ?` |
| `findByStatusIn(List<Status>)` | `WHERE status IN (...)` |
| `countByUserId` | `SELECT COUNT(*) WHERE user_id = ?` |

**Ejemplo avanzado:**

```java
List<SavingsGoalEntity> findByUserIdAndStatusAndTargetAmountGreaterThanOrderByCreatedAtDesc(
    Long userId, 
    GoalStatus status, 
    BigDecimal minAmount
);

// SQL generado:
// SELECT * FROM savings_goals 
// WHERE user_id = ? 
//   AND status = ? 
//   AND target_amount > ? 
// ORDER BY created_at DESC
```

### 🔧 Service Layer

```java
@Service
@RequiredArgsConstructor  // Lombok: Constructor injection
public class SavingsGoalService {
    
    private final SavingsGoalRepository repository;
    
    public SavingsGoalEntity getGoalById(Long goalId) {
        return repository.findById(goalId)
            .orElseThrow(() -> new GoalNotFoundException(goalId));
    }
    
    public List<SavingsGoalEntity> getGoalsByUserId(Long userId) {
        return repository.findByUserId(userId);
    }
    
    public List<SavingsGoalEntity> getActiveGoalsByUserId(Long userId) {
        return repository.findByUserIdAndStatus(userId, GoalStatus.ACTIVE);
    }
    
    @Transactional
    public SavingsGoalEntity createGoal(SavingsGoalEntity goal) {
        // Validaciones
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Target amount must be positive");
        }
        
        // Defaults
        goal.setStatus(GoalStatus.ACTIVE);
        goal.setCurrentAmount(BigDecimal.ZERO);
        
        return repository.save(goal);
    }
}
```

**¿Por qué `@RequiredArgsConstructor`?**

**Sin Lombok:**
```java
private final SavingsGoalRepository repository;

public SavingsGoalService(SavingsGoalRepository repository) {
    this.repository = repository;
}
```

**Con Lombok:**
```java
@RequiredArgsConstructor
private final SavingsGoalRepository repository;
// Constructor generado automáticamente
```

**Inyección de dependencias:** Spring ve el constructor y automáticamente inyecta el repository.

### 🎨 Resolver Layer

```java
@DgsComponent
@RequiredArgsConstructor
public class SavingsGoalResolver {
    
    private final SavingsGoalService service;
    
    @DgsQuery
    public Map<String, Object> savingsGoal(@InputArgument String id) {
        SavingsGoalEntity entity = service.getGoalById(Long.parseLong(id));
        return mapToGraphQL(entity);
    }
    
    @DgsQuery
    public List<Map<String, Object>> savingsGoals(@InputArgument String userId) {
        return service.getGoalsByUserId(Long.parseLong(userId)).stream()
            .map(this::mapToGraphQL)
            .collect(Collectors.toList());
    }
}
```

**Responsabilidades del Resolver:**
1. ✅ Parsear argumentos GraphQL (`String` → `Long`)
2. ✅ Llamar al service
3. ✅ Mapear Entity → GraphQL Type
4. ❌ NO lógica de negocio
5. ❌ NO acceso directo a repository

---

## Sección 4.3: Mutations Persistentes con Transacciones

### 💾 Mutations con Side Effects

**Diferencia clave:**

**Query:** Solo lectura, no modifica estado
```graphql
{
  savingsGoal(id: "1") { name }
}
```

**Mutation:** Modifica estado (crea, actualiza, elimina)
```graphql
mutation {
  createSavingsGoal(input: { ... }) {
    success
    message
  }
}
```

### 🔄 Transacciones con @Transactional

#### ¿Qué es una transacción?

**Definición:** Conjunto de operaciones que se ejecutan como una unidad atómica.

**Propiedades ACID:**

| Propiedad | Significado | Ejemplo |
|-----------|-------------|---------|
| **A**tomicity | Todo o nada | Si falla paso 3 de 5, se revierten pasos 1 y 2 |
| **C**onsistency | Reglas siempre válidas | Balance nunca negativo |
| **I**solation | Transacciones no se afectan | Usuario A y B operan independiente |
| **D**urability | Cambios permanentes | Después de commit, sobrevive a crash |

#### Ejemplo Sin Transacción (MAL)

```java
public void transferMoney(Long fromGoal, Long toGoal, BigDecimal amount) {
    // Paso 1: Restar de fromGoal
    SavingsGoalEntity from = repository.findById(fromGoal).get();
    from.setCurrentAmount(from.getCurrentAmount().subtract(amount));
    repository.save(from);
    
    // 💥 ERROR AQUÍ (crash, network, etc.)
    
    // Paso 2: Sumar a toGoal (nunca se ejecuta)
    SavingsGoalEntity to = repository.findById(toGoal).get();
    to.setCurrentAmount(to.getCurrentAmount().add(amount));
    repository.save(to);
    
    // Resultado: ¡Dinero desapareció! 💸
}
```

#### Ejemplo Con Transacción (BIEN)

```java
@Transactional
public void transferMoney(Long fromGoal, Long toGoal, BigDecimal amount) {
    // Paso 1: Restar
    SavingsGoalEntity from = repository.findById(fromGoal).get();
    from.setCurrentAmount(from.getCurrentAmount().subtract(amount));
    repository.save(from);
    
    // 💥 ERROR AQUÍ
    
    // Paso 2: Sumar (nunca se ejecuta)
    SavingsGoalEntity to = repository.findById(toGoal).get();
    to.setCurrentAmount(to.getCurrentAmount().add(amount));
    repository.save(to);
    
    // Si hay error: ROLLBACK automático
    // Resultado: Ambos goals sin cambios ✅
}
```

**Flujo:**

```
┌─────────────────────────────────────────────────────┐
│  @Transactional START                               │
├─────────────────────────────────────────────────────┤
│  1. BEGIN TRANSACTION                               │
│  2. UPDATE savings_goals SET current_amount = ...   │
│  3. UPDATE savings_goals SET current_amount = ...   │
│  4. COMMIT TRANSACTION  ← Si todo OK                │
│     O                                                │
│     ROLLBACK  ← Si hay exception                    │
└─────────────────────────────────────────────────────┘
```

### 📝 Mutation en GraphQL

```java
@DgsMutation
public Map<String, Object> createSavingsGoal(@InputArgument Map<String, Object> input) {
    try {
        // 1. Construir entidad desde input
        SavingsGoalEntity entity = SavingsGoalEntity.builder()
            .userId(Long.parseLong(input.get("userId").toString()))
            .name(input.get("name").toString())
            .targetAmount(new BigDecimal(input.get("targetAmount").toString()))
            .category(SavingsGoalEntity.GoalCategory.valueOf(
                input.get("category").toString()))
            .build();
        
        // 2. Llamar service (con @Transactional)
        SavingsGoalEntity saved = service.createGoal(entity);
        
        // 3. Construir response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Goal created successfully");
        response.put("goal", mapToGraphQL(saved));
        return response;
        
    } catch (Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Error: " + e.getMessage());
        response.put("goal", null);
        return response;
    }
}
```

**Response Type Pattern:**

```graphql
type SavingsGoalResponse {
  success: Boolean!
  message: String!
  goal: SavingsGoal  # nullable si falla
}
```

**Ventajas:**
- ✅ Cliente sabe si funcionó sin revisar errores
- ✅ Mensaje descriptivo para el usuario
- ✅ Datos del goal creado si exitoso

---

## Sección 4.4: Manejo de Errores y Excepciones

### 🚨 Custom Exceptions

```java
public class GoalNotFoundException extends RuntimeException {
    public GoalNotFoundException(Long goalId) {
        super("Savings goal not found with ID: " + goalId);
    }
}

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
```

### 🎯 GraphQL Exception Handler

```java
@Component
public class GraphQLExceptionHandler implements DataFetcherExceptionHandler {
    
    @Override
    public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(
            DataFetcherExceptionHandlerParameters handlerParameters) {
        
        Throwable exception = handlerParameters.getException();
        
        TypedGraphQLError error;
        
        if (exception instanceof GoalNotFoundException) {
            error = TypedGraphQLError.newBuilder()
                .message(exception.getMessage())
                .errorType(ErrorType.NOT_FOUND)
                .path(handlerParameters.getPath())
                .build();
                
        } else if (exception instanceof UnauthorizedAccessException) {
            error = TypedGraphQLError.newBuilder()
                .message(exception.getMessage())
                .errorType(ErrorType.PERMISSION_DENIED)
                .path(handlerParameters.getPath())
                .build();
                
        } else {
            error = TypedGraphQLError.newBuilder()
                .message("Internal server error")
                .errorType(ErrorType.INTERNAL)
                .build();
        }
        
        return CompletableFuture.completedFuture(
            DataFetcherExceptionHandlerResult.newResult()
                .error(error)
                .build()
        );
    }
}
```

**Error Types:**

| Tipo | Cuándo usar | HTTP equivalente |
|------|-------------|------------------|
| `NOT_FOUND` | Recurso no existe | 404 |
| `PERMISSION_DENIED` | Sin permisos | 403 |
| `BAD_REQUEST` | Input inválido | 400 |
| `UNAUTHENTICATED` | No autenticado | 401 |
| `INTERNAL` | Error del servidor | 500 |

**Response de error:**

```json
{
  "errors": [
    {
      "message": "Savings goal not found with ID: 999",
      "locations": [],
      "path": ["savingsGoal"],
      "extensions": {
        "classification": "NOT_FOUND"
      }
    }
  ],
  "data": null
}
```

---

## Conceptos Avanzados

### 🔍 N+1 Problem Preview

**Problema:**

```graphql
{
  savingsGoals(userId: "1") {
    name
    user {  # ← Lazy loading
      email
    }
  }
}
```

**Queries ejecutadas:**

```sql
-- Query 1: Obtener goals
SELECT * FROM savings_goals WHERE user_id = 1;  -- 3 results

-- Query 2: Obtener user del goal 1
SELECT * FROM users WHERE id = 1;

-- Query 3: Obtener user del goal 2
SELECT * FROM users WHERE id = 1;  -- ¡Mismo user!

-- Query 4: Obtener user del goal 3
SELECT * FROM users WHERE id = 1;  -- ¡Mismo user otra vez!

-- Total: 4 queries (1 + 3)
```

**Solución:** DataLoader (veremos en capítulos avanzados)

### 🎭 DTOs vs Entities

**DTO (Data Transfer Object):**

```java
public class CreateSavingsGoalDTO {
    private Long userId;
    private String name;
    private BigDecimal targetAmount;
    private GoalCategory category;
    // Solo campos necesarios para crear
}
```

**Ventaja:** Validación y transformación clara

**Entity:**

```java
@Entity
public class SavingsGoalEntity {
    // Todos los campos de la tabla
    private Long goalId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // etc.
}
```

**Pattern:**

```
GraphQL Input → DTO → Entity → DB
DB → Entity → DTO → GraphQL Type
```

### 🔐 Soft Delete vs Hard Delete

**Hard Delete (lo que usamos):**
```java
repository.deleteById(goalId);
// Goal desaparece de la BD
```

**Soft Delete (mejor para producción):**
```java
goal.setStatus(GoalStatus.DELETED);
goal.setDeletedAt(LocalDateTime.now());
repository.save(goal);
// Goal sigue en BD pero "marcado" como eliminado
```

**Ventajas Soft Delete:**
- ✅ Auditoría (saber quién y cuándo eliminó)
- ✅ Recovery (restaurar datos)
- ✅ Integridad referencial
- ✅ Analytics históricos

---

## Antipatrones y Mejores Prácticas

### ❌ Antipatrón 1: Lógica en Resolver

**MAL:**
```java
@DgsQuery
public List<Map<String, Object>> activeSavingsGoals(@InputArgument String userId) {
    List<SavingsGoalEntity> all = repository.findByUserId(Long.parseLong(userId));
    
    // ❌ Filtrado en resolver
    return all.stream()
        .filter(g -> g.getStatus() == GoalStatus.ACTIVE)
        .map(this::mapToGraphQL)
        .collect(Collectors.toList());
}
```

**BIEN:**
```java
@DgsQuery
public List<Map<String, Object>> activeSavingsGoals(@InputArgument String userId) {
    return service.getActiveGoalsByUserId(Long.parseLong(userId))
        .stream()
        .map(this::mapToGraphQL)
        .collect(Collectors.toList());
}

// Service:
public List<SavingsGoalEntity> getActiveGoalsByUserId(Long userId) {
    return repository.findByUserIdAndStatus(userId, GoalStatus.ACTIVE);
}
```

### ❌ Antipatrón 2: No usar @Transactional

**MAL:**
```java
public void addContribution(Long goalId, BigDecimal amount) {
    SavingsGoalEntity goal = repository.findById(goalId).get();
    goal.setCurrentAmount(goal.getCurrentAmount().add(amount));
    repository.save(goal);
    
    // Si esto falla, goal ya cambió ↑
    contributionRepository.save(contribution);
}
```

**BIEN:**
```java
@Transactional
public void addContribution(Long goalId, BigDecimal amount) {
    SavingsGoalEntity goal = repository.findById(goalId).get();
    goal.setCurrentAmount(goal.getCurrentAmount().add(amount));
    repository.save(goal);
    
    contributionRepository.save(contribution);
    // Si falla, todo se revierte automáticamente
}
```

### ❌ Antipatrón 3: Exponer Primary Keys

**MAL:**
```graphql
type SavingsGoal {
  goalId: Int!  # ❌ Expone PK interno
}
```

**BIEN:**
```graphql
type SavingsGoal {
  id: ID!  # ✅ Opaco para el cliente
}
```

**Razón:** El cliente no debe saber detalles de implementación de la BD.

### ✅ Mejores Prácticas

1. **Usar Builder Pattern:**
```java
SavingsGoalEntity goal = SavingsGoalEntity.builder()
    .userId(userId)
    .name(name)
    .targetAmount(amount)
    .build();
```

2. **Validar en Service, no en Resolver:**
```java
@Service
public class SavingsGoalService {
    public SavingsGoalEntity createGoal(SavingsGoalEntity goal) {
        // Validaciones aquí
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("...");
        }
        return repository.save(goal);
    }
}
```

3. **Usar Optional correctamente:**
```java
// ❌ MAL
SavingsGoalEntity goal = repository.findById(id).get();  // Puede lanzar NoSuchElementException

// ✅ BIEN
SavingsGoalEntity goal = repository.findById(id)
    .orElseThrow(() -> new GoalNotFoundException(id));
```

4. **Connection Pooling:**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
```

---

## Casos de Uso Reales

### 💳 Sistema Bancario

**Transferencia entre goals:**

```java
@Transactional
public void transferBetweenGoals(Long fromId, Long toId, BigDecimal amount) {
    SavingsGoalEntity from = repository.findById(fromId)
        .orElseThrow(() -> new GoalNotFoundException(fromId));
    SavingsGoalEntity to = repository.findById(toId)
        .orElseThrow(() -> new GoalNotFoundException(toId));
    
    // Validar fondos
    if (from.getCurrentAmount().compareTo(amount) < 0) {
        throw new InsufficientFundsException();
    }
    
    // Transferir
    from.setCurrentAmount(from.getCurrentAmount().subtract(amount));
    to.setCurrentAmount(to.getCurrentAmount().add(amount));
    
    repository.saveAll(Arrays.asList(from, to));
    
    // Si cualquier paso falla: ROLLBACK automático
}
```

### 📊 Analytics en Tiempo Real

**Dashboard de progreso:**

```java
public Map<String, Object> getUserDashboard(Long userId) {
    List<SavingsGoalEntity> goals = repository.findByUserId(userId);
    
    BigDecimal totalTarget = goals.stream()
        .map(SavingsGoalEntity::getTargetAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    BigDecimal totalSaved = goals.stream()
        .map(SavingsGoalEntity::getCurrentAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    long activeCount = goals.stream()
        .filter(g -> g.getStatus() == GoalStatus.ACTIVE)
        .count();
    
    Map<String, Object> dashboard = new HashMap<>();
    dashboard.put("totalGoals", goals.size());
    dashboard.put("activeGoals", activeCount);
    dashboard.put("totalTarget", totalTarget);
    dashboard.put("totalSaved", totalSaved);
    dashboard.put("overallProgress", calculateOverallProgress(totalSaved, totalTarget));
    
    return dashboard;
}
```

### 🎯 Gamificación

**Logros automáticos:**

```java
@Transactional
public void checkAndAwardAchievements(Long userId) {
    List<SavingsGoalEntity> goals = repository.findByUserId(userId);
    
    // Logro: Primera meta completada
    boolean hasCompleted = goals.stream()
        .anyMatch(g -> g.getStatus() == GoalStatus.COMPLETED);
    if (hasCompleted && !userHasAchievement(userId, "FIRST_GOAL")) {
        awardAchievement(userId, "FIRST_GOAL");
    }
    
    // Logro: $10,000 ahorrados
    BigDecimal totalSaved = goals.stream()
        .map(SavingsGoalEntity::getCurrentAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (totalSaved.compareTo(new BigDecimal("10000")) >= 0) {
        awardAchievement(userId, "SAVER_10K");
    }
}
```

---

## 🎓 Resumen del Capítulo

### Conceptos Clave

1. **PostgreSQL + Docker** = Desarrollo sin instalaciones complejas
2. **JPA** = Mapeo objeto-relacional automático
3. **Capas** = Resolver → Service → Repository
4. **@Transactional** = ACID garantizado
5. **Exception Handling** = Errores profesionales

### Lo que aprendimos

✅ Conectar GraphQL con PostgreSQL  
✅ Usar Docker para dependencias  
✅ Entidades JPA vs GraphQL Types  
✅ Queries y Mutations persistentes  
✅ Transacciones ACID  
✅ Manejo de errores profesional  
✅ Arquitectura en capas  

### Próximo Capítulo

**Chapter 5: Apollo Federation**
- Microservicios con GraphQL
- Subgraphs y Supergraph
- Entity references
- Queries distribuidas

---

**Curso:** GraphQL con Netflix DGS Framework  
**Feature:** Smart Savings Goals  
**Database:** PostgreSQL 15  
**Framework:** Spring Boot 3.2 + Netflix DGS 8.2