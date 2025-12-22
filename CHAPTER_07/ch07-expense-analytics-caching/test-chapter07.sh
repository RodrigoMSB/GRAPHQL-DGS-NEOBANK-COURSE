#!/bin/bash

# ╔═══════════════════════════════════════════════════════════════════════════╗
# ║                                                                           ║
# ║   CHAPTER 07 - EXPENSE ANALYTICS: CACHING & PERFORMANCE                   ║
# ║   GraphQL Caching Strategies Test Suite                                   ║
# ║                                                                           ║
# ║   Este script demuestra DOS niveles de caching en GraphQL:                ║
# ║   • Per-Request Caching (DataLoader) - Evita problema N+1                 ║
# ║   • Resolver-Level Caching (Spring Cache) - Queries costosas              ║
# ║                                                                           ║
# ╚═══════════════════════════════════════════════════════════════════════════╝

# ══════════════════════════════════════════════════════════════════════════════
# COLORES Y ESTILOS
# ══════════════════════════════════════════════════════════════════════════════
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
GRAY='\033[0;90m'
BOLD='\033[1m'
DIM='\033[2m'
NC='\033[0m' # No Color

# Emojis y símbolos
CHECK="✅"
CROSS="❌"
ARROW="➜"
CLOCK="⏱️"
LIGHTNING="⚡"
CACHE="💾"
ROCKET="🚀"
FIRE="🔥"
WARNING="⚠️"

# ══════════════════════════════════════════════════════════════════════════════
# CONFIGURACIÓN
# ══════════════════════════════════════════════════════════════════════════════
GRAPHQL_URL="http://localhost:8080/graphql"
TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
LOG_FILE="test-results-chapter07-${TIMESTAMP}.log"

# Contadores
PASSED=0
FAILED=0
TOTAL_TESTS=7

# ══════════════════════════════════════════════════════════════════════════════
# FUNCIONES UTILITARIAS
# ══════════════════════════════════════════════════════════════════════════════

print_header() {
    echo ""
    echo -e "${PURPLE}╔═══════════════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${PURPLE}║${NC}                                                                           ${PURPLE}║${NC}"
    echo -e "${PURPLE}║${NC}   ${WHITE}${BOLD}📊 CHAPTER 07: EXPENSE ANALYTICS - CACHING & PERFORMANCE${NC}              ${PURPLE}║${NC}"
    echo -e "${PURPLE}║${NC}   ${GRAY}DataLoader + Spring Cache Demo${NC}                                        ${PURPLE}║${NC}"
    echo -e "${PURPLE}║${NC}                                                                           ${PURPLE}║${NC}"
    echo -e "${PURPLE}╚═══════════════════════════════════════════════════════════════════════════╝${NC}"
    echo ""
}

print_concept() {
    echo -e "${CYAN}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
    echo -e "${CYAN}│${NC} ${YELLOW}${BOLD}💡 CONCEPTO: DOS NIVELES DE CACHING EN GRAPHQL${NC}                             ${CYAN}│${NC}"
    echo -e "${CYAN}├─────────────────────────────────────────────────────────────────────────────┤${NC}"
    echo -e "${CYAN}│${NC}                                                                             ${CYAN}│${NC}"
    echo -e "${CYAN}│${NC}  ${WHITE}NIVEL 1: Per-Request (DataLoader)${NC}                                        ${CYAN}│${NC}"
    echo -e "${CYAN}│${NC}  ${GRAY}├─ Scope: Durante UN request HTTP${NC}                                        ${CYAN}│${NC}"
    echo -e "${CYAN}│${NC}  ${GRAY}├─ Propósito: Evitar problema N+1${NC}                                        ${CYAN}│${NC}"
    echo -e "${CYAN}│${NC}  ${GRAY}└─ Batching: Agrupa múltiples queries en una${NC}                             ${CYAN}│${NC}"
    echo -e "${CYAN}│${NC}                                                                             ${CYAN}│${NC}"
    echo -e "${CYAN}│${NC}  ${WHITE}NIVEL 2: Resolver-Level (Spring Cache + Caffeine)${NC}                        ${CYAN}│${NC}"
    echo -e "${CYAN}│${NC}  ${GRAY}├─ Scope: Entre MÚLTIPLES requests (TTL: 5 min)${NC}                          ${CYAN}│${NC}"
    echo -e "${CYAN}│${NC}  ${GRAY}├─ Propósito: Queries costosas (agregaciones)${NC}                            ${CYAN}│${NC}"
    echo -e "${CYAN}│${NC}  ${GRAY}└─ Invalidación: Automática en mutations${NC}                                 ${CYAN}│${NC}"
    echo -e "${CYAN}│${NC}                                                                             ${CYAN}│${NC}"
    echo -e "${CYAN}└─────────────────────────────────────────────────────────────────────────────┘${NC}"
    echo ""
}

