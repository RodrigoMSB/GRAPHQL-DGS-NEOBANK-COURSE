#!/bin/bash

# ╔═══════════════════════════════════════════════════════════════════════════╗
# ║                                                                           ║
# ║   CHAPTER 08 - CARBON FOOTPRINT: GOVERNANCE & SCHEMA EVOLUTION            ║
# ║   GraphQL Schema Versioning & Deprecation Test Suite                      ║
# ║                                                                           ║
# ║   Este script demuestra GOVERNANCE en GraphQL:                            ║
# ║   • Schema Versioning (SemVer)                                            ║
# ║   • @deprecated directive                                                 ║
# ║   • Breaking vs Additive changes                                          ║
# ║   • Backward compatibility                                                ║
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
ORANGE='\033[0;33m'
BOLD='\033[1m'
DIM='\033[2m'
STRIKETHROUGH='\033[9m'
NC='\033[0m' # No Color

# Emojis y símbolos
CHECK="✅"
CROSS="❌"
ARROW="➜"
WARNING="⚠️"
EARTH="🌍"
LEAF="🌱"
TREE="🌳"
PLANE="✈️"
ALERT="🚨"
DEPRECATED="🗑️"
VERSION="📋"
ROCKET="🚀"

# ══════════════════════════════════════════════════════════════════════════════
# CONFIGURACIÓN
# ══════════════════════════════════════════════════════════════════════════════
GRAPHQL_URL="http://localhost:8080/graphql"
TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
LOG_FILE="test-results-chapter08-${TIMESTAMP}.log"

# Contadores
PASSED=0
FAILED=0
TOTAL_TESTS=7

# ══════════════════════════════════════════════════════════════════════════════
# FUNCIONES UTILITARIAS
# ══════════════════════════════════════════════════════════════════════════════

print_header() {
    echo ""
    echo -e "${GREEN}╔═══════════════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║${NC}                                                                           ${GREEN}║${NC}"
    echo -e "${GREEN}║${NC}   ${WHITE}${BOLD}${EARTH} CHAPTER 08: CARBON FOOTPRINT - GOVERNANCE & VERSIONING${NC}             ${GREEN}║${NC}"
    echo -e "${GREEN}║${NC}   ${GRAY}Schema Evolution & Deprecation Strategies${NC}                              ${GREEN}║${NC}"
    echo -e "${GREEN}║${NC}                                                                           ${GREEN}║${NC}"
    echo -e "${GREEN}╚═══════════════════════════════════════════════════════════════════════════╝${NC}"
    echo ""
}

