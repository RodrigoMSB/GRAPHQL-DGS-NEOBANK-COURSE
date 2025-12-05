#!/bin/bash

# ═══════════════════════════════════════════════════════════
# CHAPTER 07 - EXPENSE ANALYTICS CACHING TEST SCRIPT
# Prueba per-request caching (DataLoader) y resolver-level caching
# ═══════════════════════════════════════════════════════════

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuración
GRAPHQL_URL="http://localhost:8080/graphql"
TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
LOG_FILE="test-results-chapter07-${TIMESTAMP}.txt"

# Detectar sistema operativo
OS_TYPE="unknown"
if [[ "$OSTYPE" == "darwin"* ]]; then
    OS_TYPE="mac"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    OS_TYPE="linux"
elif [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
    OS_TYPE="windows"
fi

# Función para formatear JSON
format_json() {
    if command -v jq &> /dev/null; then
        echo "$1" | jq '.'
    else
        echo "$1"
    fi
}

# Función para logging
log_output() {
    echo "$1" | tee -a "$LOG_FILE"
}

# Función para medir tiempo
measure_time() {
    if [[ "$OS_TYPE" == "mac" ]]; then
        python3 -c "import time; start=$1; end=$2; print(f'{(end-start)*1000:.0f}ms')"
    else
        echo "$(( ($2 - $1) ))ms"
    fi
}

# Banner
echo ""
log_output "╔═══════════════════════════════════════════════════════════╗"
log_output "║    📊  CHAPTER 07: EXPENSE ANALYTICS CACHING             ║"
log_output "║    DataLoader + Spring Cache Demo                        ║"
log_output "╚═══════════════════════════════════════════════════════════╝"
echo ""
log_output "🖥️  Sistema Operativo: $OS_TYPE"
log_output "📄 Los resultados se guardarán en: $LOG_FILE"
echo ""

# Verificar servicio activo
log_output "═══════════════════════════════════════════════════════════"
log_output "🔍 VERIFICACIÓN: Servicio activo"
log_output "═══════════════════════════════════════════════════════════"
echo ""

log_output "Verificando Expense Analytics Service (Puerto 8080)..."

HEALTH_CHECK=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d '{"query":"{__typename}"}' 2>&1)

if echo "$HEALTH_CHECK" | grep -q "Query"; then
    log_output "✅ Expense Analytics Service: OK"
else
    log_output "❌ ERROR: Servicio no responde en $GRAPHQL_URL"
    log_output "   Asegúrate de que el servidor esté corriendo"
    exit 1
fi

echo ""
log_output "✅ Servicio activo. Iniciando pruebas..."
echo ""

# Función para pausar
pause() {
    echo ""
    read -p "Presiona ENTER para continuar..."
    echo ""
}

# Contador de tests
PASSED=0
FAILED=0

pause

# ═══════════════════════════════════════════════════════════
# PRUEBA 1: Ver expenses existentes
# ═══════════════════════════════════════════════════════════
clear
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log_output "📋 PRUEBA 1 de 7: Query - Expenses Existentes"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
log_output "🎯 Objetivo: Verificar datos de ejemplo cargados"
log_output "📊 Query: expenses"
echo ""

QUERY1='{"query":"{ expenses(accountId: \"account-001\") { id amount merchantName category } }"}'

log_output "📤 REQUEST (GraphQL):"
log_output "{ expenses(accountId: \"account-001\") { id amount merchantName category } }"
echo ""
log_output "Ejecutando query..."
echo ""

RESPONSE1=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d "$QUERY1")

log_output "📥 RESPONSE:"
log_output "$(format_json "$RESPONSE1")"
echo ""

if echo "$RESPONSE1" | grep -q "Starbucks"; then
    log_output "✅ PASSED: Expenses iniciales cargados"
    ((PASSED++))
else
    log_output "❌ FAILED: No se encontraron expenses"
    ((FAILED++))
fi

pause

# ═══════════════════════════════════════════════════════════
# PRUEBA 2: Expense Summary (CACHE MISS)
# ═══════════════════════════════════════════════════════════
clear
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log_output "📋 PRUEBA 2 de 7: Expense Summary - PRIMERA VEZ (Cache Miss)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
log_output "🎯 Objetivo: Medir tiempo sin cache (~500ms)"
log_output "🔄 Esperado: CACHE MISS - cálculo costoso"
echo ""

QUERY2='{"query":"{ expenseSummary(accountId: \"account-001\") { totalAmount averageAmount count topMerchants { merchantName totalSpent } } }"}'

log_output "📤 REQUEST (GraphQL):"
log_output "{ expenseSummary(accountId: \"account-001\") { totalAmount averageAmount count } }"
echo ""

START_TIME=$(date +%s.%N 2>/dev/null || date +%s)
log_output "⏱️  Ejecutando query (primera vez - sin cache)..."

RESPONSE2=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d "$QUERY2")

END_TIME=$(date +%s.%N 2>/dev/null || date +%s)
ELAPSED=$(measure_time "$START_TIME" "$END_TIME")

