package com.ims.ims.exception;

/**
 * استثناء عندما لا يتم العثور على البيانات المطلوبة
 * HTTP Status: 404 NOT_FOUND
 * 
 * الاستخدام:
 * throw new ResourceNotFoundException("المنتج غير موجود برقم: " + id);
 * throw new ResourceNotFoundException("المستخدم غير موجود: " + username);
 * 
 * أمثلة:
 * - المنتج غير موجود
 * - الفئة غير موجودة
 * - المورد غير موجود
 * - المستخدم غير موجود
 */
public class ResourceNotFoundException extends RuntimeException {
    
    private String resourceName;
    private String fieldName;
    private Object fieldValue;
    
    /**
     * Constructor بسيط برسالة فقط
     * @param message رسالة الخطأ
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructor مع رسالة والسبب الأصلي
     * @param message رسالة الخطأ
     * @param cause السبب الأصلي
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructor مفصل مع بيانات المورد
     * @param resourceName اسم المورد (Product, User, Category...)
     * @param fieldName اسم الحقل (id, username, sku...)
     * @param fieldValue قيمة الحقل
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s غير موجود/موجودة: %s = %s", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
    
    public String getResourceName() {
        return resourceName;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public Object getFieldValue() {
        return fieldValue;
    }
}