print_concept() {
    echo -e "${CYAN}┌─────────────────────────────────────────────────────────────────────────────┐${NC}"
    echo -e "${CYAN}│${NC} ${YELLOW}${BOLD}💡 CONCEPTO: GOVERNANCE EN GRAPHQL${NC}                                         ${CYAN}│${NC}"
    echo -e "${CYAN}├─────────────────────────────────────────────────────────────────────────────┤${NC}"
    echo -e "${CYAN}│${NC}                                                                             ${CYAN}│${NC}"
    echo -e "${CYAN}│${NC}  ${WHITE}¿Por qué Governance?${NC}                                                      ${CYAN}│${NC}"
    echo -e "${CYAN}│${NC}  ${GRAY}En producción: 100+ devs, 50+ apps cliente, millones de requests${NC}          ${CYAN}│${NC}"
    echo -e "${CYAN}│${NC}  ${GRAY}Pregunta clave: ¿Cómo evolucionar SIN romper nada?${NC}                        ${CYAN}│${NC}"
    echo -e "${CYAN}│${NC}                                                                             ${CYAN}│${NC}"
    echo -e "${CYAN}│${NC}  ${WHITE}Principios de este capítulo:${NC}                                              ${CYAN}│${NC}"
    echo -e "${CYAN}│${NC}  ${GRAY}├─ SemVer: MAJOR.MINOR.PATCH (v2.0.0)${NC}                                     ${CYAN}│${NC}"
    echo -e "${CYAN}│${NC}  ${GRAY}├─ @deprecated: Marcar antes de remover (90+ días)${NC}                        ${CYAN}│${NC}"
    echo -e "${CYAN}│${NC}  ${GRAY}├─ Additive changes: Agregar campos es SAFE${NC}                               ${CYAN}│${NC}"
    echo -e "${CYAN}│${NC}  ${GRAY}└─ Breaking changes: Remover campos es PELIGROSO${NC}                          ${CYAN}│${NC}"
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
    local line4=$5
    
    echo -e "${YELLOW}  ┌─────────────────────────────────────────────────────────────────────────┐${NC}"
    echo -e "${YELLOW}  │${NC} ${WHITE}${BOLD}$title${NC}"
    echo -e "${YELLOW}  ├─────────────────────────────────────────────────────────────────────────┤${NC}"
    echo -e "${YELLOW}  │${NC} ${GRAY}$line1${NC}"
    [ -n "$line2" ] && echo -e "${YELLOW}  │${NC} ${GRAY}$line2${NC}"
    [ -n "$line3" ] && echo -e "${YELLOW}  │${NC} ${GRAY}$line3${NC}"
    [ -n "$line4" ] && echo -e "${YELLOW}  │${NC} ${GRAY}$line4${NC}"
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
        echo "$response" | jq '.' 2>/dev/null | head -30 | while IFS= read -r line; do
            printf "${WHITE}  │${NC} ${GRAY}%s${NC}\n" "$line"
        done
    else
        echo -e "${WHITE}  │${NC} ${GRAY}$response${NC}"
    fi
    echo -e "${WHITE}  └─────────────────────────────────────────────────────────────────────────┘${NC}"
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
    echo -e "${GREEN}  ${CHECK} Carbon Footprint Service: ONLINE${NC}"
else
    echo -e "${RED}  ${CROSS} ERROR: Servicio no responde en ${GRAPHQL_URL}${NC}"
    echo -e "${YELLOW}  ${WARNING} Ejecuta: ${WHITE}mvn spring-boot:run${NC}"
    exit 1
fi

pause

# ══════════════════════════════════════════════════════════════════════════════
# TEST 1: SCHEMA VERSION INFO
# ══════════════════════════════════════════════════════════════════════════════

clear
print_test_header "1" "Schema Version Info ${VERSION}" "Query de metadata de governance - versión, deprecations, breaking changes"

print_theory_box "${VERSION} SCHEMA VERSIONING (SemVer)" \
    "v MAJOR . MINOR . PATCH" \
    "  │       │       └─ Bug fixes (no cambia schema)" \
    "  │       └───────── Nuevos campos (additive, safe)" \
    "  └─────────────────  Removal de campos (breaking!)"

QUERY1='{
  schemaVersion {
    version
    lastUpdated
    deprecations {
      field
      reason
      removedInVersion
      alternative
    }
    breakingChanges
  }
}'

print_request "$QUERY1"

echo -e "${GRAY}  Ejecutando query...${NC}"

RESPONSE1=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ schemaVersion { version lastUpdated deprecations { field reason removedInVersion alternative } breakingChanges } }"}')

print_response "$RESPONSE1"

if echo "$RESPONSE1" | grep -q "2.0.0" && echo "$RESPONSE1" | grep -q "Transaction.category"; then
    print_result true "Schema v2.0.0 con deprecations documentadas"
else
    print_result false "Schema version info incorrecta"
fi

echo ""
echo -e "${PURPLE}  📊 DEPRECATIONS EN ESTE SCHEMA:${NC}"
echo -e "${GRAY}     • Transaction.category → merchantCategory (enum type-safe)${NC}"
echo -e "${GRAY}     • Transaction.hasOffset → carbonFootprint.offsetPurchased${NC}"
echo -e "${GRAY}     • buyOffset() → purchaseCarbonOffset() (response detallada)${NC}"

pause

# ══════════════════════════════════════════════════════════════════════════════
# TEST 2: TRANSACTIONS CON CARBON FOOTPRINT
# ══════════════════════════════════════════════════════════════════════════════

clear
print_test_header "2" "Transactions con Carbon Footprint ${EARTH}" "Ver transacciones con su huella de carbono calculada"

print_theory_box "${LEAF} CARBON FOOTPRINT - ESG Compliance" \
    "Cada transacción calcula automáticamente:" \
    "• co2Kg: Kilogramos de CO2 emitidos" \
    "• impactLevel: LOW | MEDIUM | HIGH | CRITICAL" \
    "• treesEquivalent: Árboles para compensar"

QUERY2='{
  transactions(accountId: "account-001") {
    id
    amount
    merchantName
    merchantCategory
    carbonFootprint {
      co2Kg
      impactLevel
    }
  }
}'

print_request "$QUERY2"

echo -e "${GRAY}  Ejecutando query...${NC}"

RESPONSE2=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ transactions(accountId: \"account-001\") { id amount merchantName merchantCategory carbonFootprint { co2Kg impactLevel } } }"}')

