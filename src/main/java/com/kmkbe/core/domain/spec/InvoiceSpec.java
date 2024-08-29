package com.kmkbe.core.domain.spec;

import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.Invoice;
import com.kmkbe.core.domain.request.PaginationRequest;
import io.netty.util.internal.StringUtil;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class InvoiceSpec {
    public static Specification<Invoice> list(PaginationRequest request) {
       /* return byInvoiceGreaterThanToday()
                .and(
                        bySearchBy(
                                request.getSearchBy().getSearch(),
                                request.getSearchBy().getValue()
                        )
                );*/
        return bySearchBy(
                request.getSearchBy(),
                request.getSearchValue()
        );
    }

    public static Specification<Invoice> listDistributionDetail(
            FinancingHdr financingHdr,
            PaginationRequest request
    ) {
        return byFinancingHdr(financingHdr)
                .and(
                        bySearchBy(
                                request.getSearchBy(),
                                request.getSearchValue()
                        )
                );
    }

    public static Specification<Invoice> byInvoiceGreaterThanToday() {
        return (root, query, criteriaBuilder) -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date today = null;
            try {
                today = sdf.parse(sdf.format(new Date()));
            } catch (ParseException e) {
                log.error("failed to parsed date {}", e.getMessage());
            }

            if (today == null) {
                return null;
            }

            Predicate predicate = criteriaBuilder.and(
                    criteriaBuilder.greaterThan(root.get("invoice_date").as(Date.class), today)
            );

            return predicate;
        };
    }

    public static Specification<Invoice> bySearchBy(String searchBy, String value) {
        return (root, query, criteriaBuilder) -> {
            if (searchBy == null || StringUtil.isNullOrEmpty(value)) {
                return null;
            }

            return switch (searchBy.toLowerCase()) {
                case "noinvoice" -> criteriaBuilder.and(criteriaBuilder.equal(root.get("cust_inv_no"), value));
                case "pemberikerja" -> null;
                //return criteriaBuilder.and(criteriaBuilder.equal(root.get("bo"), value));
                case "item" -> null;
                //return criteriaBuilder.and(criteriaBuilder.equal(root.get("invoice_due_date"), value));
                case "tanggalinvoice" -> criteriaBuilder.and(criteriaBuilder.equal(root.get("invoice_date"), value));
                case "jatuhtempoinvoice" ->
                        criteriaBuilder.and(criteriaBuilder.equal(root.get("invoice_due_date"), value));
                case "jumlahtagihan" -> criteriaBuilder.and(criteriaBuilder.equal(root.get("invoice_amt"), value));
                default -> null;
            };
        };
    }

    public static Specification<Invoice> byFinancingHdr(FinancingHdr financingHdr) {
        return (root, query, criteriaBuilder) -> {
            Join<Invoice, FinancingDtl> joinFinancingDtl = root.join("financing_dtl", JoinType.INNER);
            Join<FinancingDtl, FinancingHdr> joinFinancingHdr = joinFinancingDtl.join("financing_hdr", JoinType.INNER);

            return criteriaBuilder.and(
                    criteriaBuilder.equal(
                            joinFinancingHdr.get("financing_hdr_code"), financingHdr.getFinancingHdrCode()
                    )
            );
        };
    }
}
