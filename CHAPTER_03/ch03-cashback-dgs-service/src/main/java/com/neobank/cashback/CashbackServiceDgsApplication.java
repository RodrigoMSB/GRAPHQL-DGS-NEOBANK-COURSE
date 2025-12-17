package com.neobank.cashback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CAPÍTULO 3: IMPLEMENTACIÓN DE SERVICIO GRAPHQL CON NETFLIX DGS
 * 
 * Cashback Service DGS - Aplicación principal
 * 
 * 🎯 PROPÓSITO:
 * Este proyecto demuestra cómo implementar un servicio GraphQL completo
 * usando Netflix DGS Framework, el framework oficial de Netflix.
 * 
 * 📚 SECCIONES QUE CUBRE:
 * - Sección 3.1: Introducción a Netflix DGS Framework
 * - Sección 3.2: Configuración del proyecto con Spring Boot
 * - Sección 3.3: Implementación de resolvers con @DgsQuery, @DgsMutation, @DgsData
 * - Sección 3.4: Mutations y lógica de negocio integrada
 * - Sección 3.5: Optimización con DataLoader (problema N+1)
 * 
 * 🏗️ ARQUITECTURA DGS:
 * ```
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                      GraphQL Request                           │
 * └─────────────────────────────────────────────────────────────────┘
 *                              │
 *                              ▼
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    DGS Framework                                │
 * │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐ │
 * │  │ DataFetcher │  │ DataLoader  │  │ Scalar Configuration    │ │
 * │  │ @DgsQuery   │  │ BatchLoader │  │ Money, DateTime         │ │
 * │  │ @DgsMutation│  │ N+1 → 1     │  │                         │ │
 * │  └─────────────┘  └─────────────┘  └─────────────────────────┘ │
 * └─────────────────────────────────────────────────────────────────┘
 *                              │
 *                              ▼
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    Service Layer                                │
 * │              CashbackService (lógica de negocio)                │
 * └─────────────────────────────────────────────────────────────────┘
 *                              │
 *                              ▼
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    Repository Layer                             │
 * │         UserRepository, RewardRepository (in-memory)           │
 * └─────────────────────────────────────────────────────────────────┘
 * ```
 * 
 * 📦 ESTRUCTURA DE PAQUETES:
 * - datafetcher/  → Resolvers DGS (@DgsQuery, @DgsMutation, @DgsData)
 * - dataloader/   → DataLoaders para optimización N+1
 * - domain/       → Entidades de dominio
 * - repository/   → Repositorios in-memory (simulan BD)
 * - service/      → Lógica de negocio
 * - config/       → Configuración de scalars custom
 * 
 * 🔗 ENDPOINTS:
 * - GraphiQL: http://localhost:8080/graphiql
 * - GraphQL:  http://localhost:8080/graphql
 * 
 * 🎓 DIFERENCIAS CON SPRING GRAPHQL (Cap. 2):
 * ┌───────────────────┬──────────────────────┬─────────────────────┐
 * │ Aspecto           │ Spring GraphQL       │ Netflix DGS         │
 * ├───────────────────┼──────────────────────┼─────────────────────┤
 * │ Anotación Query   │ @QueryMapping        │ @DgsQuery           │
 * │ Anotación Mutation│ @MutationMapping     │ @DgsMutation        │
 * │ Campo anidado     │ @SchemaMapping       │ @DgsData            │
 * │ Componente        │ @Controller          │ @DgsComponent       │
 * │ DataLoader        │ Manual               │ @DgsDataLoader      │
 * │ Code generation   │ No incluido          │ Incluido            │
 * └───────────────────┴──────────────────────┴─────────────────────┘
 * 
 * @author NeoBank GraphQL Course
 * @version 3.0.0
 */
@SpringBootApplication
public class CashbackServiceDgsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CashbackServiceDgsApplication.class, args);
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🚀 CASHBACK SERVICE DGS - STARTED");
        System.out.println("=".repeat(60));
        System.out.println("📊 GraphQL Endpoint: http://localhost:8080/graphql");
        System.out.println("🎮 GraphiQL UI:      http://localhost:8080/graphiql");
        System.out.println("=".repeat(60));
        System.out.println("📚 Capítulo 3: Netflix DGS Framework + DataLoader");
        System.out.println("=".repeat(60) + "\n");
    }
}