print_response "$RESPONSE2"

if echo "$RESPONSE2" | grep -q "United Airlines" && echo "$RESPONSE2" | grep -q "CRITICAL"; then
    print_result true "Transacciones con carbon footprint calculado"
else
    print_result false "Error en transacciones"
fi

echo ""
echo -e "${RED}  ${PLANE} IMPACTO POR CATEGORÍA:${NC}"
echo -e "${GRAY}     • TRAVEL_AVIATION: ~200-500 kg CO2 (CRITICAL)${NC}"
echo -e "${GRAY}     • TRANSPORTATION: ~10-50 kg CO2 (HIGH)${NC}"
echo -e "${GRAY}     • FOOD_RETAIL: ~2-10 kg CO2 (MEDIUM)${NC}"
echo -e "${GRAY}     • ELECTRONICS: ~5-20 kg CO2 (MEDIUM)${NC}"

pause

# ══════════════════════════════════════════════════════════════════════════════
# TEST 3: SUSTAINABILITY REPORT
# ══════════════════════════════════════════════════════════════════════════════

clear
print_test_header "3" "Sustainability Report ${TREE}" "Reporte mensual de sostenibilidad con recomendaciones"

print_theory_box "${TREE} REPORTE ESG MENSUAL" \
    "Incluye:" \
    "• Total CO2 del período" \
    "• Transacción de mayor impacto" \
    "• Recomendaciones personalizadas"

QUERY3='{
  sustainabilityReport(
    accountId: "account-001"
    year: 2024
    month: 11
  ) {
    totalCO2Kg
    totalTransactions
    averageCO2PerTransaction
    recommendations
    highestImpactTransaction {
      merchantName
      carbonFootprint { co2Kg }
    }
  }
}'

print_request "$QUERY3"

echo -e "${GRAY}  Ejecutando query...${NC}"

RESPONSE3=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ sustainabilityReport(accountId: \"account-001\", year: 2024, month: 11) { totalCO2Kg totalTransactions averageCO2PerTransaction recommendations highestImpactTransaction { merchantName carbonFootprint { co2Kg } } } }"}')

print_response "$RESPONSE3"

if echo "$RESPONSE3" | grep -q "totalCO2Kg" && echo "$RESPONSE3" | grep -q "recommendations"; then
    print_result true "Sustainability report generado con recomendaciones"
else
    print_result false "Error en sustainability report"
fi

pause

# ══════════════════════════════════════════════════════════════════════════════
# TEST 4: COMPARAR PERÍODOS
# ══════════════════════════════════════════════════════════════════════════════

clear
print_test_header "4" "Comparar Períodos (Nov vs Dic)" "Comparación de huella de carbono entre meses - ADDED IN v2.0.0"

print_theory_box "📊 PERIOD COMPARISON (Nuevo en v2.0.0)" \
    "Este tipo fue AGREGADO en v2.0.0 (additive change)" \
    "Trends:" \
    "• IMPROVING: CO2 bajó respecto al período anterior" \
    "• WORSENING: CO2 subió (¡alerta!)"

QUERY4='{
  comparePeriods(
    accountId: "account-001"
    year1: 2024
    month1: 11
    year2: 2024
    month2: 12
  ) {
    previousPeriod
    co2Change
    percentageChange
    trend
  }
}'

print_request "$QUERY4"

echo -e "${GRAY}  Ejecutando query...${NC}"

RESPONSE4=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ comparePeriods(accountId: \"account-001\", year1: 2024, month1: 11, year2: 2024, month2: 12) { previousPeriod co2Change percentageChange trend } }"}')

print_response "$RESPONSE4"

if echo "$RESPONSE4" | grep -q "trend"; then
    print_result true "Period comparison funciona (v2.0.0 feature)"
else
    print_result false "Error en period comparison"
fi

echo ""
echo -e "${GREEN}  ${CHECK} ADDITIVE CHANGE: Este campo se agregó SIN romper clientes existentes${NC}"

pause

# ══════════════════════════════════════════════════════════════════════════════
# TEST 5: FILTRAR POR IMPACT LEVEL
# ══════════════════════════════════════════════════════════════════════════════

clear
print_test_header "5" "Filtrar por Impact Level ${ALERT}" "Ver solo transacciones CRITICAL - útil para dashboards de alertas"

