package com.ims.ims.exception;

/**
 * استثناء عندما تكون البيانات المدخلة غير صحيحة
 * HTTP Status: 400 BAD_REQUEST
 * 
 * الاستخدام:
 * throw new InvalidDataException("سعر المنتج يجب أن يكون موجباً");
 * throw new InvalidDataException("كلمة المرور ضعيفة جداً");
 * 
 * أمثلة:
 * - سعر سالب
 * - كمية سالبة
 * - بريد إلكتروني غير صحيح
 * - كلمة مرور فارغة
 */
public class InvalidDataException extends RuntimeException {
    
    private String fieldName;
    
    /**
     * Constructor بسيط برسالة فقط
     * @param message رسالة الخطأ
     */
    public InvalidDataException(String message) {
        super(message);
    }
    
    /**
     * Constructor مع رسالة والسبب الأصلي
     * @param message رسالة الخطأ
     * @param cause السبب الأصلي
     */
    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructor مفصل
     * @param fieldName اسم الحقل الذي فيه خطأ
     * @param message رسالة تفصيلية
     */
    public InvalidDataException(String fieldName, String message) {
        super(String.format("الحقل '%s' غير صحيح: %s", fieldName, message));
        this.fieldName = fieldName;
    }
    
    public String getFieldName() {
        return fieldName;
    }
}