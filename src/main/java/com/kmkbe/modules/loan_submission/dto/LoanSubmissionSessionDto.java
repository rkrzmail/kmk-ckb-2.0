package com.kmkbe.modules.loan_submission.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.Date;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanSubmissionSessionDto {
    @JsonIgnore
    private Integer id;

    private Integer lastStep;
    private Date dtmCrt;
    private Date dtmUpd;
    private Object session;
}