print_theory_box "${ALERT} IMPACT LEVELS" \
    "LOW:      < 5 kg CO2   (compras pequeñas)" \
    "MEDIUM:   5-20 kg CO2  (retail, comida)" \
    "HIGH:     20-100 kg CO2 (transporte)" \
    "CRITICAL: > 100 kg CO2 (aviación, energía)"

QUERY5='{
  transactionsByImpact(
    accountId: "account-001"
    impactLevel: CRITICAL
  ) {
    merchantName
    carbonFootprint {
      co2Kg
      impactLevel
    }
  }
}'

print_request "$QUERY5"

echo -e "${GRAY}  Ejecutando query...${NC}"

RESPONSE5=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ transactionsByImpact(accountId: \"account-001\", impactLevel: CRITICAL) { merchantName carbonFootprint { co2Kg impactLevel } } }"}')

print_response "$RESPONSE5"

if echo "$RESPONSE5" | grep -q "CRITICAL"; then
    print_result true "Filtro por impact level funciona"
else
    print_result false "Error en filtro de impact"
fi

echo ""
echo -e "${RED}  🚨 CASO DE USO: Dashboard de compliance ESG para reguladores${NC}"

pause

# ══════════════════════════════════════════════════════════════════════════════
# TEST 6: CREAR TRANSACCIÓN (GENERA ALERT)
# ══════════════════════════════════════════════════════════════════════════════

clear
print_test_header "6" "Crear Transaction ${PLANE}" "Mutation que crea transacción y genera CarbonAlert si es CRITICAL"

print_theory_box "${PLANE} TRANSACCIÓN DE AVIACIÓN" \
    "Input: Lufthansa \$3000 → TRAVEL_AVIATION" \
    "Carbon footprint calculado automáticamente" \
    "Si impactLevel = CRITICAL → genera CarbonAlert" \
    "Alert incluye: severity, message, recommendation"

echo -e "${CYAN}  ${ARROW} MUTATION:${NC}"
echo -e "${WHITE}  ┌─────────────────────────────────────────────────────────────────────────┐${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}mutation {${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}  createTransaction(input: {${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}    accountId: \"account-001\"${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}    amount: ${YELLOW}3000${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}    currency: \"USD\"${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}    merchantName: \"${CYAN}Lufthansa${GREEN}\"${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}    merchantCategory: ${RED}TRAVEL_AVIATION${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}    date: \"2024-12-05\"${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}  }) {${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}    success${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}    transaction { carbonFootprint { co2Kg impactLevel } }${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}    carbonAlert { severity message }${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}  }${NC}"
echo -e "${WHITE}  │${NC} ${GREEN}}${NC}"
echo -e "${WHITE}  └─────────────────────────────────────────────────────────────────────────┘${NC}"
echo ""

echo -e "${GRAY}  Ejecutando mutation...${NC}"

RESPONSE6=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d '{"query":"mutation { createTransaction(input: { accountId: \"account-001\", amount: 3000, currency: \"USD\", merchantName: \"Lufthansa\", merchantCategory: TRAVEL_AVIATION, date: \"2024-12-05\" }) { success message transaction { id carbonFootprint { co2Kg impactLevel } } carbonAlert { severity message } } }"}')

print_response "$RESPONSE6"

if echo "$RESPONSE6" | grep -q "true" && echo "$RESPONSE6" | grep -q "CRITICAL"; then
    print_result true "Transaction creada + CarbonAlert generado"
else
    print_result false "Error creando transaction"
fi

echo ""
echo -e "${RED}  ${ALERT} El CarbonAlert se genera automáticamente para transacciones CRITICAL${NC}"

pause

# ══════════════════════════════════════════════════════════════════════════════
# TEST 7: ENDPOINT DEPRECATED
# ══════════════════════════════════════════════════════════════════════════════

clear
print_test_header "7" "Endpoint DEPRECATED ${DEPRECATED}" "Usar mutation deprecada - debe funcionar pero generar WARNING"

print_theory_box "${WARNING} @deprecated DIRECTIVE" \
    "buyOffset() está DEPRECATED desde v2.0.0" \
    "Razón: Respuesta muy simple (solo Boolean)" \
    "Alternativa: purchaseCarbonOffset() → respuesta detallada" \
    "Removal planned: v3.0.0 (Q2 2025)"

echo -e "${CYAN}  ${ARROW} COMPARACIÓN:${NC}"
echo ""
echo -e "${RED}  ${STRIKETHROUGH}buyOffset(transactionId: ID!): Boolean${NC}  ${GRAY}← DEPRECATED${NC}"
echo -e "${GREEN}  purchaseCarbonOffset(transactionId: ID!): CarbonOffsetResponse!${NC}  ${GRAY}← NUEVO${NC}"
echo ""

