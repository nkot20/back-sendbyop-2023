package com.sendByOP.expedition.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

@Component
@Profile("prod")
public class RSAEncryptionUtil {
    private static final String ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;

    @Value("${server.ssl.key-store}")
    private String keystorePath;

    @Value("${server.ssl.key-store-password}")
    private String keystorePassword;

    private KeyPair keyPair;

    @PostConstruct
    public void init() throws Exception {
        File keystoreFile = new File(keystorePath);
        if (!keystoreFile.exists()) {
            generateAndSaveKeyPair();
        } else {
            loadKeyPair();
        }
    }

    private void generateAndSaveKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
        generator.initialize(KEY_SIZE);
        keyPair = generator.generateKeyPair();

        // Save to keystore
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry("rsa", keyPair.getPrivate(), keystorePassword.toCharArray(), new java.security.cert.Certificate[]{});
        
        try (FileOutputStream fos = new FileOutputStream(keystorePath)) {
            keyStore.store(fos, keystorePassword.toCharArray());
        }
    }

    private void loadKeyPair() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new java.io.FileInputStream(keystorePath), keystorePassword.toCharArray());
        PrivateKey privateKey = (PrivateKey) keyStore.getKey("rsa", keystorePassword.toCharArray());
        PublicKey publicKey = keyStore.getCertificate("rsa").getPublicKey();
        keyPair = new KeyPair(publicKey, privateKey);
    }

    public String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public String getPublicKey() {
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        return Base64.getEncoder().encodeToString(publicKeyBytes);
    }
}