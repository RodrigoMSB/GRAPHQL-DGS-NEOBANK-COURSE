#!/usr/bin/env bash

################################################################################
# CHAPTER 04: SMART SAVINGS GOALS CON JPA Y POSTGRESQL
# Script de Testing Automatizado - VERSIÓN EDUCATIVA
#
# Compatible con:
#   - macOS (Bash 3.2+)
#   - Linux (Bash 4.0+)
#   - Windows GitBash (Bash 4.4+)
#
# Uso: 
#   ./test-chapter04.sh           (modo interactivo)
#   ./test-chapter04.sh -s        (modo silencioso)
################################################################################

export LC_ALL=C

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
MAGENTA='\033[1;35m'
CYAN='\033[0;36m'
BLUE='\033[0;34m'
WHITE='\033[1;37m'
GRAY='\033[0;90m'
NC='\033[0m'

# Config
BASE_URL="http://localhost:8080"
GRAPHQL_ENDPOINT="${BASE_URL}/graphql"
OUTPUT_FILE="test-results-chapter04-$(date +%Y%m%d-%H%M%S).txt"

INTERACTIVE=true
if [ "$1" = "-s" ]; then
    INTERACTIVE=false
fi

TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

print_colored() {
    printf "%b\n" "$1" | tee -a "$OUTPUT_FILE"
}

pause() {
    if [ "$INTERACTIVE" = true ]; then
        print_colored "${YELLOW}⏸️  Presiona Enter para continuar...${NC}"
        read -r
    else
        sleep 0.5
    fi
}

log() {
    print_colored "$1"
}

print_section() {
    log ""
    log "${CYAN}================================================================================${NC}"
    log "${CYAN}$1${NC}"
    log "${CYAN}================================================================================${NC}"
    log ""
}

print_subsection() {
    log ""
    log "${MAGENTA}────────────────────────────────────────────────────────────────────────────${NC}"
    log "${MAGENTA}$1${NC}"
    log "${MAGENTA}────────────────────────────────────────────────────────────────────────────${NC}"
}

print_concept() {
    log ""
    log "${WHITE}📚 CONCEPTO:${NC}"
    log "${GRAY}$1${NC}"
    log ""
}

# Función para ejecutar tests con REQUEST visible y formateado
run_graphql_test() {
    local test_name="$1"
    local graphql_query="$2"
    local validation="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    log "${YELLOW}🧪 Test #${TOTAL_TESTS}: ${test_name}${NC}"
    log ""
    
    # Mostrar el REQUEST
    log "${BLUE}📤 REQUEST:${NC}"
    log "${WHITE}   POST ${GRAPHQL_ENDPOINT}${NC}"
    log "${WHITE}   Content-Type: application/json${NC}"
    log ""
    
    # Mostrar el BODY JSON formateado correctamente
    local display_query
    display_query=$(echo "$graphql_query" | sed 's/\\"/"/g')
    
    log "${BLUE}📋 BODY:${NC}"
    log "${GRAY}   {${NC}"
    log "${GRAY}     \"query\": \"${CYAN}${display_query}${GRAY}\"${NC}"
    log "${GRAY}   }${NC}"
    log ""
    
    # Ejecutar curl
    log "${BLUE}⚡ Ejecutando...${NC}"
    response=$(curl -s -X POST "${GRAPHQL_ENDPOINT}" \
        -H "Content-Type: application/json" \
        -d "{\"query\":\"$graphql_query\"}" 2>&1)
    exit_code=$?
    
    # Mostrar respuesta formateada
    log ""
    log "${BLUE}📥 RESPONSE:${NC}"
    if command -v jq >/dev/null 2>&1; then
        formatted=$(echo "$response" | jq '.' 2>/dev/null || echo "$response")
        echo "$formatted" | while IFS= read -r line; do
            log "${GREEN}   $line${NC}"
        done
    else
        log "${GREEN}   $response${NC}"
    fi
    
    log ""
    
    # Validar usando el response CRUDO (sin colores)
    if [ $exit_code -eq 0 ] && echo "$response" | grep -qE "$validation"; then
        log "${GREEN}   ✅ PASSED${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        log "${RED}   ❌ FAILED${NC}"
        log "${RED}   Expected pattern: $validation${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    
    log ""
    pause
}

