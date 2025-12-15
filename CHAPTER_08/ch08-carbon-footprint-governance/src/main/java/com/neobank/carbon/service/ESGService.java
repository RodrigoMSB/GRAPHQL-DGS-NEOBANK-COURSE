package com.neobank.carbon.service;

import com.neobank.carbon.model.ESGScore;
import com.neobank.carbon.model.MerchantCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ESGService {
    
    private static final Logger log = LoggerFactory.getLogger(ESGService.class);
    
    private final Map<String, ESGScore> esgScores = new HashMap<>();
    
    public ESGService() {
        initializeESGData();
    }
    
    public ESGScore getESGScore(String merchantName, MerchantCategory category) {
        return esgScores.getOrDefault(merchantName, generateDefaultESGScore(category));
    }
    
    private ESGScore generateDefaultESGScore(MerchantCategory category) {
        int baseScore = switch (category) {
            case ENERGY -> 45;
            case TRAVEL_AVIATION -> 50;
            case TRANSPORTATION -> 55;
            case FASHION_RETAIL -> 60;
            case ELECTRONICS -> 65;
            case FOOD_RETAIL -> 70;
            case TRAVEL_HOSPITALITY -> 65;
            case SERVICES -> 75;
            case OTHER -> 60;
        };
        
        Random random = new Random();
        int variation = random.nextInt(11) - 5;
        int overall = Math.max(0, Math.min(100, baseScore + variation));
        
        return ESGScore.builder()
                .overall(overall)
                .environmental(overall + random.nextInt(11) - 5)
                .social(overall + random.nextInt(11) - 5)
                .governance(overall + random.nextInt(11) - 5)
                .lastUpdated(LocalDateTime.now())
                .certifications(new ArrayList<>())
                .build();
    }
    
    private void initializeESGData() {
        esgScores.put("Whole Foods", ESGScore.builder()
                .overall(85).environmental(90).social(82).governance(83)
                .lastUpdated(LocalDateTime.now())
                .certifications(Arrays.asList("B-Corp", "Fair Trade", "Organic Certified"))
                .build());
        
        esgScores.put("Tesla Supercharger", ESGScore.builder()
                .overall(88).environmental(95).social(80).governance(90)
                .lastUpdated(LocalDateTime.now())
                .certifications(Arrays.asList("Carbon Neutral", "Renewable Energy 100%"))
                .build());
        
        esgScores.put("Patagonia", ESGScore.builder()
                .overall(92).environmental(95).social(90).governance(91)
                .lastUpdated(LocalDateTime.now())
                .certifications(Arrays.asList("B-Corp", "Fair Trade", "1% for the Planet"))
                .build());
        
        esgScores.put("United Airlines", ESGScore.builder()
                .overall(55).environmental(45).social(60).governance(60)
                .lastUpdated(LocalDateTime.now())
                .certifications(Arrays.asList("Sustainable Aviation Fuel Initiative"))
                .build());
        
        log.info("Initialized ESG scores for {} merchants", esgScores.size());
    }
}
