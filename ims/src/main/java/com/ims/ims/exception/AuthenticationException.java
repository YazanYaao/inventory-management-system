package com.ims.ims.exception;

/**
 * استثناء عندما يفشل التحقق من الهوية
 * HTTP Status: 401 UNAUTHORIZED
 * 
 * الاستخدام:
 * throw new AuthenticationException("اسم المستخدم أو كلمة المرور غير صحيحة");
 * throw new AuthenticationException("حساب المستخدم معطل");
 * 
 * أمثلة:
 * - اسم مستخدم غير صحيح
 * - كلمة مرور غير صحيحة
 * - حساب معطل
 * - انتهت صلاحية الجلسة
 */
public class AuthenticationException extends RuntimeException {
    
    private String username;
    
    /**
     * Constructor بسيط برسالة فقط
     * @param message رسالة الخطأ
     */
    public AuthenticationException(String message) {
        super(message);
    }
    
    /**
     * Constructor مع رسالة والسبب الأصلي
     * @param message رسالة الخطأ
     * @param cause السبب الأصلي
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructor مع رسالة واسم المستخدم
     * @param username اسم المستخدم الذي فشل
     * @param message رسالة الخطأ
     */
    public AuthenticationException(String username, String message) {
        super(String.format("فشل التحقق من هوية المستخدم '%s': %s", username, message));
        this.username = username;
    }
    
    public String getUsername() {
        return username;
    }
}