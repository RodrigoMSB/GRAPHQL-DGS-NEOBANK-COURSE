package com.neobank.carbon.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ESGScore {
    
    private Integer overall;
    private Integer environmental;
    private Integer social;
    private Integer governance;
    private LocalDateTime lastUpdated;
    private List<String> certifications;
}
