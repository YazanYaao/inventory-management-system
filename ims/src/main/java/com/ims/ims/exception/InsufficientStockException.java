package com.ims.ims.exception;


 
/**
 * استثناء عندما لا يكون هناك مخزون كافي
 * HTTP Status: 400 BAD_REQUEST
 * 
 * الاستخدام:
 * throw new InsufficientStockException(
 *     "المخزون غير كافي",
 *     5,  // الكمية المتوفرة
 *     10  // الكمية المطلوبة
 * );
 * 
 * أمثلة:
 * - محاولة بيع كمية أكثر من المتوفر
 * - محاولة شراء كمية أكثر من رصيد الحساب
 */
public class InsufficientStockException extends RuntimeException {
    
    private Integer availableStock;
    private Integer requestedStock;
    private Long productId;
    
    /**
     * Constructor مع رسالة فقط
     * @param message رسالة الخطأ
     */
    public InsufficientStockException(String message) {
        super(message);
    }
    
    /**
     * Constructor مع رسالة والكميات
     * @param message رسالة الخطأ
     * @param availableStock الكمية المتوفرة
     * @param requestedStock الكمية المطلوبة
     */
    public InsufficientStockException(String message, Integer availableStock, Integer requestedStock) {
        super(String.format("%s. المتوفر: %d, المطلوب: %d", message, availableStock, requestedStock));
        this.availableStock = availableStock;
        this.requestedStock = requestedStock;
    }
    
    /**
     * Constructor مع رسالة والكميات ورقم المنتج
     * @param productId رقم المنتج
     * @param availableStock الكمية المتوفرة
     * @param requestedStock الكمية المطلوبة
     */
    public InsufficientStockException(Long productId, Integer availableStock, Integer requestedStock) {
        super(String.format(
            "المخزون غير كافي للمنتج #%d. المتوفر: %d, المطلوب: %d",
            productId, availableStock, requestedStock
        ));
        this.productId = productId;
        this.availableStock = availableStock;
        this.requestedStock = requestedStock;
    }
    
    public Integer getAvailableStock() {
        return availableStock;
    }
    
    public Integer getRequestedStock() {
        return requestedStock;
    }
    
    public Long getProductId() {
        return productId;
    }
}
 