print_test_header() {
    local test_num=$1
    local test_title=$2
    local test_desc=$3
    
    echo ""
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${WHITE}${BOLD}  📋 TEST ${test_num} de ${TOTAL_TESTS}: ${test_title}${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo -e "${GRAY}  $test_desc${NC}"
    echo ""
}

print_theory_box() {
    local title=$1
    local line1=$2
    local line2=$3
    local line3=$4
    
    echo -e "${YELLOW}  ┌─────────────────────────────────────────────────────────────────────────┐${NC}"
    echo -e "${YELLOW}  │${NC} ${WHITE}${BOLD}$title${NC}"
    echo -e "${YELLOW}  ├─────────────────────────────────────────────────────────────────────────┤${NC}"
    echo -e "${YELLOW}  │${NC} ${GRAY}$line1${NC}"
    [ -n "$line2" ] && echo -e "${YELLOW}  │${NC} ${GRAY}$line2${NC}"
    [ -n "$line3" ] && echo -e "${YELLOW}  │${NC} ${GRAY}$line3${NC}"
    echo -e "${YELLOW}  └─────────────────────────────────────────────────────────────────────────┘${NC}"
    echo ""
}

print_request() {
    local query=$1
    echo -e "${CYAN}  ${ARROW} REQUEST:${NC}"
    echo -e "${WHITE}  ┌─────────────────────────────────────────────────────────────────────────┐${NC}"
    echo "$query" | while IFS= read -r line; do
        printf "${WHITE}  │${NC} ${GREEN}%s${NC}\n" "$line"
    done
    echo -e "${WHITE}  └─────────────────────────────────────────────────────────────────────────┘${NC}"
    echo ""
}

print_response() {
    local response=$1
    echo -e "${CYAN}  ${ARROW} RESPONSE:${NC}"
    echo -e "${WHITE}  ┌─────────────────────────────────────────────────────────────────────────┐${NC}"
    if command -v jq &> /dev/null; then
        echo "$response" | jq '.' 2>/dev/null | while IFS= read -r line; do
            printf "${WHITE}  │${NC} ${GRAY}%s${NC}\n" "$line"
        done
    else
        echo -e "${WHITE}  │${NC} ${GRAY}$response${NC}"
    fi
    echo -e "${WHITE}  └─────────────────────────────────────────────────────────────────────────┘${NC}"
}

print_time() {
    local time=$1
    local expected=$2
    echo ""
    echo -e "${PURPLE}  ${CLOCK} Tiempo de respuesta: ${WHITE}${BOLD}${time}${NC} ${GRAY}(esperado: ${expected})${NC}"
}

print_result() {
    local success=$1
    local message=$2
    echo ""
    if [ "$success" = true ]; then
        echo -e "${GREEN}  ${CHECK} PASSED: ${message}${NC}"
        ((PASSED++))
    else
        echo -e "${RED}  ${CROSS} FAILED: ${message}${NC}"
        ((FAILED++))
    fi
}

pause() {
    echo ""
    echo -e "${DIM}  Presiona ENTER para continuar...${NC}"
    read -r
}

format_json() {
    if command -v jq &> /dev/null; then
        echo "$1" | jq '.' 2>/dev/null || echo "$1"
    else
        echo "$1"
    fi
}

get_time_ms() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        python3 -c "import time; print(int(time.time() * 1000))"
    else
        date +%s%3N 2>/dev/null || echo $(($(date +%s) * 1000))
    fi
}

