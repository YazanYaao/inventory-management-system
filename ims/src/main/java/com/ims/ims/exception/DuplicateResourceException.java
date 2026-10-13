package com.ims.ims.exception;


 
/**
 * استثناء عندما تحاول إنشاء بيانات مكررة
 * HTTP Status: 409 CONFLICT
 * 
 * الاستخدام:
 * throw new DuplicateResourceException("هناك منتج برمز SKU: P001 بالفعل");
 * throw new DuplicateResourceException("هناك مستخدم باسم: ahmed بالفعل");
 * 
 * أمثلة:
 * - رمز SKU مكرر
 * - اسم مستخدم مكرر
 * - بريد إلكتروني مكرر
 * - اسم فئة مكرر
 */
public class DuplicateResourceException extends RuntimeException {
    
    private String fieldName;
    private Object fieldValue;
    
    /**
     * Constructor بسيط برسالة فقط
     * @param message رسالة الخطأ
     */
    public DuplicateResourceException(String message) {
        super(message);
    }
    
    /**
     * Constructor مع رسالة والسبب الأصلي
     * @param message رسالة الخطأ
     * @param cause السبب الأصلي
     */
    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructor مفصل
     * @param fieldName اسم الحقل (sku, username, email...)
     * @param fieldValue قيمة الحقل
     */
    public DuplicateResourceException(String fieldName, Object fieldValue) {
        super(String.format("القيمة '%s' موجودة بالفعل للحقل: %s", fieldValue, fieldName));
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public Object getFieldValue() {
        return fieldValue;
    }
}