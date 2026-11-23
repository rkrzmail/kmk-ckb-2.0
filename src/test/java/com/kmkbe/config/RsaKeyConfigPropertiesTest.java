package com.kmkbe.config;

import org.junit.jupiter.api.Test;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.assertj.core.api.Assertions.assertThat;

class RsaKeyConfigPropertiesTest {

  @Test
  void holdsPublicAndPrivateKeys() throws Exception {
    var kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    var kp = kpg.generateKeyPair();
    var props = new RsaKeyConfigProperties((RSAPublicKey) kp.getPublic(), (RSAPrivateKey) kp.getPrivate());

    assertThat(props.publicKey()).isEqualTo(kp.getPublic());
    assertThat(props.privateKey()).isEqualTo(kp.getPrivate());
  }
}
