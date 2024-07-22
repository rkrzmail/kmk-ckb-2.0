package com.kmkbe;

import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.modules.customer.dto.AddressDto;
import com.kmkbe.modules.customer.entity.CustomerCompany;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.testng.annotations.Test;

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
}
