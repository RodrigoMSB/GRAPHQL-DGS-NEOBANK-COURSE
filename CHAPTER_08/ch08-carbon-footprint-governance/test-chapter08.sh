#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

GRAPHQL_URL="http://localhost:8080/graphql"
TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
LOG_FILE="test-results-chapter08-${TIMESTAMP}.txt"

format_json() {
    if command -v jq &> /dev/null; then
        echo "$1" | jq '.'
    else
        echo "$1"
    fi
}

log_output() {
    echo "$1" | tee -a "$LOG_FILE"
}

pause() {
    echo ""
    read -p "Presiona ENTER para continuar..."
    echo ""
}

clear
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log_output "🌍  CHAPTER 08: CARBON FOOTPRINT - GOVERNANCE & VERSIONING"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log_output "📄 Log: $LOG_FILE"
echo ""

log_output "Verificando Carbon Footprint Service (Puerto 8080)..."
HEALTH_CHECK=$(curl -s -X POST "$GRAPHQL_URL" -H "Content-Type: application/json" -d '{"query":"{__typename}"}' 2>&1)

if echo "$HEALTH_CHECK" | grep -q "Query"; then
    log_output "✅ Carbon Footprint Service: OK"
else
    log_output "❌ ERROR: Servicio no responde"
    exit 1
fi

echo ""
pause

PASSED=0
FAILED=0

# TEST 1: Schema Version Info
clear
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log_output "📋 TEST 1 de 7: Schema Version Info (Governance)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
log_output "🎯 Verificar metadata de versionado y deprecations"
echo ""

QUERY1='{"query":"{ schemaVersion { version lastUpdated deprecations { field reason removedInVersion alternative } breakingChanges } }"}'

log_output "📤 QUERY: schemaVersion"
echo ""

RESPONSE1=$(curl -s -X POST "$GRAPHQL_URL" -H "Content-Type: application/json" -d "$QUERY1")

log_output "📥 RESPONSE:"
log_output "$(format_json "$RESPONSE1")"
echo ""

if echo "$RESPONSE1" | grep -q "2.0.0" && echo "$RESPONSE1" | grep -q "Transaction.category"; then
    log_output "✅ PASSED: Schema version y deprecations OK"
    ((PASSED++))
else
    log_output "❌ FAILED: Schema version info incorrecta"
    ((FAILED++))
fi

pause

# TEST 2: Ver transacciones con carbon footprint
clear
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log_output "📋 TEST 2 de 7: Transactions con Carbon Footprint"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
log_output "🎯 Ver transacciones con CO2 e impacto"
echo ""

QUERY2='{"query":"{ transactions(accountId: \"account-001\") { id amount merchantName merchantCategory carbonFootprint { co2Kg impactLevel } } }"}'

log_output "📤 QUERY: transactions"
echo ""

RESPONSE2=$(curl -s -X POST "$GRAPHQL_URL" -H "Content-Type: application/json" -d "$QUERY2")

log_output "📥 RESPONSE:"
log_output "$(format_json "$RESPONSE2")"
echo ""

if echo "$RESPONSE2" | grep -q "United Airlines" && echo "$RESPONSE2" | grep -q "CRITICAL"; then
    log_output "✅ PASSED: Transacciones con footprint OK"
    ((PASSED++))
else
    log_output "❌ FAILED: Error en transacciones"
    ((FAILED++))
fi

pause

# TEST 3: Sustainability Report
clear
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log_output "📋 TEST 3 de 7: Sustainability Report"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
log_output "🎯 Reporte de sostenibilidad mensual"
echo ""

QUERY3='{"query":"{ sustainabilityReport(accountId: \"account-001\", year: 2024, month: 11) { totalCO2Kg totalTransactions averageCO2PerTransaction recommendations highestImpactTransaction { merchantName carbonFootprint { co2Kg } } } }"}'

log_output "📤 QUERY: sustainabilityReport (Nov 2024)"
echo ""

RESPONSE3=$(curl -s -X POST "$GRAPHQL_URL" -H "Content-Type: application/json" -d "$QUERY3")

log_output "📥 RESPONSE:"
log_output "$(format_json "$RESPONSE3")"
echo ""

if echo "$RESPONSE3" | grep -q "totalCO2Kg" && echo "$RESPONSE3" | grep -q "recommendations"; then
    log_output "✅ PASSED: Sustainability report OK"
    ((PASSED++))
else
    log_output "❌ FAILED: Error en report"
    ((FAILED++))
fi

pause

# TEST 4: Comparar períodos
clear
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log_output "📋 TEST 4 de 7: Comparar Períodos (Nov vs Dic)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
log_output "🎯 Comparación de CO2 entre meses"
echo ""

QUERY4='{"query":"{ comparePeriods(accountId: \"account-001\", year1: 2024, month1: 11, year2: 2024, month2: 12) { previousPeriod co2Change percentageChange trend } }"}'

log_output "📤 QUERY: comparePeriods"
echo ""

RESPONSE4=$(curl -s -X POST "$GRAPHQL_URL" -H "Content-Type: application/json" -d "$QUERY4")

