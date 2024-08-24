package com.kmkbe.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.utils.AESUtils;
import com.kmkbe.core.utils.CommonFormattingUtils;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.modules.remote.request.ActiveDirectoryRemoteRequest;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
public class AESTest {

    @Test
    void encryption() throws JsonProcessingException {
        ActiveDirectoryRemoteRequest params =  ActiveDirectoryRemoteRequest.builder()
                .build();

        LinkedHashMap<String, Object> json = ObjectUtils.objectToJson(params);
        String encrypt = AESUtils.encrypt(ObjectUtils.jsonToStr(json));

        assertThat(CommonFormattingUtils.cleanBase64(encrypt))
                .isEqualTo(CommonFormattingUtils.cleanBase64(AESUtils.EXAMPLE_ENCRYPT_AES));
    }

    @Test
    void decryption() throws JsonProcessingException {
        ActiveDirectoryRemoteRequest params = ActiveDirectoryRemoteRequest.builder().build();
        LinkedHashMap<String, Object> json = ObjectUtils.objectToJson(params);
        String encrypt = AESUtils.encrypt(ObjectUtils.jsonToStr(json));

        assertThat(ObjectUtils.strToJson(AESUtils.decrypt(encrypt)))
                .isEqualTo(json);
    }
}

