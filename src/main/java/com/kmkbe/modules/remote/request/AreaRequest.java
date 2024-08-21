package com.kmkbe.modules.remote.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaRequest {
    //@RequestParam(value = "area")
    @NotNull(message = "area is required")
    private PropCriteriaGenericTypeRequest.AreaPropName area;

    @NotNull(message = "value is required")
    @NotEmpty(message = "value shouldn't be empty")
    private String value;
}
