package com.neobank.cashback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CAPÍTULO 2: DISEÑO DE SCHEMAS GRAPHQL
 * 
 * Cashback Rewards Program - Aplicación principal
 * 
 * 🎯 PROPÓSITO:
 * Este proyecto demuestra las mejores prácticas de diseño de schemas GraphQL
 * mediante un sistema de recompensas de cashback para un neobank.
 * 
 * 📚 SECCIONES QUE CUBRE:
 * - Sección 2.1: Diseño de schemas orientado a dominio
 * - Sección 2.2: Object types vs Input types
 * - Sección 2.3: Queries y Mutations complejas
 * - Sección 2.4: Custom Scalars (DateTime, Money, Email, Percentage)
 * - Sección 2.5: Documentación y deprecación de schemas
 * 
 * 🏗️ ARQUITECTURA DEL DOMINIO:
 * ```
 * User (1) ──────┬────── (N) Transaction
 *                │              │
 *                │              │ (1:1)
 *                │              ▼
 *                └────── (N) Reward
 * ```
 * 
 * 📦 ESTRUCTURA DE PAQUETES:
 * - model/          → Entidades de dominio (User, Transaction, Reward)
 * - model/input/    → Input types para mutations
 * - model/response/ → Response types para mutations
 * - graphql/        → Resolvers (Query y Mutation)
 * - service/        → Lógica de negocio
 * - config/         → Configuración de scalars custom
 * 
 * 🔗 ENDPOINTS:
 * - GraphiQL: http://localhost:8080/graphiql
 * - GraphQL:  http://localhost:8080/graphql
 * 
 * @author NeoBank GraphQL Course
 * @version 2.0.0
 */
@SpringBootApplication
public class CashbackRewardsApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CashbackRewardsApplication.class, args);
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🎁 Cashback Rewards GraphQL Server Started!");
        System.out.println("=".repeat(70));
        System.out.println("📊 GraphiQL UI:      http://localhost:8080/graphiql");
        System.out.println("🔗 GraphQL Endpoint: http://localhost:8080/graphql");
        System.out.println("=".repeat(70) + "\n");
    }
}