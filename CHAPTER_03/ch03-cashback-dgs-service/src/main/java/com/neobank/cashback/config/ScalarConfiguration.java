package com.neobank.cashback.config;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsRuntimeWiring;
import graphql.scalars.ExtendedScalars;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.RuntimeWiring;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Configuración de Custom Scalars para Netflix DGS.
 * 
 * 🎓 SECCIÓN 3.2: CONFIGURACIÓN DEL PROYECTO
 * 
 * ¿QUÉ SON LOS SCALARS?
 * Los scalars son los tipos primitivos de GraphQL. Por defecto existen:
 * - Int, Float, String, Boolean, ID
 * 
 * Para tipos más específicos, creamos Custom Scalars.
 * 
 * 📦 SCALARS CONFIGURADOS:
 * ┌────────────┬─────────────────┬──────────────────────────────────┐
 * │ Scalar     │ Java Type       │ Uso                              │
 * ├────────────┼─────────────────┼──────────────────────────────────┤
 * │ Money      │ BigDecimal      │ Montos monetarios ($45.50)       │
 * │ Date       │ LocalDate       │ Fechas (2024-01-15)              │
 * │ DateTime   │ LocalDateTime   │ Fechas con hora (ISO-8601)       │
 * └────────────┴─────────────────┴──────────────────────────────────┘
 * 
 * 🎓 DIFERENCIA CON SPRING GRAPHQL:
 * 
 * En Spring GraphQL (Cap. 2):
 * ```java
 * @Bean
 * public RuntimeWiringConfigurer runtimeWiringConfigurer() {
 *     return wiringBuilder -> wiringBuilder.scalar(...);
 * }
 * ```
 * 
 * En DGS (Cap. 3):
 * ```java
 * @DgsRuntimeWiring
 * public RuntimeWiring.Builder addScalars(RuntimeWiring.Builder builder) {
 *     return builder.scalar(...);
 * }
 * ```
 * 
 * 🔑 COERCING:
 * Cada scalar necesita un Coercing con 3 métodos:
 * - serialize():      Java → GraphQL (respuestas al cliente)
 * - parseValue():     JSON variable → Java (variables en request)
 * - parseLiteral():   Inline literal → Java (valores hardcoded en query)
 * 
 * @see cashback-service.graphqls (declaración de scalars)
 */
@DgsComponent
public class ScalarConfiguration {
    
    /**
     * Registra los custom scalars en el runtime de DGS.
     * 
     * 🎓 ¿CÓMO FUNCIONA?
     * 1. DGS carga el schema (cashback-service.graphqls)
     * 2. Encuentra declaraciones: scalar Money, scalar DateTime
     * 3. Busca la implementación en @DgsRuntimeWiring
     * 4. Asocia cada nombre con su Coercing
     * 
     * @param builder RuntimeWiring builder proporcionado por DGS
     * @return Builder con los scalars registrados
     */
    @DgsRuntimeWiring
    public RuntimeWiring.Builder addScalars(RuntimeWiring.Builder builder) {
        return builder
                // ─────────────────────────────────────────────────────────
                // MONEY SCALAR
                // Usa BigDecimal para precisión monetaria (evitar errores de float)
                // Ejemplo: "45.50" → BigDecimal(45.50)
                // ─────────────────────────────────────────────────────────
                .scalar(ExtendedScalars.newAliasedScalar("Money")
                        .aliasedScalar(ExtendedScalars.GraphQLBigDecimal)
                        .build())
                
                // ─────────────────────────────────────────────────────────
                // DATE SCALAR (de graphql-java-extended-scalars)
                // Maneja LocalDate automáticamente
                // Ejemplo: "2024-01-15" → LocalDate.of(2024, 1, 15)
                // ─────────────────────────────────────────────────────────
                .scalar(ExtendedScalars.Date)
                
                // ─────────────────────────────────────────────────────────
                // DATETIME SCALAR (implementación custom)
                // Para LocalDateTime con formato ISO-8601
                // Ejemplo: "2024-01-15T14:30:00" → LocalDateTime
                // ─────────────────────────────────────────────────────────
                .scalar(createLocalDateTimeScalar());
    }
    
