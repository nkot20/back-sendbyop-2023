package com.sendByOP.expedition.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
public class VerificationResult {

    private  String id;
    private  String[] errors;
    private  boolean valid;

    public VerificationResult(String sid) {
        this.id = sid;
    }
}