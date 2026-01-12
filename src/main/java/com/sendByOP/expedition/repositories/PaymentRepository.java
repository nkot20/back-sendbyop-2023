package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Customer;
import com.sendByOP.expedition.models.entities.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    /**
     * Trouve les paiements par client
     */
    List<Payment> findByClient(Customer client);

    /**
     * Trouve les paiements par client avec pagination, triés par date décroissante
     */
    Page<Payment> findByClientOrderByPaymentDateDesc(Customer client, Pageable pageable);

    /**
     * Trouve les paiements par email du client
     */
    @Query("SELECT p FROM Payment p WHERE p.client.email = :email ORDER BY p.paymentDate DESC")
    List<Payment> findByClientEmailOrderByPaymentDateDesc(@Param("email") String email);

    /**
     * Trouve les paiements par email du client avec pagination
     */
    @Query("SELECT p FROM Payment p WHERE p.client.email = :email ORDER BY p.paymentDate DESC")
    Page<Payment> findByClientEmailOrderByPaymentDateDesc(@Param("email") String email, Pageable pageable);

    /**
     * Compte le nombre de paiements par client
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.client.email = :email")
    long countByClientEmail(@Param("email") String email);

    /**
     * Calcule le total des paiements par client
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.client.email = :email")
    Double sumAmountByClientEmail(@Param("email") String email);
}