    /**
     * Crea un scalar GraphQL personalizado para LocalDateTime.
     * 
     * 🎓 FORMATO ISO-8601:
     * - "2024-01-15T14:30:00" (sin zona horaria)
     * - "2024-01-15T14:30:00.123" (con milisegundos)
     * 
     * 💡 ¿POR QUÉ CUSTOM Y NO ExtendedScalars.DateTime?
     * ExtendedScalars.DateTime usa OffsetDateTime (con zona horaria).
     * Nosotros usamos LocalDateTime (sin zona) que es más simple
     * para este ejemplo.
     * 
     * @return GraphQLScalarType configurado para LocalDateTime
     */
    private GraphQLScalarType createLocalDateTimeScalar() {
        return GraphQLScalarType.newScalar()
                .name("DateTime")
                .description("Fecha y hora en formato ISO-8601 (ej: 2024-01-15T14:30:00)")
                .coercing(new Coercing<LocalDateTime, String>() {
                    
                    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    
                    /**
                     * Java → GraphQL (respuestas).
                     * Convierte LocalDateTime a String ISO-8601.
                     */
                    @Override
                    public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
                        if (dataFetcherResult instanceof LocalDateTime) {
                            return ((LocalDateTime) dataFetcherResult).format(formatter);
                        }
                        throw new CoercingSerializeException(
                                "Expected a LocalDateTime but was: " + dataFetcherResult.getClass()
                        );
                    }
                    
                    /**
                     * JSON variable → Java.
                     * Convierte String de variable JSON a LocalDateTime.
                     * 
                     * Ejemplo en query:
                     * mutation($date: DateTime!) { ... }
                     * Variables: { "date": "2024-01-15T14:30:00" }
                     */
                    @Override
                    public LocalDateTime parseValue(Object input) throws CoercingParseValueException {
                        try {
                            if (input instanceof String) {
                                return LocalDateTime.parse((String) input, formatter);
                            }
                            throw new CoercingParseValueException(
                                    "Expected a String but was: " + input.getClass()
                            );
                        } catch (DateTimeParseException e) {
                            throw new CoercingParseValueException(
                                    "Invalid DateTime format. Expected ISO-8601 (e.g., '2024-01-15T14:30:00')", e
                            );
                        }
                    }
                    
                    /**
                     * Inline literal → Java.
                     * Convierte valor hardcoded en query a LocalDateTime.
                     * 
                     * Ejemplo en query:
                     * { rewardsAfter(date: "2024-01-15T14:30:00") { ... } }
                     */
                    @Override
                    public LocalDateTime parseLiteral(Object input) throws CoercingParseLiteralException {
                        try {
                            if (input instanceof String) {
                                return LocalDateTime.parse((String) input, formatter);
                            }
                            throw new CoercingParseLiteralException(
                                    "Expected a String literal but was: " + input.getClass()
                            );
                        } catch (DateTimeParseException e) {
                            throw new CoercingParseLiteralException(
                                    "Invalid DateTime format. Expected ISO-8601", e
                            );
                        }
                    }
                })
                .build();
    }
}

/*
 * =============================================================================
 * RESUMEN PEDAGÓGICO - SECCIÓN 3.2
 * =============================================================================
 * 
 * 📊 SCALARS REGISTRADOS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  Money     │  BigDecimal      │  ExtendedScalars alias                 │
 * │  Date      │  LocalDate       │  ExtendedScalars.Date                  │
 * │  DateTime  │  LocalDateTime   │  Custom implementation                 │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 🎯 EN EL SCHEMA SE DECLARAN ASÍ:
 * ```graphql
 * scalar Money
 * scalar Date
 * scalar DateTime
 * 
 * type Reward {
 *   amount: Money!
 *   earnedAt: DateTime!
 *   expiresAt: DateTime
 * }
 * ```
 * 
 * 💡 LIBRERÍA USADA:
 * graphql-java-extended-scalars proporciona scalars pre-built:
 * - GraphQLBigDecimal, GraphQLBigInteger
 * - Date, DateTime, Time
 * - Url, Email
 * - Y muchos más...
 * 
 * =============================================================================
 */