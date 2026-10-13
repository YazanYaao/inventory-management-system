package com.ims.ims.model;

/**
 * أنواع المعاملات (العمليات) على المخزون
 */
public enum TransactionType {
    ADDITION("Addition"),        
    WITHDRAWAL("Withdrawal"),    
    RETURN("Return"),            
    ADJUSTMENT("Adjustment"),      
    SALE("Sale"),                
    PURCHASE("Purchase");        
    
    private final String displayName;
    
    TransactionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
