package com.app.stock.dto;

public class AiSummaryDto {

    private String summary;

    public AiSummaryDto() {
    }

    public AiSummaryDto(String summary) {
        this.summary = summary;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
