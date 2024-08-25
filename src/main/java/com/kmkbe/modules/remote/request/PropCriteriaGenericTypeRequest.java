package com.kmkbe.modules.remote.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PropCriteriaGenericTypeRequest {
    @Builder.Default
    @JsonProperty("DataType")
    private String dataType = "";

    @Builder.Default
    private Integer high = 0;

    @Builder.Default
    private Boolean isCriteriaDataTable = false;

    @Builder.Default
    private Integer low = 0;

    private Object propName;

    @Builder.Default
    private RestrictionType restriction = RestrictionType.Like;

    String value;

    public enum AreaPropName {
        kecamatan("RZ.AREA_CODE_1"),
        kelurahan("RZ.AREA_CODE_2"),
        kota("RZ.CITY"),
        zipCode("RZ.ZIPCODE"),
        province("RZ.PROVINCE");

        @JsonValue
        private final String value;

        AreaPropName(String value) {
            this.value = value;
        }
    }

    public enum CwrPropName {
        cwrNo("C.CWR_NO");

        @JsonValue
        private final String value;

        CwrPropName(String value) {
            this.value = value;
        }
    }

    public enum AgreementPropName {
        agreementNo("AGR.AGRMNT_NO");

        @JsonValue
        private final String value;

        AgreementPropName(String value) {
            this.value = value;
        }
    }

    public enum RestrictionType {
        Eq,
        Like
    }
}
