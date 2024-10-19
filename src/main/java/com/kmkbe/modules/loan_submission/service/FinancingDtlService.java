package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.dto.InvoiceDto;
import com.kmkbe.core.domain.entity.Bouwheer;
import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.Invoice;
import com.kmkbe.core.domain.mapper.InvoiceMapper;
import com.kmkbe.core.domain.model.PostedInvoicePayload;
import com.kmkbe.core.domain.repository.FinancingDtlRepository;
import com.kmkbe.core.domain.repository.InvoiceRepository;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancingDtlService {
    private final FinancingDtlRepository financingDtlRepository;
    private final InvoiceRepository invoiceRepository;
    public FinancingDtl findBy(String bouwheerInvNo) {
        try {
            return financingDtlRepository.findFirstByBouwheerInvNo(bouwheerInvNo).orElse(null);
        } catch (Exception e) {
            log.error("findBy, error {}", e.getMessage());
            throw e;
        }
    }

    public List<FinancingDtl> findAllBy(FinancingHdr financingHdr) {
        try {
            return financingDtlRepository.findAllByFinancingHdr(financingHdr).orElse(new ArrayList<>());
        } catch (Exception e) {
            log.error("findAllBy, error {}", e.getMessage());
            throw e;
        }
    }

    /**
     * <p>@postedInvoices is invoices that already posted from MST integration</p>
     * <p>@createdInvoices is invoices that created from Entity or DB</p>
     */
    public List<FinancingDtl> createBulk(
            Customer customer,
            Bouwheer bouwheer,
            FinancingHdr financingHdr,
            List<PostedInvoicePayload> postedInvoices,
            List<InvoiceDto> createdInvoices
    ) {
        try {
            return IntStream.range(0, postedInvoices.size())
                    .mapToObj((index) -> {
                        final FinancingDtl detail = new FinancingDtl();
                        {
                            Invoice invoice = InvoiceMapper.INSTANCE.entityFromDto(
                                    createdInvoices
                                            .stream()
                                            .filter(item -> item.getBouwheerInvNo().equals(postedInvoices.get(index).getBouwheerInvoiceNo()))
                                            .findFirst()
                                            .orElseThrow(() -> new IllegalStateException(""))
                            );

                            invoice.setUsrCrt(customer.getCustName());
                            invoice.setDtmCrt(DateTimeUtils.now());
                            invoice.setCustomer(customer);
                            invoice.setBouwheer(bouwheer);

                            detail.setInvoice(invoice);
                            detail.setFinancingDtlCode(UUID.randomUUID());
                            detail.setBouwheerInvNo(postedInvoices.get(index).getBouwheerInvoiceNo());
                            //detail.setInvoiceSeqno((long) index + 1);
                            detail.setFinancingHdr(financingHdr);
                            detail.setUsrCrt(customer.getCustName());
                            detail.setDtmCrt(DateTimeUtils.now());



                        }

                        financingDtlRepository.save(detail);
                        return detail;
                    })
                    .toList();
        } catch (Exception e) {
            log.error("createBulk, error {}", e.getMessage());
            throw e;
        }
    }

    public void delete(FinancingDtl financingDtl) {
        try {
            financingDtlRepository.delete(financingDtl);
        } catch (Exception e) {
            log.error("delete, error {}", e.getMessage());
            throw e;
        }
    }

    public void updatePaid(FinancingInvoicePaidRequest request, FinancingHdr financingHdr) {
        try {
            List<FinancingDtl> financingDtls = financingDtlRepository.findAllByFinancingHdr(financingHdr).orElse(null);
            if (financingDtls != null) {
                for (FinancingDtl financingDtl : financingDtls) {
                    for (FinancingInvoicePaidRequest.InvoicePaid invoicePaid : request.getInvoicePaid()) {
                        if (invoicePaid.getInvoiceNo().equalsIgnoreCase(financingDtl.getInvoice().getCustInvNo())) {
                            financingDtl.getInvoice().setStatus("PAID");
                            financingDtl.getInvoice().setUsrUpd(financingHdr.getUsrUpd());
                            financingDtl.getInvoice().setDtmUpd(DateTimeUtils.now());

                            financingDtl.setBouwheerPaidDate(request.getInvoicePaid().getFirst().getPostingDate().toInstant());
                            financingDtl.setDtmUpd(DateTimeUtils.now());
                            financingDtl.setUsrUpd(financingHdr.getUsrUpd());
                            financingDtlRepository.save(financingDtl);
                            break;
                        }
                    }//for
                }//for
            }

        } catch (Exception e) {
            log.error("updatePaid, error {}", e.getMessage());
            throw e;
        }
    }
}
