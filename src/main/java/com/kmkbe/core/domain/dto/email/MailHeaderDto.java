package com.kmkbe.core.domain.dto.email;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Setter
@Data
@Builder
@Jacksonized
public class MailHeaderDto {
    public int processTime;
    public MailStatusDto success;
    public Object errors;
}
