package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.VerificationResult;

public interface IPhoneVerificationService {
    
    /**
     * Initiates the phone verification process
     * @param phone The phone number to verify
     * @return VerificationResult containing the verification status
     * @throws SendByOpException if verification process fails
     */
    VerificationResult startVerification(String phone) throws SendByOpException;

    /**
     * Checks the verification code for a given phone number
     * @param phone The phone number to verify
     * @param code The verification code to check
     * @return VerificationResult containing the verification status
     * @throws SendByOpException if verification check fails
     */
    VerificationResult checkVerification(String phone, String code) throws SendByOpException;
}