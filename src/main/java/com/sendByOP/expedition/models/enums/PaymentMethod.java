package com.sendByOP.expedition.models.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    ORANGE_MONEY("Orange Money", "OM"),
    MTN_MOBILE_MONEY("MTN Mobile Money", "MTN"),
    CREDIT_CARD("Carte Bancaire", "CARD"),
    PAYPAL("PayPal", "PAYPAL");
    
    private final String displayName;
    private final String code;
    
    PaymentMethod(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }
}