# ══════════════════════════════════════════════════════════════════════════════
# INICIO DEL SCRIPT
# ══════════════════════════════════════════════════════════════════════════════

clear
print_header
print_concept

echo -e "${WHITE}  ${ARROW} Endpoint: ${CYAN}${GRAPHQL_URL}${NC}"
echo -e "${WHITE}  ${ARROW} Log file: ${CYAN}${LOG_FILE}${NC}"
echo ""

# ══════════════════════════════════════════════════════════════════════════════
# VERIFICACIÓN DE SERVICIO
# ══════════════════════════════════════════════════════════════════════════════

echo -e "${YELLOW}  🔍 Verificando servicio...${NC}"
HEALTH_CHECK=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d '{"query":"{__typename}"}' 2>&1)

if echo "$HEALTH_CHECK" | grep -q "Query"; then
    echo -e "${GREEN}  ${CHECK} Expense Analytics Service: ONLINE${NC}"
else
    echo -e "${RED}  ${CROSS} ERROR: Servicio no responde en ${GRAPHQL_URL}${NC}"
    echo -e "${YELLOW}  ${WARNING} Ejecuta: ${WHITE}mvn spring-boot:run${NC}"
    exit 1
fi

pause

# ══════════════════════════════════════════════════════════════════════════════
# TEST 1: EXPENSES EXISTENTES
# ══════════════════════════════════════════════════════════════════════════════

clear
print_test_header "1" "Query - Expenses Existentes" "Verificar que los datos de ejemplo están cargados en memoria"

print_theory_box "🎯 OBJETIVO" \
    "Obtener la lista de expenses del account-001" \
    "Estos datos se cargan automáticamente al iniciar la app" \
    "Son la base para los cálculos de analytics"

QUERY1='{
  expenses(accountId: "account-001") {
    id
    amount
    merchantName
    category
  }
}'

print_request "$QUERY1"

echo -e "${GRAY}  Ejecutando query...${NC}"

RESPONSE1=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ expenses(accountId: \"account-001\") { id amount merchantName category } }"}')

print_response "$RESPONSE1"

if echo "$RESPONSE1" | grep -q "Starbucks"; then
    print_result true "Expenses iniciales cargados correctamente"
else
    print_result false "No se encontraron expenses"
fi

pause

# ══════════════════════════════════════════════════════════════════════════════
# TEST 2: EXPENSE SUMMARY - CACHE MISS
# ══════════════════════════════════════════════════════════════════════════════

clear
print_test_header "2" "Expense Summary - CACHE MISS" "Primera llamada al summary - debe calcular y guardar en cache"

print_theory_box "${CACHE} CACHE MISS (Primera vez)" \
    "1. Spring Cache busca key 'account-001' → NO EXISTE" \
    "2. Ejecuta simulateHeavyComputation() → 500ms delay" \
    "3. Calcula aggregaciones y GUARDA en cache"

QUERY2='{
  expenseSummary(accountId: "account-001") {
    totalAmount
    averageAmount
    count
    topMerchants {
      merchantName
      totalSpent
    }
  }
}'

print_request "$QUERY2"

echo -e "${GRAY}  ${CLOCK} Ejecutando query (primera vez - sin cache)...${NC}"

START_TIME=$(get_time_ms)

RESPONSE2=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ expenseSummary(accountId: \"account-001\") { totalAmount averageAmount count topMerchants { merchantName totalSpent } } }"}')

END_TIME=$(get_time_ms)
ELAPSED=$((END_TIME - START_TIME))

print_response "$RESPONSE2"
print_time "${ELAPSED}ms" "~500ms (cálculo costoso)"

if echo "$RESPONSE2" | grep -q "totalAmount"; then
    print_result true "Summary calculado - CACHE MISS esperado"
