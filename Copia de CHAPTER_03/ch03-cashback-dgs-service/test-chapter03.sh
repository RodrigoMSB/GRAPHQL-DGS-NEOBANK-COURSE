#!/usr/bin/env bash

################################################################################
# CHAPTER 03: IMPLEMENTACIÓN DE GRAPHQL CON DGS (NETFLIX JAVA)
# Script de Testing Automatizado - VERSIÓN PORTABLE
#
# Compatible con:
#   - macOS (Bash 3.2+)
#   - Linux (Bash 4.0+)
#   - Windows GitBash (Bash 4.4+)
#
# Uso: 
#   ./test-chapter03.sh           (modo interactivo)
#   ./test-chapter03.sh -s        (modo silencioso)
################################################################################

# Forzar locale consistente para comandos de fecha
export LC_ALL=C

# Colores (usando printf en lugar de echo -e para compatibilidad Mac)
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
MAGENTA='\033[1;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# Config
BASE_URL="http://localhost:8080"
GRAPHQL_ENDPOINT="${BASE_URL}/graphql"
OUTPUT_FILE="test-results-chapter03-$(date +%Y%m%d-%H%M%S).txt"

INTERACTIVE=true
if [ "$1" = "-s" ]; then
    INTERACTIVE=false
fi

TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Función para imprimir con colores (compatible con Mac y Linux)
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

