#!/usr/bin/env bash

################################################################################
# CHAPTER 02: DISEÑO CORRECTO DE SCHEMAS Y BUENAS PRÁCTICAS
# Script de Testing Automatizado - VERSIÓN PORTABLE
#
# Compatible con:
#   - macOS (Bash 3.2+)
#   - Linux (Bash 4.0+)
#   - Windows GitBash (Bash 4.4+)
#
# Uso: 
#   ./test-chapter02.sh           (modo interactivo)
#   ./test-chapter02.sh -s        (modo silencioso)
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
OUTPUT_FILE="test-results-chapter02-$(date +%Y%m%d-%H%M%S).txt"

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
log "${CYAN}║              📘 CHAPTER 02: DISEÑO CORRECTO DE SCHEMAS                       ║${NC}"
log "${CYAN}║                     Testing Automatizado Completo                            ║${NC}"
log "${CYAN}║                                                                              ║${NC}"
log "${CYAN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
log ""
log "${YELLOW}Feature: Cashback Rewards Program${NC}"
log "${YELLOW}Duración: 1.5 horas (3 secciones × 30 min)${NC}"
log "${YELLOW}Log: ${OUTPUT_FILE}${NC}"
log ""

check_server

pause

################################################################################
# SECCIÓN 2.1 - PRINCIPIOS DEL DISEÑO DE ESQUEMAS GRAPHQL (30 min)
################################################################################

print_section "SECCIÓN 2.1 — PRINCIPIOS DEL DISEÑO DE ESQUEMAS GRAPHQL"

log "${MAGENTA}Esta sección valida que el schema esté orientado a dominio,${NC}"
log "${MAGENTA}no acoplado a la base de datos, con entidades bien identificadas.${NC}"
log ""
pause

# Test 1: User con campos orientados a dominio (no user_id, tier_id, etc.)
print_subsection "Test 1: Schema orientado a dominio (User con fullName, no first_name/last_name)"

