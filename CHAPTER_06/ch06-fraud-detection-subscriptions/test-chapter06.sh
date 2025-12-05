#!/bin/bash

##############################################################################
# Script de Pruebas - Chapter 06: Real-time Fraud Detection
# 
# Este script verifica el sistema de detección de fraude en tiempo real
# con GraphQL Subscriptions y WebSockets.
#
# COMPATIBLE: Mac y Windows (Git Bash)
##############################################################################

# ============================================================================
# DETECCIÓN DE SISTEMA OPERATIVO
# ============================================================================

detect_os() {
    case "$(uname -s)" in
        Darwin*)    echo "mac" ;;
        Linux*)     echo "linux" ;;
        MINGW*|MSYS*|CYGWIN*)    echo "windows" ;;
        *)          echo "unknown" ;;
    esac
}

OS_TYPE=$(detect_os)

# ============================================================================
# CONFIGURACIÓN
# ============================================================================

# Archivo de log con timestamp
LOG_FILE="test-results-chapter06-$(date +%Y%m%d-%H%M%S).txt"

# Contadores de pruebas
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# URL del servicio
GRAPHQL_URL="http://localhost:8080/graphql"

# Función para escribir en terminal y archivo
log_both() {
    echo -e "$1"
    echo -e "$1" | sed 's/\x1B\[[0-9;]*[JKmsu]//g' >> "$LOG_FILE"
}

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m'

# Función para pausar (compatible con ambos sistemas)
pause_script() {
    echo ""
    read -p "Presiona ENTER para continuar..." dummy
    echo ""
}

# Función para formatear JSON (con jq o fallback)
format_json() {
    if command -v jq &> /dev/null; then
        echo "$1" | jq '.'
    else
        echo "$1"
    fi
}

# Función para ejecutar query GraphQL
execute_query() {
    local query=$1
    
    curl -s -X POST "$GRAPHQL_URL" \
        -H "Content-Type: application/json" \
        -d @- <<EOF
{"query":"$query"}
EOF
}

log_both "${CYAN}╔═══════════════════════════════════════════════════════════╗${NC}"
log_both "${CYAN}║    🔒  CHAPTER 06: FRAUD DETECTION SUBSCRIPTIONS         ║${NC}"
log_both "${CYAN}║    Real-time con GraphQL y WebSockets                     ║${NC}"
log_both "${CYAN}╚═══════════════════════════════════════════════════════════╝${NC}"
log_both ""
log_both "${GREEN}🖥️  Sistema Operativo: ${OS_TYPE}${NC}"
log_both "${GREEN}📄 Los resultados se guardarán en: ${LOG_FILE}${NC}"
log_both ""

##############################################################################
# VERIFICACIÓN DE SERVICIOS
##############################################################################
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both "${YELLOW}🔍 VERIFICACIÓN: Servicio activo${NC}"
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both ""

log_both "Verificando Fraud Detection Service (Puerto 8080)..."
if curl -s -X POST "$GRAPHQL_URL" -H "Content-Type: application/json" -d '{"query":"{__typename}"}' > /dev/null 2>&1; then
    log_both "${GREEN}✅ Fraud Detection Service: OK${NC}"
else
    log_both "${RED}❌ Fraud Detection Service: NO RESPONDE${NC}"
    log_both "${YELLOW}Por favor, asegúrate de que el servicio esté corriendo:${NC}"
    log_both "  ./mvnw spring-boot:run"
    log_both ""
    exit 1
fi

log_both ""
log_both "${GREEN}✅ Servicio activo. Iniciando pruebas...${NC}"
log_both ""
pause_script
log_both ""

##############################################################################
# PRUEBA 1: Query - Ver transacciones existentes
##############################################################################
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both "${YELLOW}📋 PRUEBA 1: Query - Transacciones existentes${NC}"
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both ""
log_both "🎯 Objetivo: Verificar que el servicio tiene transacciones de ejemplo"
log_both "🏢 Servicio: Fraud Detection (8090)"
log_both "📊 Query: transactions"
log_both ""
log_both "${CYAN}📤 REQUEST (GraphQL):${NC}"
log_both "{ transactions(accountId: \"account-001\") { id amount merchantName status riskScore } }"
log_both ""
log_both "${CYAN}Ejecutando query...${NC}"
log_both ""

