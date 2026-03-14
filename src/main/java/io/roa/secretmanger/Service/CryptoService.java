package io.roa.secretmanger.Service;

public interface CryptoService {
    String encrypt(String plainText);

    String decrypt(String encryptedBase64);
}
