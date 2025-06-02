package com.kmkbe.core.domain.spec;

import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.nikita.utils.Utils;
import jakarta.persistence.criteria.Expression;
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
//    public static Specification<FinancingDtl> custInvoiceFilterBy(UUID financeHdrCode, String searchBy, String searchValue) {
//        return (root, query, criteriaBuilder) -> {
//            Join<FinancingDtl, Invoice> joinInvoice = root.join("invoice", JoinType.LEFT);
//            Join<FinancingDtl, FinancingHdr> joinFinancingHdr = root.join("financingHdr", JoinType.INNER);
//            Join<FinancingHdr, Customer> joinCustomer = joinFinancingHdr.join("customer", JoinType.INNER);
//            Join<FinancingHdr, Agreement> joinAgreement = joinFinancingHdr.join("agreement", JoinType.INNER);
//            Join<FinancingHdr, Bouwheer> joinBouwheer = joinFinancingHdr.join("bouwheer", JoinType.LEFT);
//
//            Predicate predicate = criteriaBuilder.and(
//                    //criteriaBuilder.equal(root.get("financeHdrCode"), financeHdrCode)//financing_hdr_code
//                     criteriaBuilder.equal(joinFinancingHdr.get("financingHdrCode"), financeHdrCode)
//            );
//
//            if (StringUtils.isNotEmpty(searchBy) && StringUtils.isNotEmpty(searchValue)) {
//                if (searchBy.equalsIgnoreCase("customerInvoiceNo")|| searchBy.equalsIgnoreCase("custInvNo" )) {
//                    predicate = criteriaBuilder.and(
//                            predicate,
//                            criteriaBuilder.like(joinInvoice.get("custInvNo"), "%"+searchValue+"%")
//                    );
//                } else  if (searchBy.equalsIgnoreCase("invoiceDescription")) {
//                    predicate = criteriaBuilder.and(
//                            predicate,
//                            criteriaBuilder.like(joinInvoice.get("invoiceDescription"), "%"+searchValue+"%")
//                    );
//                } else  if (searchBy.equalsIgnoreCase("agreementCode")) {
//                    predicate = criteriaBuilder.and(
//                            predicate,
//                            criteriaBuilder.equal(joinAgreement.get("agreementCode"), searchValue)
//                    );
//                } else if (searchBy.equalsIgnoreCase("bouwheerName")) {
//                    predicate = criteriaBuilder.and(
//                            predicate,
//                            criteriaBuilder.like(joinBouwheer.get("bouwheerName"), "%"+searchValue+"%")
//                    );
//                } else if (searchBy.equalsIgnoreCase("invoiceDate")) {//invoiceDueDate
//                    predicate = criteriaBuilder.and(
//                            predicate,
//                            criteriaBuilder.equal(joinInvoice.get("invoiceDate"), DateTimeUtils.formatDateTime(searchValue))
//                    );
//                } else if (searchBy.equalsIgnoreCase("invoiceDueDate")) {//
//                    predicate = criteriaBuilder.and(
//                            predicate,
//                            criteriaBuilder.equal(joinInvoice.get("invoiceDueDate"), DateTimeUtils.formatDateTime(searchValue))
//                    );
//                } else if (searchBy.equalsIgnoreCase("invoiceAmount")) {//invoiceAmount
//                    predicate = criteriaBuilder.and(
//                            predicate,
//                            criteriaBuilder.equal(joinInvoice.get("invoiceAmt"), Utils.getIntCurr(searchValue))
//                    );
//                } else if (searchBy.equalsIgnoreCase("status")) {
//                    if (searchValue.equalsIgnoreCase("new")) {
//                        predicate = criteriaBuilder.and(
//                                predicate,
//                                criteriaBuilder.equal(joinFinancingHdr.get("financingStatus"), "NEW"),
//                                criteriaBuilder.equal(joinFinancingHdr.get("financingStep"), "NEW")
//                        );
//                    } else if (searchValue.equalsIgnoreCase("inprocess")) {
//                        predicate = criteriaBuilder.and(
//                                predicate,
//                                criteriaBuilder.equal(joinFinancingHdr.get("financingStatus"), "INPROCESS"),
//                                criteriaBuilder.or(
//                                        criteriaBuilder.equal(joinFinancingHdr.get("financingStep"), "INPROCESS"),
//                                        criteriaBuilder.equal(joinFinancingHdr.get("financingStep"), "ASSIGNMENT"),
//                                        criteriaBuilder.equal(joinFinancingHdr.get("financingStep"), "SIGNING"),
//                                        criteriaBuilder.equal(joinFinancingHdr.get("financingStep"), "SIGNED")
//                                )
//                        );
//                    } else if (searchValue.equalsIgnoreCase("live")) {
//                        predicate = criteriaBuilder.and(
//                                predicate,
//                                criteriaBuilder.equal(joinFinancingHdr.get("financingStatus"), "LIVE")
//                        );
//                    } else if (searchValue.equalsIgnoreCase("completed")) {
//                        predicate = criteriaBuilder.and(
//                                predicate,
//                                criteriaBuilder.equal(joinFinancingHdr.get("financingStatus"), "COMPLETED")
//                        );
//                    }
//                }
//            }
//
//            return predicate;
//        };
//    }
public static Specification<FinancingDtl> custInvoiceFilterBy(UUID financeHdrCode, String searchBy, String searchValue) {
    return (root, query, cb) -> {
        Join<FinancingDtl, Invoice> joinInvoice = root.join("invoice", JoinType.INNER);
        Join<FinancingDtl, FinancingHdr> joinFinancingHdr = root.join("financingHdr", JoinType.INNER);
        Join<FinancingHdr, Customer> joinCustomer = joinFinancingHdr.join("customer", JoinType.INNER);
        Join<FinancingHdr, Agreement> joinAgreement = joinFinancingHdr.join("agreement", JoinType.LEFT);
        Join<FinancingHdr, Bouwheer> joinBouwheer = joinFinancingHdr.join("bouwheer", JoinType.INNER);

        Predicate predicate = cb.equal(joinFinancingHdr.get("financingHdrCode"), financeHdrCode);

        if (StringUtils.isNotEmpty(searchBy) && StringUtils.isNotEmpty(searchValue)) {
            // Normalize search value
            String normalizedSearchValue = normalizeString(searchValue);

            switch (searchBy.toLowerCase()) {
                case "customerinvoiceno":
                case "custinvno":
                    predicate = cb.and(predicate,
                            cb.like(cb.lower(joinInvoice.get("custInvNo")), "%" + normalizedSearchValue + "%"));
                    break;
                case "invoicedescription":
                    predicate = cb.and(predicate,
                            cb.like(cb.lower(joinInvoice.get("invoiceDescription")), "%" + normalizedSearchValue + "%"));
                    break;
                case "agreementcode":
                    predicate = cb.and(predicate,
                            cb.like(cb.lower(joinAgreement.get("agreementCode")), "%" + normalizedSearchValue + "%"));
                    break;
                case "ponumber":
                    predicate = cb.and(predicate,
                            cb.like(cb.lower(joinInvoice.get("poNumber")), "%" + normalizedSearchValue + "%"));
                    break;
                case "bouwheername":
                    // Handle smart search for bouwheerName
                    Expression<String> cleanedBouwheerName = cb.lower(
                            cb.function("regexp_replace", String.class,
                                    joinBouwheer.get("bouwheerName"),
                                    cb.literal("(\\.|,|pt|cv|tbk|\\s)+"), cb.literal(""), cb.literal("g"))
                    );
                    predicate = cb.and(predicate,
                            cb.like(cleanedBouwheerName, "%" + normalizedSearchValue.replaceAll("(\\.|,|\\s)+", "") + "%"));
                    break;
                case "invoicedate":
                    Expression<String> invoiceDateStr = cb.function("TO_CHAR", String.class,
                            joinInvoice.get("invoiceDate"),
                            cb.literal("DD/MM/YYYY"));
                    predicate = cb.and(predicate,
                            cb.like(invoiceDateStr, "%" + normalizedSearchValue + "%"));
                    break;
                case "invoiceduedate":
                    Expression<String> invoiceDueDateStr = cb.function("TO_CHAR", String.class,
                            joinInvoice.get("invoiceDueDate"),
                            cb.literal("DD/MM/YYYY"));
                    predicate = cb.and(predicate,
                            cb.like(invoiceDueDateStr, "%" + normalizedSearchValue + "%"));
                    break;
                case "invoiceamount":
                    Expression<String> invoiceAmountStr = cb.function("TO_CHAR", String.class,
                            joinInvoice.get("invoiceAmt"),
                            cb.literal("999999999.99"));
                    String cleanedAmount = normalizeInvoiceAmount(searchValue);
                    predicate = cb.and(predicate,
                            cb.like(invoiceAmountStr, "%" + cleanedAmount + "%"));
                    break;

                case "status":
                    switch (normalizedSearchValue) {
                        case "new":
                            predicate = cb.and(predicate,
                                    cb.equal(joinFinancingHdr.get("financingStatus"), "NEW"),
                                    cb.equal(joinFinancingHdr.get("financingStep"), "NEW"));
                            break;
                        case "inprocess":
                            predicate = cb.and(predicate,
                                    cb.equal(joinFinancingHdr.get("financingStatus"), "INPROCESS"),
                                    cb.or(
                                            cb.equal(joinFinancingHdr.get("financingStep"), "INPROCESS"),
                                            cb.equal(joinFinancingHdr.get("financingStep"), "ASSIGNMENT"),
                                            cb.equal(joinFinancingHdr.get("financingStep"), "SIGNING"),
                                            cb.equal(joinFinancingHdr.get("financingStep"), "SIGNED")
                                    ));
                            break;
                        case "live":
                            predicate = cb.and(predicate,
                                    cb.equal(joinFinancingHdr.get("financingStatus"), "LIVE"));
                            break;
                        case "completed":
                            predicate = cb.and(predicate,
                                    cb.equal(joinFinancingHdr.get("financingStatus"), "COMPLETED"));
                            break;
                    }
                    break;

            }
        }

        return predicate;
    };
}

    // Utility untuk normalisasi string (hilangin special chars kecil dan corporate words)
    private static String normalizeString(String input) {
        return input.toLowerCase()
                .replaceAll("(pt|cv|tbk)", "") // remove corporate words
                .replaceAll("[\\s\\.,]+", "") // remove whitespace, dot, and comma
                .trim();
    }

    private static String normalizeInvoiceAmount(String input) {
        if (input == null) return "";

        // Hapus "IDR" (case insensitive), spasi, dan titik
        String cleaned = input.toUpperCase()
                .replace("IDR", "")
                .replaceAll("[\\.\\s]", "")  // hapus titik dan spasi
                .trim();

        return cleaned;
    }
}
