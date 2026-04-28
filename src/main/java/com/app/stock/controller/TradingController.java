package com.app.stock.controller;

import com.app.stock.dto.AiSummaryDto;
import com.app.stock.dto.PortfolioDto;
import com.app.stock.dto.TradeRequest;
import com.app.stock.dto.TransactionDto;
import com.app.stock.service.AiSummaryService;
import com.app.stock.service.TradingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
public class TradingController {

    private final TradingService tradingService;
    private final AiSummaryService aiSummaryService;
    private final String aiCachePrefix;

    public TradingController(TradingService tradingService,
                             AiSummaryService aiSummaryService,
                             @Value("${app.ai.summary-cache-key-prefix}") String aiCachePrefix) {
        this.tradingService = tradingService;
        this.aiSummaryService = aiSummaryService;
        this.aiCachePrefix = aiCachePrefix;
    }

    @PostMapping("/buy")
    public ResponseEntity<TransactionDto> buy(@RequestParam Long userId, @RequestBody TradeRequest request) {
        return ResponseEntity.ok(tradingService.executeTrade(userId, request, "BUY"));
    }

    @PostMapping("/sell")
    public ResponseEntity<TransactionDto> sell(@RequestParam Long userId, @RequestBody TradeRequest request) {
        return ResponseEntity.ok(tradingService.executeTrade(userId, request, "SELL"));
    }

    @GetMapping("/portfolio")
    public ResponseEntity<List<PortfolioDto>> portfolio(@RequestParam Long userId) {
        return ResponseEntity.ok(tradingService.getPortfolio(userId));
    }

    @GetMapping("/summary")
    public ResponseEntity<AiSummaryDto> summary(@RequestParam Long userId) {
        String cacheKey = aiCachePrefix + userId;
        String portfolioSummary = "Portfolio summary for user " + userId;
        String summary = aiSummaryService.getSummary(cacheKey, portfolioSummary);
        return ResponseEntity.ok(new AiSummaryDto(summary));
    }
}
