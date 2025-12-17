package com.neobank.savings;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CAPÍTULO 4: SMART SAVINGS GOALS CON PERSISTENCIA JPA + POSTGRESQL
 * 
 * Aplicación de Metas de Ahorro Inteligente.
 * 
 * 🎯 PROPÓSITO:
 * Este proyecto demuestra cómo integrar GraphQL con una base de datos real
 * usando Spring Data JPA y PostgreSQL, incluyendo:
 * - Entidades JPA mapeadas a tablas
 * - Repositorios con queries derivadas
 * - Transacciones en mutations
 * - Campos calculados en GraphQL
 * 
 * 📚 SECCIONES QUE CUBRE:
 * - Sección 4.1: Integración de JPA con GraphQL
 * - Sección 4.2: Entidades, repositorios y mapeo
 * - Sección 4.3: Transacciones en mutations (@Transactional)
 * - Sección 4.4: Campos calculados y proyecciones
 * - Sección 4.5: Manejo de errores y validaciones
 * 
 * 🏗️ ARQUITECTURA:
 * ```
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    GraphQL Request                              │
 * └─────────────────────────────────────────────────────────────────┘
 *                              │
 *                              ▼
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    Resolver (DGS)                               │
 * │         SavingsGoalResolver (@DgsComponent)                     │
 * └─────────────────────────────────────────────────────────────────┘
 *                              │
 *                              ▼
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    Service Layer                                │
 * │      SavingsGoalService (@Service, @Transactional)              │
 * └─────────────────────────────────────────────────────────────────┘
 *                              │
 *                              ▼
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    Repository Layer                             │
 * │      SavingsGoalRepository (JpaRepository)                      │
 * └─────────────────────────────────────────────────────────────────┘
 *                              │
 *                              ▼
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    PostgreSQL Database                          │
 * │              Tabla: savings_goals                               │
 * └─────────────────────────────────────────────────────────────────┘
 * ```
 * 
 * 📦 ESTRUCTURA DE PAQUETES:
 * - model/      → Entidades JPA (@Entity)
 * - repository/ → Repositorios Spring Data JPA
 * - service/    → Lógica de negocio con @Transactional
 * - resolver/   → DataFetchers DGS
 * - scalar/     → Custom Scalars (Money)
 * 
 * 🐘 BASE DE DATOS:
 * - PostgreSQL 15 (via Docker)
 * - Tabla: savings_goals
 * - Datos iniciales: data.sql
 * 
 * 🔗 ENDPOINTS:
 * - GraphiQL: http://localhost:8080/graphiql
 * - GraphQL:  http://localhost:8080/graphql
 * 
 * 🐳 INICIAR BASE DE DATOS:
 * ```bash
 * docker-compose up -d
 * ```
 * 
 * @author NeoBank GraphQL Course
 * @version 4.0.0
 */
@SpringBootApplication
public class SavingsApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SavingsApplication.class, args);
        
        System.out.println("\n" + "=".repeat(65));
        System.out.println("💰 SMART SAVINGS GOALS SERVICE - STARTED");
        System.out.println("=".repeat(65));
        System.out.println("📊 GraphQL Endpoint: http://localhost:8080/graphql");
        System.out.println("🎮 GraphiQL UI:      http://localhost:8080/graphiql");
        System.out.println("🐘 PostgreSQL:       localhost:5432/savingsdb");
        System.out.println("=".repeat(65));
        System.out.println("📚 Capítulo 4: Persistencia JPA + Transacciones");
        System.out.println("=".repeat(65) + "\n");
    }
}