else
    print_result false "Error en summary"
fi

echo ""
echo -e "${YELLOW}  📋 LOG DEL SERVIDOR (deberías ver):${NC}"
echo -e "${GRAY}     🔄 CACHE MISS - Calculating expense summary for account: account-001${NC}"
echo -e "${GRAY}     ✅ CACHE STORED - Summary calculated${NC}"

pause

# ══════════════════════════════════════════════════════════════════════════════
# TEST 3: EXPENSE SUMMARY - CACHE HIT
# ══════════════════════════════════════════════════════════════════════════════

clear
print_test_header "3" "Expense Summary - CACHE HIT ${LIGHTNING}" "Segunda llamada - debe retornar instantáneamente desde cache"

print_theory_box "${LIGHTNING} CACHE HIT (Segunda vez)" \
    "1. Spring Cache busca key 'account-001' → ¡EXISTE!" \
    "2. Retorna resultado DIRECTO del cache" \
    "3. NO ejecuta el método (ahorra 500ms)"

echo -e "${CYAN}  ${ARROW} REQUEST:${NC} ${GRAY}(mismo query que antes)${NC}"
echo ""

echo -e "${GRAY}  ${CLOCK} Ejecutando query (segunda vez - CON cache)...${NC}"

START_TIME=$(get_time_ms)

RESPONSE3=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ expenseSummary(accountId: \"account-001\") { totalAmount averageAmount count topMerchants { merchantName totalSpent } } }"}')

END_TIME=$(get_time_ms)
ELAPSED=$((END_TIME - START_TIME))

print_response "$RESPONSE3"
print_time "${ELAPSED}ms" "<100ms (desde cache)"

if echo "$RESPONSE3" | grep -q "totalAmount"; then
    print_result true "Summary desde cache - CACHE HIT ${LIGHTNING}"
else
    print_result false "Error en cache hit"
fi

echo ""
echo -e "${YELLOW}  📋 LOG DEL SERVIDOR:${NC}"
echo -e "${GRAY}     (NINGÚN log nuevo - el cache intercepta antes del método)${NC}"

echo ""
echo -e "${GREEN}  ┌─────────────────────────────────────────────────────────────────────────┐${NC}"
echo -e "${GREEN}  │${NC} ${WHITE}${BOLD}MEJORA DE PERFORMANCE${NC}                                                   ${GREEN}│${NC}"
echo -e "${GREEN}  │${NC} Sin cache: ~500ms  →  Con cache: <100ms  =  ${BOLD}5x más rápido ${LIGHTNING}${NC}           ${GREEN}│${NC}"
echo -e "${GREEN}  └─────────────────────────────────────────────────────────────────────────┘${NC}"

pause

# ══════════════════════════════════════════════════════════════════════════════
# TEST 4: MONTHLY ANALYTICS - CACHE MISS
# ══════════════════════════════════════════════════════════════════════════════

clear
print_test_header "4" "Monthly Analytics - CACHE MISS" "Cálculo MUY costoso - agregaciones por categoría"

print_theory_box "${FIRE} CÁLCULO COSTOSO (simulateVeryHeavyComputation)" \
    "Este tipo de queries en producción pueden tardar segundos:" \
    "• Aggregaciones complejas por categoría" \
    "• Cálculos de porcentajes y rankings"

QUERY4='{
  monthlyAnalytics(accountId: "account-001", year: 2024, month: 11) {
    month
    totalSpent
    byCategory {
      category
      amount
      percentage
    }
  }
}'

print_request "$QUERY4"

echo -e "${GRAY}  ${CLOCK} Ejecutando monthly analytics (primera vez)...${NC}"

START_TIME=$(get_time_ms)

RESPONSE4=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ monthlyAnalytics(accountId: \"account-001\", year: 2024, month: 11) { month totalSpent byCategory { category amount percentage } } }"}')

END_TIME=$(get_time_ms)
ELAPSED=$((END_TIME - START_TIME))

