package com.neobank.savings.scalar;

import com.netflix.graphql.dgs.DgsScalar;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

import java.math.BigDecimal;

/**
 * Custom Scalar para valores monetarios.
 * 
 * 🎓 SECCIÓN 4.2: CUSTOM SCALARS CON DGS
 * 
 * Este scalar maneja la conversión entre:
 * - Java: BigDecimal (precisión exacta para dinero)
 * - GraphQL: Float/Number (para el cliente)
 * 
 * 💡 ¿POR QUÉ UN SCALAR PARA DINERO?
 * 
 * GraphQL tiene Float, pero Float tiene problemas de precisión:
 * ```javascript
 * 0.1 + 0.2 = 0.30000000000000004  // ❌ Error!
 * ```
 * 
 * En el servidor usamos BigDecimal para precisión:
 * ```java
 * new BigDecimal("0.1").add(new BigDecimal("0.2"))  // = 0.3 ✅
 * ```
 * 
 * El scalar hace la conversión:
 * - serialize: BigDecimal → Double (para enviar al cliente)
 * - parseValue: String/Number → BigDecimal (de variables JSON)
 * - parseLiteral: GraphQL literal → BigDecimal (de valores inline)
 * 
 * 🎓 @DgsScalar vs RuntimeWiring:
 * 
 * DGS simplifica el registro de scalars:
 * ```java
 * // Con DGS (simple)
 * @DgsScalar(name = "Money")
 * public class MoneyScalar implements Coercing<...> { }
 * 
 * // Sin DGS (más código)
 * @Bean
 * public RuntimeWiringConfigurer configurer() {
 *     return builder -> builder.scalar(
 *         GraphQLScalarType.newScalar()
 *             .name("Money")
 *             .coercing(new MoneyCoercing())
 *             .build()
 *     );
 * }
 * ```
 * 
 * @see savings-schema.graphqls (scalar Money declaration)
 */
@DgsScalar(name = "Money")
public class MoneyScalar implements Coercing<BigDecimal, Double> {
    
    /**
     * Serializa BigDecimal a Double para enviar al cliente.
     * 
     * Dirección: Java → GraphQL Response
     * 
     * 💡 EJEMPLO:
     * ```
     * Entity: targetAmount = BigDecimal("5000.00")
     * JSON Response: "targetAmount": 5000.0
     * ```
     * 
     * @param dataFetcherResult Valor del campo (BigDecimal)
     * @return Double para el JSON de respuesta
     * @throws CoercingSerializeException si no es BigDecimal
     */
    @Override
    public Double serialize(Object dataFetcherResult) throws CoercingSerializeException {
        if (dataFetcherResult instanceof BigDecimal) {
            return ((BigDecimal) dataFetcherResult).doubleValue();
        }
        throw new CoercingSerializeException(
                "Expected BigDecimal but got: " + dataFetcherResult.getClass().getName()
        );
    }
    
    /**
     * Parsea un valor de variable JSON a BigDecimal.
     * 
     * Dirección: GraphQL Variable → Java
     * 
     * 💡 EJEMPLO:
     * ```graphql
     * mutation($amount: Money!) {
     *   deposit(amount: $amount) { ... }
     * }
     * 
     * Variables: { "amount": 500.50 }
     * ```
     * 
     * El valor 500.50 llega como String o Number, y lo convertimos a BigDecimal.
     * 
     * @param input Valor de la variable (String o Number)
     * @return BigDecimal para usar en Java
     * @throws CoercingParseValueException si no se puede convertir
     */
    @Override
    public BigDecimal parseValue(Object input) throws CoercingParseValueException {
        try {
            return new BigDecimal(input.toString());
        } catch (NumberFormatException e) {
            throw new CoercingParseValueException(
                    "Invalid Money value: " + input + ". Expected a number."
            );
        }
    }
    
    /**
     * Parsea un literal inline de GraphQL a BigDecimal.
     * 
     * Dirección: GraphQL Literal → Java
     * 
     * 💡 EJEMPLO:
     * ```graphql
     * mutation {
     *   deposit(amount: 500.50) { ... }  # ← literal inline
     * }
     * ```
     * 
     * El literal puede ser IntValue (500) o FloatValue (500.50).
     * 
     * @param input Valor literal del AST de GraphQL
     * @return BigDecimal para usar en Java
     * @throws CoercingParseLiteralException si no es número
     */
    @Override
    public BigDecimal parseLiteral(Object input) throws CoercingParseLiteralException {
        if (input instanceof IntValue) {
            // Literal entero: 500
            return new BigDecimal(((IntValue) input).getValue());
        } else if (input instanceof FloatValue) {
            // Literal decimal: 500.50
            return ((FloatValue) input).getValue();
        }
        throw new CoercingParseLiteralException(
                "Expected a number literal but got: " + input.getClass().getName()
        );
    }
}

/*
 * =============================================================================
 * RESUMEN PEDAGÓGICO - CUSTOM SCALARS
 * =============================================================================
 * 
 * 📊 FLUJO DE CONVERSIÓN:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  Método          │  Dirección          │  Ejemplo                      │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │  serialize()     │  Java → JSON        │  BigDecimal → Double          │
 * │  parseValue()    │  Variable → Java    │  "500.50" → BigDecimal        │
 * │  parseLiteral()  │  Literal → Java     │  500.50 → BigDecimal          │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 💡 EN EL SCHEMA:
 * ```graphql
 * scalar Money
 * 
 * type SavingsGoal {
 *   targetAmount: Money!
 *   currentAmount: Money!
 * }
 * ```
 * 
 * =============================================================================
 */