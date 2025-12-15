package com.neobank.carbon.governance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SchemaVersionService {
    
    private static final Logger log = LoggerFactory.getLogger(SchemaVersionService.class);
    
    public Map<String, Object> getVersionInfo() {
        Map<String, Object> versionInfo = new HashMap<>();
        
        versionInfo.put("version", "2.0.0");
        versionInfo.put("lastUpdated", "2024-12-05");
        versionInfo.put("deprecations", getDeprecations());
        versionInfo.put("breakingChanges", getBreakingChanges());
        versionInfo.put("changelog", "See CHANGELOG.md for full details");
        
        return versionInfo;
    }
    
    private List<Map<String, String>> getDeprecations() {
        List<Map<String, String>> deprecations = new ArrayList<>();
        
        Map<String, String> dep1 = new HashMap<>();
        dep1.put("field", "Transaction.category");
        dep1.put("reason", "Use merchantCategory enum for type safety");
        dep1.put("removedInVersion", "3.0.0");
        dep1.put("alternative", "merchantCategory");
        deprecations.add(dep1);
        
        Map<String, String> dep2 = new HashMap<>();
        dep2.put("field", "Transaction.hasOffset");
        dep2.put("reason", "Moved to CarbonFootprint type");
        dep2.put("removedInVersion", "3.0.0");
        dep2.put("alternative", "carbonFootprint.offsetPurchased");
        deprecations.add(dep2);
        
        Map<String, String> dep3 = new HashMap<>();
        dep3.put("field", "Mutation.buyOffset");
        dep3.put("reason", "Use purchaseCarbonOffset for detailed response");
        dep3.put("removedInVersion", "3.0.0");
        dep3.put("alternative", "purchaseCarbonOffset");
        deprecations.add(dep3);
        
        return deprecations;
    }
    
    private List<String> getBreakingChanges() {
        return Arrays.asList(
            "v2.0.0: Added MerchantCategory enum (replacing String category)",
            "v2.0.0: Added CarbonBreakdown type",
            "v2.0.0: Added PeriodComparison type",
            "v2.0.0: Added SchemaVersionInfo query"
        );
    }
}
