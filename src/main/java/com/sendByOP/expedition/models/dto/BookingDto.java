package com.sendByOP.expedition.models.dto;

import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class BookingDto implements Serializable {
    private Integer id;
    private Date bookingDate;
    private String bookingTime;
    private Integer paymentStatus;
    private Integer expeditionStatus;
    private String customerReview;
    private String senderReview;
    private int senderReceptionStatus;
    private int customerReceptionStatus;
    private Integer receiverId;
    private Integer flightId;
    private Integer customerId;
    private int cancelled;
    private int transporterPaymentStatus;
    private List<Integer> parcelIds;

}
