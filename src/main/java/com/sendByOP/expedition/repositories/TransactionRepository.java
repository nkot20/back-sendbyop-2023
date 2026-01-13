package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Transaction;
import com.sendByOP.expedition.models.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    
    Optional<Transaction> findByTransactionReference(String transactionReference);
    
    Optional<Transaction> findByExternalTransactionId(String externalTransactionId);
    
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    
    List<Transaction> findByBookingIdOrderByCreatedAtDesc(Integer bookingId);
    
    List<Transaction> findByCustomerIdOrderByCreatedAtDesc(Integer customerId);
    
    List<Transaction> findByStatusAndCreatedAtBefore(TransactionStatus status, LocalDateTime dateTime);
    
    @Query("SELECT t FROM Transaction t WHERE t.booking.id = :bookingId AND t.status = :status ORDER BY t.createdAt DESC")
    List<Transaction> findByBookingIdAndStatus(@Param("bookingId") Integer bookingId, 
                                                 @Param("status") TransactionStatus status);
    
    @Query("SELECT t FROM Transaction t WHERE t.customer.email = :email ORDER BY t.createdAt DESC")
    List<Transaction> findByCustomerEmail(@Param("email") String email);
    
    @Query("SELECT t FROM Transaction t WHERE t.customer.email = :email ORDER BY t.createdAt DESC")
    List<Transaction> findByCustomerEmailOrderByCreatedAtDesc(@Param("email") String email);
    
    @Query("SELECT t FROM Transaction t WHERE t.customer.email = :email")
    Page<Transaction> findByCustomerEmailOrderByCreatedAtDesc(@Param("email") String email, Pageable pageable);
    
    boolean existsByBookingIdAndStatus(Integer bookingId, TransactionStatus status);
}
