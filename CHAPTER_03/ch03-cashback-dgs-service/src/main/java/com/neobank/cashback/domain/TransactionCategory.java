package com.neobank.cashback.domain;

/**
 * Categorías de transacciones para calcular cashback diferenciado.
 * 
 * Cada categoría puede tener diferentes porcentajes de cashback
 * según la estrategia comercial del banco.
 */
public enum TransactionCategory {
    GROCERIES,        // Supermercados - típicamente alto cashback
    RESTAURANTS,      // Restaurantes - cashback medio-alto
    TRANSPORTATION,   // Uber, metro, gasolina
    ENTERTAINMENT,    // Cine, streaming, conciertos
    SHOPPING,         // Retail general
    HEALTH,           // Farmacias, consultas médicas
    TRAVEL,           // Vuelos, hoteles - alto cashback
    UTILITIES,        // Servicios básicos - bajo cashback
    EDUCATION,        // Cursos, libros
    OTHER             // Categoría por defecto
}
