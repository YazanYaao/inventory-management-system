package com.ims.ims.exception;

/**
 * استثناء عندما لا يكون لديك صلاحيات كافية
 * HTTP Status: 403 FORBIDDEN
 * 
 * الاستخدام:
 * throw new AuthorizationException("ليس لديك صلاحيات كافية لحذف هذا المستخدم");
 * throw new AuthorizationException("فقط المسؤول يمكنه تنفيذ هذه العملية");
 * 
 * أمثلة:
 * - المستخدم العادي يحاول حذف جميع البيانات
 * - موظف يحاول تعديل راتب موظف آخر
 * - مستخدم غير مصرح يحاول الوصول لبيانات حساسة
 */
public class AuthorizationException extends RuntimeException {
    
    private String requiredRole;
    private String action;
    
    /**
     * Constructor بسيط برسالة فقط
     * @param message رسالة الخطأ
     */
    public AuthorizationException(String message) {
        super(message);
    }
    
    /**
     * Constructor مع رسالة والسبب الأصلي
     * @param message رسالة الخطأ
     * @param cause السبب الأصلي
     */
    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructor مفصل
     * @param action العملية المطلوبة
     * @param requiredRole الدور المطلوب
     */
    public AuthorizationException(String action, String requiredRole) {
        super(String.format("صلاحيات غير كافية. العملية: %s, الدور المطلوب: %s", action, requiredRole));
        this.action = action;
        this.requiredRole = requiredRole;
    }
    
    public String getRequiredRole() {
        return requiredRole;
    }
    
    public String getAction() {
        return action;
    }
}
 