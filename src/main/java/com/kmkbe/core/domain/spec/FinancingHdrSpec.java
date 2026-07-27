package com.kmkbe.core.domain.spec;

import com.kmkbe.core.domain.entity.*;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import io.netty.util.internal.StringUtil;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;

public class FinancingHdrSpec {
    public static final String FIND_ASSIGNMENT_LIST_SQL = """
            """;


    public static Specification<FinancingHdr> bySearchBy(String searchBy, String value) {
        return (root, query, criteriaBuilder) -> {
            Join<FinancingHdr, Customer> joinCust = root.join("cust_code", JoinType.INNER);
            Join<FinancingHdr, Bouwheer> joinBouwheer = root.join("bouwheer_code", JoinType.INNER);
            Join<FinancingHdr, FinancingDtl> joinDtl = root.join("financing_hdr_code", JoinType.INNER);

            if (searchBy == null || StringUtil.isNullOrEmpty(value)) {
                return criteriaBuilder.conjunction();
            }

            return switch (searchBy.toLowerCase()) {
                case "status" -> criteriaBuilder.and(criteriaBuilder.equal(root.get("financing_status"), value));
                case "namadebitur" -> criteriaBuilder.and(criteriaBuilder.equal(joinCust.get("cust_name"), value));
                case "pemberikerja" ->
                        criteriaBuilder.and(criteriaBuilder.equal(joinBouwheer.get("bouwheer_name"), value));
                case "noinvoice" -> criteriaBuilder.and(criteriaBuilder.equal(joinDtl.get("invoice_code"), value));
                case "cabang" -> null;
                default -> null;
            };
        };
    }

    public static Specification<FinancingHdr> byStepStatus(String searchBy, String value) {
        return (root, query, criteriaBuilder) -> {

            // Define lists of possible statuses and steps
            List<String> financingStatusList = Arrays.asList("INPROCESS", "LIVE", "COMPLETED");
            List<String> financingStepList = Arrays.asList("SIGNED", "GOLIVE", "PAID", "REFUND");

            // Base conditions for status and step filtering
            Predicate stepStatusPredicate = criteriaBuilder.and(
                    root.get("financingStatus").in(financingStatusList),
                    root.get("financingStep").in(financingStepList)
            );

            // Dynamic search filter based on searchBy and value
            Predicate searchPredicate;
            Join<FinancingHdr, Customer> joinCust = root.join("customer", JoinType.INNER);
            Join<FinancingHdr, Bouwheer> joinBouwheer = root.join("bouwheer", JoinType.INNER);
            Join<FinancingHdr, FinancingDtl> joinDtl = root.join("financingDtls", JoinType.INNER);

            if (searchBy == null || value == null || value.isEmpty()) {
                searchPredicate = criteriaBuilder.conjunction(); // No filter applied
            } else {
                // Apply specific filters based on `searchBy` value
                searchPredicate = switch (searchBy.toLowerCase()) {
                    case "status" -> criteriaBuilder.equal(root.get("financing_status"), value);
                    case "namadebitur" -> criteriaBuilder.equal(joinCust.get("cust_name"), value);
                    case "pemberikerja" -> criteriaBuilder.equal(joinBouwheer.get("bouwheer_name"), value);
                    case "noinvoice" -> criteriaBuilder.equal(joinDtl.get("invoice_code"), value);
                    default -> criteriaBuilder.conjunction(); // Default if no match
                };
            }

            // Combine both predicates (step/status and search filters)
            return criteriaBuilder.and(stepStatusPredicate, searchPredicate);
        };
    }


    public static Specification<FinancingHdr> byDisbursement(String searchBy, String value) {
        return (root, query, criteriaBuilder) -> {
            List<String> financingStatusList = Arrays.asList("INPROCESS", "LIVE", "COMPLETED");
            List<String> financingStepList = Arrays.asList("SIGNED", "GOLIVE", "PAID", "REFUND");

            // Base conditions for status and step filtering
            Predicate stepStatusPredicate = criteriaBuilder.and(
                    root.get("financingStatus").in(financingStatusList),
                    root.get("financingStep").in(financingStepList)
            );

            Predicate searchPredicate;
            Join<FinancingHdr,Agreement> joinAgreement = root.join("agreement", JoinType.INNER);
            Join<Agreement, DisbursementLog> joinDisbursement = joinAgreement.join("disbursementLogs", JoinType.INNER);
            Join<FinancingHdr, Customer> joinCust = root.join("customer", JoinType.INNER);
            Join<FinancingHdr, Bouwheer> joinBouwheer = root.join("bouwheer", JoinType.INNER);


            if (searchBy == null || value == null || value.isEmpty()) {
                searchPredicate = criteriaBuilder.conjunction(); // No filter applied
            } else {
                // Apply specific filters based on `searchBy` value
                searchPredicate = switch (searchBy.toLowerCase()) {
                    case "status" -> criteriaBuilder.equal(root.get("financing_status"), value);
                    case "namadebitur" -> criteriaBuilder.equal(joinCust.get("cust_name"), value);
                    case "pemberikerja" -> criteriaBuilder.equal(joinBouwheer.get("bouwheer_name"), value);
                    default -> criteriaBuilder.conjunction(); // Default if no match
                };
            }

            // Combine both predicates (step/status and search filters)
            return criteriaBuilder.and(stepStatusPredicate, searchPredicate);
        };
    }


    public static Specification<FinancingHdr> bySearchTOCBy(String searchBy, String value) {
        return (root, query, criteriaBuilder) -> {
            Join<FinancingHdr, Customer> joinCust = root.join("cust_code", JoinType.INNER);
            Join<FinancingHdr, Bouwheer> joinBouwheer = root.join("bouwheer_code", JoinType.INNER);
            Join<FinancingHdr, FinancingDtl> joinDtl = root.join("financing_hdr_code", JoinType.INNER);

            if (searchBy == null || StringUtil.isNullOrEmpty(value)) {
                return criteriaBuilder.conjunction();
            }

            return switch (searchBy.toLowerCase()) {
                case "status" -> criteriaBuilder.and(criteriaBuilder.equal(root.get("financing_status"), value));
                case "namadebitur" -> criteriaBuilder.and(criteriaBuilder.equal(joinCust.get("cust_name"), value));
                case "pemberikerja" ->
                        criteriaBuilder.and(criteriaBuilder.equal(joinBouwheer.get("bouwheer_name"), value));
                case "noinvoice" -> criteriaBuilder.and(criteriaBuilder.equal(joinDtl.get("invoice_code"), value));
                case "cabang" -> null;
                default -> null;
            };
        };
    }


    public static Specification<SimulationHist> bySimulationHist(String financing_hdr_code,  String searchBy, String value) {
        return (root, query, criteriaBuilder) -> {

            root.get("financing_hdr_code").in(financing_hdr_code);
            // Base conditions for status and step filtering
            Predicate stepStatusPredicate = criteriaBuilder.equal( root.get("financing_hdr_code"),  financing_hdr_code   );

            // Dynamic search filter based on searchBy and value
            Predicate searchPredicate;

            if (searchBy == null || value == null || value.isEmpty()) {
                searchPredicate = criteriaBuilder.conjunction(); // No filter applied
            } else {
                // Apply specific filters based on `searchBy` value
                searchPredicate = criteriaBuilder.conjunction(); // No filter applied
            }

            // Combine both predicates (step/status and search filters)
            //return criteriaBuilder.and(stepStatusPredicate, searchPredicate);
            return stepStatusPredicate;
        };
    }
}
