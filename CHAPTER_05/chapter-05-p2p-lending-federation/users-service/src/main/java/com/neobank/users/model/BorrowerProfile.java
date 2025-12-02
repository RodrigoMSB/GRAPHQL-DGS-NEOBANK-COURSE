package com.neobank.users.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowerProfile {
    private Integer creditScore;
    private Double totalBorrowed;
    private Integer activeLoans;
    private Double defaultRate;
    private Boolean verified;
    private KYCStatus kycStatus;
    
    public enum KYCStatus {
        PENDING, VERIFIED, REJECTED
    }
}
