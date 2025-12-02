package com.neobank.users.service;

import com.neobank.users.model.BorrowerProfile;
import com.neobank.users.model.LenderProfile;
import com.neobank.users.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UsersService {
    
    private final Map<String, User> users = new HashMap<>();
    
    public UsersService() {
        initializeData();
    }
    
    private void initializeData() {
        // Lender 1
        users.put("user-001", User.builder()
            .id("user-001")
            .email("alice.lender@neobank.com")
            .fullName("Alice Thompson")
            .userType(User.UserType.LENDER)
            .lenderProfile(LenderProfile.builder()
                .totalLent(150000.0)
                .activeLoans(12)
                .averageReturn(8.5)
                .riskTolerance(LenderProfile.RiskTolerance.MODERATE)
                .verified(true)
                .build())
            .createdAt(LocalDateTime.now().minusYears(2).toString())
            .reputation(4.8)
            .build());
        
        // Lender 2
        users.put("user-002", User.builder()
            .id("user-002")
            .email("bob.investor@neobank.com")
            .fullName("Bob Martinez")
            .userType(User.UserType.LENDER)
            .lenderProfile(LenderProfile.builder()
                .totalLent(250000.0)
                .activeLoans(20)
                .averageReturn(9.2)
                .riskTolerance(LenderProfile.RiskTolerance.AGGRESSIVE)
                .verified(true)
                .build())
            .createdAt(LocalDateTime.now().minusYears(3).toString())
            .reputation(4.9)
            .build());
        
        // Borrower 1
        users.put("user-003", User.builder()
            .id("user-003")
            .email("carlos.borrower@neobank.com")
            .fullName("Carlos Rodriguez")
            .userType(User.UserType.BORROWER)
            .borrowerProfile(BorrowerProfile.builder()
                .creditScore(720)
                .totalBorrowed(50000.0)
                .activeLoans(2)
                .defaultRate(0.0)
                .verified(true)
                .kycStatus(BorrowerProfile.KYCStatus.VERIFIED)
                .build())
            .createdAt(LocalDateTime.now().minusYears(1).toString())
            .reputation(4.7)
            .build());
        
        // Borrower 2
        users.put("user-004", User.builder()
            .id("user-004")
            .email("diana.startup@neobank.com")
            .fullName("Diana Chen")
            .userType(User.UserType.BORROWER)
            .borrowerProfile(BorrowerProfile.builder()
                .creditScore(680)
                .totalBorrowed(30000.0)
                .activeLoans(1)
                .defaultRate(0.0)
                .verified(true)
                .kycStatus(BorrowerProfile.KYCStatus.VERIFIED)
                .build())
            .createdAt(LocalDateTime.now().minusMonths(6).toString())
            .reputation(4.5)
            .build());
        
        // Both (Lender and Borrower)
        users.put("user-005", User.builder()
            .id("user-005")
            .email("emily.hybrid@neobank.com")
            .fullName("Emily Watson")
            .userType(User.UserType.BOTH)
            .lenderProfile(LenderProfile.builder()
                .totalLent(80000.0)
                .activeLoans(8)
                .averageReturn(7.8)
                .riskTolerance(LenderProfile.RiskTolerance.CONSERVATIVE)
                .verified(true)
                .build())
            .borrowerProfile(BorrowerProfile.builder()
                .creditScore(750)
                .totalBorrowed(20000.0)
                .activeLoans(1)
                .defaultRate(0.0)
                .verified(true)
                .kycStatus(BorrowerProfile.KYCStatus.VERIFIED)
                .build())
            .createdAt(LocalDateTime.now().minusYears(1).toString())
            .reputation(4.6)
            .build());
    }
    
    public User getUserById(String id) {
        return users.get(id);
    }
    
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    public List<User> getVerifiedLenders() {
        return users.values().stream()
            .filter(u -> (u.getUserType() == User.UserType.LENDER || u.getUserType() == User.UserType.BOTH))
            .filter(u -> u.getLenderProfile() != null && u.getLenderProfile().getVerified())
            .collect(Collectors.toList());
    }
    
    public List<User> getVerifiedBorrowers() {
        return users.values().stream()
            .filter(u -> (u.getUserType() == User.UserType.BORROWER || u.getUserType() == User.UserType.BOTH))
            .filter(u -> u.getBorrowerProfile() != null && u.getBorrowerProfile().getVerified())
            .collect(Collectors.toList());
    }
    
    public User createUser(String email, String fullName, User.UserType userType) {
        String id = "user-" + String.format("%03d", users.size() + 1);
        User user = User.builder()
            .id(id)
            .email(email)
            .fullName(fullName)
            .userType(userType)
            .createdAt(LocalDateTime.now().toString())
            .reputation(0.0)
            .build();
        
        users.put(id, user);
        return user;
    }
}
