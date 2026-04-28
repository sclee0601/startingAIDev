package com.app.stock.service;

import com.app.stock.dto.TradeRequest;
import com.app.stock.dto.TransactionDto;
import com.app.stock.dto.PortfolioDto;
import com.app.stock.entity.Portfolio;
import com.app.stock.entity.Transaction;
import com.app.stock.entity.User;
import com.app.stock.repository.PortfolioRepository;
import com.app.stock.repository.TransactionRepository;
import com.app.stock.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TradingService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;

    public TradingService(UserRepository userRepository,
                          PortfolioRepository portfolioRepository,
                          TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.portfolioRepository = portfolioRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionDto executeTrade(Long userId, TradeRequest request, String type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user.not.found"));

        Portfolio portfolio = portfolioRepository.findByOwnerAndSymbol(user, request.getSymbol())
                .orElseGet(() -> new Portfolio(request.getSymbol(), 0, user));

        int change = request.getQuantity();
        if ("SELL" .equalsIgnoreCase(type)) {
            if (portfolio.getQuantity() < change) {
                throw new IllegalArgumentException("insufficient.quantity");
            }
            portfolio.setQuantity(portfolio.getQuantity() - change);
        } else {
            portfolio.setQuantity(portfolio.getQuantity() + change);
        }

        portfolioRepository.save(portfolio);

        Transaction transaction = new Transaction(user, request.getSymbol(), change, request.getPrice(), Instant.now(), type);
        Transaction saved = transactionRepository.save(transaction);

        return new TransactionDto(saved.getId(), saved.getSymbol(), saved.getQuantity(), saved.getPrice(), saved.getExecutedAt(), saved.getType());
    }

    @Transactional(readOnly = true)
    public List<PortfolioDto> getPortfolio(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user.not.found"));
        return portfolioRepository.findByOwner(user).stream()
                .map(p -> new PortfolioDto(p.getSymbol(), p.getQuantity()))
                .collect(Collectors.toList());
    }
}
