# TEORIA - Chapter 06: Subscriptions y Tiempo Real en GraphQL

## 📚 Índice

1. Introducción al Tiempo Real
2. GraphQL Subscriptions
3. WebSockets vs HTTP
4. Polling vs Subscriptions (Push vs Pull)
5. Arquitectura de Subscriptions en DGS
6. Publisher/Subscriber Pattern
7. Reactive Streams (Project Reactor)
8. Implementación Práctica
9. Caso de Uso: Detección de Fraude
10. Backpressure
11. Escalabilidad y Producción
12. Antipatrones y Mejores Prácticas

---

## 1. Introducción al Tiempo Real

### ¿Qué significa "Tiempo Real"?

**Tiempo real** = Los datos llegan al cliente **inmediatamente** después de ocurrir en el servidor, sin que el cliente los solicite explícitamente.

### Ejemplos cotidianos:

- 💳 **Banca:** Notificación push cuando usas tu tarjeta
- 📈 **Trading:** Precio de acciones actualizándose cada segundo
- 💬 **WhatsApp:** Mensaje llega sin refrescar
- 🚗 **Uber:** Ubicación del conductor en tiempo real
- 🎮 **Gaming:** Estado del juego sincronizado entre jugadores

### ¿Por qué es crítico en detección de fraude?

```
Transacción fraudulenta de $50,000

CON POLLING (cada 30 seg):
❌ Fraude detectado → Espera 30 seg → Notificación
   Resultado: Dinero ya transferido

CON SUBSCRIPTIONS (push inmediato):
✅ Fraude detectado → Push instantáneo (<100ms) → Bloqueo
   Resultado: Transacción bloqueada a tiempo
```

**Cada segundo cuenta en fraude bancario.**

---

## 2. GraphQL Subscriptions

### Los 3 tipos de operaciones GraphQL

```graphql
type Query {
  # PULL: Cliente solicita datos
  users: [User]
}

type Mutation {
  # REQUEST/RESPONSE: Cliente envía cambio, recibe confirmación
  createUser(input: UserInput): User
}

type Subscription {
  # PUSH: Servidor envía datos cuando ocurre un evento
  userCreated: User
}
```

### Analogía con periódicos

| Operación | Analogía | Flujo |
|-----------|----------|-------|
| **Query** | Ir al kiosco a comprar periódico | Tú vas por él |
| **Mutation** | Enviar carta al editor | Envías, recibes respuesta |
| **Subscription** | Suscripción a domicilio | Te llega automáticamente |

### Sintaxis de Subscription

```graphql
subscription AlertasFraude {
  fraudAlertDetected(accountId: "account-001") {
    id
    riskLevel
    reasons
    transaction {
      amount
      merchantName
    }
  }
}
```

**¿Qué pasa?**
1. Cliente abre conexión WebSocket
2. Servidor mantiene conexión abierta
3. Cuando se detecta fraude → Servidor EMPUJA datos
4. Cliente recibe evento instantáneamente

---

## 3. WebSockets vs HTTP

### HTTP Tradicional (Request/Response)

```
Cliente                    Servidor
  |                           |
  |---- GET /api/data ------->|
  |                           |
  |<--- 200 OK (JSON) --------|
  |                           |
  [Conexión cerrada]

Para nuevo dato → nuevo request completo (headers, handshake, etc)
```

**Overhead por request:**
- Headers HTTP: ~500 bytes
- TCP handshake: 3 paquetes
- TLS handshake: 4 paquetes

### WebSocket (Bidireccional Persistente)

```
Cliente                    Servidor
  |                           |
  |--- HTTP Upgrade --------->|
  |<-- 101 Switching ---------|
  |                           |
  |====== WEBSOCKET ==========|  ← Conexión ABIERTA
  |                           |
  |<----- Mensaje 1 ----------|  ← Servidor PUSH
  |----- Mensaje 2 ---------->|  ← Cliente envía
  |<----- Mensaje 3 ----------|
  |                           |
  [Conexión permanece abierta]
```

**Ventajas:**
- ✅ Overhead mínimo (2 bytes por mensaje)
- ✅ Latencia ultra baja (~10ms vs ~200ms HTTP)
- ✅ Bidireccional nativo
- ✅ Una conexión, múltiples mensajes

### Handshake WebSocket