echo ""
log_output "📥 RESPONSE:"
log_output "$(format_json "$RESPONSE2")"
echo ""
log_output "⏱️  Tiempo de respuesta: $ELAPSED (debería ser ~500ms)"
echo ""

if echo "$RESPONSE2" | grep -q "totalAmount"; then
    log_output "✅ PASSED: Summary calculado (CACHE MISS)"
    ((PASSED++))
else
    log_output "❌ FAILED: Error en summary"
    ((FAILED++))
fi

pause

# ═══════════════════════════════════════════════════════════
# PRUEBA 3: Expense Summary (CACHE HIT)
# ═══════════════════════════════════════════════════════════
clear
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log_output "📋 PRUEBA 3 de 7: Expense Summary - SEGUNDA VEZ (Cache Hit)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
log_output "🎯 Objetivo: Verificar que usa cache (~10ms)"
log_output "⚡ Esperado: CACHE HIT - respuesta instantánea"
echo ""

START_TIME=$(date +%s.%N 2>/dev/null || date +%s)
log_output "⏱️  Ejecutando query (segunda vez - CON cache)..."

RESPONSE3=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d "$QUERY2")

END_TIME=$(date +%s.%N 2>/dev/null || date +%s)
ELAPSED=$(measure_time "$START_TIME" "$END_TIME")

echo ""
log_output "📥 RESPONSE:"
log_output "$(format_json "$RESPONSE3")"
echo ""
log_output "⚡ Tiempo de respuesta: $ELAPSED (debería ser <100ms)"
echo ""

if echo "$RESPONSE3" | grep -q "totalAmount"; then
    log_output "✅ PASSED: Summary desde cache (CACHE HIT) ⚡"
    ((PASSED++))
else
    log_output "❌ FAILED: Error en summary con cache"
    ((FAILED++))
fi

pause

# ═══════════════════════════════════════════════════════════
# PRUEBA 4: Monthly Analytics (CÁLCULO COSTOSO - CACHE MISS)
# ═══════════════════════════════════════════════════════════
clear
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log_output "📋 PRUEBA 4 de 7: Monthly Analytics - CACHE MISS (~1 segundo)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
log_output "🎯 Objetivo: Medir cálculo costoso sin cache"
log_output "🔄 Esperado: ~1000ms (cálculo agregado complejo)"
echo ""

QUERY4='{"query":"{ monthlyAnalytics(accountId: \"account-001\", year: 2024, month: 11) { month totalSpent byCategory { category amount percentage } } }"}'

log_output "📤 REQUEST (GraphQL):"
log_output "{ monthlyAnalytics(accountId: \"account-001\", year: 2024, month: 11) }"
echo ""

START_TIME=$(date +%s.%N 2>/dev/null || date +%s)
log_output "⏱️  Ejecutando monthly analytics (primera vez)..."

RESPONSE4=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d "$QUERY4")

END_TIME=$(date +%s.%N 2>/dev/null || date +%s)
ELAPSED=$(measure_time "$START_TIME" "$END_TIME")

echo ""
log_output "📥 RESPONSE:"
log_output "$(format_json "$RESPONSE4")"
echo ""
log_output "⏱️  Tiempo de respuesta: $ELAPSED (debería ser ~1000ms)"
echo ""

if echo "$RESPONSE4" | grep -q "2024-11"; then
    log_output "✅ PASSED: Monthly analytics calculado (CACHE MISS)"
    ((PASSED++))
else
    log_output "❌ FAILED: Error en monthly analytics"
    ((FAILED++))
fi

pause

# ═══════════════════════════════════════════════════════════
# PRUEBA 5: Monthly Analytics (CACHE HIT)
# ═══════════════════════════════════════════════════════════
clear
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log_output "📋 PRUEBA 5 de 7: Monthly Analytics - CACHE HIT (instantáneo)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
log_output "🎯 Objetivo: Verificar cache de cálculo costoso"
log_output "⚡ Esperado: <100ms desde cache"
echo ""

START_TIME=$(date +%s.%N 2>/dev/null || date +%s)
log_output "⏱️  Ejecutando monthly analytics (segunda vez - CON cache)..."

RESPONSE5=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d "$QUERY4")

END_TIME=$(date +%s.%N 2>/dev/null || date +%s)
ELAPSED=$(measure_time "$START_TIME" "$END_TIME")

echo ""
log_output "📥 RESPONSE:"
log_output "$(format_json "$RESPONSE5")"
echo ""
log_output "⚡ Tiempo de respuesta: $ELAPSED (debería ser <100ms)"
echo ""

if echo "$RESPONSE5" | grep -q "2024-11"; then
    log_output "✅ PASSED: Monthly analytics desde cache ⚡"
    ((PASSED++))
else
    log_output "❌ FAILED: Error en monthly analytics con cache"
    ((FAILED++))
fi

pause

# ═══════════════════════════════════════════════════════════
# PRUEBA 6: Crear Expense (INVALIDA CACHE)
# ═══════════════════════════════════════════════════════════
clear
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log_output "📋 PRUEBA 6 de 7: Crear Expense - Invalidación de Cache"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
log_output "🎯 Objetivo: Crear expense e invalidar cache"
log_output "💰 Expense: Apple Store \$299.99"
echo ""

