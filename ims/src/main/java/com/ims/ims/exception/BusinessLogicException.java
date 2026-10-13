package com.ims.ims.exception;

/**
 * استثناء عام لأي خطأ في منطق الأعمال
 * HTTP Status: 422 UNPROCESSABLE_ENTITY
 * 
 * الاستخدام:
 * throw new BusinessLogicException("لا يمكن حذف هذا السجل لأنه مرتبط ببيانات أخرى");
 * throw new BusinessLogicException("العملية تتعارض مع قواعد النظام");
 * 
 * أمثلة:
 * - محاولة تحويل مبلغ أكثر من الرصيد
 * - محاولة حذف بيانات مرتبطة
 * - محاولة تنفيذ عملية تتعارض مع الحالة الحالية
 */
public class BusinessLogicException extends RuntimeException {
    
    private String businessRule;
    
    /**
     * Constructor بسيط برسالة فقط
     * @param message رسالة الخطأ
     */
    public BusinessLogicException(String message) {
        super(message);
    }
    
    /**
     * Constructor مع رسالة والسبب الأصلي
     * @param message رسالة الخطأ
     * @param cause السبب الأصلي
     */
    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructor مع رسالة وقاعدة الأعمال المنتهكة
     * @param message رسالة الخطأ
     * @param businessRule القاعدة المنتهكة
     */
    public BusinessLogicException(String message, String businessRule) {
        super(String.format("%s (القاعدة المنتهكة: %s)", message, businessRule));
        this.businessRule = businessRule;
    }
    
    public String getBusinessRule() {
        return businessRule;
    }
}
 