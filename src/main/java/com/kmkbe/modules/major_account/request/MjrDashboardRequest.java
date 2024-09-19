package com.kmkbe.modules.major_account.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class MjrDashboardRequest {
    private Date startDate;
    private Date endDate;
    private Integer page;
}
