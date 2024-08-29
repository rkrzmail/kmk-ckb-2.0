package com.kmkbe.core.domain.spec;

import com.kmkbe.core.domain.entity.Bouwheer;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.FinancingHdr;
import io.netty.util.internal.StringUtil;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class FinancingHdrSpec {
    public static Specification<FinancingHdr> bySearchBy(String searchBy, String value) {
        return (root, query, criteriaBuilder) -> {
            Join<FinancingHdr, Customer> joinCust = root.join("cust_code", JoinType.INNER);
            Join<FinancingHdr, Bouwheer> joinBouwheer = root.join("bouwheer_code", JoinType.INNER);

            if (searchBy == null || StringUtil.isNullOrEmpty(value)) {
                return criteriaBuilder.and(criteriaBuilder.equal(root.get("1"), "1"));
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
}
