package com.neobank.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CAPÍTULO 1: FUNDAMENTOS DE GRAPHQL Y CONTEXTO CORPORATIVO
 * 
 * Investment Portfolio Tracker - Aplicación principal
 * 
 * Este proyecto demuestra:
 * - Sección 1.1: Comparación REST vs GraphQL
 * - Sección 1.2: Componentes base (Schema, Types, Queries, Mutations, Resolvers)
 * - Sección 1.3: Consultas anidadas y uso de variables
 * - Sección 1.4: Filtros, orden y paginación
 * - Sección 1.5: Tipado, nullabilidad y seguridad básica
 * 
 * @author NeoBank GraphQL Course
 * @version 1.0.0
 */
@SpringBootApplication
public class PortfolioApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortfolioApplication.class, args);
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🚀 Investment Portfolio GraphQL Server Started!");
        System.out.println("=".repeat(80));
        System.out.println("📊 GraphiQL UI: http://localhost:8080/graphiql");
        System.out.println("🔗 GraphQL Endpoint: http://localhost:8080/graphql");
        System.out.println("🌐 REST API: http://localhost:8080/api/rest/");
        System.out.println("=".repeat(80) + "\n");
    }
}
