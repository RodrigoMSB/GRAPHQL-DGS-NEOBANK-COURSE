package com.neobank.cashback.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Usuario del programa de cashback.
 * 
 * SECCIÓN 2.1: Diseño orientado a dominio
 * - Entidad central del sistema
 * - Tiene relación one-to-many con Transaction y Reward
 * - Campos calculados (availableCashback, totalSpent) se resuelven en el resolver
 * 
 * SECCIÓN 2.2: Object type vs Input type
 * - Este es un OUTPUT type (lo que el servidor retorna)
 * - NO se usa directamente para crear usuarios (usaríamos un CreateUserInput)
 */
public class User {
    /**
     * ID único del usuario.
     * En GraphQL: ID! (non-null scalar)
     */
    private String id;
    
    /**
     * Email del usuario (validado).
     * En GraphQL: Email! (custom scalar con validación)
     */
    private String email;
    
    /**
     * Nombre completo.
     * En GraphQL: String!
     */
    private String fullName;
    
    /**
     * Nivel en el programa de cashback.
     * Determina el porcentaje base de recompensas.
     * En GraphQL: CashbackTier! (enum non-null)
     */
    private CashbackTier tier;
    
    /**
     * Fecha de inscripción en el programa.
     * En GraphQL: DateTime! (custom scalar)
     */
    private LocalDateTime enrolledAt;
    
    // ------------------------------------------------------------------------
    // NOTA: Las relaciones (transactions, rewards) NO están aquí como campos
    // En GraphQL, se resuelven dinámicamente en el resolver:
    //
    // @SchemaMapping
    // public List<Transaction> transactions(User user) {
    //     return transactionService.findByUserId(user.getId());
    // }
    //
    // Esto permite:
    // - Lazy loading (solo si el cliente los pide)
    // - Filtrado y paginación
    // - No cargar todo en memoria
    // ------------------------------------------------------------------------
    
    // ------------------------------------------------------------------------
    // CAMPOS CALCULADOS
    // Estos NO están en la base de datos, se calculan en el resolver
    // ------------------------------------------------------------------------
    
    /**
     * Total de cashback disponible para redimir.
     * Se calcula sumando todas las rewards con status AVAILABLE.
     * 
     * En GraphQL schema:
     * availableCashback: Money!
     * 
     * En Java resolver:
     * public Double availableCashback(User user) {
     *     return rewardService.calculateAvailable(user.getId());
     * }
     */
    // NO hay campo aquí - se calcula en resolver
    
    /**
     * Total de cashback ganado históricamente.
     * Suma de todas las rewards creadas (independiente del status).
     */
    // NO hay campo aquí - se calcula en resolver
    
    /**
     * Total gastado en transacciones.
     * Suma de amounts de todas las transactions CONFIRMED.
     */
    // NO hay campo aquí - se calcula en resolver
    
    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================
    
    public User() {
    }
    
    public User(String id, String email, String fullName, CashbackTier tier,
                LocalDateTime enrolledAt) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.tier = tier;
        this.enrolledAt = enrolledAt;
    }
    
    // =========================================================================
    // GETTERS
    // =========================================================================
    
    public String getId() {
        return id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public CashbackTier getTier() {
        return tier;
    }
    
    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }
    
    // =========================================================================
    // SETTERS
    // =========================================================================
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public void setTier(CashbackTier tier) {
        this.tier = tier;
    }
    
    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }
    
    // =========================================================================
    // EQUALS, HASHCODE, TOSTRING
    // =========================================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", tier=" + tier +
                ", enrolledAt=" + enrolledAt +
                '}';
    }
    
    // =========================================================================
    // BUILDER
    // =========================================================================
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String id;
        private String email;
        private String fullName;
        private CashbackTier tier;
        private LocalDateTime enrolledAt;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }
        
        public Builder tier(CashbackTier tier) {
            this.tier = tier;
            return this;
        }
        
        public Builder enrolledAt(LocalDateTime enrolledAt) {
            this.enrolledAt = enrolledAt;
            return this;
        }
        
        public User build() {
            return new User(id, email, fullName, tier, enrolledAt);
        }
    }
}
