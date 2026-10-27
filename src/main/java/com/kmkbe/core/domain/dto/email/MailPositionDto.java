package com.kmkbe.core.domain.dto.email;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import java.util.ArrayList;

@Getter
@Setter
@Data
@Builder
@Jacksonized
public class MailPositionDto {
    public MailHeaderDto header;
    public ArrayList<MailDataDto> data;
}