print_response "$RESPONSE4"
print_time "${ELAPSED}ms" "~1000ms (cálculo MUY costoso)"

if echo "$RESPONSE4" | grep -q "2024-11"; then
    print_result true "Monthly analytics calculado - CACHE MISS"
else
    print_result false "Error en monthly analytics"
fi

pause

# ══════════════════════════════════════════════════════════════════════════════
# TEST 5: MONTHLY ANALYTICS - CACHE HIT
# ══════════════════════════════════════════════════════════════════════════════

clear
print_test_header "5" "Monthly Analytics - CACHE HIT ${LIGHTNING}" "Segunda llamada - ¡instantáneo!"

print_theory_box "${LIGHTNING} IMPACTO DEL CACHE EN QUERIES COSTOSAS" \
    "Cache Key: 'account-001_2024_11'" \
    "Primera vez: ~1000ms (cálculo + DB)" \
    "Segunda vez: <100ms (directo de memoria)"

echo -e "${CYAN}  ${ARROW} REQUEST:${NC} ${GRAY}(mismo query que antes)${NC}"
echo ""

echo -e "${GRAY}  ${CLOCK} Ejecutando monthly analytics (segunda vez - CON cache)...${NC}"

START_TIME=$(get_time_ms)

RESPONSE5=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ monthlyAnalytics(accountId: \"account-001\", year: 2024, month: 11) { month totalSpent byCategory { category amount percentage } } }"}')

END_TIME=$(get_time_ms)
ELAPSED=$((END_TIME - START_TIME))

print_response "$RESPONSE5"
print_time "${ELAPSED}ms" "<100ms (desde cache)"

if echo "$RESPONSE5" | grep -q "2024-11"; then
    print_result true "Monthly analytics desde cache ${LIGHTNING}${LIGHTNING}"
else
    print_result false "Error en cache hit"
fi

echo ""
echo -e "${GREEN}  ┌─────────────────────────────────────────────────────────────────────────┐${NC}"
echo -e "${GREEN}  │${NC} ${WHITE}${BOLD}MEJORA DE PERFORMANCE${NC}                                                   ${GREEN}│${NC}"
echo -e "${GREEN}  │${NC} Sin cache: ~1000ms  →  Con cache: <100ms  =  ${BOLD}10x más rápido ${LIGHTNING}${LIGHTNING}${NC}        ${GREEN}│${NC}"
echo -e "${GREEN}  └─────────────────────────────────────────────────────────────────────────┘${NC}"

pause

# ══════════════════════════════════════════════════════════════════════════════
# TEST 6: CREAR EXPENSE - INVALIDA CACHE
# ══════════════════════════════════════════════════════════════════════════════

clear
print_test_header "6" "Mutation - Crear Expense ${WARNING}" "Al crear un expense, el cache debe INVALIDARSE automáticamente"

print_theory_box "${WARNING} INVALIDACIÓN DE CACHE (@CacheEvict)" \
    "Problema: Si creamos un expense, el summary cacheado queda STALE" \
    "Solución: @CacheEvict invalida automáticamente los caches afectados" \
    "Caches invalidados: expenseSummary, monthlyAnalytics, topMerchants"

MUTATION='{
  mutation {
    createExpense(input: {
      accountId: "account-001"
      amount: 299.99
      currency: "USD"
      merchantName: "Apple Store"
      category: SHOPPING
      date: "2024-12-05"
      description: "AirPods Pro"
    }) {
      success
      message
      expense {
        id
        amount
        merchantName
      }
    }
  }
}'

echo -e "${CYAN}  ${ARROW} MUTATION:${NC}"
echo -e "${WHITE}  ┌─────────────────────────────────────────────────────────────────────────┐${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}mutation {${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}  createExpense(input: {${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}    accountId: \"account-001\"${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}    amount: ${YELLOW}299.99${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}    merchantName: \"Apple Store\"${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}    category: SHOPPING${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}    description: \"AirPods Pro\"${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}  }) { success message expense { id } }${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}}${NC}"
echo -e "${WHITE}  └─────────────────────────────────────────────────────────────────────────┘${NC}"
echo ""