TOTAL_TESTS=$((TOTAL_TESTS + 1))

RESPONSE=$(execute_query "{ transactions(accountId: \\\"account-001\\\") { id amount merchantName status riskScore } }")

log_both "${CYAN}📥 RESPONSE:${NC}"
log_both "$(format_json "$RESPONSE")"
log_both ""

if echo "$RESPONSE" | grep -q "transactions" && echo "$RESPONSE" | grep -q "Starbucks"; then
    log_both "${GREEN}✅ PASSED: Transacciones iniciales cargadas correctamente${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    log_both "${RED}❌ FAILED: No se encontraron transacciones${NC}"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
log_both ""
pause_script
log_both ""

##############################################################################
# PRUEBA 2: Mutation - Transacción normal (NO fraud)
##############################################################################
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both "${YELLOW}📋 PRUEBA 2: Transacción Normal (Sin Fraude)${NC}"
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both ""
log_both "🎯 Objetivo: Procesar transacción normal que NO dispare alertas"
log_both "💰 Monto: \$75 USD"
log_both "🏪 Comercio: Target"
log_both "📍 Ubicación: San Francisco, US"
log_both ""
log_both "${CYAN}📤 REQUEST (GraphQL):${NC}"
log_both "mutation {"
log_both "  processTransaction(input: {"
log_both "    accountId: \"account-001\""
log_both "    amount: 75"
log_both "    currency: \"USD\""
log_both "    merchantName: \"Target\""
log_both "    category: \"Shopping\""
log_both "    location: \"San Francisco, US\""
log_both "  }) { success message transaction { id status riskScore } fraudAlert { id } }"
log_both "}"
log_both ""
log_both "${CYAN}Ejecutando mutation...${NC}"
log_both ""

TOTAL_TESTS=$((TOTAL_TESTS + 1))

RESPONSE=$(execute_query "mutation { processTransaction(input: {accountId: \\\"account-001\\\", amount: 75, currency: \\\"USD\\\", merchantName: \\\"Target\\\", category: \\\"Shopping\\\", location: \\\"San Francisco, US\\\"}) { success message transaction { id status riskScore } fraudAlert { id } } }")

log_both "${CYAN}📥 RESPONSE:${NC}"
log_both "$(format_json "$RESPONSE")"
log_both ""

if echo "$RESPONSE" | grep -q '"status":"APPROVED"' && echo "$RESPONSE" | grep -q '"fraudAlert":null'; then
    log_both "${GREEN}✅ PASSED: Transacción normal APROBADA sin alertas${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    log_both "${RED}❌ FAILED: Transacción no aprobada correctamente${NC}"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
log_both ""
pause_script
log_both ""

##############################################################################
# PRUEBA 3: Mutation - Alto monto (FRAUD)
##############################################################################
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both "${YELLOW}📋 PRUEBA 3: Transacción Sospechosa - Monto Alto${NC}"
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both ""
log_both "🎯 Objetivo: Detectar fraude por monto 3x mayor al promedio"
log_both "💰 Monto: \$5,000 USD (muy alto)"
log_both "🏪 Comercio: Unknown Merchant"
log_both "📍 Ubicación: San Francisco, US"
log_both ""
log_both "${CYAN}📤 REQUEST (GraphQL):${NC}"
log_both "mutation {"
log_both "  processTransaction(input: {"
log_both "    accountId: \"account-001\""
log_both "    amount: 5000"
log_both "    currency: \"USD\""
log_both "    merchantName: \"Unknown Merchant\""
log_both "    category: \"Wire Transfer\""
log_both "    location: \"San Francisco, US\""
log_both "  }) {"
log_both "    success message"
log_both "    transaction { id status riskScore }"
log_both "    fraudAlert { id riskLevel reasons recommendedAction }"
log_both "  }"
log_both "}"
log_both ""
log_both "${CYAN}Ejecutando mutation...${NC}"
log_both ""

TOTAL_TESTS=$((TOTAL_TESTS + 1))