# Función para tests con validación numérica específica
run_graphql_test_numeric() {
    local test_name="$1"
    local graphql_query="$2"
    local json_path="$3"
    local expected_value="$4"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    log "${YELLOW}🧪 Test #${TOTAL_TESTS}: ${test_name}${NC}"
    log ""
    
    log "${BLUE}📤 REQUEST:${NC}"
    log "${WHITE}   POST ${GRAPHQL_ENDPOINT}${NC}"
    log "${WHITE}   Content-Type: application/json${NC}"
    log ""
    
    local display_query
    display_query=$(echo "$graphql_query" | sed 's/\\"/"/g')
    
    log "${BLUE}📋 BODY:${NC}"
    log "${GRAY}   {${NC}"
    log "${GRAY}     \"query\": \"${CYAN}${display_query}${GRAY}\"${NC}"
    log "${GRAY}   }${NC}"
    log ""
    
    log "${BLUE}⚡ Ejecutando...${NC}"
    response=$(curl -s -X POST "${GRAPHQL_ENDPOINT}" \
        -H "Content-Type: application/json" \
        -d "{\"query\":\"$graphql_query\"}" 2>&1)
    
    log ""
    log "${BLUE}📥 RESPONSE:${NC}"
    if command -v jq >/dev/null 2>&1; then
        formatted=$(echo "$response" | jq '.' 2>/dev/null || echo "$response")
        echo "$formatted" | while IFS= read -r line; do
            log "${GREEN}   $line${NC}"
        done
    else
        log "${GREEN}   $response${NC}"
    fi
    
    log ""
    
    # Validar valor numérico
    actual_value=$(echo "$response" | jq -r "$json_path" 2>/dev/null)
    
    if [ "$actual_value" = "$expected_value" ] || [ "$actual_value" = "${expected_value}.0" ]; then
        log "${GREEN}   ✅ PASSED (valor: $actual_value)${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        log "${RED}   ❌ FAILED${NC}"
        log "${RED}   Expected: $expected_value, Got: $actual_value${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    
    log ""
    pause
}

check_server() {
    log "${YELLOW}🔍 Verificando que el servidor esté corriendo...${NC}"
    
    if curl -s "${GRAPHQL_ENDPOINT}" > /dev/null 2>&1; then
        log "${GREEN}✅ Servidor corriendo en ${BASE_URL}${NC}"
        log ""
        return 0
    else
        log "${RED}❌ ERROR: Servidor NO está corriendo en ${BASE_URL}${NC}"
        log "${YELLOW}Por favor:${NC}"
        log "${YELLOW}  1. docker-compose up -d${NC}"
        log "${YELLOW}  2. mvn spring-boot:run${NC}"
        log ""
        exit 1
    fi
}

check_dependencies() {
    log "${YELLOW}🔍 Verificando dependencias...${NC}"
    
    if ! command -v curl &> /dev/null; then
        log "${RED}❌ curl no instalado${NC}"
        exit 1
    fi
    log "${GREEN}✅ curl instalado${NC}"
    
    if ! command -v jq &> /dev/null; then
        log "${RED}❌ jq no instalado (instalar con: brew install jq)${NC}"
        exit 1
    fi
    log "${GREEN}✅ jq instalado${NC}"
    log ""
}

################################################################################
# HEADER
################################################################################

clear
log "${CYAN}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
log "${CYAN}║                                                                              ║${NC}"
log "${CYAN}║        📘 CHAPTER 04: SMART SAVINGS GOALS CON JPA Y POSTGRESQL              ║${NC}"
log "${CYAN}║                     Testing Automatizado Completo                            ║${NC}"
log "${CYAN}║                                                                              ║${NC}"
log "${CYAN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
log ""
log "${YELLOW}Feature: Smart Savings Goals (Metas de Ahorro Inteligentes)${NC}"
log "${YELLOW}Base de datos: PostgreSQL 15 + Spring Data JPA${NC}"
log "${YELLOW}Duración: 1.75 horas${NC}"
log "${YELLOW}Log: ${OUTPUT_FILE}${NC}"
log ""

