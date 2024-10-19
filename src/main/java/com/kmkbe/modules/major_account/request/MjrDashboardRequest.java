package com.kmkbe.modules.major_account.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
public class MjrDashboardRequest {
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date startDate;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date endDate;
    private Integer page;
}
