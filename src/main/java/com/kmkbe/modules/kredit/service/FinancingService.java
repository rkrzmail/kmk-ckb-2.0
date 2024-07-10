package com.kmkbe.modules.kredit.service;

import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.kredit.constant.FinancingStatus;
import com.kmkbe.modules.kredit.entity.Bouwheer;
import com.kmkbe.modules.kredit.entity.FinancingDtl;
import com.kmkbe.modules.kredit.entity.FinancingHdr;
import com.kmkbe.modules.kredit.mapper.FinancingMapper;
import com.kmkbe.modules.kredit.repository.BouwheerRepository;
import com.kmkbe.modules.kredit.repository.FinancingDtlRepository;
import com.kmkbe.modules.kredit.repository.FinancingHdrRepository;
import com.kmkbe.modules.kredit.request.CreateFinancingRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancingService {
    private final FinancingHdrRepository financingHdrRepository;
    private final FinancingDtlRepository financingDtlRepository;
    private final CustomerRepository customerRepository;
    private final BouwheerRepository bouwheerRepository;

    @Transactional
    public String create(CreateFinancingRequest request) {
        try {
            final Optional<Customer> findCust = customerRepository.findByCustCode(request.getCustCode());
            if (findCust.isEmpty()) {
                throw new IllegalStateException("Customer not found");
            }

            final Optional<Bouwheer> findBouwheer = bouwheerRepository.findByBouwheerCode(request.getBouwheerCode());
            if (findBouwheer.isEmpty()) {
                throw new IllegalStateException("Bouwheer not found");
            }

            final Customer cust = findCust.get();
            final Bouwheer bouwheer = findBouwheer.get();
            final FinancingHdr header = FinancingMapper.INSTANCE.headerFromCreateRequest(request);
            {
                // fresh input will store as DRAFT
                header.setFinancingStatus(FinancingStatus.DRAFT.name());
                header.setUsrCrt(cust.getCustName());
                header.setDtmCrt(Instant.now());
                header.setCustCode(cust);
                header.setBouwheerCode(bouwheer);
                financingHdrRepository.save(header);
            }


            request.getDetails()
                    .forEach((item) -> {
                        final FinancingDtl detail = new FinancingDtl();
                        {
                            detail.setBouwheerInvNo(item.getInvoiceNumber());
                            detail.setInvoiceSeqno(item.getInvoiceSeqNumber());
                            detail.setFinancingHdrCode(header);
                            detail.setUsrCrt(cust.getCustName());
                            detail.setDtmCrt(Instant.now());
                            financingDtlRepository.save(detail);
                        }
                    });
            return "Financing Successfully Create";
        } catch (Exception e) {
            log.error("create, error {}", e.getMessage());
            throw e;
        }
    }
}
