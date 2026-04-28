package com.app.stock.dto;

public class PortfolioDto {

    private String symbol;
    private Integer quantity;

    public PortfolioDto() {
    }

    public PortfolioDto(String symbol, Integer quantity) {
        this.symbol = symbol;
        this.quantity = quantity;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