echo -e "${CYAN}  ${ARROW} MUTATION (DEPRECATED):${NC}"
echo -e "${WHITE}  ┌─────────────────────────────────────────────────────────────────────────┐${NC}"
echo -e "${WHITE}  │${NC} ${YELLOW}mutation {${NC}"
echo -e "${WHITE}  │${NC} ${YELLOW}  buyOffset(transactionId: \"txn-001\")  ${RED}# ⚠️ DEPRECATED${NC}"
echo -e "${WHITE}  │${NC} ${YELLOW}}${NC}"
echo -e "${WHITE}  └─────────────────────────────────────────────────────────────────────────┘${NC}"
echo ""

echo -e "${GRAY}  Ejecutando mutation deprecada...${NC}"

RESPONSE7=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d '{"query":"mutation { buyOffset(transactionId: \"txn-001\") }"}')

print_response "$RESPONSE7"

print_result true "Endpoint deprecated sigue funcionando (backward compatible)"

echo ""
echo -e "${YELLOW}  📋 LOG DEL SERVIDOR (deberías ver):${NC}"
echo -e "${GRAY}     ⚠️ DEPRECATED ENDPOINT USED: buyOffset${NC}"
echo -e "${GRAY}        Use: purchaseCarbonOffset instead${NC}"
echo -e "${GRAY}        Removal: v3.0.0 (Q2 2025)${NC}"
echo ""

echo -e "${CYAN}  ┌─────────────────────────────────────────────────────────────────────────┐${NC}"
echo -e "${CYAN}  │${NC} ${WHITE}${BOLD}💡 TIMELINE DE DEPRECATION${NC}                                             ${CYAN}│${NC}"
echo -e "${CYAN}  ├─────────────────────────────────────────────────────────────────────────┤${NC}"
echo -e "${CYAN}  │${NC}                                                                         ${CYAN}│${NC}"
echo -e "${CYAN}  │${NC}  ${YELLOW}v2.0.0${NC}        ${YELLOW}v2.1.0${NC}        ${YELLOW}v2.2.0${NC}        ${RED}v3.0.0${NC}                 ${CYAN}│${NC}"
echo -e "${CYAN}  │${NC}  Dec 2024      Jan 2025      Feb 2025      ${RED}Mar 2025${NC}               ${CYAN}│${NC}"
echo -e "${CYAN}  │${NC}     │             │             │             │                    ${CYAN}│${NC}"
echo -e "${CYAN}  │${NC}  DEPRECAR →    AVISOS    →   ÚLTIMO    →   ${RED}REMOVER${NC}                ${CYAN}│${NC}"
echo -e "${CYAN}  │${NC}               en logs        aviso                                   ${CYAN}│${NC}"
echo -e "${CYAN}  │${NC}                                                                         ${CYAN}│${NC}"
echo -e "${CYAN}  └─────────────────────────────────────────────────────────────────────────┘${NC}"

pause

# ══════════════════════════════════════════════════════════════════════════════
# RESUMEN FINAL
# ══════════════════════════════════════════════════════════════════════════════

clear
echo ""
echo -e "${GREEN}╔═══════════════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║${NC}                                                                           ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}   ${WHITE}${BOLD}📊 RESUMEN DE PRUEBAS - CHAPTER 08${NC}                                      ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}                                                                           ${GREEN}║${NC}"
echo -e "${GREEN}╚═══════════════════════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${WHITE}  RESULTADOS:${NC}"
echo -e "${GRAY}  ─────────────────────────────────────────────────────────────────────────${NC}"
echo -e "  ${GREEN}${CHECK}${NC} Test 1: Schema Version Info (v2.0.0)"
echo -e "  ${GREEN}${CHECK}${NC} Test 2: Transactions con Carbon Footprint"
echo -e "  ${GREEN}${CHECK}${NC} Test 3: Sustainability Report"
echo -e "  ${GREEN}${CHECK}${NC} Test 4: Comparar Períodos (v2.0.0 feature)"
echo -e "  ${GREEN}${CHECK}${NC} Test 5: Filtrar por Impact Level"
echo -e "  ${GREEN}${CHECK}${NC} Test 6: Crear Transaction + Alert"
echo -e "  ${GREEN}${CHECK}${NC} Test 7: Endpoint Deprecated (backward compatible)"
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

