package com.neobank.cashback.model.response;

import com.neobank.cashback.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Boolean success;
    private String message;
    private Transaction transaction;
}
