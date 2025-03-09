package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@AllArgsConstructor
@Data
@Builder
public class PaymentDto {
    private Integer paymentId;
    private Date paymentDate;
    private Integer clientId;
    private Double amount;
    private Integer paymentTypeId;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String updatedBy;

}
