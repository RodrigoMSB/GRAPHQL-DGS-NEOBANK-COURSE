package com.neobank.cashback.config;

import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import graphql.schema.Coercing;
import java.time.LocalDateTime;

/**
 * Configuración de GraphQL con Custom Scalars.
 * 
 * 🎓 SECCIÓN 2.4: CUSTOM SCALARS
 * 
 * ¿QUÉ ES UN SCALAR?
 * Los scalars son los tipos primitivos de GraphQL. Los built-in son:
 * - Int, Float, String, Boolean, ID
 * 
 * ¿POR QUÉ CUSTOM SCALARS?
 * A veces necesitamos tipos más específicos que los built-in:
 * - DateTime → Para fechas y horas (LocalDateTime en Java)
 * - Money → Para montos con precisión decimal
 * - Email → Para emails con validación
 * - Percentage → Para porcentajes
 * 
 * 💡 ANALOGÍA:
 * Los scalars custom son como crear tipos de datos especializados en SQL.
 * En vez de usar VARCHAR para todo, creas un tipo EMAIL que valida formato.
 * 
 * 📦 SCALARS DEFINIDOS EN ESTA CLASE:
 * ┌─────────────┬────────────────────┬─────────────────────────────────┐
 * │ Scalar      │ Java Type          │ Uso                             │
 * ├─────────────┼────────────────────┼─────────────────────────────────┤
 * │ DateTime    │ LocalDateTime      │ Fechas de transacciones         │
 * │ Money       │ BigDecimal         │ Montos monetarios               │
 * │ Percentage  │ BigDecimal         │ Porcentajes de cashback         │
 * │ Email       │ String             │ Emails de usuarios              │
 * └─────────────┴────────────────────┴─────────────────────────────────┘
 * 
 * 🔑 COERCING:
 * Cada scalar necesita un Coercing que define 3 operaciones:
 * - serialize(): Java → GraphQL (respuestas)
 * - parseValue(): GraphQL variable → Java (variables JSON)
 * - parseLiteral(): GraphQL literal → Java (valores inline)
 * 
 * @see schema.graphqls (declaración de scalars)
 */
@Configuration
public class GraphQLConfig {
    
    /**
     * Configura los custom scalars para el schema GraphQL.
     * 
     * 🎓 ¿CÓMO FUNCIONA?
     * 1. Spring GraphQL carga el schema.graphqls
     * 2. Encuentra declaraciones como "scalar DateTime"
     * 3. Busca la implementación en RuntimeWiringConfigurer
     * 4. Asocia el nombre del scalar con su Coercing
     * 
     * @return Configurador con todos los scalars registrados
     */
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                // ─────────────────────────────────────────────────────────
                // DATETIME SCALAR
                // Convierte entre LocalDateTime (Java) y String ISO-8601 (GraphQL)
                // Ejemplo: "2024-01-15T10:30:00"
                // ─────────────────────────────────────────────────────────
                .scalar(GraphQLScalarType.newScalar()
                        .name("DateTime")
                        .description("Fecha y hora en formato ISO-8601")
                        .coercing(new Coercing<LocalDateTime, String>() {
                            @Override
                            public String serialize(Object dataFetcherResult) {
                                // Java → JSON: LocalDateTime → String
                                return dataFetcherResult.toString();
                            }
                            @Override
                            public LocalDateTime parseValue(Object input) {
                                // JSON variable → Java: String → LocalDateTime
                                return LocalDateTime.parse(input.toString());
                            }
                            @Override
                            public LocalDateTime parseLiteral(Object input) {
                                // Inline literal → Java: String → LocalDateTime
                                return LocalDateTime.parse(input.toString());
                            }
                        })
                        .build())
                
                // ─────────────────────────────────────────────────────────
                // MONEY SCALAR
                // Para montos monetarios con precisión decimal
                // Usa BigDecimal para evitar errores de punto flotante
                // ─────────────────────────────────────────────────────────
                .scalar(createMoneyScalar())
                
                // ─────────────────────────────────────────────────────────
                // PERCENTAGE SCALAR
                // Para porcentajes de cashback (1.0 = 1%)
                // ─────────────────────────────────────────────────────────
                .scalar(createPercentageScalar())
                
                // ─────────────────────────────────────────────────────────
                // EMAIL SCALAR
                // Para direcciones de email
                // En producción, añadir validación de formato
                // ─────────────────────────────────────────────────────────
                .scalar(createEmailScalar());
    }
    
    /**
     * Crea el scalar Money para valores monetarios.
     * 
     * 💡 ¿POR QUÉ BIGDECIMAL Y NO DOUBLE?
     * Double tiene problemas de precisión:
     * 0.1 + 0.2 = 0.30000000000000004 (¡incorrecto!)
     * 
     * BigDecimal es exacto para operaciones monetarias.
     */
    private GraphQLScalarType createMoneyScalar() {
        return GraphQLScalarType.newScalar()
                .name("Money")
                .description("Valor monetario con precisión decimal")
                .coercing(ExtendedScalars.GraphQLBigDecimal.getCoercing())
                .build();
    }
    
    /**
     * Crea el scalar Percentage para valores porcentuales.
     * 
     * Convención: 1.0 = 1%, 100.0 = 100%
     */
    private GraphQLScalarType createPercentageScalar() {
        return GraphQLScalarType.newScalar()
                .name("Percentage")
                .description("Porcentaje (1.0 = 1%)")
                .coercing(ExtendedScalars.GraphQLBigDecimal.getCoercing())
                .build();
    }
    
    /**
     * Crea el scalar Email para direcciones de correo.
     * 
     * ⚠️ NOTA: En producción, añadir validación de formato en parseValue().
     * Ejemplo: Verificar regex ^[A-Za-z0-9+_.-]+@(.+)$
     */
    private GraphQLScalarType createEmailScalar() {
        return GraphQLScalarType.newScalar()
                .name("Email")
                .description("Dirección de correo electrónico")
                .coercing(new Coercing<String, String>() {
                    @Override
                    public String serialize(Object dataFetcherResult) {
                        return dataFetcherResult.toString();
                    }
                    @Override
                    public String parseValue(Object input) {
                        // TODO: Añadir validación de formato email
                        return input.toString();
                    }
                    @Override
                    public String parseLiteral(Object input) {
                        return input.toString();
                    }
                })
                .build();
    }
}

/*
 * =============================================================================
 * RESUMEN PEDAGÓGICO - SECCIÓN 2.4
 * =============================================================================
 * 
 * 📊 CUSTOM SCALARS REGISTRADOS:
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  DateTime    │  LocalDateTime  │  Fechas ISO-8601                      │
 * │  Money       │  BigDecimal     │  Montos monetarios precisos           │
 * │  Percentage  │  BigDecimal     │  Porcentajes de cashback              │
 * │  Email       │  String         │  Emails (sin validación en este demo) │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 🎯 EN EL SCHEMA SE DECLARAN ASÍ:
 * ```graphql
 * scalar DateTime
 * scalar Money
 * scalar Percentage
 * scalar Email
 * 
 * type User {
 *   email: Email!
 *   enrolledAt: DateTime!
 * }
 * 
 * type Transaction {
 *   amount: Money!
 *   cashbackPercentage: Percentage!
 * }
 * ```
 * 
 * =============================================================================
 */