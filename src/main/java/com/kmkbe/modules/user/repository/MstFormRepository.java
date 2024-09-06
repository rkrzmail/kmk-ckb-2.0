package com.kmkbe.modules.user.repository;

import com.kmkbe.modules.user.entity.MstForm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MstFormRepository extends JpaRepository<MstForm, String> {
    List<MstForm> findAllByFormCodeIn(List<String> formCodes);

    default List<MstForm> findAllSuperAdminAccount() {
        return findAllByFormCodeIn(List.of(
                "menu_mjr_account",
                "mnu_distr_cbg",
                "mnu_mst_cbg",
                "mnu_mst_prd"
        ));
    }

    default List<MstForm> findAllMajorAccount() {
        return findAllByFormCodeIn(List.of(
                "menu_mjr_account"
        ));
    }

    default List<MstForm> findAllFinanceAccount() {
        return findAllByFormCodeIn(List.of(
                ""
        ));
    }

    default List<MstForm> findAllManagementAccount() {
        return findAllByFormCodeIn(List.of(
                ""
        ));
    }
}
