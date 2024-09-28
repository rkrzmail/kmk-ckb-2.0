package com.kmkbe.core.domain.spec;

import com.kmkbe.core.domain.entity.Bouwheer;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.FinancingHdr;
import io.netty.util.internal.StringUtil;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class FinancingHdrSpec {
    public static final String FIND_ASSIGNMENT_LIST_SQL = """
            """;


    public static Specification<FinancingHdr> bySearchBy(String searchBy, String value) {
        return (root, query, criteriaBuilder) -> {
            Join<FinancingHdr, Customer> joinCust = root.join("cust_code", JoinType.INNER);
            Join<FinancingHdr, Bouwheer> joinBouwheer = root.join("bouwheer_code", JoinType.INNER);

            if (searchBy == null || StringUtil.isNullOrEmpty(value)) {
                return criteriaBuilder.conjunction();
            }

            return switch (searchBy.toLowerCase()) {
                case "status" -> criteriaBuilder.and(criteriaBuilder.equal(root.get("financing_status"), value));
                case "namadebitur" -> criteriaBuilder.and(criteriaBuilder.equal(joinCust.get("cust_name"), value));
                case "pemberikerja" ->
                        criteriaBuilder.and(criteriaBuilder.equal(joinBouwheer.get("bouwheer_name"), value));
                case "cabang" -> null;
                default -> null;
            };
        };
    }

    public static Specification<FinancingHdr> byStepStatus() {
        return (root, query, criteriaBuilder) -> {

            return criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("financing_status"),"INPROCESS"),
                    criteriaBuilder.equal(root.get("financing_step"),"SIGNED")
            );
        };
    }
}
