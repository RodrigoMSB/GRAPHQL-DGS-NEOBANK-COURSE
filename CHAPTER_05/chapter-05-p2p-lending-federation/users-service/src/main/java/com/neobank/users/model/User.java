package com.neobank.users.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String email;
    private String fullName;
    private UserType userType;
    private LenderProfile lenderProfile;
    private BorrowerProfile borrowerProfile;
    private String createdAt;
    private Double reputation;
    
    public enum UserType {
        LENDER, BORROWER, BOTH
    }
}