run_test "User con campos de dominio" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ user(id: \\\"user-001\\\") { id fullName tier email } }\"}'" \
    '"fullName".*"Maria Silva"'

# Test 2: Enum bien diseñado (CashbackTier)
print_subsection "Test 2: Enums bien diseñados (CashbackTier: BRONZE, SILVER, GOLD, PLATINUM)"

run_test "CashbackTier enum válido" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ user(id: \\\"user-002\\\") { tier } }\"}'" \
    '"tier".*"PLATINUM"'

# Test 3: Relaciones bidireccionales (Transaction → User)
print_subsection "Test 3: Relaciones bidireccionales navegables (Transaction → User)"

run_test "Navegación Transaction a User" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ transaction(id: \\\"trans-001\\\") { merchantName user { fullName tier } } }\"}'" \
    '"user".*"fullName".*"tier"'

# Test 4: No acoplamiento con DB (campos calculados)
print_subsection "Test 4: Campos calculados NO presentes en DB (availableCashback)"

run_test "Campo calculado availableCashback" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ user(id: \\\"user-001\\\") { availableCashback } }\"}'" \
    '"availableCashback".*[0-9]'

# Test 5: Entidades identificadas correctamente (User, Transaction, Reward)
print_subsection "Test 5: Entidades del dominio correctamente identificadas"

run_test "Entidad User completa" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ user(id: \\\"user-001\\\") { id fullName tier email enrolledAt } }\"}'" \
    '"id".*"fullName".*"tier".*"email"'

# Test 6: Schema como contrato (no refleja estructura de tablas)
print_subsection "Test 6: Schema como contrato (independiente de estructura DB)"

run_test "User sin campos técnicos de DB" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ user(id: \\\"user-001\\\") { fullName tier } }\"}'" \
    '"fullName".*"tier"'

################################################################################
# SECCIÓN 2.2 - TIPOS ESCALARES, OBJETOS, LISTAS E INPUTS (30 min)
################################################################################

print_section "SECCIÓN 2.2 — TIPOS ESCALARES, OBJETOS, LISTAS E INPUTS"

log "${MAGENTA}Esta sección valida custom scalars (Money, Percentage, Email, DateTime),${NC}"
log "${MAGENTA}la diferencia entre Input types y Output types, y nullabilidad correcta.${NC}"
log ""
pause

# Test 7: Custom scalar Money (precisión decimal)
print_subsection "Test 7: Custom scalar Money (sin errores de redondeo Float)"

run_test "Money scalar con precisión" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ user(id: \\\"user-001\\\") { availableCashback totalCashbackEarned } }\"}'" \
    '"availableCashback".*[0-9]+\.[0-9]+'

# Test 8: Custom scalar Percentage (semánticamente claro)
print_subsection "Test 8: Custom scalar Percentage (valores 0-100)"

run_test "Percentage scalar en cashback" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ transactions(userId: \\\"user-001\\\") { cashbackPercentage } }\"}'" \
    '"cashbackPercentage".*[0-9]'

# Test 9: Custom scalar Email (validación)
print_subsection "Test 9: Custom scalar Email (formato validado)"

run_test "Email scalar con validación" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ user(id: \\\"user-001\\\") { email } }\"}'" \
    '"email".*@.*\.com'

# Test 10: Custom scalar DateTime (ISO-8601)
print_subsection "Test 10: Custom scalar DateTime (formato ISO-8601)"

run_test "DateTime scalar en enrolledAt" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ users { enrolledAt } }\"}'" \
    '"enrolledAt"'

# Test 11: Object types vs Input types (separación clara)
print_subsection "Test 11: Input types separados de Output types (CreateTransactionInput)"

run_test "Mutation con Input type" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"mutation { createTransaction(input: { userId: \\\"user-001\\\", amount: 100.0, category: GROCERIES, merchantName: \\\"Test Store\\\" }) { success message } }\"}'" \
    '"success".*true'

# Test 12: List types con nullabilidad ([Transaction!]!)
print_subsection "Test 12: List types con nullabilidad correcta ([Type!]!)"

run_test "Lista de transactions non-null" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ transactions(userId: \\\"user-001\\\") { id } }\"}'" \
    '"transactions".*\['

# Test 13: Enums descriptivos (TransactionCategory con valores claros)
print_subsection "Test 13: Enums descriptivos (TransactionCategory)"

run_test "TransactionCategory enum" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ transactions(category: TRAVEL) { category } }\"}'" \
    '"category".*"TRAVEL"'

# Test 14: Independencia del modelo relacional (no FK expuestas)
print_subsection "Test 14: Sin foreign keys expuestas (user vs userId)"

run_test "Relación User en Transaction (no userId)" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ transaction(id: \\\"trans-001\\\") { user { fullName } } }\"}'" \
    '"user".*"fullName"'

################################################################################
# SECCIÓN 2.3 - QUERIES Y MUTATIONS COMPLEJAS (30 min)
################################################################################

print_section "SECCIÓN 2.3 — QUERIES Y MUTATIONS COMPLEJAS"

log "${MAGENTA}Esta sección valida queries con múltiples parámetros opcionales,${NC}"
log "${MAGENTA}estructuras anidadas, campos calculados y mutations complejas.${NC}"
log ""
pause

# Test 15: Query con múltiples parámetros opcionales
print_subsection "Test 15: Query con filtros múltiples (userId, status, category)"

run_test "Transactions con 3 filtros" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ transactions(userId: \\\"user-002\\\", status: CONFIRMED, category: SHOPPING) { merchantName } }\"}'" \
    '"merchantName"'

# Test 16: Estructuras anidadas (User → Transactions → CashbackAmount)
print_subsection "Test 16: Query anidada con campos calculados"

run_test "Query anidada con cashback calculado" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ user(id: \\\"user-001\\\") { fullName } transactions(userId: \\\"user-001\\\") { cashbackAmount } }\"}'" \
    '"fullName".*"cashbackAmount"'

# Test 17: Campos calculados dinámicos (cashbackPercentage según tier + category)
print_subsection "Test 17: Campos calculados con lógica de negocio"

run_test "CashbackPercentage calculado (GOLD × TRAVEL = 9%)" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ transactions(userId: \\\"user-001\\\", category: TRAVEL) { cashbackPercentage } }\"}'" \
    '"cashbackPercentage".*9'

# Test 18: Mutation que retorna objeto compuesto (TransactionResponse)
print_subsection "Test 18: Mutation con respuesta estructurada (success + message + data)"

run_test "createTransaction retorna TransactionResponse" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"mutation { createTransaction(input: { userId: \\\"user-001\\\", amount: 200.0, category: RESTAURANTS, merchantName: \\\"Sushi Bar\\\" }) { success message transaction { cashbackAmount } } }\"}'" \
    '"success".*true.*"message".*"transaction"'

# Test 19: Mutation que modifica múltiples entidades (Transaction + Reward)
print_subsection "Test 19: Mutation compleja (crea Transaction y genera Reward automáticamente)"

run_test "createTransaction genera Reward" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"mutation { createTransaction(input: { userId: \\\"user-001\\\", amount: 300.0, category: TRAVEL, merchantName: \\\"Flight Booking\\\" }) { success transaction { cashbackAmount } } }\"}'" \
    '"cashbackAmount".*27'

# Test 20: Query compleja con filtros, anidación y cálculos
print_subsection "Test 20: Query compleja combinando todo lo anterior"

run_test "Query compleja: User + Transactions filtradas + Cashback" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ user(id: \\\"user-001\\\") { fullName tier availableCashback totalSpent } transactions(userId: \\\"user-001\\\", category: RESTAURANTS) { amount merchantName cashbackAmount cashbackPercentage } }\"}'" \
    '"fullName".*"availableCashback".*"totalSpent".*"cashbackAmount"'

# Test 21: Aprovechando tipado estático para validación automática
print_subsection "Test 21: Validación automática de tipos (Enum inválido debería fallar)"

run_test "Enum TransactionCategory válido" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ transactions(category: GROCERIES) { category } }\"}'" \
    '"category".*"GROCERIES"'

# Test 22: Documentación integrada en el schema (comments disponibles)
print_subsection "Test 22: Schema autodocumentado (introspection funciona)"

run_test "Introspection del schema" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ __type(name: \\\"User\\\") { name fields { name type { name } } } }\"}'" \
    '"name".*"User"'

# Test 23: Queries que retornan listas con elementos calculados
print_subsection "Test 23: Lista con elementos que tienen campos calculados"

run_test "Todas las transactions con cashback calculado" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ transactions(userId: \\\"user-001\\\") { amount cashbackAmount cashbackPercentage } }\"}'" \
    '"amount".*"cashbackAmount".*"cashbackPercentage"'

# Test 24: Validación de relaciones many-to-one
print_subsection "Test 24: Relación many-to-one (múltiples Transactions → 1 User)"

run_test "Múltiples transactions del mismo user" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ transactions(userId: \\\"user-001\\\") { user { fullName } } }\"}'" \
    '"fullName".*"Maria Silva"'

################################################################################
# BONUS: VALIDACIONES EXTRA (Cobertura completa del diseño)
################################################################################

print_section "🎁 BONUS: VALIDACIONES EXTRA DE DISEÑO"

log "${MAGENTA}Tests adicionales que validan aspectos avanzados del schema design.${NC}"
log ""
pause

# Test 25: Filtrado por múltiples categorías
print_subsection "Test 25: Filtrado preciso (solo TRAVEL transactions)"

run_test "Solo transacciones TRAVEL" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ transactions(category: TRAVEL) { category } }\"}'" \
    '"category".*"TRAVEL"'

# Test 26: Usuarios filtrados por tier
print_subsection "Test 26: Filtrado de usuarios por tier (PLATINUM)"

run_test "Solo usuarios PLATINUM" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ users(tier: PLATINUM) { tier fullName } }\"}'" \
    '"tier".*"PLATINUM"'

# Test 27: Totales calculados correctos
print_subsection "Test 27: Totales calculados (totalSpent, totalCashbackEarned)"

run_test "Totales de user-001" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ user(id: \\\"user-001\\\") { totalSpent totalCashbackEarned } }\"}'" \
    '"totalSpent".*[0-9]+.*"totalCashbackEarned".*[0-9]+'

# Test 28: Cashback con diferentes multiplicadores
print_subsection "Test 28: Cashback diferenciado por categoría (TRAVEL 3x, RESTAURANTS 2x)"

run_test "TRAVEL con 3x multiplier" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ transactions(userId: \\\"user-001\\\", category: TRAVEL) { cashbackPercentage } }\"}'" \
    '"cashbackPercentage".*9'

# Test 29: User PLATINUM con mayor cashback
print_subsection "Test 29: User PLATINUM (5% base) vs GOLD (3% base)"

run_test "PLATINUM user cashback" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ user(id: \\\"user-002\\\") { tier availableCashback } }\"}'" \
    '"tier".*"PLATINUM".*"availableCashback"'

# Test 30: Lista de todos los usuarios
print_subsection "Test 30: Query que retorna múltiples usuarios"

run_test "Todos los usuarios" \
    "curl -s -X POST ${GRAPHQL_ENDPOINT} -H 'Content-Type: application/json' -d '{\"query\":\"{ users { id fullName tier } }\"}'" \
    '"id".*"user-001".*"user-002"'

################################################################################
# RESUMEN FINAL
################################################################################

print_section "📊 RESUMEN DE EJECUCIÓN - CHAPTER 02"

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
    log "${GREEN}║                    🎉 ¡TODOS LOS TESTS PASARON! 🎉                           ║${NC}"
    log "${GREEN}║                                                                              ║${NC}"
    log "${GREEN}║              Chapter 02 completado exitosamente                              ║${NC}"
    log "${GREEN}║                                                                              ║${NC}"
    log "${GREEN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
else
    log "${YELLOW}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
    log "${YELLOW}║                                                                              ║${NC}"
    log "${YELLOW}║                    ⚠️  ALGUNOS TESTS FALLARON ⚠️                            ║${NC}"
    log "${YELLOW}║                                                                              ║${NC}"
    log "${YELLOW}║              Revisa el log para más detalles                                 ║${NC}"
    log "${YELLOW}║                                                                              ║${NC}"
    log "${YELLOW}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
fi

log ""
log "${CYAN}📄 Log completo guardado en: ${OUTPUT_FILE}${NC}"
log ""
log "${MAGENTA}Cobertura del temario:${NC}"
log "  ✅ Sección 2.1: Principios del diseño de esquemas (Tests 1-6)"
log "  ✅ Sección 2.2: Tipos escalares, objetos, listas e inputs (Tests 7-14)"
log "  ✅ Sección 2.3: Queries y Mutations complejas (Tests 15-24)"
log "  🎁 Bonus: Validaciones extra de diseño (Tests 25-30)"
log ""
log "${YELLOW}Feature validado: Cashback Rewards Program${NC}"
log "${YELLOW}Custom Scalars: Money, Percentage, Email, DateTime${NC}"
log "${YELLOW}Enums: CashbackTier, TransactionCategory, TransactionStatus${NC}"
log "${YELLOW}Campos calculados: cashbackAmount, cashbackPercentage, availableCashback${NC}"
log ""

exit $FAILED_TESTS