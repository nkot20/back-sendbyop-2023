package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.models.dto.AdminPaymentDto;
import com.sendByOP.expedition.models.dto.AdminPayoutDto;
import com.sendByOP.expedition.models.entities.Payout;
import com.sendByOP.expedition.models.entities.Transaction;
import com.sendByOP.expedition.models.enums.PayoutStatus;
import com.sendByOP.expedition.models.enums.TransactionStatus;
import com.sendByOP.expedition.repositories.PayoutRepository;
import com.sendByOP.expedition.repositories.TransactionRepository;
import com.sendByOP.expedition.services.iServices.IAdminPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Implémentation du service de gestion des paiements pour l'administration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPaymentService implements IAdminPaymentService {
    
    private final TransactionRepository transactionRepository;
    private final PayoutRepository payoutRepository;
    
    @Override
    public Page<AdminPaymentDto> getAllPayments(Pageable pageable) {
        log.info("Fetching all payments with pagination");
        Page<Transaction> transactions = transactionRepository.findAllByOrderByCreatedAtDesc(pageable);
        return transactions.map(this::convertToPaymentDto);
    }
    
    @Override
    public Page<AdminPaymentDto> getPaymentsByStatus(TransactionStatus status, Pageable pageable) {
        log.info("Fetching payments by status: {}", status);
        Page<Transaction> transactions = transactionRepository.findByStatus(status, pageable);
        return transactions.map(this::convertToPaymentDto);
    }
    
    @Override
    public Page<AdminPaymentDto> searchPayments(String search, Pageable pageable) {
        log.info("Searching payments with query: {}", search);
        Page<Transaction> transactions = transactionRepository.searchTransactions(search, pageable);
        return transactions.map(this::convertToPaymentDto);
    }
    
    @Override
    public AdminPaymentDto getPaymentById(Integer id) {
        log.info("Fetching payment by id: {}", id);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        return convertToPaymentDto(transaction);
    }
    
    @Override
    public Page<AdminPayoutDto> getAllPayouts(Pageable pageable) {
        log.info("Fetching all payouts with pagination");
        Page<Payout> payouts = payoutRepository.findAllByOrderByCreatedAtDesc(pageable);
        return payouts.map(this::convertToPayoutDto);
    }
    
    @Override
    public Page<AdminPayoutDto> getPayoutsByStatus(PayoutStatus status, Pageable pageable) {
        log.info("Fetching payouts by status: {}", status);
        Page<Payout> payouts = payoutRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return payouts.map(this::convertToPayoutDto);
    }
    
    @Override
    public Page<AdminPayoutDto> searchPayouts(String search, Pageable pageable) {
        log.info("Searching payouts with query: {}", search);
        Page<Payout> payouts = payoutRepository.searchPayouts(search, pageable);
        return payouts.map(this::convertToPayoutDto);
    }
    
    @Override
    public AdminPayoutDto getPayoutById(Long id) {
        log.info("Fetching payout by id: {}", id);
        Payout payout = payoutRepository.findById(id.intValue())
                .orElseThrow(() -> new RuntimeException("Payout not found with id: " + id));
        return convertToPayoutDto(payout);
    }
    
    @Override
    @Transactional
    public AdminPayoutDto processPayou(Long id, String transactionId, String paymentMethod) {
        log.info("Processing payout id: {} with transaction: {}", id, transactionId);
        Payout payout = payoutRepository.findById(id.intValue())
                .orElseThrow(() -> new RuntimeException("Payout not found with id: " + id));
        
        payout.markAsCompleted(transactionId);
        payout.setPaymentMethod(paymentMethod);
        
        Payout savedPayout = payoutRepository.save(payout);
        return convertToPayoutDto(savedPayout);
    }
    
    /**
     * Convertit java.util.Date en LocalDateTime
     */
    private LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
    
    /**
     * Convertit une Transaction en AdminPaymentDto
     */
    private AdminPaymentDto convertToPaymentDto(Transaction transaction) {
        return AdminPaymentDto.builder()
                .id(transaction.getId())
                .transactionReference(transaction.getTransactionReference())
                .externalTransactionId(transaction.getExternalTransactionId())
                .bookingId(transaction.getBooking() != null ? transaction.getBooking().getId() : null)
                .bookingReference(transaction.getBooking() != null ? 
                    "BKG-" + transaction.getBooking().getId() : null)
                .customerId(transaction.getCustomer() != null ? transaction.getCustomer().getId() : null)
                .customerName(transaction.getCustomer() != null ? 
                    transaction.getCustomer().getFirstName() + " " + transaction.getCustomer().getLastName() : null)
                .customerEmail(transaction.getCustomer() != null ? transaction.getCustomer().getEmail() : null)
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .paymentMethod(transaction.getPaymentMethod())
                .status(transaction.getStatus())
                .phoneNumber(transaction.getPhoneNumber())
                .createdAt(convertToLocalDateTime(transaction.getCreatedAt()))
                .completedAt(transaction.getCompletedAt())
                .errorMessage(transaction.getErrorMessage())
                .errorCode(transaction.getErrorCode())
                .invoiceUrl(transaction.getInvoiceUrl())
                .invoiceSent(transaction.getInvoiceSent())
                .build();
    }
    
    /**
     * Convertit un Payout en AdminPayoutDto
     */
    private AdminPayoutDto convertToPayoutDto(Payout payout) {
        return AdminPayoutDto.builder()
                .id(payout.getId())
                .bookingId(payout.getBooking() != null ? payout.getBooking().getId() : null)
                .bookingReference(payout.getBooking() != null ? 
                    "BKG-" + payout.getBooking().getId() : null)
                .travelerId(payout.getTraveler() != null ? payout.getTraveler().getId() : null)
                .travelerName(payout.getTraveler() != null ? 
                    payout.getTraveler().getFirstName() + " " + payout.getTraveler().getLastName() : null)
                .travelerEmail(payout.getTraveler() != null ? payout.getTraveler().getEmail() : null)
                .totalAmount(payout.getTotalAmount())
                .travelerAmount(payout.getTravelerAmount())
                .platformAmount(payout.getPlatformAmount())
                .vatAmount(payout.getVatAmount())
                .travelerPercentage(payout.getTravelerPercentage())
                .platformPercentage(payout.getPlatformPercentage())
                .vatPercentage(payout.getVatPercentage())
                .status(payout.getStatus())
                .transactionId(payout.getTransactionId())
                .paymentMethod(payout.getPaymentMethod())
                .errorMessage(payout.getErrorMessage())
                .createdAt(convertToLocalDateTime(payout.getCreatedAt()))
                .completedAt(payout.getCompletedAt())
                .cancelledAt(payout.getCancelledAt())
                .build();
    }
}
