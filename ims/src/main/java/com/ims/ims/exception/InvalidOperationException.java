package com.ims.ims.exception;

 
/**
 * استثناء عندما تحاول تنفيذ عملية غير صحيحة أو غير مسموحة
 * HTTP Status: 400 BAD_REQUEST
 * 
 * الاستخدام:
 * throw new InvalidOperationException("لا يمكن حذف فئة تحتوي على منتجات");
 * throw new InvalidOperationException("لا يمكن حذف مورد مستخدم في منتجات");
 * 
 * أمثلة:
 * - حذف فئة تحتوي على منتجات
 * - حذف مورد مستخدم
 * - تعديل معاملة مكتملة
 */
public class InvalidOperationException extends RuntimeException {
    
    private String operation;
    private String reason;
    
    /**
     * Constructor بسيط برسالة فقط
     * @param message رسالة الخطأ
     */
    public InvalidOperationException(String message) {
        super(message);
    }
    
    /**
     * Constructor مع رسالة والسبب الأصلي
     * @param message رسالة الخطأ
     * @param cause السبب الأصلي
     */
    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructor مفصل
     * @param operation اسم العملية (حذف، تعديل...)
     * @param reason السبب الذي تمنع العملية
     */
    public InvalidOperationException(String operation, String reason) {
        super(String.format("لا يمكن تنفيذ %s: %s", operation, reason));
        this.operation = operation;
        this.reason = reason;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public String getReason() {
        return reason;
    }
}