```http
# 1. Cliente pide upgrade
GET /subscriptions HTTP/1.1
Host: localhost:8080
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==

# 2. Servidor acepta
HTTP/1.1 101 Switching Protocols
Upgrade: websocket
Connection: Upgrade

# Ahora es WebSocket, no HTTP
```

---

## 4. Polling vs Subscriptions (Push vs Pull)

### 4.1 Short Polling

Cliente pregunta cada X segundos: **"¿Hay algo nuevo?"**

```javascript
setInterval(() => {
  fetch('/api/fraudAlerts')
    .then(res => res.json())
    .then(data => updateUI(data));
}, 5000); // Cada 5 segundos
```

**Problemas:**
- ❌ **Latencia:** Hasta 5 segundos para recibir alerta
- ❌ **Carga:** 720 requests/hora aunque no haya cambios
- ❌ **Ancho de banda:** Headers en cada request
- ❌ **Escalabilidad:** 1000 usuarios = 720,000 requests/hora

### 4.2 Long Polling

Cliente pregunta y servidor **espera** hasta tener respuesta:

```javascript
function longPoll() {
  fetch('/api/fraudAlerts/wait') // Servidor espera
    .then(res => res.json())
    .then(data => {
      updateUI(data);
      longPoll(); // Repetir
    });
}
```

**Mejor latencia pero:**
- ❌ Threads del servidor bloqueados esperando
- ❌ Timeouts HTTP (30-120 seg)
- ❌ Overhead de reconectar constantemente

### 4.3 WebSocket Subscriptions (PUSH)

Servidor avisa cuando hay cambios:

```graphql
subscription {
  fraudAlertDetected(accountId: "account-001") {
    id
    riskLevel
  }
}
```

**Ventajas:**
- ✅ **Latencia:** <100ms (casi instantáneo)
- ✅ **Eficiencia:** Una conexión, infinitos eventos
- ✅ **Escalabilidad:** Idle no consume recursos
- ✅ **Bidireccional:** Cliente cancela cuando quiera

### Comparación Cuantitativa

**Escenario:** 1000 usuarios, 1 hora, 10 eventos reales totales

| Método | Requests | Ancho Banda | Latencia |
|--------|----------|-------------|----------|
| Short Polling (5s) | 720,000 | ~360 MB | 2.5 seg |
| Long Polling | ~12,000 | ~6 MB | 5 seg |
| WebSocket | 1,000 | ~50 KB | <100ms |

---

## 5. Arquitectura de Subscriptions en DGS

### Flujo Completo

```
┌─────────────┐
│   Cliente   │  1. Abre WebSocket + Subscribe
└──────┬──────┘
       │
       ▼
┌─────────────────────────────┐
│ Subscription Resolver       │  2. @DgsSubscription
│ @DgsSubscription            │     Retorna Flux<T>
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ Publisher (Sinks.Many)      │  3. Multicast sink
│ - fraudAlertSink            │
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ FraudDetectionService       │  4. Detecta fraude
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ Publisher.emit(alert)       │  5. Emite evento
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ Flux.filter(accountId)      │  6. Filtra
└──────┬──────────────────────┘
       │
       ▼
┌─────────────────────────────┐
│ WebSocket push al cliente   │  7. Push
└─────────────────────────────┘
```

### Componentes Clave

#### 1. Schema

```graphql
type Subscription {
  fraudAlertDetected(accountId: String!): FraudAlert!
}
```

#### 2. Resolver

```java
@DgsComponent
public class FraudSubscriptionResolver {
    
    @DgsSubscription
    public Flux<FraudAlert> fraudAlertDetected(@InputArgument String accountId) {
        return publisher.getFraudAlertFlux(accountId);
    }
}
```

#### 3. Publisher

```java
@Component
public class FraudAlertPublisher {
    
    private final Sinks.Many<FraudAlert> sink = 
        Sinks.many().multicast().onBackpressureBuffer();
    
    public void publish(FraudAlert alert) {
        sink.tryEmitNext(alert);
    }
    
    public Flux<FraudAlert> getFraudAlertFlux(String accountId) {
        return sink.asFlux()
            .filter(a -> a.getAccountId().equals(accountId));
    }
}
```

---

## 6. Publisher/Subscriber Pattern

### Concepto

**Pub/Sub** desacopla emisores de receptores:

```
Publisher (no sabe quién escucha)
    |
    ▼
[Event Bus]
    |
    ├──▶ Subscriber 1
    ├──▶ Subscriber 2
    └──▶ Subscriber 3
```

### Analogía: Radio FM

- **Publisher:** Estación de radio emite señal
- **Event:** Canción
- **Subscribers:** Radios sintonizadas
- **Key:** La estación NO sabe quiénes escuchan

### En nuestro sistema

```
FraudAlertPublisher
    |
    emite FraudAlert
    |
    ├──▶ Cliente Web 1 (Alice)
    ├──▶ Cliente Web 2 (Bob)
    └──▶ App Móvil 3 (Carlos)
```

**Ventajas:**
- ✅ Desacoplamiento
- ✅ Escalabilidad
- ✅ Flexibilidad

---

## 7. Reactive Streams (Project Reactor)

### ¿Qué es Reactive Programming?

Programación basada en **streams de datos asíncronos** y **propagación de cambios**.

### Publisher y Subscriber

```
Publisher<T> ──emite──▶ Subscriber<T>

Publisher: Flux<T> (0..N) o Mono<T> (0..1)
Subscriber: Recibe eventos
```

### Operadores

```java
Flux<FraudAlert> alerts = sink.asFlux()
    .filter(a -> a.getRiskLevel() == CRITICAL)  // Filtrar
    .map(a -> enrich(a))                        // Transformar
    .doOnNext(a -> log.info("Alert: {}", a))    // Side-effect
    .doOnCancel(() -> log.info("Cancelled"));   // Cleanup
```

### Marble Diagrams

**Flux normal:**
```
--1--2--3--4--5-->
```

**Con filter:**
```
--1--2--3--4--5-->
filter(x -> x % 2 == 0)
-----2-----4----->
```

**Con map:**
```
--1--2--3-->
map(x -> x * 10)
--10-20-30->
```

---

## 8. Implementación Práctica

### 8.1 Dependencias

```xml
<dependency>
    <groupId>com.netflix.graphql.dgs</groupId>
    <artifactId>graphql-dgs-subscriptions-websockets-autoconfigure</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

### 8.2 Configuración

```yaml
dgs:
  graphql:
    websocket:
      enabled: true
      path: /subscriptions
```

### 8.3 Publisher

```java
@Component
public class FraudAlertPublisher {
    
    private final Sinks.Many<FraudAlert> sink = 
        Sinks.many().multicast().onBackpressureBuffer();
    
    public void publishFraudAlert(FraudAlert alert) {
        sink.tryEmitNext(alert);
    }
    
    public Flux<FraudAlert> getFraudAlertFlux(String accountId) {
        return sink.asFlux()
            .filter(a -> a.getTransaction()
                          .getAccountId()
                          .equals(accountId))
            .doOnSubscribe(s -> 
                log.info("New subscriber: {}", accountId))
            .doOnCancel(() -> 
                log.info("Cancelled: {}", accountId));
    }
}
```

### 8.4 Resolver

```java
@DgsComponent
public class FraudSubscriptionResolver {
    
    @Autowired
    private FraudAlertPublisher publisher;
    
    @DgsSubscription
    public Flux<FraudAlert> fraudAlertDetected(@InputArgument String accountId) {
        return publisher.getFraudAlertFlux(accountId);
    }
}
```

### 8.5 Trigger (Mutation)

```java
@DgsMutation
public TransactionResponse processTransaction(@InputArgument TransactionInput input) {
    
    Transaction txn = transactionService.create(input);
    FraudAlert alert = fraudDetectionService.analyze(txn);
    
    if (alert != null) {
        fraudAlertPublisher.publishFraudAlert(alert); // ← TRIGGER
    }
    
    return new TransactionResponse(txn, alert);
}
```

---

## 9. Caso de Uso: Detección de Fraude

### Flujo Paso a Paso

#### Paso 1: Cliente se suscribe

```graphql
subscription {
  fraudAlertDetected(accountId: "account-001") {
    id
    riskLevel
  }
}
```

#### Paso 2: Usuario hace transacción

```graphql
mutation {
  processTransaction(input: {
    accountId: "account-001"
    amount: 10000
    location: "Nigeria"
  }) {
    success
  }
}
```

#### Paso 3: Servidor detecta fraude

```java
public FraudAlert analyzeTransaction(Transaction txn) {
    double riskScore = 0.0;
    List<String> reasons = new ArrayList<>();
    
    if (txn.getAmount() > averageAmount * 3) {
        riskScore += 30;
        reasons.add("High amount");
    }
    
    if (txn.getLocation().contains("Nigeria")) {
        riskScore += 40;
        reasons.add("High-risk location");
    }
    
    if (riskScore > 0) {
        return new FraudAlert(txn, reasons, riskScore);
    }
    return null;
}
```

#### Paso 4: Publisher emite

```java
FraudAlert alert = fraudDetectionService.analyzeTransaction(txn);
if (alert != null) {
    fraudAlertPublisher.publishFraudAlert(alert);
}
```

#### Paso 5: Cliente recibe

```json
{
  "data": {
    "fraudAlertDetected": {
      "id": "alert-123",
      "riskLevel": "HIGH"
    }
  }
}
```

**Sin refrescar la página. Automático. Instantáneo.**

---

## 10. Backpressure

### ¿Qué es Backpressure?

Mecanismo para manejar **productores rápidos con consumidores lentos**.

```
Publisher emite 1000 eventos/seg
    ↓