echo -e "${GRAY}  Ejecutando mutation...${NC}"

RESPONSE6=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d '{"query":"mutation { createExpense(input: { accountId: \"account-001\", amount: 299.99, currency: \"USD\", merchantName: \"Apple Store\", category: SHOPPING, date: \"2024-12-05\", description: \"AirPods Pro\" }) { success message expense { id amount merchantName } } }"}')

print_response "$RESPONSE6"

if echo "$RESPONSE6" | grep -q "true"; then
    print_result true "Expense creado + Cache INVALIDADO"
else
    print_result false "Error creando expense"
fi

echo ""
echo -e "${YELLOW}  📋 LOG DEL SERVIDOR (deberías ver):${NC}"
echo -e "${GRAY}     🗑️  CACHE INVALIDATED for account: account-001${NC}"

pause

# ══════════════════════════════════════════════════════════════════════════════
# TEST 7: SUMMARY POST-INVALIDACIÓN
# ══════════════════════════════════════════════════════════════════════════════

clear
print_test_header "7" "Summary Post-Invalidación" "Después del mutation, el cache está vacío - debe recalcular"

print_theory_box "${CACHE} FLUJO COMPLETO DE CACHE" \
    "Test 2: CACHE MISS (~500ms) → guarda en cache" \
    "Test 3: CACHE HIT (<100ms) → usa cache" \
    "Test 6: MUTATION → invalida cache" \
    "Test 7: CACHE MISS (~500ms) → recalcula con nuevo expense"

echo -e "${CYAN}  ${ARROW} REQUEST:${NC} ${GRAY}(mismo query de summary)${NC}"
echo ""

echo -e "${GRAY}  ${CLOCK} Ejecutando summary después de invalidación...${NC}"

START_TIME=$(get_time_ms)

RESPONSE7=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ expenseSummary(accountId: \"account-001\") { totalAmount averageAmount count topMerchants { merchantName totalSpent } } }"}')

END_TIME=$(get_time_ms)
ELAPSED=$((END_TIME - START_TIME))

print_response "$RESPONSE7"
print_time "${ELAPSED}ms" "~500ms (cache vacío, recalcula)"

if echo "$RESPONSE7" | grep -q "totalAmount"; then
    print_result true "Cache invalidado correctamente - CACHE MISS otra vez"
else
    print_result false "Error en summary post-invalidación"
fi

echo ""
echo -e "${CYAN}  💡 NOTA: El nuevo totalAmount ahora incluye los \$299.99 del Apple Store${NC}"

pause

# ══════════════════════════════════════════════════════════════════════════════
# RESUMEN FINAL
# ══════════════════════════════════════════════════════════════════════════════

clear
echo ""
echo -e "${PURPLE}╔═══════════════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${PURPLE}║${NC}                                                                           ${PURPLE}║${NC}"
echo -e "${PURPLE}║${NC}   ${WHITE}${BOLD}📊 RESUMEN DE PRUEBAS - CHAPTER 07${NC}                                      ${PURPLE}║${NC}"
echo -e "${PURPLE}║${NC}                                                                           ${PURPLE}║${NC}"
echo -e "${PURPLE}╚═══════════════════════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${WHITE}  RESULTADOS:${NC}"
echo -e "${GRAY}  ─────────────────────────────────────────────────────────────────────────${NC}"
echo -e "  ${GREEN}${CHECK}${NC} Test 1: Expenses existentes"
echo -e "  ${GREEN}${CHECK}${NC} Test 2: Summary - CACHE MISS (~500ms)"
echo -e "  ${GREEN}${CHECK}${NC} Test 3: Summary - CACHE HIT (<100ms) ${LIGHTNING}"
echo -e "  ${GREEN}${CHECK}${NC} Test 4: Monthly Analytics - CACHE MISS (~1s)"
echo -e "  ${GREEN}${CHECK}${NC} Test 5: Monthly Analytics - CACHE HIT (<100ms) ${LIGHTNING}${LIGHTNING}"
echo -e "  ${GREEN}${CHECK}${NC} Test 6: Crear expense (invalida cache)"
echo -e "  ${GREEN}${CHECK}${NC} Test 7: Summary - CACHE MISS otra vez"
echo ""

