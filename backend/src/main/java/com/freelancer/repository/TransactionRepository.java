package com.freelancer.repository;

import com.freelancer.entity.Transaction;
import com.freelancer.entity.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByContractId(Long contractId);

    List<Transaction> findByContractIdOrderByCreatedAtDesc(Long contractId);

    Optional<Transaction> findByVnpTxnRef(String vnpTxnRef);

    Optional<Transaction> findByTransactionCode(String code);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type AND t.status = 'SUCCESS'")
    BigDecimal sumByTypeSuccess(@Param("type") TransactionType type);

    @Query(value = """
            SELECT CAST(t.created_at AS DATE) AS day, COALESCE(SUM(t.amount), 0) AS total
            FROM transactions t
            WHERE t.type = 'DEPOSIT'
              AND t.status = 'SUCCESS'
              AND t.created_at >= CAST(:from AS DATE)
            GROUP BY CAST(t.created_at AS DATE)
            ORDER BY day ASC
            """, nativeQuery = true)
    List<Object[]> dailyDepositLast7Days(@Param("from") LocalDate from);
}
