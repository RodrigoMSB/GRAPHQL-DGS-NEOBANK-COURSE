package com.neobank.cashback.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Usuario del sistema NeoBank con su información de rewards.
 * 
 * En un sistema real, esta entidad vendría de otro microservicio (User Service).
 * Aquí la simplificamos para el ejemplo.
 */
public class User {
    
    private String id;
    private String email;
    private String fullName;
    private RewardTier tier;
    private BigDecimal totalCashbackEarned;
    private BigDecimal availableCashback;
    private LocalDateTime createdAt;
    
    // Constructor vacío para DGS
    public User() {
    }
    
    // Constructor completo
    public User(String id, String email, String fullName, RewardTier tier, 
                BigDecimal totalCashbackEarned, BigDecimal availableCashback, 
                LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.tier = tier;
        this.totalCashbackEarned = totalCashbackEarned;
        this.availableCashback = availableCashback;
        this.createdAt = createdAt;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public RewardTier getTier() {
        return tier;
    }
    
    public void setTier(RewardTier tier) {
        this.tier = tier;
    }
    
    public BigDecimal getTotalCashbackEarned() {
        return totalCashbackEarned;
    }
    
    public void setTotalCashbackEarned(BigDecimal totalCashbackEarned) {
        this.totalCashbackEarned = totalCashbackEarned;
    }
    
    public BigDecimal getAvailableCashback() {
        return availableCashback;
    }
    
    public void setAvailableCashback(BigDecimal availableCashback) {
        this.availableCashback = availableCashback;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
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
                ", availableCashback=" + availableCashback +
                '}';
    }
}
