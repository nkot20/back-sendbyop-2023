package com.sendByOP.expedition.Twilio;

import com.twilio.Twilio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioInitializer {
    private final TwilioProperties twilioproperties;

    @Autowired
    public TwilioInitializer(TwilioProperties twilioproperties)
    {
        this.twilioproperties=twilioproperties;
        Twilio.init(twilioproperties.getAccountSid(), twilioproperties.getAuthToken());
        System.out.println("Twilio initialized with account-"+twilioproperties.getAccountSid());
    }
}