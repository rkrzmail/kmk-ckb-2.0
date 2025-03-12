package com.kmkbe.core.domain.dto.email;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Builder
public class MailHeaderDto {
    public int processTime;
    public MailStatusDto success;
    public Object errors;
}