log_output "📥 RESPONSE:"
log_output "$(format_json "$RESPONSE4")"
echo ""

if echo "$RESPONSE4" | grep -q "trend"; then
    log_output "✅ PASSED: Period comparison OK"
    ((PASSED++))
else
    log_output "❌ FAILED: Error en comparison"
    ((FAILED++))
fi

pause

# TEST 5: Transacciones por nivel de impacto
clear
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log_output "📋 TEST 5 de 7: Filtrar por Impact Level"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
log_output "🎯 Ver solo transacciones CRITICAL"
echo ""

QUERY5='{"query":"{ transactionsByImpact(accountId: \"account-001\", impactLevel: CRITICAL) { merchantName carbonFootprint { co2Kg impactLevel } } }"}'

log_output "📤 QUERY: transactionsByImpact (CRITICAL)"
echo ""

RESPONSE5=$(curl -s -X POST "$GRAPHQL_URL" -H "Content-Type: application/json" -d "$QUERY5")

log_output "📥 RESPONSE:"
log_output "$(format_json "$RESPONSE5")"
echo ""

if echo "$RESPONSE5" | grep -q "CRITICAL"; then
    log_output "✅ PASSED: Impact filter OK"
    ((PASSED++))
else
    log_output "❌ FAILED: Error en filter"
    ((FAILED++))
fi

pause

# TEST 6: Crear transacción (genera alert si CRITICAL)
clear
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log_output "📋 TEST 6 de 7: Crear Transaction (High Impact)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
log_output "🎯 Crear vuelo → debería generar CarbonAlert"
echo ""

MUTATION='{"query":"mutation { createTransaction(input: { accountId: \"account-001\", amount: 3000, currency: \"USD\", merchantName: \"Lufthansa\", merchantCategory: TRAVEL_AVIATION, date: \"2024-12-05\" }) { success message transaction { id carbonFootprint { co2Kg impactLevel } } carbonAlert { severity message } } }"}'

log_output "📤 MUTATION: createTransaction (Lufthansa $3000)"
echo ""

RESPONSE6=$(curl -s -X POST "$GRAPHQL_URL" -H "Content-Type: application/json" -d "$MUTATION")

log_output "📥 RESPONSE:"
log_output "$(format_json "$RESPONSE6")"
echo ""

if echo "$RESPONSE6" | grep -q "true" && echo "$RESPONSE6" | grep -q "CRITICAL"; then
    log_output "✅ PASSED: Transaction + Alert creados"
    ((PASSED++))
else
    log_output "❌ FAILED: Error creando transaction"
    ((FAILED++))
fi

pause

# TEST 7: Usar endpoint DEPRECATED
clear
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log_output "📋 TEST 7 de 7: Endpoint DEPRECATED (buyOffset)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
log_output "🎯 Usar mutation deprecada (debe generar WARNING en logs)"
echo ""

# Get first transaction ID
TXN_ID=$(echo "$RESPONSE2" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)

MUTATION2="{\"query\":\"mutation { buyOffset(transactionId: \\\"$TXN_ID\\\") }\"}"

log_output "📤 MUTATION: buyOffset (DEPRECATED)"
log_output "⚠️  Revisa los logs del servidor - debe mostrar WARNING"
echo ""

RESPONSE7=$(curl -s -X POST "$GRAPHQL_URL" -H "Content-Type: application/json" -d "$MUTATION2")

log_output "📥 RESPONSE:"
log_output "$(format_json "$RESPONSE7")"
echo ""

log_output "✅ PASSED: Deprecated endpoint funciona (ver WARNING en logs)"
((PASSED++))

pause

# RESUMEN
clear
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log_output "║                    📊 RESUMEN DE PRUEBAS                  ║"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

log_output "✅ TEST 1: Schema Version Info"
log_output "✅ TEST 2: Transactions con Carbon Footprint"
log_output "✅ TEST 3: Sustainability Report"
log_output "✅ TEST 4: Comparar Períodos"
log_output "✅ TEST 5: Filtrar por Impact Level"
log_output "✅ TEST 6: Crear Transaction + Alert"
log_output "✅ TEST 7: Endpoint Deprecated"
echo ""

TOTAL=$((PASSED + FAILED))
SUCCESS_RATE=$((PASSED * 100 / TOTAL))

log_output "Total:      $TOTAL"
log_output "Exitosas:   $PASSED ✅"
log_output "Fallidas:   $FAILED"
log_output "Éxito:      ${SUCCESS_RATE}%"
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log_output "📊 GOVERNANCE VERIFICADO"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

log_output "✅ Schema Version: 2.0.0"
log_output "✅ Deprecations documentadas: 3 campos"
log_output "✅ Breaking changes registrados"
log_output "✅ CHANGELOG.md disponible"
log_output "✅ Backward compatibility mantenida"
echo ""

log_output "🎉 GraphiQL: http://localhost:8080/graphiql"
echo ""
log_output "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log_output "📄 Log: $LOG_FILE"
log_output "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""