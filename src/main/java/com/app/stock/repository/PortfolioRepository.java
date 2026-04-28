package com.app.stock.repository;

import com.app.stock.entity.Portfolio;
import com.app.stock.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByOwner(User owner);
    Optional<Portfolio> findByOwnerAndSymbol(User owner, String symbol);
}