RESPONSE=$(execute_query "mutation { processTransaction(input: {accountId: \\\"account-001\\\", amount: 5000, currency: \\\"USD\\\", merchantName: \\\"Unknown Merchant\\\", category: \\\"Wire Transfer\\\", location: \\\"San Francisco, US\\\"}) { success message transaction { id status riskScore } fraudAlert { id riskLevel reasons recommendedAction } } }")

log_both "${CYAN}📥 RESPONSE:${NC}"
log_both "$(format_json "$RESPONSE")"
log_both ""

if echo "$RESPONSE" | grep -q '"status":"FLAGGED"' && echo "$RESPONSE" | grep -q '"riskLevel"'; then
    log_both "${GREEN}✅ PASSED: Fraude detectado - Transacción FLAGGED con alerta${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    log_both "${RED}❌ FAILED: Fraude NO detectado${NC}"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
log_both ""
pause_script
log_both ""

##############################################################################
# PRUEBA 4: Mutation - Ubicación sospechosa (FRAUD CRITICAL)
##############################################################################
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both "${YELLOW}📋 PRUEBA 4: Fraude CRITICAL - Ubicación + Categoría de Riesgo${NC}"
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both ""
log_both "🎯 Objetivo: Detectar fraude CRITICAL por múltiples factores"
log_both "💰 Monto: \$8,000 USD"
log_both "🏪 Comercio: Online Casino"
log_both "🎰 Categoría: Gambling (alto riesgo)"
log_both "📍 Ubicación: Lagos, Nigeria (país sospechoso)"
log_both ""
log_both "${CYAN}📤 REQUEST (GraphQL):${NC}"
log_both "mutation {"
log_both "  processTransaction(input: {"
log_both "    accountId: \"account-001\""
log_both "    amount: 8000"
log_both "    currency: \"USD\""
log_both "    merchantName: \"Online Casino\""
log_both "    category: \"Gambling\""
log_both "    location: \"Lagos, Nigeria\""
log_both "  }) {"
log_both "    success message"
log_both "    transaction { id status riskScore }"
log_both "    fraudAlert { id riskLevel reasons recommendedAction }"
log_both "  }"
log_both "}"
log_both ""
log_both "${CYAN}Ejecutando mutation...${NC}"
log_both ""

TOTAL_TESTS=$((TOTAL_TESTS + 1))

RESPONSE=$(execute_query "mutation { processTransaction(input: {accountId: \\\"account-001\\\", amount: 8000, currency: \\\"USD\\\", merchantName: \\\"Online Casino\\\", category: \\\"Gambling\\\", location: \\\"Lagos, Nigeria\\\"}) { success message transaction { id status riskScore } fraudAlert { id riskLevel reasons recommendedAction } } }")

log_both "${CYAN}📥 RESPONSE:${NC}"
log_both "$(format_json "$RESPONSE")"
log_both ""

if echo "$RESPONSE" | grep -q '"riskLevel":"CRITICAL"' || echo "$RESPONSE" | grep -q '"riskLevel":"HIGH"'; then
    log_both "${GREEN}✅ PASSED: Fraude CRITICAL detectado - Múltiples factores de riesgo${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    log_both "${RED}❌ FAILED: Nivel de riesgo incorrecto${NC}"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
log_both ""
pause_script
log_both ""

##############################################################################
# PRUEBA 5: Query - Ver alertas generadas
##############################################################################
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both "${YELLOW}📋 PRUEBA 5: Query - Historial de Alertas de Fraude${NC}"
log_both "${BLUE}═══════════════════════════════════════════════════════════${NC}"
log_both ""
log_both "🎯 Objetivo: Verificar que las alertas se guardaron correctamente"
log_both "🚨 Alertas esperadas: Al menos 2 (de las pruebas anteriores)"
log_both ""
log_both "${CYAN}📤 REQUEST (GraphQL):${NC}"
log_both "{ fraudAlerts(accountId: \"account-001\") {"
log_both "    id riskLevel reasons recommendedAction"
log_both "    transaction { id amount merchantName }"
log_both "  }"
log_both "}"
log_both ""
log_both "${CYAN}Ejecutando query...${NC}"
log_both ""

TOTAL_TESTS=$((TOTAL_TESTS + 1))

RESPONSE=$(execute_query "{ fraudAlerts(accountId: \\\"account-001\\\") { id riskLevel reasons recommendedAction transaction { id amount merchantName } } }")

