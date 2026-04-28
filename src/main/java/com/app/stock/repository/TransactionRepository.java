package com.app.stock.repository;

import com.app.stock.entity.Transaction;
import com.app.stock.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserOrderByExecutedAtDesc(User user);
}