Subscriber procesa 10 eventos/seg

Sin backpressure:
❌ Buffer crece infinitamente
❌ OutOfMemoryError
```

### Estrategias

#### 1. Buffer

```java
Sinks.many().multicast().onBackpressureBuffer(256)
```

#### 2. Drop

```java
Flux.onBackpressureDrop(alert -> 
    log.warn("Dropped: {}", alert.getId())
)
```

#### 3. Latest

```java
Flux.onBackpressureLatest()
```

#### 4. Error

```java
Flux.onBackpressureError()
```

---

## 11. Escalabilidad y Producción

### Problema: Múltiples Instancias

```
Load Balancer
    ├── Server 1 (subscriber de Alice)
    └── Server 2 (procesa mutation)

❌ Evento en Server 2 NO llega a Alice en Server 1
```

### Solución: Redis Pub/Sub

```java
@Component
public class RedisEventBus {
    
    public void publish(FraudAlert alert) {
        redis.convertAndSend("fraud-alerts", alert);
    }
    
    @EventListener
    public void onRedisMessage(FraudAlert alert) {
        localPublisher.emit(alert);
    }
}
```

### Monitoreo

```java
// Métricas
registry.counter("subscriptions.active").increment();
registry.timer("subscriptions.latency").record(duration);

// Grafana Dashboard
- Active subscriptions
- Events emitted/sec
- Latency p50/p95/p99
```

---

## 12. Antipatrones y Mejores Prácticas

### ❌ Antipatrón 1: Subscription en lugar de Query

```graphql
# MAL
subscription {
  allTransactions { id }
}

# BIEN
query {
  transactions { id }
}

subscription {
  transactionCreated { id }
}
```

### ❌ Antipatrón 2: Sin filtrado

```graphql
# MAL - privacy leak
subscription {
  allFraudAlerts { id }
}

# BIEN
subscription {
  fraudAlertDetected(accountId: "user-123") { id }
}
```

### ✅ Mejores Prácticas

#### 1. Timeout

```java
Flux.timeout(Duration.ofSeconds(60))
```

#### 2. Rate Limiting

```java
public boolean allowSubscription(String userId) {
    return cache.get(userId).incrementAndGet() <= 10;
}
```

#### 3. Graceful Degradation

```java
Flux.retry(3)
    .onErrorResume(e -> Flux.empty())
```

#### 4. Logging

```java
.doOnSubscribe(s -> log.info("START"))
.doOnNext(e -> log.info("EVENT: {}", e))
.doOnCancel(() -> log.info("CANCEL"))
```

---

## 📊 Resumen

| Concepto | Explicación |
|----------|-------------|
| **Subscription** | Operación GraphQL para push |
| **WebSocket** | Protocolo bidireccional persistente |
| **Pub/Sub** | Patrón de desacoplamiento |
| **Reactive Streams** | Manejo asíncrono con backpressure |
| **Sinks.Many** | Broadcast reactor |

### Cuándo usar Subscriptions

✅ **SÍ:**
- Notificaciones tiempo real
- Dashboards live
- Juegos multiplayer
- Colaboración

❌ **NO:**
- Datos estáticos
- Archivos grandes
- Datos que cambian lento

---

**NeoBank - Fraud Detection System**  
*Chapter 06 - Real-time con GraphQL Subscriptions* 