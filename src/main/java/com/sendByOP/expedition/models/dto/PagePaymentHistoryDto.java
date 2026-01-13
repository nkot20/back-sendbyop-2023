package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour la réponse paginée de l'historique des paiements
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagePaymentHistoryDto {
    private List<PaymentHistoryDto> content;
    private Integer totalElements;
    private Integer totalPages;
    private Integer size;
    private Integer number;
    private Boolean first;
    private Boolean last;
}
