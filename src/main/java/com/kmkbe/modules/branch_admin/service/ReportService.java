package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.CustomerDto;
import com.kmkbe.core.domain.dto.DuedateDto;
import com.kmkbe.core.domain.dto.ProyeksiDto;
import com.kmkbe.core.domain.dto.VisitorDto;
import com.kmkbe.core.domain.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {
    @Autowired
    private CustomerRepository customerRepository;

    public List<VisitorDto> getVisitorReport() {
        return customerRepository.findVisitorData();
    }

}
