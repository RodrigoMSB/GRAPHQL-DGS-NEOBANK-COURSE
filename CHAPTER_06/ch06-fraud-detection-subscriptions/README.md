# Chapter 06: Real-time Fraud Detection con GraphQL Subscriptions

## 🎯 Objetivo del Capítulo

Implementar un sistema de **detección de fraude en tiempo real** utilizando **GraphQL Subscriptions** con WebSockets, permitiendo notificaciones instantáneas sobre transacciones sospechosas sin necesidad de polling.

---

## 🏗️ Arquitectura del Sistema

```
┌─────────────────┐
│   Cliente Web   │
│   (GraphiQL)    │
└────────┬────────┘
         │
    WebSocket (bidireccional)
         │
┌────────▼────────────────────┐
│   GraphQL Server (DGS)      │
│   Puerto 8080               │
├─────────────────────────────┤
│  @DgsSubscription           │
│  - fraudAlertDetected       │
│  - transactionStatusChanged │
└────────┬────────────────────┘
         │
┌────────▼────────────────────┐
│  FraudAlertPublisher        │
│  (Reactive Flux)            │
│  Sinks.Many (multicast)     │
└────────┬────────────────────┘
         │
┌────────▼────────────────────┐
│  FraudDetectionService      │
│  - 6 reglas de detección    │
│  - Risk scoring             │
└─────────────────────────────┘
```

---

## 📊 Conceptos Clave Implementados

### 1. GraphQL Subscriptions
- **WebSocket bidireccional** para comunicación en tiempo real
- **Publisher/Subscriber pattern** con Project Reactor
- **Filtrado por accountId** para multi-tenancy
- **Backpressure handling** con `onBackpressureBuffer`

### 2. Sistema de Detección de Fraude

#### 6 Reglas Implementadas:

| # | Regla | Puntos | Descripción |
|---|-------|--------|-------------|
| 1 | **Monto Inusual** | +30 | Transacción > 3x promedio histórico |
| 2 | **Ubicación Sospechosa** | +40 | Países de alto riesgo (Nigeria, Rusia, etc.) |
| 3 | **Categoría de Riesgo** | +25 | Gambling, Cryptocurrency, Wire Transfer |
| 4 | **Velocity Check** | +20 | >3 transacciones en últimos 5 minutos |
| 5 | **Hora Inusual** | +15 | Transacciones entre 3 AM - 5 AM |
| 6 | **Monto Redondo** | +10 | Múltiplos exactos de $1,000 ≥ $5,000 |

### 3. Risk Scoring

```
Score    Risk Level    Acción Recomendada
------   -----------   --------------------------------------------------
0-24     LOW           Monitor closely for follow-up transactions
25-49    MEDIUM        Flag for manual review within 24 hours
50-79    HIGH          Require additional verification (2FA/OTP)
80+      CRITICAL      BLOCK transaction immediately and contact customer
```

---

## 🚀 Ejecución del Proyecto

### Requisitos Previos
- Java 17+
- Maven 3.8+
- Puerto 8080 disponible

### Opción 1: Maven (Línea de comandos)
```bash
cd ch06-fraud-detection-subscriptions
./mvnw clean spring-boot:run
```

### Opción 2: Spring Tool Suite / IntelliJ
1. Importar como **Maven Project**
2. Run As → **Spring Boot App**
3. Esperar a que inicie (ver logs)

### Verificar que arrancó
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{__typename}"}'
```

---

## 🧪 Pruebas Automatizadas

### Ejecutar test script
```bash
chmod +x test-chapter06.sh
./test-chapter06.sh
```

**Output esperado:** 5/5 pruebas PASADAS (100%)

---

## 🌐 Interfaz GraphiQL

Abrir en navegador: **http://localhost:8080/**

---

## 📝 Ejemplos de Uso

### 1. Query - Ver Transacciones Existentes

```graphql
query VerTransacciones {
  transactions(accountId: "account-001") {
    id
    amount
    merchantName
    status
    riskScore
    timestamp
  }
}
```

**Respuesta esperada:** 3 transacciones de ejemplo (Starbucks, Amazon, Uber)

---

### 2. Mutation - Transacción Normal (Sin Fraude)

```graphql
mutation TransaccionNormal {
  processTransaction(input: {
    accountId: "account-001"
    amount: 50
    currency: "USD"
    merchantName: "Starbucks"
    category: "Food & Drink"
    location: "San Francisco, US"
  }) {
    success
    message
    transaction {
      id
      status
      riskScore
    }
    fraudAlert {
      id
    }
  }
}
```

**Resultado esperado:**
- ✅ `status: APPROVED`
- ✅ `riskScore: 0.0`
- ✅ `fraudAlert: null`

---

### 3. 🔥 Subscription - Alertas en Tiempo Real

**Paso 1:** Abrir pestaña en GraphiQL y subscribirse:

```graphql
subscription AlertasFraude {
  fraudAlertDetected(accountId: "account-001") {
    id
    riskLevel
    reasons
    recommendedAction
    transaction {
      id
      amount
      merchantName
      location
    }
    detectedAt
  }
}
```

**Paso 2:** En **otra pestaña**, ejecutar mutation de fraude:

```graphql
mutation FraudeCritical {
  processTransaction(input: {
    accountId: "account-001"
    amount: 8000
    currency: "USD"
    merchantName: "Online Casino"
    category: "Gambling"
    location: "Lagos, Nigeria"
  }) {
    success
    fraudAlert {
      id
      riskLevel
    }
  }
}
```

**Resultado:** La subscription se dispara automáticamente **sin refrescar** 🔥

---

## 📐 Estructura del Código

### Models
- **Transaction**: Transacción bancaria con risk score
- **FraudAlert**: Alerta con razones y nivel de riesgo
- **RiskLevel**: Enum (LOW, MEDIUM, HIGH, CRITICAL)

### Services
- **TransactionService**: CRUD + cálculo de promedios
- **FraudDetectionService**: Motor con 6 reglas

### Publisher
- **FraudAlertPublisher**: Gestiona Flux reactivos
  - `Sinks.Many` para broadcast
  - Filtrado por accountId

### Resolvers
- **TransactionResolver**: Queries + Mutations
- **FraudSubscriptionResolver**: @DgsSubscription

---

## 🔍 Debugging

```bash
tail -f logs/spring.log | grep "fraud"
```

**Logs importantes:**
```
[INFO] New subscription created for fraud alerts
[WARN] FRAUD DETECTED: alert-abc123 - Risk Score: 95.0
[INFO] Publishing fraud alert
[DEBUG] Emitting fraud alert to subscriber
```

---

## 🎓 Conceptos Pedagógicos

### Push vs Pull

| Aspecto | Polling | Subscriptions |
|---------|---------|--------------|
| Latencia | Alta | Baja |
| Carga servidor | Alta | Baja |
| Ancho de banda | Alto | Bajo |
| Experiencia | Delayed | Real-time |

### Reactive Streams

- **Flux**: Publisher 0..N elementos
- **Operadores**: filter, map, doOnNext
- **Backpressure**: Cliente lento no bloquea servidor

---

## 🏆 Resultado Esperado

```
Total de Pruebas:     5
Pruebas Exitosas:     5 ✅
Tasa de Éxito:        100%
```

---

**NeoBank - Real-time Fraud Detection**  
*Chapter 06 - GraphQL Subscriptions*