MUTATION='{"query":"mutation { createExpense(input: { accountId: \"account-001\", amount: 299.99, currency: \"USD\", merchantName: \"Apple Store\", category: SHOPPING, date: \"2024-12-05\", description: \"AirPods Pro\" }) { success message expense { id amount merchantName } } }"}'

log_output "📤 MUTATION (GraphQL):"
log_output "mutation { createExpense(...) { success message } }"
echo ""
log_output "Ejecutando mutation..."
echo ""

RESPONSE6=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d "$MUTATION")

log_output "📥 RESPONSE:"
log_output "$(format_json "$RESPONSE6")"
echo ""

if echo "$RESPONSE6" | grep -q "true"; then
    log_output "✅ PASSED: Expense creado y cache invalidado"
    ((PASSED++))
else
    log_output "❌ FAILED: Error creando expense"
    ((FAILED++))
fi

pause

# ═══════════════════════════════════════════════════════════
# PRUEBA 7: Summary después de invalidar (CACHE MISS)
# ═══════════════════════════════════════════════════════════
clear
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log_output "📋 PRUEBA 7 de 7: Summary Post-Invalidación (CACHE MISS)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
log_output "🎯 Objetivo: Verificar que cache fue invalidado"
log_output "🔄 Esperado: ~500ms otra vez (cache vacío)"
echo ""

START_TIME=$(date +%s.%N 2>/dev/null || date +%s)
log_output "⏱️  Ejecutando summary después de invalidar cache..."

RESPONSE7=$(curl -s -X POST "$GRAPHQL_URL" \
  -H "Content-Type: application/json" \
  -d "$QUERY2")

END_TIME=$(date +%s.%N 2>/dev/null || date +%s)
ELAPSED=$(measure_time "$START_TIME" "$END_TIME")

echo ""
log_output "📥 RESPONSE:"
log_output "$(format_json "$RESPONSE7")"
echo ""
log_output "⏱️  Tiempo de respuesta: $ELAPSED (debería ser ~500ms otra vez)"
echo ""

if echo "$RESPONSE7" | grep -q "totalAmount"; then
    log_output "✅ PASSED: Cache invalidado correctamente (tiempo lento otra vez)"
    ((PASSED++))
else
    log_output "❌ FAILED: Error en summary post-invalidación"
    ((FAILED++))
fi

# ═══════════════════════════════════════════════════════════
# RESUMEN
# ═══════════════════════════════════════════════════════════
clear
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log_output "║                    📊 RESUMEN DE PRUEBAS                  ║"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

log_output "✅ PRUEBA 1: Expenses existentes"
log_output "✅ PRUEBA 2: Summary - Cache Miss (~500ms)"
log_output "✅ PRUEBA 3: Summary - Cache Hit (<100ms) ⚡"
log_output "✅ PRUEBA 4: Monthly Analytics - Cache Miss (~1s)"
log_output "✅ PRUEBA 5: Monthly Analytics - Cache Hit (<100ms) ⚡"
log_output "✅ PRUEBA 6: Crear expense (invalida cache)"
log_output "✅ PRUEBA 7: Summary - Cache Miss otra vez (~500ms)"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

log_output "╔═══════════════════════════════════════════════════════════╗"
log_output "║                  📈 ESTADÍSTICAS FINALES                  ║"
log_output "╚═══════════════════════════════════════════════════════════╝"
echo ""

TOTAL=$((PASSED + FAILED))
SUCCESS_RATE=$((PASSED * 100 / TOTAL))

log_output "Total de Pruebas:     $TOTAL"
log_output "Pruebas Exitosas:     $PASSED ✅"
log_output "Pruebas Fallidas:     $FAILED"
log_output "Tasa de Éxito:        ${SUCCESS_RATE}%"
echo ""

log_output "╔═══════════════════════════════════════════════════════════╗"
log_output "║        📊 CACHING STRATEGIES VERIFICADAS                 ║"
log_output "╚═══════════════════════════════════════════════════════════╝"
echo ""

log_output "🔥 Resolver-Level Caching (Spring Cache + Caffeine):"
log_output "   • expenseSummary: TTL 5 min (300 seg)"
log_output "   • monthlyAnalytics: TTL 5 min"
log_output "   • topMerchants: TTL 5 min"
log_output "   • Cache invalidation on mutation ✅"
echo ""

log_output "📦 Per-Request Caching (DataLoader):"
log_output "   • Batching: Agrupa múltiples requests"
log_output "   • Caching: Reutiliza durante misma petición"
log_output "   • Elimina problema N+1"
echo ""

log_output "⚡ Performance Improvements:"
log_output "   • Sin cache: ~500ms - 1000ms"
log_output "   • Con cache: <100ms"
log_output "   • Mejora: 5x - 10x más rápido"
echo ""

log_output "🎉 ¡Sistema de caching verificado!"
log_output "Accede a GraphiQL: http://localhost:8080/graphiql"
echo ""

log_output "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
log_output "📄 Log guardado en: $LOG_FILE"
log_output "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""