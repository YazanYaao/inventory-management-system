package com.ims.ims.model;

/**
 * تعريف الأدوار المختلفة في النظام
 * 
 * كل مستخدم يجب أن يكون له دور واحد
 */
public enum Role {
    ADMIN("مسؤول النظام"),           // صلاحيات كاملة
    STAFF("موظف");                   // صلاحيات محدودة
    
    private final String displayName;
    
    Role(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}