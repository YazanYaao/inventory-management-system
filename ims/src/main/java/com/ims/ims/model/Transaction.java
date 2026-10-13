
package com.ims.ims.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
/**
 * نموذج المعاملات (العمليات على المخزون)
 * 
 * كل عملية على المخزون (إضافة، سحب، بيع، إلخ) يتم تسجيلها هنا
 * لتتبع التاريخ كامل للمخزون
 */
@Entity
@Table(name = "transactions")

public class Transaction {
    
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // الحقول (Fields)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;
    
    // العلاقة مع المنتج
    // كل معاملة تتعلق بمنتج واحد
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties("transactions") // يمنع المنتج من محاولة فتح قائمة العمليات مرة أخرى
    private Product product;
    
    // العلاقة مع المستخدم
    // كل معاملة يجب أن يقوم بها مستخدم معين
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"transactions", "password"}) // يمنع المستخدم من فتح العمليات ويخفي الباسورد للأمان
    private User user;
    
    // نوع المعاملة (إضافة، سحب، بيع، إلخ)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "نوع المعاملة مطلوب")
    @Column(name = "transaction_type", length = 20)
    private TransactionType transactionType;
    
    // الكمية المتأثرة
    @Min(value = 1, message = "الكمية يجب أن تكون أكثر من صفر")
    @Column(nullable = false)
    private Integer quantity;
    
    // ملاحظات حول المعاملة
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    // تاريخ ووقت المعاملة
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate = LocalDateTime.now();
    
    // الكمية قبل المعاملة (للتتبع)
    @Column(name = "previous_quantity")
    private Integer previousQuantity;
    
    // الكمية بعد المعاملة (للتتبع)
    @Column(name = "new_quantity")
    private Integer newQuantity;
    
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Constructors
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    public Transaction() {}
    
    public Transaction(Product product, User user, TransactionType transactionType,
                      Integer quantity, String notes, Integer previousQuantity, 
                      Integer newQuantity) {
        this.product = product;
        this.user = user;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.notes = notes;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
    }
    
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Getters and Setters
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public TransactionType getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
    
    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
    
    public Integer getPreviousQuantity() {
        return previousQuantity;
    }
    
    public void setPreviousQuantity(Integer previousQuantity) {
        this.previousQuantity = previousQuantity;
    }
    
    public Integer getNewQuantity() {
        return newQuantity;
    }
    
    public void setNewQuantity(Integer newQuantity) {
        this.newQuantity = newQuantity;
    }
    
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Helper Methods
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    // حساب التغيير في الكمية
    public Integer getQuantityChange() {
        if (previousQuantity == null || newQuantity == null) {
            return null;
        }
        return newQuantity - previousQuantity;
    }
    
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Override Methods
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", product=" + (product != null ? product.getName() : null) +
                ", user=" + (user != null ? user.getUsername() : null) +
                ", transactionType=" + transactionType +
                ", quantity=" + quantity +
                ", transactionDate=" + transactionDate +
                ", previousQuantity=" + previousQuantity +
                ", newQuantity=" + newQuantity +
                '}';
    }
}
