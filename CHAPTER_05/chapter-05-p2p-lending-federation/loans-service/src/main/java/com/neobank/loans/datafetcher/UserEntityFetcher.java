package com.neobank.loans.datafetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsEntityFetcher;

import java.util.HashMap;
import java.util.Map;

/**
 * EntityFetcher para el tipo User en el subgrafo Loans
 * Crea un stub que será completamente resuelto por el subgrafo Users
 */
@DgsComponent
public class UserEntityFetcher {
    
    @DgsEntityFetcher(name = "User")
    public Map<String, Object> resolveUser(Map<String, Object> values) {
        // Retornamos un stub con solo el ID
        // Apollo Router lo completará con datos del subgrafo Users
        Map<String, Object> user = new HashMap<>();
        user.put("id", values.get("id"));
        return user;
    }
}