run_test() {
    local test_name="$1"
    local curl_command="$2"
    local validation="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    log "${YELLOW}🧪 Test #${TOTAL_TESTS}: ${test_name}${NC}"
    log "   Ejecutando..."
    
    response=$(eval "$curl_command" 2>&1)
    exit_code=$?
    
    # Formatear JSON si jq está disponible
    if command -v jq >/dev/null 2>&1; then
        formatted=$(echo "$response" | jq -C '.' 2>/dev/null || echo "$response")
        if [ ${#formatted} -gt 400 ]; then
            log "   📄 Respuesta:\n${formatted:0:400}..."
        else
            log "   📄 Respuesta:\n$formatted"
        fi
    else
        if [ ${#response} -gt 200 ]; then
            log "   📄 Respuesta: ${response:0:200}..."
        else
            log "   📄 Respuesta: $response"
        fi
    fi
    
    if [ $exit_code -eq 0 ] && echo "$response" | grep -qE "$validation"; then
        log "   ${GREEN}✅ PASSED${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        log "   ${RED}❌ FAILED${NC}"
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
        log "${YELLOW}Por favor inicia el servidor con: mvn spring-boot:run${NC}"
        log ""
        exit 1
    fi
}

################################################################################
# HEADER
################################################################################

clear
log "${CYAN}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
log "${CYAN}║                                                                              ║${NC}"
log "${CYAN}║         📘 CHAPTER 03: IMPLEMENTACIÓN DE GRAPHQL CON DGS                    ║${NC}"
log "${CYAN}║                     Testing Automatizado Completo                            ║${NC}"
log "${CYAN}║                                                                              ║${NC}"
log "${CYAN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
log ""
log "${YELLOW}Feature: Cashback Rewards System${NC}"
log "${YELLOW}Framework: Netflix DGS (Domain Graph Service)${NC}"
log "${YELLOW}Duración: 2.5 horas (5 secciones × 30 min)${NC}"
log "${YELLOW}Log: ${OUTPUT_FILE}${NC}"
log ""

check_server

pause

################################################################################
# SECCIÓN 3.1 - FRAMEWORK DGS Y ESTRUCTURA DE PROYECTO (30 min)
################################################################################

print_section "SECCIÓN 3.1 — INTRODUCCIÓN AL FRAMEWORK DGS Y ESTRUCTURA DE PROYECTO"

log "${MAGENTA}Esta sección valida que DGS esté configurado correctamente,${NC}"
log "${MAGENTA}que el schema se cargue automáticamente, y que los endpoints estén expuestos.${NC}"
log ""
pause

# Test 1: Endpoint GraphQL disponible
print_subsection "Test 1: Endpoint /graphql disponible"

run_test "Endpoint GraphQL responde" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ __typename }\"}'" \
    '"__typename".*"Query"'

# Test 2: GraphiQL UI disponible
print_subsection "Test 2: GraphiQL UI disponible en /graphiql"

run_test "GraphiQL UI accesible" \
    "curl -s ${BASE_URL}/graphiql" \
    'graphiql'

# Test 3: Introspection habilitada
print_subsection "Test 3: Introspection habilitada"

run_test "Introspection query funciona" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ __schema { types { name } } }\"}'" \
    '"__schema".*"types"'

# Test 4: Schema cargado automáticamente
print_subsection "Test 4: Schema GraphQL cargado desde resources/schema/"

run_test "Tipos del schema disponibles" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ __type(name: \\\"User\\\") { name fields { name } } }\"}'" \
    '"name".*"User".*"fields"'

# Test 5: Enums registrados
print_subsection "Test 5: Enums del schema (RewardTier, RewardStatus, etc.)"

run_test "Enum RewardTier disponible" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ __type(name: \\\"RewardTier\\\") { enumValues { name } } }\"}'" \
    '"enumValues".*"BRONZE".*"SILVER"'

# Test 6: Estructura del proyecto DGS
print_subsection "Test 6: Verificar que DGS esté funcionando correctamente"

run_test "DGS procesa queries correctamente" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ user(id: \\\"user-001\\\") { id } }\"}'" \
    '"data".*"user"'

################################################################################
# SECCIÓN 3.2 - DEFINICIÓN DEL SCHEMA Y GENERACIÓN AUTOMÁTICA DE CLASES (30 min)
################################################################################

print_section "SECCIÓN 3.2 — DEFINICIÓN DEL SCHEMA Y GENERACIÓN AUTOMÁTICA DE CLASES"

log "${MAGENTA}Esta sección valida que el schema GraphQL esté correctamente definido,${NC}"
log "${MAGENTA}que los tipos de entidad, queries y mutations existan, y que el plugin${NC}"
log "${MAGENTA}DGS Codegen funcione para generar clases Java automáticamente.${NC}"
log ""
pause

# Test 7: Schema define tipo User completo
print_subsection "Test 7: Tipo User definido en schema con todos sus campos"

run_test "Schema tiene tipo User con estructura completa" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ __type(name: \\\"User\\\") { name fields { name type { name } } } }\"}'" \
    '"name".*"User".*"id".*"email".*"fullName".*"tier"'

# Test 8: Schema define tipo Reward completo
print_subsection "Test 8: Tipo Reward definido en schema con todos sus campos"

run_test "Schema tiene tipo Reward con estructura completa" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ __type(name: \\\"Reward\\\") { name fields { name type { name } } } }\"}'" \
    '"name".*"Reward".*"amount".*"status".*"category"'

# Test 9: Queries básicas definidas
print_subsection "Test 9: Queries básicas definidas en el schema"

run_test "Query type tiene operaciones básicas" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ __type(name: \\\"Query\\\") { fields { name } } }\"}'" \
    '"fields".*"user".*"usersByTier"'

# Test 10: Mutations básicas definidas
print_subsection "Test 10: Mutations básicas definidas en el schema"

run_test "Mutation type tiene operaciones básicas" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ __type(name: \\\"Mutation\\\") { fields { name } } }\"}'" \
    '"fields".*"createReward".*"redeemCashback"'

# Test 11: Custom Scalars definidos (DateTime)
print_subsection "Test 11: Custom Scalar DateTime definido en schema"

run_test "DateTime scalar registrado" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ __type(name: \\\"DateTime\\\") { name kind } }\"}'" \
    '"name".*"DateTime"'

# Test 12: Input types generados funcionan
print_subsection "Test 12: Input types (CreateRewardInput) generados por codegen"

run_test "Input type CreateRewardInput funciona correctamente" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"mutation { createReward(input: { userId: \\\"user-001\\\", transactionId: \\\"txn-test-001\\\", transactionAmount: 500.0, category: TRAVEL, description: \\\"Test\\\" }) { id amount } }\"}'" \
    '"id".*"amount"'

################################################################################
# SECCIÓN 3.3 - IMPLEMENTACIÓN DE RESOLVERS CON @DgsData (30 min)
################################################################################

print_section "SECCIÓN 3.3 — IMPLEMENTACIÓN DE RESOLVERS CON @DgsData"

log "${MAGENTA}Esta sección valida la implementación de resolvers usando @DgsQuery,${NC}"
log "${MAGENTA}@DgsMutation, @DgsData para campos anidados, y @InputArgument.${NC}"
log ""
pause

# Test 13: @DgsQuery simple
print_subsection "Test 13: Resolver con @DgsQuery (query simple)"

run_test "Query user con @DgsQuery" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ user(id: \\\"user-002\\\") { fullName tier } }\"}'" \
    '"fullName".*"tier"'

# Test 14: @DgsQuery con argumentos/filtros
print_subsection "Test 14: Resolver con @DgsQuery y @InputArgument"

run_test "Query usersByTier con argumentos" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ usersByTier(tier: GOLD) { fullName tier } }\"}'" \
    '"tier".*"GOLD"'

# Test 15: @DgsQuery con Input type complejo
print_subsection "Test 15: Query con Input type complejo (RewardsFilterInput)"

run_test "Filtro complejo con RewardsFilterInput" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ rewards(filter: { status: ACTIVE, category: TRAVEL }) { amount category status } }\"}'" \
    '"status".*"ACTIVE".*"category".*"TRAVEL"'

# Test 16: @DgsData para campo anidado (Reward -> User)
print_subsection "Test 16: Resolver con @DgsData para navegación anidada"

run_test "Navegación Reward -> User con @DgsData" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ reward(id: \\\"reward-100\\\") { amount user { fullName tier } } }\"}'" \
    '"user".*"fullName".*"tier"'

# Test 17: @DgsData para campo anidado (User -> Rewards)
print_subsection "Test 17: Navegación inversa User -> Rewards con @DgsData"

run_test "Navegación User -> Rewards con @DgsData" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ user(id: \\\"user-003\\\") { fullName rewards { amount category } } }\"}'" \
    '"rewards".*"amount".*"category"'

# Test 18: Query agregada con cálculos
print_subsection "Test 18: Query con cálculos agregados (rewardsSummary)"

run_test "Summary con totales calculados" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ rewardsSummary(userId: \\\"user-001\\\") { totalEarned availableBalance rewardsByCategory { category totalAmount } } }\"}'" \
    '"totalEarned".*"availableBalance".*"rewardsByCategory"'

################################################################################
# SECCIÓN 3.4 - MUTATIONS Y LÓGICA DE NEGOCIO (30 min)
################################################################################

print_section "SECCIÓN 3.4 — MUTATIONS Y LÓGICA DE NEGOCIO INTEGRADA"

log "${MAGENTA}Esta sección valida mutations con @DgsMutation, validaciones de entrada,${NC}"
log "${MAGENTA}lógica de negocio encapsulada en servicios, y manejo de errores.${NC}"
log ""
pause

# Test 19: Mutation createReward
print_subsection "Test 19: Mutation createReward con lógica de negocio"

run_test "Crear reward calculando cashback" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"mutation { createReward(input: { userId: \\\"user-001\\\", transactionId: \\\"txn-test-001\\\", transactionAmount: 1000.0, category: TRAVEL, description: \\\"Vuelo a Miami\\\" }) { id amount category multiplier } }\"}'" \
    '"amount".*"category".*"TRAVEL"'

# Test 20: Mutation redeemCashback
print_subsection "Test 20: Mutation redeemCashback con validaciones"

run_test "Redimir cashback" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"mutation { redeemCashback(input: { userId: \\\"user-002\\\", amount: 50.0, destinationAccount: \\\"ACC-123456\\\" }) { success message redeemedAmount } }\"}'" \
    '"success".*true.*"message"'

# Test 21: Mutation con validación de balance insuficiente
print_subsection "Test 21: Validación de balance insuficiente"

run_test "Redención rechazada por balance insuficiente" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"mutation { redeemCashback(input: { userId: \\\"user-005\\\", amount: 99999.0, destinationAccount: \\\"ACC-999\\\" }) { success message } }\"}'" \
    '"success"[[:space:]]*:[[:space:]]*false.*[Ii]nsufficient'

# Test 22: Mutation updateRewardStatus
print_subsection "Test 22: Mutation updateRewardStatus (admin)"

run_test "Actualizar estado de reward" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"mutation { updateRewardStatus(input: { rewardId: \\\"reward-101\\\", newStatus: CANCELLED, reason: \\\"Test de cancelación\\\" }) { id status } }\"}'" \
    '"status".*"CANCELLED"'

# Test 23: Mutation upgradeUserTier
print_subsection "Test 23: Mutation upgradeUserTier"

run_test "Upgrade de tier de usuario" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"mutation { upgradeUserTier(userId: \\\"user-005\\\", newTier: SILVER) { fullName tier } }\"}'" \
    '"tier".*"SILVER"'

# Test 24: Mutation batch (expireOldRewards)
print_subsection "Test 24: Mutation batch expireOldRewards"

run_test "Expirar rewards vencidas (batch)" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"mutation { expireOldRewards }\"}'" \
    '"data"'

################################################################################
# SECCIÓN 3.5 - DATALOADER Y PROBLEMA N+1 (30 min)
################################################################################

print_section "SECCIÓN 3.5 — OPTIMIZACIÓN CON DATALOADER Y PREVENCIÓN DEL PROBLEMA N+1"

log "${MAGENTA}Esta sección valida el uso de DataLoader para batch loading,${NC}"
log "${MAGENTA}demostrando cómo se resuelve el problema N+1 en GraphQL.${NC}"
log ""
pause

# Test 25: Query que activaría N+1 sin DataLoader
print_subsection "Test 25: Query múltiple User -> Rewards (batch loading)"

run_test "Multiples usuarios con sus rewards (DataLoader activo)" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ usersByTier(tier: BRONZE) { fullName rewards { amount category } } }\"}'" \
    '"fullName".*"rewards".*"amount"'

# Test 26: Query inversa que activaría N+1
print_subsection "Test 26: Query múltiple Reward -> User (batch loading)"

run_test "Múltiples rewards con su user (DataLoader activo)" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ rewards(filter: { status: ACTIVE }) { amount user { fullName tier } } }\"}'" \
    '"amount".*"user".*"fullName"'

# Test 27: Query profunda anidada
print_subsection "Test 27: Query profundamente anidada User -> Rewards -> User"

run_test "Navegación User -> Rewards -> User (3 niveles)" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ user(id: \\\"user-001\\\") { fullName rewards { amount user { tier } } } }\"}'" \
    '"fullName".*"rewards".*"tier"'

# Test 28: Query con filtros + DataLoader
print_subsection "Test 28: Query filtrada con DataLoader"

run_test "userRewards con filtro de status (DataLoader)" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ userRewards(userId: \\\"user-003\\\", status: ACTIVE) { amount user { fullName } } }\"}'" \
    '"amount".*"user"'

# Test 29: Múltiples usuarios PLATINUM con rewards
print_subsection "Test 29: Tier PLATINUM con todas sus rewards (batch eficiente)"

run_test "PLATINUM users con rewards (DataLoader batch)" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ usersByTier(tier: PLATINUM) { fullName availableCashback rewards { amount category status } } }\"}'" \
    '"fullName".*"availableCashback".*"rewards"'

# Test 30: Query con CashbackRules y cálculos
print_subsection "Test 30: Query de reglas + cálculo de cashback"

run_test "CashbackRules y calculateCashback" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ cashbackRule(category: TRAVEL) { basePercentage tierMultipliers { platinum } } calculateCashback(userId: \\\"user-004\\\", transactionAmount: 5000.0, category: TRAVEL) }\"}'" \
    '"basePercentage".*"calculateCashback"'

################################################################################
# RESUMEN FINAL
################################################################################

print_section "📊 RESUMEN DE TESTS - CHAPTER 03"

log "${CYAN}Total de tests ejecutados: ${TOTAL_TESTS}${NC}"
log "${GREEN}✅ Tests exitosos: ${PASSED_TESTS}${NC}"
log "${RED}❌ Tests fallidos: ${FAILED_TESTS}${NC}"
log ""

if [ $FAILED_TESTS -eq 0 ]; then
    log "${GREEN}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
    log "${GREEN}║                                                                              ║${NC}"
    log "${GREEN}║                   🎉 ¡TODOS LOS TESTS PASARON! 🎉                           ║${NC}"
    log "${GREEN}║                                                                              ║${NC}"
    log "${GREEN}║            El CHAPTER 03 está funcionando perfectamente.                     ║${NC}"
    log "${GREEN}║                                                                              ║${NC}"
    log "${GREEN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
else
    log "${RED}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
    log "${RED}║                                                                              ║${NC}"
    log "${RED}║                   ⚠️  ALGUNOS TESTS FALLARON ⚠️                            ║${NC}"
    log "${RED}║                                                                              ║${NC}"
    log "${RED}║           Revisa el log para más detalles: ${OUTPUT_FILE}                    ║${NC}"
    log "${RED}║                                                                              ║${NC}"
    log "${RED}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
fi

log ""
log "${YELLOW}Log completo guardado en: ${OUTPUT_FILE}${NC}"
log ""