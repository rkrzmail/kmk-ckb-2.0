package com.kmkbe.core;

import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.core.domain.dto.AddressDto;
import com.kmkbe.core.domain.entity.CustomerCompany;
import org.assertj.core.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class ObjectTest {
    private static final CustomerCompany company = CustomerCompany.builder()
            .rt("08")
            .rw("06")
            .zipCode("12345")
            .city("Jakarta")
            .area("Cempaka Putih")
            .build();

    @Test
    void testConvertObj() throws IllegalAccessException {
        Map<String, Object> map = ObjectUtils.castObjectToMap(company);
        assertThat(company.getRt())
                .isEqualTo(map.get("rt"));
    }

    @Test
    void testConvertAndCastObj() {
        try {
            Map<String, Object> map = ObjectUtils.castObjectToMap(company);
            AddressDto addressDto = ObjectUtils.castObjectFromMap(map, new AddressDto());
            assertThat(addressDto.getRt()).isEqualTo(company.getRt());
        } catch (Exception ignored) {
        }
    }

    @Test
    void testUri(){
        String uri = "http://10.42.52.63:8088/UploadRoot/vendor/legaldocs/181/LEGAL_DOC_459ad39448c0fd6fd5ff896fc13a8418.pdf";
        URI u;
        try {
            u = new URI(uri);
            u = new URI("https", "dev1-danasakti.csulfinance.com/viewimage", u.getPath(), u.getFragment());
            uri = u.toString();
        } catch (URISyntaxException e) {
            u = null;
        }

        System.out.println(uri);
        Assertions.assertThat(uri).isEqualTo("https://dev1-danasakti.csulfinance.com/viewimage/UploadRoot/vendor/legaldocs/181/LEGAL_DOC_459ad39448c0fd6fd5ff896fc13a8418.pdf");
    }
}
