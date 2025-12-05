# Chapter 05: Apollo Federation con Netflix DGS

## 🎯 Objetivo
Demostrar arquitectura federada con GraphQL usando dos microservicios independientes.

---

## 🏗️ Arquitectura
```
Users Service (8081)          Loans Service (8082)
├── User (@key)               ├── Loan (@key)
├── LenderProfile             └── User @extends
└── BorrowerProfile               ├── loansAsLender
                                  └── loansAsBorrower
```

**Con Apollo Router:** Se unificarían en puerto 8080

---

## 🚀 Ejecutar
```bash
cd ch05-p2p-lending-federation
docker-compose up -d
```

Verifica:
```bash
docker-compose ps
```

---

## 🧪 Queries de Prueba

### Users Service (8081)
```bash
curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ users { id fullName email userType } }"}'
```

**Resultado esperado:** Lista de 5 usuarios

### Loans Service (8082)
```bash
curl -X POST http://localhost:8082/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ loans { id amount status borrower { id } } }"}'
```

**Resultado esperado:** Lista de 5 préstamos con referencias a usuarios

---

## 📐 Conceptos de Federation

### 1. @key (Entity)
**Archivo:** `users-service/src/main/resources/schema/users-schema.graphqls`
```graphql
type User @key(fields: "id") {
  id: ID!
  email: String!
  fullName: String!
}
```

✅ Marca `User` como entidad federada

### 2. @extends (Extend Type)
**Archivo:** `loans-service/src/main/resources/schema/loans-schema.graphqls`
```graphql
type User @key(fields: "id") @extends {
  id: ID! @external
  loansAsLender: [Loan!]!
  loansAsBorrower: [Loan!]!
}
```

✅ Agrega campos a `User` desde otro servicio

### 3. Entity Resolution
**Archivo:** `users-service/src/main/java/.../UserEntityFetcher.java`
```java
@DgsEntityFetcher(name = "User")
public User resolveUser(Map<String, Object> values) {
    String id = (String) values.get("id");
    return usersService.getUserById(id);
}
```

✅ Resuelve entidades por ID

---

## 🔗 En Producción con Apollo Router

Con Apollo Router configurado, ambos servicios se unificarían:
```bash
# Query federada (atraviesa ambos servicios)
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ user(id: \"user-001\") { fullName loansAsLender { amount } } }"}'
```

Apollo Router orquestaría:
1. Consulta `user` en Users Service (8081)
2. Consulta `loansAsLender` en Loans Service (8082)
3. Combina resultados y retorna unified response

---

## 📊 Ventajas de Federation

✅ **Separación de dominios** - Cada equipo maneja su servicio  
✅ **Escalabilidad independiente** - Escala Users o Loans según necesidad  
✅ **Despliegue autónomo** - Deploy sin afectar otros servicios  
✅ **Ownership claro** - Users own User, Loans own Loan  

---

## 🛑 Detener
```bash
docker-compose down
```