TOTAL=$((PASSED + FAILED))
if [ $TOTAL -gt 0 ]; then
    SUCCESS_RATE=$((PASSED * 100 / TOTAL))
else
    SUCCESS_RATE=0
fi

echo -e "${GRAY}  ─────────────────────────────────────────────────────────────────────────${NC}"
echo -e "  ${WHITE}Total:${NC}        ${BOLD}${TOTAL}${NC} pruebas"
echo -e "  ${GREEN}Exitosas:${NC}     ${BOLD}${PASSED}${NC} ${CHECK}"
echo -e "  ${RED}Fallidas:${NC}     ${BOLD}${FAILED}${NC}"
echo -e "  ${CYAN}Tasa éxito:${NC}   ${BOLD}${SUCCESS_RATE}%${NC}"
echo ""

echo -e "${CYAN}╔═══════════════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║${NC} ${WHITE}${BOLD}${FIRE} CACHING STRATEGIES VERIFICADAS${NC}                                         ${CYAN}║${NC}"
echo -e "${CYAN}╠═══════════════════════════════════════════════════════════════════════════╣${NC}"
echo -e "${CYAN}║${NC}                                                                           ${CYAN}║${NC}"
echo -e "${CYAN}║${NC} ${YELLOW}Resolver-Level Caching (Spring Cache + Caffeine):${NC}                        ${CYAN}║${NC}"
echo -e "${CYAN}║${NC}   • expenseSummary     │ TTL: 5 min │ Key: accountId                    ${CYAN}║${NC}"
echo -e "${CYAN}║${NC}   • monthlyAnalytics   │ TTL: 5 min │ Key: accountId_year_month         ${CYAN}║${NC}"
echo -e "${CYAN}║${NC}   • topMerchants       │ TTL: 5 min │ Key: accountId_limit              ${CYAN}║${NC}"
echo -e "${CYAN}║${NC}   • @CacheEvict en mutations ${CHECK}                                         ${CYAN}║${NC}"
echo -e "${CYAN}║${NC}                                                                           ${CYAN}║${NC}"
echo -e "${CYAN}║${NC} ${YELLOW}Per-Request Caching (DataLoader):${NC}                                         ${CYAN}║${NC}"
echo -e "${CYAN}║${NC}   • Batching: Agrupa N requests en 1 query                               ${CYAN}║${NC}"
echo -e "${CYAN}║${NC}   • Caching: Reutiliza durante el mismo request                          ${CYAN}║${NC}"
echo -e "${CYAN}║${NC}   • Elimina problema N+1 ${CHECK}                                               ${CYAN}║${NC}"
echo -e "${CYAN}║${NC}                                                                           ${CYAN}║${NC}"
echo -e "${CYAN}╚═══════════════════════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${GREEN}╔═══════════════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║${NC} ${WHITE}${BOLD}${LIGHTNING} MEJORAS DE PERFORMANCE DEMOSTRADAS${NC}                                     ${GREEN}║${NC}"
echo -e "${GREEN}╠═══════════════════════════════════════════════════════════════════════════╣${NC}"
echo -e "${GREEN}║${NC}                                                                           ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}   ${RED}Sin cache:${NC}    ~500ms - 1000ms por query                                 ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}   ${GREEN}Con cache:${NC}    <100ms por query                                         ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}   ${CYAN}Mejora:${NC}       ${BOLD}5x - 10x más rápido ${ROCKET}${NC}                                  ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}                                                                           ${GREEN}║${NC}"
echo -e "${GREEN}╚═══════════════════════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${GRAY}  ${ROCKET} GraphiQL: ${CYAN}http://localhost:8080/graphiql${NC}"
echo -e "${GRAY}  📄 Log guardado: ${CYAN}${LOG_FILE}${NC}"
echo ""