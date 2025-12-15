package com.neobank.carbon.model;

import java.time.LocalDateTime;
import java.util.List;

public class ESGScore {
    
    private Integer overall;
    private Integer environmental;
    private Integer social;
    private Integer governance;
    private LocalDateTime lastUpdated;
    private List<String> certifications;
    
    public ESGScore() {}
    
    public ESGScore(Integer overall, Integer environmental, Integer social, Integer governance,
                    LocalDateTime lastUpdated, List<String> certifications) {
        this.overall = overall;
        this.environmental = environmental;
        this.social = social;
        this.governance = governance;
        this.lastUpdated = lastUpdated;
        this.certifications = certifications;
    }
    
    public Integer getOverall() { return overall; }
    public void setOverall(Integer overall) { this.overall = overall; }
    public Integer getEnvironmental() { return environmental; }
    public void setEnvironmental(Integer environmental) { this.environmental = environmental; }
    public Integer getSocial() { return social; }
    public void setSocial(Integer social) { this.social = social; }
    public Integer getGovernance() { return governance; }
    public void setGovernance(Integer governance) { this.governance = governance; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    public List<String> getCertifications() { return certifications; }
    public void setCertifications(List<String> certifications) { this.certifications = certifications; }
    
    public static Builder builder() { return new Builder(); }
    
    public static class Builder {
        private Integer overall, environmental, social, governance;
        private LocalDateTime lastUpdated;
        private List<String> certifications;
        
        public Builder overall(Integer overall) { this.overall = overall; return this; }
        public Builder environmental(Integer environmental) { this.environmental = environmental; return this; }
        public Builder social(Integer social) { this.social = social; return this; }
        public Builder governance(Integer governance) { this.governance = governance; return this; }
        public Builder lastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; return this; }
        public Builder certifications(List<String> certifications) { this.certifications = certifications; return this; }
        
        public ESGScore build() {
            return new ESGScore(overall, environmental, social, governance, lastUpdated, certifications);
        }
    }
}
