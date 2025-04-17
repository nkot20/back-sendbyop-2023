package com.sendByOP.expedition.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@Schema(description = "DTO for email information")
public class EmailDto {
    @Schema(description = "Email recipient address", example = "user@example.com")
    private String to;

    @Schema(description = "Email message content", example = "This is the email body content")
    private String body;

    @Schema(description = "Email subject line", example = "Important Notification")
    private String topic;
}
