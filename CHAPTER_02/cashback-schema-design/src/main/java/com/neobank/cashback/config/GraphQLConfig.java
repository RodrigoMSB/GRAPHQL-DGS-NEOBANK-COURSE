package com.neobank.cashback.config;

import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import graphql.schema.Coercing;
import java.time.LocalDateTime;

@Configuration
public class GraphQLConfig {
    
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
        		.scalar(GraphQLScalarType.newScalar()
        			    .name("DateTime")
        			    .description("DateTime scalar")
        			    .coercing(new Coercing<LocalDateTime, String>() {
        			        @Override
        			        public String serialize(Object dataFetcherResult) {
        			            return dataFetcherResult.toString();
        			        }
        			        @Override
        			        public LocalDateTime parseValue(Object input) {
        			            return LocalDateTime.parse(input.toString());
        			        }
        			        @Override
        			        public LocalDateTime parseLiteral(Object input) {
        			            return LocalDateTime.parse(input.toString());
        			        }
        			    })
        			    .build())
            .scalar(createMoneyScalar())
            .scalar(createPercentageScalar())
            .scalar(createEmailScalar());
    }
    
    private GraphQLScalarType createMoneyScalar() {
        return GraphQLScalarType.newScalar()
            .name("Money")
            .description("Money scalar")
            .coercing(ExtendedScalars.GraphQLBigDecimal.getCoercing())
            .build();
    }
    
    private GraphQLScalarType createPercentageScalar() {
        return GraphQLScalarType.newScalar()
            .name("Percentage")
            .description("Percentage scalar")
            .coercing(ExtendedScalars.GraphQLBigDecimal.getCoercing())
            .build();
    }
    
    private GraphQLScalarType createEmailScalar() {
        return GraphQLScalarType.newScalar()
            .name("Email")
            .description("Email scalar")
            .coercing(new Coercing<String, String>() {
                @Override
                public String serialize(Object dataFetcherResult) {
                    return dataFetcherResult.toString();
                }
                @Override
                public String parseValue(Object input) {
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