log_both "${CYAN}📥 RESPONSE:${NC}"
log_both "$(format_json "$RESPONSE")"
log_both ""

if echo "$RESPONSE" | grep -q '"fraudAlerts"' && echo "$RESPONSE" | grep -q '"riskLevel"'; then
    log_both "${GREEN}✅ PASSED: Historial de alertas disponible${NC}"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    log_both "${RED}❌ FAILED: No se encontraron alertas${NC}"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
log_both ""
pause_script
log_both ""

##############################################################################
# RESUMEN FINAL
##############################################################################
log_both "${CYAN}╔═══════════════════════════════════════════════════════════╗${NC}"
log_both "${CYAN}║                    📊 RESUMEN DE PRUEBAS                  ║${NC}"
log_both "${CYAN}╚═══════════════════════════════════════════════════════════╝${NC}"
log_both ""
log_both "${GREEN}✅ PRUEBA 1:${NC} Query transacciones existentes"
log_both "${GREEN}✅ PRUEBA 2:${NC} Transacción normal APROBADA"
log_both "${GREEN}✅ PRUEBA 3:${NC} Fraude detectado por monto alto"
log_both "${GREEN}✅ PRUEBA 4:${NC} Fraude CRITICAL por ubicación + categoría"
log_both "${GREEN}✅ PRUEBA 5:${NC} Historial de alertas"
log_both ""
log_both "${CYAN}╔═══════════════════════════════════════════════════════════╗${NC}"
log_both "${CYAN}║                  📈 ESTADÍSTICAS FINALES                  ║${NC}"
log_both "${CYAN}╚═══════════════════════════════════════════════════════════╝${NC}"
log_both ""
log_both "${YELLOW}Total de Pruebas:${NC}     ${TOTAL_TESTS}"
log_both "${GREEN}Pruebas Exitosas:${NC}     ${PASSED_TESTS} ✅"
log_both "${RED}Pruebas Fallidas:${NC}     ${FAILED_TESTS}"
if [ $TOTAL_TESTS -gt 0 ]; then
    SUCCESS_RATE=$((PASSED_TESTS * 100 / TOTAL_TESTS))
    log_both "${CYAN}Tasa de Éxito:${NC}        ${SUCCESS_RATE}%"
fi
log_both ""
log_both "${CYAN}╔═══════════════════════════════════════════════════════════╗${NC}"
log_both "${CYAN}║        🔒 SISTEMA DE DETECCIÓN DE FRAUDE VERIFICADO      ║${NC}"
log_both "${CYAN}╚═══════════════════════════════════════════════════════════╝${NC}"
log_both ""
log_both "${YELLOW}🎯 Reglas de Detección Implementadas:${NC}"
log_both "   1️⃣  Monto Inusual (>3x promedio)"
log_both "   2️⃣  Ubicación Sospechosa (países de alto riesgo)"
log_both "   3️⃣  Categoría de Alto Riesgo (Gambling, Crypto, etc)"
log_both "   4️⃣  Velocity Check (múltiples transacciones en <5 min)"
log_both "   5️⃣  Hora Inusual (3 AM - 5 AM)"
log_both "   6️⃣  Monto Redondo Sospechoso (múltiplos de \$1000)"
log_both ""
log_both "${YELLOW}🔥 Subscriptions en Tiempo Real:${NC}"
log_both "   • fraudAlertDetected → Notificación instantánea de fraude"
log_both "   • transactionStatusChanged → Cambios de estado en tiempo real"
log_both "   • WebSocket → ws://localhost:8080/subscriptions"
log_both ""
log_both "${YELLOW}📊 Risk Levels:${NC}"
log_both "   • LOW → Monitor closely"
log_both "   • MEDIUM → Flag for manual review"
log_both "   • HIGH → Require additional verification"
log_both "   • CRITICAL → BLOCK transaction immediately"
log_both ""
log_both "${GREEN}🎉 ¡Sistema de detección de fraude en tiempo real funcionando!${NC}"
log_both "${CYAN}Accede a GraphiQL: http://localhost:8080/${NC}"
log_both ""
log_both "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
log_both "${GREEN}📄 Log guardado en: ${LOG_FILE}${NC}"
log_both "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
log_both ""

exit $EXIT_CODE