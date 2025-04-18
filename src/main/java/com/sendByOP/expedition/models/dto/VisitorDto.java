package com.sendByOP.expedition.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for visitor information")
public class VisitorDto {
    @Schema(description = "Unique identifier for the visitor", example = "1")
    private Integer id;
    
    @NotNull(message = "Visit date cannot be null")
    @Schema(description = "Date and time of the visit", example = "2023-12-25T10:30:00")
    private LocalDateTime visitDate;
    
    @Schema(description = "IP address of the visitor", example = "192.168.1.1")
    private String ipAddress;
    
    @Schema(description = "User agent string of the visitor's browser", example = "Mozilla/5.0")
    private String userAgent;
    
    @Schema(description = "Referrer URL of the visit", example = "https://example.com")
    private String referrer;
}