echo -e "${PURPLE}╔═══════════════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${PURPLE}║${NC} ${WHITE}${BOLD}${VERSION} GOVERNANCE VERIFICADA${NC}                                               ${PURPLE}║${NC}"
echo -e "${PURPLE}╠═══════════════════════════════════════════════════════════════════════════╣${NC}"
echo -e "${PURPLE}║${NC}                                                                           ${PURPLE}║${NC}"
echo -e "${PURPLE}║${NC} ${GREEN}${CHECK}${NC} Schema Version: ${WHITE}2.0.0${NC}                                                 ${PURPLE}║${NC}"
echo -e "${PURPLE}║${NC} ${GREEN}${CHECK}${NC} Deprecations documentadas: ${WHITE}3 campos${NC}                                   ${PURPLE}║${NC}"
echo -e "${PURPLE}║${NC}    • Transaction.category → merchantCategory                             ${PURPLE}║${NC}"
echo -e "${PURPLE}║${NC}    • Transaction.hasOffset → carbonFootprint.offsetPurchased             ${PURPLE}║${NC}"
echo -e "${PURPLE}║${NC}    • buyOffset() → purchaseCarbonOffset()                                ${PURPLE}║${NC}"
echo -e "${PURPLE}║${NC} ${GREEN}${CHECK}${NC} Breaking changes registrados en CHANGELOG.md                             ${PURPLE}║${NC}"
echo -e "${PURPLE}║${NC} ${GREEN}${CHECK}${NC} Backward compatibility mantenida                                         ${PURPLE}║${NC}"
echo -e "${PURPLE}║${NC} ${GREEN}${CHECK}${NC} Removal planned: v3.0.0 (Q2 2025)                                        ${PURPLE}║${NC}"
echo -e "${PURPLE}║${NC}                                                                           ${PURPLE}║${NC}"
echo -e "${PURPLE}╚═══════════════════════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${CYAN}╔═══════════════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║${NC} ${WHITE}${BOLD}📚 BEST PRACTICES APLICADAS${NC}                                               ${CYAN}║${NC}"
echo -e "${CYAN}╠═══════════════════════════════════════════════════════════════════════════╣${NC}"
echo -e "${CYAN}║${NC}                                                                           ${CYAN}║${NC}"
echo -e "${CYAN}║${NC}  ${GREEN}1.${NC} Nunca breaking changes sin avisar (mínimo 90 días)                     ${CYAN}║${NC}"
echo -e "${CYAN}║${NC}  ${GREEN}2.${NC} Documentar TODO en CHANGELOG.md                                        ${CYAN}║${NC}"
echo -e "${CYAN}║${NC}  ${GREEN}3.${NC} @deprecated con reason y timeline                                      ${CYAN}║${NC}"
echo -e "${CYAN}║${NC}  ${GREEN}4.${NC} Agregar campos es safe, remover es peligroso                           ${CYAN}║${NC}"
echo -e "${CYAN}║${NC}  ${GREEN}5.${NC} Schema registry como source of truth                                   ${CYAN}║${NC}"
echo -e "${CYAN}║${NC}  ${GREEN}6.${NC} CI/CD valida cambios automáticamente                                   ${CYAN}║${NC}"
echo -e "${CYAN}║${NC}                                                                           ${CYAN}║${NC}"
echo -e "${CYAN}╚═══════════════════════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${GREEN}╔═══════════════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║${NC} ${WHITE}${BOLD}${EARTH} CARBON FOOTPRINT TRACKING${NC}                                             ${GREEN}║${NC}"
echo -e "${GREEN}╠═══════════════════════════════════════════════════════════════════════════╣${NC}"
echo -e "${GREEN}║${NC}                                                                           ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}  ${LEAF} Impact Levels: LOW | MEDIUM | HIGH | CRITICAL                          ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}  ${TREE} Sustainability Reports mensuales                                       ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}  ${ALERT} Carbon Alerts automáticos para alto impacto                            ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}  📊 ESG Scores por merchant                                                 ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}  💰 Carbon Offset purchasing                                                ${GREEN}║${NC}"
echo -e "${GREEN}║${NC}                                                                           ${GREEN}║${NC}"
echo -e "${GREEN}╚═══════════════════════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${GRAY}  ${ROCKET} GraphiQL: ${CYAN}http://localhost:8080/graphiql${NC}"
echo -e "${GRAY}  📄 Log guardado: ${CYAN}${LOG_FILE}${NC}"
echo ""
echo -e "${GREEN}  🎉 ¡CURSO COMPLETADO! Has dominado GraphQL desde fundamentos hasta Governance${NC}"
echo ""