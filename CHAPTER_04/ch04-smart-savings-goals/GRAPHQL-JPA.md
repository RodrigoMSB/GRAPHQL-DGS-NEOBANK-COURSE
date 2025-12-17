# 📚 CAPÍTULO 4: Smart Savings Goals - GraphQL + JPA + PostgreSQL

## 🎯 Tema Central
**Persistencia de datos con JPA (Java Persistence API) y transacciones en GraphQL**

Este capítulo conecta GraphQL con una base de datos real (PostgreSQL) usando Spring Data JPA, demostrando cómo manejar operaciones CRUD con transacciones atómicas.

---

## 🏦 Feature del NeoBank
**Smart Savings Goals** - Metas de ahorro inteligentes donde los usuarios pueden:
- Crear metas de ahorro (vacaciones, emergencia, casa, etc.)
- Depositar dinero hacia sus metas
- Ver el progreso en porcentaje
- Pausar, completar o cancelar metas

---

## 📊 Stack Tecnológico

| Componente | Tecnología |
|------------|------------|
| Framework GraphQL | Netflix DGS 8.2.0 |
| Persistencia | Spring Data JPA |
| Base de datos | PostgreSQL |
| Java | 17 |
| Spring Boot | 3.2.0 |

---

## 🗂️ Estructura del Proyecto

```
ch04-smart-savings-goals/
├── src/main/java/com/neobank/savings/
│   ├── model/
│   │   └── SavingsGoalEntity.java    ← Entidad JPA con @Entity, @Table
│   ├── repository/
│   │   └── SavingsGoalRepository.java ← Interface Spring Data JPA
│   ├── service/
│   │   └── SavingsGoalService.java   ← Lógica de negocio + @Transactional
│   ├── resolver/
│   │   └── SavingsGoalResolver.java  ← Queries y Mutations DGS
│   └── scalar/
│       └── MoneyScalar.java          ← Scalar personalizado para BigDecimal
├── src/main/resources/
│   ├── schema/savings-schema.graphqls
│   ├── application.yml
│   └── data.sql                      ← Datos iniciales
└── docker-compose.yml                ← PostgreSQL containerizado
```

---

## 📖 Secciones del Capítulo

### 4.1 - Integración JPA con GraphQL
Cómo conectar resolvers DGS con repositorios JPA:
```java
@DgsComponent
public class SavingsGoalResolver {
    private final SavingsGoalService service;
    
    @DgsQuery
    public Map<String, Object> savingsGoal(@InputArgument String id) {
        SavingsGoalEntity entity = service.getGoalById(Long.parseLong(id));
        return toGraphQL(entity);  // Transforma Entity → Map para GraphQL
    }
}
```

### 4.2 - Entidades, Repositorios y Mapeo
Mapeo JPA a PostgreSQL:
```java
@Entity
@Table(name = "savings_goals")
public class SavingsGoalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long goalId;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal targetAmount;  // SIEMPRE BigDecimal para dinero
    
    @Enumerated(EnumType.STRING)
    private GoalStatus status;
}
```

### 4.3 - Transacciones en Mutations
`@Transactional` para operaciones atómicas:
```java
@Transactional
public SavingsGoalEntity deposit(Long goalId, BigDecimal amount) {
    SavingsGoalEntity goal = getGoalById(goalId);
    goal.setCurrentAmount(goal.getCurrentAmount().add(amount));
    
    if (goal.isCompleted()) {
        goal.setStatus(GoalStatus.COMPLETED);  // También se hace rollback si falla
    }
    
    return repository.save(goal);
}
```

### 4.4 - Campos Calculados
Campos que no están en BD pero se calculan en runtime:
```java
// progressPercentage NO está en la tabla, se calcula:
map.put("progressPercentage", 
    currentAmount.divide(targetAmount, 4, RoundingMode.HALF_UP)
                 .multiply(BigDecimal.valueOf(100))
                 .doubleValue());
```

### 4.5 - Manejo de Errores
Patrón de respuesta estructurada (no excepciones GraphQL):
```java
@DgsMutation
public Map<String, Object> createSavingsGoal(@InputArgument Map<String, Object> input) {
    try {
        SavingsGoalEntity saved = service.createGoal(entity);
        return Map.of("success", true, "message", "Created!", "goal", toGraphQL(saved));
    } catch (Exception e) {
        return Map.of("success", false, "message", e.getMessage(), "goal", null);
    }
}
```

---

## 🔑 Conceptos Clave

### ¿Por qué BigDecimal para dinero?
```java
double d = 0.1 + 0.2;  // = 0.30000000000000004 ❌
BigDecimal b = new BigDecimal("0.1").add(new BigDecimal("0.2"));  // = 0.3 ✅
```

### ¿Por qué @Transactional?
Sin transacción:
```
1. Guardar meta → OK
2. Actualizar balance → FALLA
Resultado: Datos inconsistentes 😱
```

Con transacción:
```
BEGIN TRANSACTION
1. Guardar meta → OK
2. Actualizar balance → FALLA
ROLLBACK (todo se revierte)
Resultado: Datos consistentes ✅
```

### Transformación Entity → GraphQL
La entidad tiene `goalId`, pero GraphQL espera `id`:
```java
private Map<String, Object> toGraphQL(SavingsGoalEntity entity) {
    Map<String, Object> map = new HashMap<>();
    map.put("id", entity.getGoalId().toString());  // goalId → id
    map.put("progressPercentage", calculateProgress(entity));  // Campo calculado
    return map;
}
```

---

## 📝 Modelo de Datos

### Enums
```java
enum GoalStatus {
    ACTIVE,      // Aceptando depósitos
    PAUSED,      // Pausada temporalmente
    COMPLETED,   // currentAmount >= targetAmount
    CANCELLED    // Cancelada por usuario
}

enum GoalCategory {
    EMERGENCY_FUND,  // Fondo de emergencia
    VACATION,        // Viajes
    HOME_PURCHASE,   // Enganche casa
    EDUCATION,       // Universidad/cursos
    RETIREMENT,      // Retiro
    INVESTMENT,      // Inversiones
    OTHER
}
```

### Ciclo de Vida
```
ACTIVE → COMPLETED (al alcanzar meta)
ACTIVE → PAUSED (usuario pausa)
ACTIVE → CANCELLED (usuario cancela)
```

---

## 🎓 Lo que el Alumno Aprende

1. **Configurar Spring Data JPA** con PostgreSQL
2. **Mapear entidades** con anotaciones `@Entity`, `@Table`, `@Column`
3. **Usar `@Transactional`** para operaciones atómicas
4. **Transformar entidades JPA** a respuestas GraphQL
5. **Implementar campos calculados** que no están en BD
6. **Manejar errores** con patrón success/message/data
7. **Usar BigDecimal** correctamente para valores monetarios

---

## 💡 Diferencia con Capítulos Anteriores

| Capítulo | Persistencia |
|----------|--------------|
| Ch01-03 | Datos en memoria (MockDataService) |
| **Ch04** | **Base de datos real (PostgreSQL + JPA)** |
