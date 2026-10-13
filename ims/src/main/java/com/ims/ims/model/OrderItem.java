package com.ims.ims.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Line item in a supplier order
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public OrderItem() {}

    public OrderItem(int quantity, BigDecimal price, Order order, Product product) {
        this.quantity = quantity;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.order = order;
        this.product = product;
    }

    public OrderItem(int quantity, BigDecimal price, Product product) {
        this.quantity = quantity;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.product = product;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BigDecimal getSubtotal() {
        if (price == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(new BigDecimal(quantity));
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", price=" + price +
                ", product=" + (product != null ? product.getName() : "N/A") +
                '}';
    }
}
