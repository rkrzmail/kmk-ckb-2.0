package com.kmkbe.core.domain.dto.email;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@Data
@Builder
public class MailPositionDto {
    public MailHeaderDto header;
    public ArrayList<MailDataDto> data;
}
