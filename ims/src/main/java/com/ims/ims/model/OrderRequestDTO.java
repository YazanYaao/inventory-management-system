package com.ims.ims.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * Data Transfer Object for creating orders via REST API
 */
public class OrderRequestDTO {

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotEmpty(message = "Product IDs list cannot be empty")
    private List<Long> productIds;

    @NotEmpty(message = "Quantities list cannot be empty")
    private List<Integer> quantities;

    @NotEmpty(message = "Prices list cannot be empty")
    private List<BigDecimal> prices;

    public OrderRequestDTO() {}

    public OrderRequestDTO(Long supplierId, List<Long> productIds, List<Integer> quantities, List<BigDecimal> prices) {
        this.supplierId = supplierId;
        this.productIds = productIds;
        this.quantities = quantities;
        this.prices = prices;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }

    public List<Integer> getQuantities() {
        return quantities;
    }

    public void setQuantities(List<Integer> quantities) {
        this.quantities = quantities;
    }

    public List<BigDecimal> getPrices() {
        return prices;
    }

    public void setPrices(List<BigDecimal> prices) {
        this.prices = prices;
    }
}