check_dependencies
check_server
pause

################################################################################
# SECCIÓN 4.1 - INTEGRACIÓN JPA CON GRAPHQL
################################################################################

print_section "SECCIÓN 4.1 — INTEGRACIÓN JPA CON GRAPHQL"

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  🎯 OBJETIVO DE ESTA SECCIÓN                                               │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Entender cómo GraphQL se integra con una base de datos real usando JPA.   │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  DIFERENCIA CON CAPÍTULOS ANTERIORES:                                      │${NC}"
log "${WHITE}│  • Cap 1-3: Datos en memoria (MockDataService)                             │${NC}"
log "${WHITE}│  • Cap 4:   Datos en PostgreSQL (Spring Data JPA)                          │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ARQUITECTURA:                                                             │${NC}"
log "${WHITE}│  ┌──────────┐    ┌──────────┐    ┌────────────┐    ┌────────────┐         │${NC}"
log "${WHITE}│  │ GraphQL  │ -> │ Resolver │ -> │  Service   │ -> │ Repository │         │${NC}"
log "${WHITE}│  │  Query   │    │ @DgsQuery│    │ @Service   │    │ JpaRepo    │         │${NC}"
log "${WHITE}│  └──────────┘    └──────────┘    └────────────┘    └─────┬──────┘         │${NC}"
log "${WHITE}│                                                          │                │${NC}"
log "${WHITE}│                                                    ┌─────▼──────┐         │${NC}"
log "${WHITE}│                                                    │ PostgreSQL │         │${NC}"
log "${WHITE}│                                                    └────────────┘         │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

# Test 1: Query básica con JPA
print_subsection "Test 1: Query básica - Obtener metas de ahorro del usuario 1"

print_concept "   Esta query obtiene todas las metas de ahorro de un usuario.
   Los datos vienen de PostgreSQL, no de memoria.
   
   JAVA REPOSITORY:
   public interface SavingsGoalRepository extends JpaRepository<SavingsGoalEntity, Long> {
       List<SavingsGoalEntity> findByUserId(String userId);
   }
   
   JAVA SERVICE:
   public List<SavingsGoalEntity> getGoalsByUserId(String userId) {
       return repository.findByUserId(userId);
   }"

run_graphql_test "Obtener metas de usuario 1" \
    '{ savingsGoals(userId: \"1\") { id name targetAmount currentAmount progressPercentage category status } }' \
    '"savingsGoals".*"name".*"targetAmount"'

# Test 2: Query con filtro de estado
print_subsection "Test 2: Query filtrada - Solo metas ACTIVAS"

print_concept "   Esta query filtra por status = ACTIVE usando JPA.
   
   JAVA REPOSITORY:
   List<SavingsGoalEntity> findByUserIdAndStatus(String userId, GoalStatus status);
   
   SQL GENERADO POR JPA:
   SELECT * FROM savings_goals 
   WHERE user_id = ? AND status = 'ACTIVE'"

run_graphql_test "Obtener solo metas activas" \
    '{ activeSavingsGoals(userId: \"1\") { id name currentAmount targetAmount progressPercentage status } }' \
    '"activeSavingsGoals".*"status".*"ACTIVE"'

# Test 3: Query por ID específico
print_subsection "Test 3: Query por ID - Obtener meta específica"

print_concept "   Buscar por ID es la operación más básica de JPA.
   
   JAVA REPOSITORY (heredado de JpaRepository):
   Optional<SavingsGoalEntity> findById(Long id);
   
   JAVA SERVICE:
   public SavingsGoalEntity getGoalById(Long id) {
       return repository.findById(id)
           .orElseThrow(() -> new GoalNotFoundException(id));
   }"

run_graphql_test "Obtener meta por ID" \
    '{ savingsGoal(id: \"1\") { id name description targetAmount currentAmount progressPercentage category status } }' \
    '"savingsGoal".*"id".*"name"'

