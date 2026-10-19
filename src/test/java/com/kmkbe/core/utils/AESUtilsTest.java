package com.kmkbe.core.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class AESUtilsTest {

    private String originalKey;

    @BeforeEach
    void setUp() {
        originalKey = AESUtils.getKey();
    }

    @AfterEach
    void tearDown() {
        AESUtils.setKey(originalKey);
    }

    @Test
    @DisplayName("H4: Legacy ECB encrypt & decrypt works seamlessly (preserving SISCA integration flow)")
    void legacyEcbEncryptAndDecryptRoundTrip() {
        String plaintext = "{\"LoginID\":\"john.doe\",\"Password\":\"Secret#123\",\"ApplicationScheme\":\"\"}";

        String ciphertext = AESUtils.encrypt(plaintext);
        assertThat(ciphertext).isNotNull().isNotBlank();
        assertThat(ciphertext).doesNotStartWith(AESUtils.GCM_PREFIX);

        String decrypted = AESUtils.decrypt(ciphertext);
        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("H4: Modern AES/GCM encrypt produces GCM-prefixed ciphertext and decrypts correctly")
    void gcmEncryptAndDecryptRoundTrip() {
        String plaintext = "Sensitive data protected with AES-256 GCM authenticated mode";

        String ciphertext = AESUtils.encryptGcm(plaintext);
        assertThat(ciphertext).isNotNull().startsWith(AESUtils.GCM_PREFIX);

        // Standard decrypt automatically detects GCM prefix
        String decrypted = AESUtils.decrypt(ciphertext);
        assertThat(decrypted).isEqualTo(plaintext);

        // Explicit decryptGcm also works
        String decryptedGcm = AESUtils.decryptGcm(ciphertext);
        assertThat(decryptedGcm).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("H4: GCM encryption produces non-deterministic ciphertexts due to random 12-byte IVs")
    void gcmProducesDifferentCiphertextEachTime() {
        String plaintext = "identical-plaintext-message";

        String ciphertext1 = AESUtils.encryptGcm(plaintext);
        String ciphertext2 = AESUtils.encryptGcm(plaintext);

        assertThat(ciphertext1).isNotEqualTo(ciphertext2);
        // But both decrypt back to the same plaintext
        assertThat(AESUtils.decrypt(ciphertext1)).isEqualTo(plaintext);
        assertThat(AESUtils.decrypt(ciphertext2)).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("H4: GCM authentication tag prevents ciphertext tampering")
    void gcmTamperProofingDetectsModification() {
        String plaintext = "financial-transaction-amount=1000000";
        String ciphertext = AESUtils.encryptGcm(plaintext);

        // Tamper with the base64 ciphertext body
        String prefix = AESUtils.GCM_PREFIX;
        String body = ciphertext.substring(prefix.length());
        byte[] raw = java.util.Base64.getDecoder().decode(body);
        // Flip one bit in the ciphertext / tag portion
        raw[raw.length - 1] ^= 0x01;
        String tamperedCiphertext = prefix + java.util.Base64.getEncoder().encodeToString(raw);

        // Tampered GCM ciphertext must fail authentication and return null safely
        String result = AESUtils.decrypt(tamperedCiphertext);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("H4: Custom secret key can be configured dynamically without hardcoding")
    void customKeyConfigurationWorks() {
        String customKey = "CustomKey1234567"; // 16 bytes for AES-128
        AESUtils.setKey(customKey);
        assertThat(AESUtils.getKey()).isEqualTo(customKey);

        String plaintext = "custom-key-protected-message";
        String encrypted = AESUtils.encrypt(plaintext);
        String decrypted = AESUtils.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(plaintext);

        // Key injection via component setter method
        AESUtils component = new AESUtils();
        component.setConfiguredKey("NewInjectedKey99");
        assertThat(AESUtils.getKey()).isEqualTo("NewInjectedKey99");
    }

    @Test
    @DisplayName("H4: URL-safe Base64 with hyphens and underscores is cleaned and decrypted")
    void urlSafeBase64HandledGracefully() {
        String plaintext = "url-safe-test-message";
        String standardCiphertext = AESUtils.encrypt(plaintext);
        // Simulate URL-safe encoding by replacing + with - and / with _
        String urlSafeCiphertext = standardCiphertext.replace('+', '-').replace('/', '_');

        String decrypted = AESUtils.decrypt(urlSafeCiphertext);
        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("H4: Null and empty inputs return null without throwing exceptions")
    void nullAndEmptyInputsHandledSafely() {
        assertThat(AESUtils.encrypt(null)).isNull();
        assertThat(AESUtils.encryptGcm(null)).isNull();
        assertThat(AESUtils.decrypt(null)).isNull();
        assertThat(AESUtils.decrypt("")).isNull();
        assertThat(AESUtils.decrypt("   ")).isNull();
        assertThat(AESUtils.decryptGcm(null)).isNull();
        assertThat(AESUtils.decryptEcb(null, "any-key")).isNull();
        assertThat(AESUtils.encryptEcb(null, "any-key")).isNull();
    }

    @Test
    @DisplayName("H4: Leaked credential constants EXAMPLE_ENCRYPT_... are completely removed from class fields")
    void leakedCredentialConstantsRemovedFromClass() {
        Field[] fields = AESUtils.class.getDeclaredFields();
        for (Field field : fields) {
            assertThat(field.getName()).doesNotContain("EXAMPLE_ENCRYPT_AES");
            assertThat(field.getName()).doesNotContain("EXAMPLE_ENCRYPT_SUPER_ADMIN");
        }
    }
}
