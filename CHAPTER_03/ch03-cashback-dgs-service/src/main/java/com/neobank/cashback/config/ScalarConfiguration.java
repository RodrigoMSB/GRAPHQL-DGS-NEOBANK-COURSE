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
 * Configuración de scalars personalizados de GraphQL.
 * 
 * DGS necesita que registremos explícitamente los scalars custom
 * que usamos en el schema (Money, Date, DateTime).
 * 
 * NOTA: Implementamos un scalar DateTime personalizado que maneja LocalDateTime
 * en formato ISO-8601 (ej: "2024-11-16T14:30:00").
 */
@DgsComponent
public class ScalarConfiguration {
    
    @DgsRuntimeWiring
    public RuntimeWiring.Builder addScalars(RuntimeWiring.Builder builder) {
        return builder
            // Money -> BigDecimal (con el NOMBRE del schema)
            .scalar(ExtendedScalars.newAliasedScalar("Money")
                .aliasedScalar(ExtendedScalars.GraphQLBigDecimal)
                .build())
            // Date -> LocalDate
            .scalar(ExtendedScalars.Date)
            // DateTime -> LocalDateTime (custom implementation)
            .scalar(createLocalDateTimeScalar());
    }
    
    /**
     * Crea un scalar GraphQL para LocalDateTime.
     * 
     * Este scalar:
     * - Serializa LocalDateTime a String en formato ISO-8601
     * - Deserializa String ISO-8601 a LocalDateTime
     * - Valida el formato de entrada
     */
    private GraphQLScalarType createLocalDateTimeScalar() {
        return GraphQLScalarType.newScalar()
            .name("DateTime")
            .description("A custom scalar that handles LocalDateTime in ISO-8601 format")
            .coercing(new Coercing<LocalDateTime, String>() {
                
                private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                
                @Override
                public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
                    if (dataFetcherResult instanceof LocalDateTime) {
                        return ((LocalDateTime) dataFetcherResult).format(formatter);
                    }
                    throw new CoercingSerializeException(
                        "Expected a LocalDateTime object but was: " + dataFetcherResult.getClass()
                    );
                }
                
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
                            "Invalid DateTime format. Expected ISO-8601 (e.g., '2024-11-16T14:30:00')", e
                        );
                    }
                }
                
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
                            "Invalid DateTime format. Expected ISO-8601 (e.g., '2024-11-16T14:30:00')", e
                        );
                    }
                }
            })
            .build();
    }
}