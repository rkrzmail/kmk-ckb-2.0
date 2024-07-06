package com.kmkbe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.utils.AESUtils;
import com.kmkbe.core.utils.CommonFormattingUtils;
import com.kmkbe.core.utils.JsonUtils;
import com.kmkbe.modules.internal.request.ActiveDirectoryRequest;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
public class AESTest {

    @Test
    void encryption() throws JsonProcessingException {
        ActiveDirectoryRequest params = new ActiveDirectoryRequest();
        LinkedHashMap<String, Object> json = JsonUtils.objectToJson(params);
        String encrypt = AESUtils.encrypt(JsonUtils.jsonToStr(json));

        assertThat(CommonFormattingUtils.cleanBase64(encrypt))
                .isEqualTo(CommonFormattingUtils.cleanBase64(AESUtils.EXAMPLE_ENCRYPT_AES));
    }

    @Test
    void decryption() throws JsonProcessingException {
        ActiveDirectoryRequest params = new ActiveDirectoryRequest();
        LinkedHashMap<String, Object> json = JsonUtils.objectToJson(params);
        String encrypt = AESUtils.encrypt(JsonUtils.jsonToStr(json));

        assertThat(JsonUtils.strToJson(AESUtils.decrypt(encrypt)))
                .isEqualTo(json);
    }
}

