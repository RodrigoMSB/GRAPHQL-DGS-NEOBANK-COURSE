package com.neobank.cashback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CHAPTER 03: Cashback Service with Netflix DGS Framework
 * 
 * Aplicación principal del servicio de recompensas cashback.
 * 
 * Netflix DGS (Domain Graph Service) es el framework oficial de Netflix
 * para construir servidores GraphQL sobre Spring Boot.
 * 
 * Ventajas de DGS:
 * - Integración nativa con Spring Boot
 * - Code generation automático desde schema.graphqls
 * - Soporte para DataLoader (batch loading)
 * - Testing utilities incluidas
 * - Anotaciones simples (@DgsQuery, @DgsMutation, @DgsData)
 * 
 * Al iniciar, DGS:
 * 1. Escanea el classpath buscando archivos .graphqls
 * 2. Registra todos los @DgsComponent como resolvers
 * 3. Expone el endpoint /graphql
 * 4. Habilita GraphiQL en /graphiql (si está configurado)
 */
@SpringBootApplication
public class CashbackServiceDgsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CashbackServiceDgsApplication.class, args);
        
        System.out.println("========================================");
        System.out.println("🚀 CASHBACK SERVICE DGS - STARTED");
        System.out.println("========================================");
        System.out.println("GraphQL Endpoint: http://localhost:8080/graphql");
        System.out.println("GraphiQL UI:      http://localhost:8080/graphiql");
        System.out.println("========================================");
    }
}