################################################################################
# SECCIÓN 4.2 - ENTIDADES JPA Y MAPEO
################################################################################

print_section "SECCIÓN 4.2 — ENTIDADES JPA Y MAPEO A GRAPHQL"

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  🎯 OBJETIVO DE ESTA SECCIÓN                                               │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Entender cómo las entidades JPA se mapean a tipos GraphQL.                │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  MAPEO ENTIDAD JPA → TIPO GRAPHQL:                                         │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  @Entity                              type SavingsGoal {                   │${NC}"
log "${WHITE}│  @Table(name = \"savings_goals\")       id: ID!                             │${NC}"
log "${WHITE}│  public class SavingsGoalEntity {     name: String!                        │${NC}"
log "${WHITE}│      @Id @GeneratedValue              targetAmount: Money!                 │${NC}"
log "${WHITE}│      private Long id;                 currentAmount: Money!                │${NC}"
log "${WHITE}│                                       progressPercentage: Float!  ← CALC   │${NC}"
log "${WHITE}│      private String name;             category: GoalCategory!              │${NC}"
log "${WHITE}│      private BigDecimal targetAmount; status: GoalStatus!                  │${NC}"
log "${WHITE}│      private BigDecimal currentAmount;}                                    │${NC}"
log "${WHITE}│      private GoalCategory category;                                        │${NC}"
log "${WHITE}│      private GoalStatus status;                                            │${NC}"
log "${WHITE}│  }                                                                         │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ⚠️  progressPercentage es CALCULADO, no está en la DB                     │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

# Test 4: Diferentes usuarios
print_subsection "Test 4: Obtener metas de usuario 2"

print_concept "   Los datos están segmentados por userId.
   Cada usuario solo ve sus propias metas.
   
   DATOS EN data.sql:
   INSERT INTO savings_goals (user_id, name, ...)
   VALUES
   (1, 'Emergency Fund', ...),
   (2, 'Home Down Payment', ...),  ← Este usuario
   (3, 'Retirement Planning', ...);"

run_graphql_test "Obtener metas de usuario 2" \
    '{ savingsGoals(userId: \"2\") { id name targetAmount currentAmount progressPercentage category status } }' \
    '"savingsGoals".*"name"'

# Test 5: Mutation CREATE
print_subsection "Test 5: Mutation - Crear nueva meta de ahorro"

print_concept "   Las mutations con JPA usan @Transactional para garantizar atomicidad.
   
   JAVA SERVICE:
   @Transactional
   public SavingsGoalEntity createGoal(CreateSavingsGoalInput input) {
       SavingsGoalEntity entity = new SavingsGoalEntity();
       entity.setUserId(input.getUserId());
       entity.setName(input.getName());
       entity.setTargetAmount(input.getTargetAmount());
       entity.setCurrentAmount(BigDecimal.ZERO);  // Empieza en 0
       entity.setStatus(GoalStatus.ACTIVE);       // Default ACTIVE
       
       return repository.save(entity);  // INSERT INTO savings_goals...
   }"

run_graphql_test "Crear nueva meta (Tesla Model 3)" \
    'mutation { createSavingsGoal(input: { userId: \"1\" name: \"Tesla Model 3\" description: \"Electric car savings\" targetAmount: 50000 category: OTHER }) { success message goal { id name targetAmount currentAmount progressPercentage category status } } }' \
    '"success".*true'

# Test 6: Verificar que se creó
print_subsection "Test 6: Verificar que la meta se creó en la DB"

print_concept "   Después de un INSERT, los datos persisten en PostgreSQL.
   Esta query verifica que la nueva meta existe.
   
   SQL EJECUTADO:
   SELECT * FROM savings_goals WHERE user_id = '1'"

run_graphql_test "Verificar meta creada existe" \
    '{ savingsGoals(userId: \"1\") { id name category status } }' \
    '"savingsGoals".*"name"'

################################################################################
# SECCIÓN 4.3 - CAMPOS CALCULADOS Y TRANSACCIONES
################################################################################

print_section "SECCIÓN 4.3 — CAMPOS CALCULADOS Y @TRANSACTIONAL"

log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  🎯 OBJETIVO DE ESTA SECCIÓN                                               │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  Entender campos calculados y el uso de @Transactional.                    │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  CAMPO CALCULADO (progressPercentage):                                     │${NC}"
log "${WHITE}│  • NO existe en la tabla de PostgreSQL                                     │${NC}"
log "${WHITE}│  • Se calcula en tiempo de ejecución                                       │${NC}"
log "${WHITE}│  • Fórmula: (currentAmount / targetAmount) * 100                           │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  JAVA (en el Resolver o Entity):                                           │${NC}"
log "${WHITE}│  public Double getProgressPercentage() {                                   │${NC}"
log "${WHITE}│      if (targetAmount.compareTo(BigDecimal.ZERO) == 0) return 0.0;        │${NC}"
log "${WHITE}│      return currentAmount                                                  │${NC}"
log "${WHITE}│          .divide(targetAmount, 4, RoundingMode.HALF_UP)                   │${NC}"
log "${WHITE}│          .multiply(BigDecimal.valueOf(100))                               │${NC}"
log "${WHITE}│          .doubleValue();                                                   │${NC}"
log "${WHITE}│  }                                                                         │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  @TRANSACTIONAL:                                                           │${NC}"
log "${WHITE}│  • Garantiza que todas las operaciones se completen o ninguna             │${NC}"
log "${WHITE}│  • Si hay error, hace ROLLBACK automático                                 │${NC}"
log "${WHITE}│  • Esencial para mutations que modifican datos                            │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
pause

# Test 7: Usuario con diferentes estados
print_subsection "Test 7: Usuario 3 con metas en diferentes estados"

print_concept "   Los Enums de Java se mapean a Enums de GraphQL.
   
   JAVA ENUM:                    GRAPHQL ENUM:
   public enum GoalStatus {      enum GoalStatus {
       ACTIVE,                       ACTIVE
       PAUSED,                       PAUSED
       COMPLETED,                    COMPLETED
       CANCELLED                     CANCELLED
   }                             }
   
   Usuario 3 tiene: ACTIVE, ACTIVE, PAUSED"

run_graphql_test "Obtener metas de usuario 3 (incluye PAUSED)" \
    '{ savingsGoals(userId: \"3\") { id name targetAmount currentAmount progressPercentage category status } }' \
    '"savingsGoals".*"status"'

# Test 8: Validar cálculo de progreso 100%
print_subsection "Test 8: Validar cálculo de progressPercentage = 100%"

print_concept "   La meta ID=3 tiene currentAmount = targetAmount = 3500
   Por lo tanto: progressPercentage = (3500/3500) * 100 = 100%
   
   DATOS EN data.sql:
   (1, 'New MacBook Pro', 'Upgrade...', 3500.00, 3500.00, 'OTHER', 'COMPLETED')
                                        ↑          ↑
                                      target    current (iguales = 100%)"

run_graphql_test_numeric "Progreso debe ser 100%" \
    '{ savingsGoal(id: \"3\") { name targetAmount currentAmount progressPercentage } }' \
    '.data.savingsGoal.progressPercentage' \
    '100'

# Test 9: Verificar filtro de activas (no PAUSED)
print_subsection "Test 9: Verificar que activeSavingsGoals excluye PAUSED"

print_concept "   La query activeSavingsGoals SOLO retorna status = ACTIVE.
   Las metas PAUSED, COMPLETED o CANCELLED no aparecen.
   
   JAVA SERVICE:
   public List<SavingsGoalEntity> getActiveGoals(String userId) {
       return repository.findByUserIdAndStatus(userId, GoalStatus.ACTIVE);
   }"

run_graphql_test "Solo metas ACTIVE (sin PAUSED)" \
    '{ activeSavingsGoals(userId: \"3\") { name status } }' \
    '"activeSavingsGoals"'

# Test 10: Crear meta con campos mínimos
print_subsection "Test 10: Crear meta con campos mínimos (sin description)"

print_concept "   GraphQL permite campos opcionales (nullables).
   En el Input, 'description' no es requerido.
   
   SCHEMA:
   input CreateSavingsGoalInput {
       userId: ID!
       name: String!
       description: String      ← Opcional (sin !)
       targetAmount: Money!
       category: GoalCategory!
   }"

run_graphql_test "Crear meta con campos mínimos" \
    'mutation { createSavingsGoal(input: { userId: \"2\" name: \"Vacation Fund\" targetAmount: 10000 category: VACATION }) { success message goal { id name description currentAmount status } } }' \
    '"success".*true.*"goal"'

################################################################################
# RESUMEN FINAL
################################################################################

print_section "📊 RESUMEN DE TESTS - CHAPTER 04"

PASS_RATE=0
if [ $TOTAL_TESTS -gt 0 ]; then
    PASS_RATE=$((PASSED_TESTS * 100 / TOTAL_TESTS))
fi

log "${CYAN}Tests Totales:    ${TOTAL_TESTS}${NC}"
log "${GREEN}Tests Exitosos:   ${PASSED_TESTS}${NC}"
log "${RED}Tests Fallidos:   ${FAILED_TESTS}${NC}"
log "${YELLOW}Tasa de Éxito:    ${PASS_RATE}%${NC}"
log ""

if [ $FAILED_TESTS -eq 0 ]; then
    log "${GREEN}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
    log "${GREEN}║                                                                              ║${NC}"
    log "${GREEN}║                   🎉 ¡TODOS LOS TESTS PASARON! 🎉                           ║${NC}"
    log "${GREEN}║                                                                              ║${NC}"
    log "${GREEN}║            El CHAPTER 04 está funcionando perfectamente.                     ║${NC}"
    log "${GREEN}║                                                                              ║${NC}"
    log "${GREEN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
else
    log "${YELLOW}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
    log "${YELLOW}║                                                                              ║${NC}"
    log "${YELLOW}║                   ⚠️  ALGUNOS TESTS FALLARON ⚠️                            ║${NC}"
    log "${YELLOW}║                                                                              ║${NC}"
    log "${YELLOW}║           Revisa el log para más detalles                                    ║${NC}"
    log "${YELLOW}║                                                                              ║${NC}"
    log "${YELLOW}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
fi

log ""
log "${WHITE}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
log "${WHITE}│  📚 RESUMEN DE CONCEPTOS APRENDIDOS                                        │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ✅ Sección 4.1: Integración JPA con GraphQL                               │${NC}"
log "${WHITE}│     • Spring Data JPA para acceso a datos                                  │${NC}"
log "${WHITE}│     • Repository pattern con JpaRepository                                 │${NC}"
log "${WHITE}│     • Queries automáticas: findByUserId, findByUserIdAndStatus            │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ✅ Sección 4.2: Entidades JPA y Mapeo                                     │${NC}"
log "${WHITE}│     • @Entity, @Table, @Id, @GeneratedValue                               │${NC}"
log "${WHITE}│     • Mapeo Entity → GraphQL Type                                          │${NC}"
log "${WHITE}│     • BigDecimal para dinero (NO Double)                                  │${NC}"
log "${WHITE}│                                                                             │${NC}"
log "${WHITE}│  ✅ Sección 4.3: Campos Calculados y @Transactional                        │${NC}"
log "${WHITE}│     • progressPercentage: calculado, no en DB                              │${NC}"
log "${WHITE}│     • @Transactional para atomicidad en mutations                         │${NC}"
log "${WHITE}│     • Enums compartidos entre Java y GraphQL                              │${NC}"
log "${WHITE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
log ""
log "${CYAN}📄 Log completo guardado en: ${OUTPUT_FILE}${NC}"
log ""
log "${WHITE}STACK TÉCNICO:${NC}"
log "${YELLOW}  • PostgreSQL 15 (Docker)${NC}"
log "${YELLOW}  • Spring Data JPA + Hibernate${NC}"
log "${YELLOW}  • Netflix DGS Framework${NC}"
log "${YELLOW}  • Custom Scalar: Money (BigDecimal)${NC}"
log ""

exit $FAILED_TESTS