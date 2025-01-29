package com.kmkbe.core.domain.spec;

import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.nikita.utils.Utils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class FinancingDtlSpec {
    public static Specification<FinancingDtl> custDashboardFilterBy(Customer customer, String searchBy, String searchValue) {
        return (root, query, criteriaBuilder) -> {
            Join<FinancingDtl, Invoice> joinInvoice = root.join("invoice", JoinType.INNER);
            Join<FinancingDtl, FinancingHdr> joinFinancingHdr = root.join("financingHdr", JoinType.INNER);
            Join<FinancingHdr, Customer> joinCustomer = joinFinancingHdr.join("customer", JoinType.INNER);
            Join<FinancingHdr, Agreement> joinAgreement = joinFinancingHdr.join("agreement", JoinType.INNER);
            Join<FinancingHdr, Bouwheer> joinBouwheer = joinFinancingHdr.join("bouwheer", JoinType.INNER);

            Predicate predicate = criteriaBuilder.and(
                    criteriaBuilder.equal(joinCustomer.get("custCode"), customer.getCustCode())
            );

            if (StringUtils.isNotEmpty(searchBy) && StringUtils.isNotEmpty(searchValue)) {
                if (searchBy.equalsIgnoreCase("custInvNo")) {
                    predicate = criteriaBuilder.and(
                            predicate,
                            criteriaBuilder.equal(joinInvoice.get("custInvNo"), searchValue)
                    );
                } else if (searchBy.equalsIgnoreCase("agreementCode")) {
                    predicate = criteriaBuilder.and(
                            predicate,
                            criteriaBuilder.equal(joinAgreement.get("agreementCode"), searchValue)
                    );
                } else if (searchBy.equalsIgnoreCase("bouwheerName")) {
                    predicate = criteriaBuilder.and(
                            predicate,
                            criteriaBuilder.equal(joinAgreement.get("bouwheerName"), searchValue)
                    );
                } else if (searchBy.equalsIgnoreCase("invoiceDate")) {
                    predicate = criteriaBuilder.and(
                            predicate,
                            criteriaBuilder.equal(joinInvoice.get("invoiceDate"), searchValue)
                    );
                } else if (searchBy.equalsIgnoreCase("status")) {
                    if (searchValue.equalsIgnoreCase("new")) {
                        predicate = criteriaBuilder.and(
                                predicate,
                                criteriaBuilder.equal(joinFinancingHdr.get("financingStatus"), "NEW"),
                                criteriaBuilder.equal(joinFinancingHdr.get("financingStep"), "NEW")
                        );
                    } else if (searchValue.equalsIgnoreCase("inprocess")) {
                        predicate = criteriaBuilder.and(
                                predicate,
                                criteriaBuilder.equal(joinFinancingHdr.get("financingStatus"), "INPROCESS"),
                                criteriaBuilder.or(
                                        criteriaBuilder.equal(joinFinancingHdr.get("financingStep"), "INPROCESS"),
                                        criteriaBuilder.equal(joinFinancingHdr.get("financingStep"), "ASSIGNMENT"),
                                        criteriaBuilder.equal(joinFinancingHdr.get("financingStep"), "SIGNING"),
                                        criteriaBuilder.equal(joinFinancingHdr.get("financingStep"), "SIGNED")
                                )
                        );
                    } else if (searchValue.equalsIgnoreCase("live")) {
                        predicate = criteriaBuilder.and(
                                predicate,
                                criteriaBuilder.equal(joinFinancingHdr.get("financingStatus"), "LIVE")
                        );
                    } else if (searchValue.equalsIgnoreCase("completed")) {
                        predicate = criteriaBuilder.and(
                                predicate,
                                criteriaBuilder.equal(joinFinancingHdr.get("financingStatus"), "COMPLETED")
                        );
                    }
                }
            }

            return predicate;
        };
    }
    public static Specification<FinancingDtl> custInvoiceFilterBy(UUID financeHdrCode, String searchBy, String searchValue) {
        return (root, query, criteriaBuilder) -> {
            Join<FinancingDtl, Invoice> joinInvoice = root.join("invoice", JoinType.INNER);
            Join<FinancingDtl, FinancingHdr> joinFinancingHdr = root.join("financingHdr", JoinType.INNER);
            Join<FinancingHdr, Customer> joinCustomer = joinFinancingHdr.join("customer", JoinType.INNER);
            Join<FinancingHdr, Agreement> joinAgreement = joinFinancingHdr.join("agreement", JoinType.INNER);
            Join<FinancingHdr, Bouwheer> joinBouwheer = joinFinancingHdr.join("bouwheer", JoinType.INNER);

            Predicate predicate = criteriaBuilder.and(
                    //criteriaBuilder.equal(root.get("financeHdrCode"), financeHdrCode)//financing_hdr_code
                     criteriaBuilder.equal(joinFinancingHdr.get("financingHdrCode"), financeHdrCode)
            );

            if (StringUtils.isNotEmpty(searchBy) && StringUtils.isNotEmpty(searchValue)) {
                if (searchBy.equalsIgnoreCase("customerInvoiceNo")|| searchBy.equalsIgnoreCase("custInvNo" )) {
                    predicate = criteriaBuilder.and(
                            predicate,
                            criteriaBuilder.like(joinInvoice.get("custInvNo"), "%"+searchValue+"%")
                    );
                } else  if (searchBy.equalsIgnoreCase("invoiceDescription")) {
                    predicate = criteriaBuilder.and(
                            predicate,
                            criteriaBuilder.like(joinInvoice.get("invoiceDescription"), "%"+searchValue+"%")
                    );
                } else  if (searchBy.equalsIgnoreCase("agreementCode")) {
                    predicate = criteriaBuilder.and(
                            predicate,
                            criteriaBuilder.equal(joinAgreement.get("agreementCode"), searchValue)
                    );
                } else if (searchBy.equalsIgnoreCase("bouwheerName")) {
                    predicate = criteriaBuilder.and(
                            predicate,
                            criteriaBuilder.like(joinBouwheer.get("bouwheerName"), "%"+searchValue+"%")
                    );
                } else if (searchBy.equalsIgnoreCase("invoiceDate")) {//invoiceDueDate
                    predicate = criteriaBuilder.and(
                            predicate,
                            criteriaBuilder.equal(joinInvoice.get("invoiceDate"), DateTimeUtils.formatDateTime(searchValue))
                    );
                } else if (searchBy.equalsIgnoreCase("invoiceDueDate")) {//
                    predicate = criteriaBuilder.and(
                            predicate,
                            criteriaBuilder.equal(joinInvoice.get("invoiceDueDate"), DateTimeUtils.formatDateTime(searchValue))
                    );
                } else if (searchBy.equalsIgnoreCase("invoiceAmount")) {//invoiceAmount
                    predicate = criteriaBuilder.and(
                            predicate,
                            criteriaBuilder.equal(joinInvoice.get("invoiceAmt"), Utils.getIntCurr(searchValue))
                    );
                } else if (searchBy.equalsIgnoreCase("status")) {
                    if (searchValue.equalsIgnoreCase("new")) {
                        predicate = criteriaBuilder.and(
                                predicate,
                                criteriaBuilder.equal(joinFinancingHdr.get("financingStatus"), "NEW"),
                                criteriaBuilder.equal(joinFinancingHdr.get("financingStep"), "NEW")
                        );
                    } else if (searchValue.equalsIgnoreCase("inprocess")) {
                        predicate = criteriaBuilder.and(
                                predicate,
                                criteriaBuilder.equal(joinFinancingHdr.get("financingStatus"), "INPROCESS"),
                                criteriaBuilder.or(
                                        criteriaBuilder.equal(joinFinancingHdr.get("financingStep"), "INPROCESS"),
                                        criteriaBuilder.equal(joinFinancingHdr.get("financingStep"), "ASSIGNMENT"),
                                        criteriaBuilder.equal(joinFinancingHdr.get("financingStep"), "SIGNING"),
                                        criteriaBuilder.equal(joinFinancingHdr.get("financingStep"), "SIGNED")
                                )
                        );
                    } else if (searchValue.equalsIgnoreCase("live")) {
                        predicate = criteriaBuilder.and(
                                predicate,
                                criteriaBuilder.equal(joinFinancingHdr.get("financingStatus"), "LIVE")
                        );
                    } else if (searchValue.equalsIgnoreCase("completed")) {
                        predicate = criteriaBuilder.and(
                                predicate,
                                criteriaBuilder.equal(joinFinancingHdr.get("financingStatus"), "COMPLETED")
                        );
                    }
                }
            }

            return predicate;
        };
    }
}
