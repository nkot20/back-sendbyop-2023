package com.sendByOP.expedition.models.dto;

import com.sendByOP.expedition.models.enums.BookingStatus;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * DTO de réponse pour une réservation créée
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDto implements Serializable {
    
    private Integer id;
    
    // Statut
    private BookingStatus status;
    
    // Dates
    private Date bookingDate;
    private LocalDateTime confirmedAt;
    private LocalDateTime paymentDeadline;
    
    // Prix
    private BigDecimal totalPrice;
    
    // Relations
    private Integer flightId;
    private Integer customerId;
    private Integer receiverId;
    
    // Photos du colis (support multi-photos)
    private List<ParcelPhotoDto> parcelPhotos;
    
    /**
     * @deprecated Utiliser parcelPhotos à la place. Conservé pour compatibilité.
     */
    @Deprecated
    private String parcelPhotoUrl;
    
    // Informations destinataire (pour confirmation)
    private String receiverFullName;
    private String receiverEmail;
    private String receiverPhoneNumber;
    
    // Informations colis
    private BigDecimal parcelWeight;
    private String parcelDescription;
}
