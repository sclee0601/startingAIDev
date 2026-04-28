package com.app.stock.dto;

public class TradeRequest {

    private String symbol;
    private Integer quantity;
    private Double price;

    public TradeRequest() {
    }

    public TradeRequest(String symbol, Integer quantity, Double price) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
