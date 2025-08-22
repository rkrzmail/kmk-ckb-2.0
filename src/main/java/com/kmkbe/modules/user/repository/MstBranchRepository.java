package com.kmkbe.modules.user.repository;

import com.kmkbe.modules.user.entity.MstBranch;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MstBranchRepository extends JpaRepository<MstBranch, String>, JpaSpecificationExecutor<MstBranch> {
    Optional<MstBranch> findByBranchCode(String code);

    @Query(
            value = """
                    select *
                    from
                        users.mst_branch
                    where
                        business_unit = 'CBU' and 
                        ( 
                         city like '%' || :city || '%'
                      or kelurahan like '%' || :kelurahan || '%'
                      or kecamatan like '%' || :kecamatan || '%'
                      )
                    order by
                        branch_id desc
                    limit 1
                    """,
            nativeQuery = true
    )
    Optional<MstBranch> findTopLikeBranchNameRawQuery(
            @Param("city") String city,
            @Param("kelurahan") String kelurahan,
            @Param("kecamatan") String kecamatan
    );

    Optional<MstBranch> findTopByCityContainingIgnoreCaseOrKelurahanContainingIgnoreCaseOrKecamatanContainingIgnoreCase(
            String city,
            String kelurahan,
            String kecamatan
    );

    List<MstBranch> findAllByIsActive(boolean isActive);

    default List<MstBranch> findAllActive() {
        return findAllByIsActive(true);
    }

//    Optional<MstBranch> findByBranchName(String cleanUsername);

    @Query("SELECT m FROM MstBranch m WHERE UPPER(m.branchName) = UPPER(:username)")
    Optional<MstBranch> findByBranchName(@Param("username") String username);

}
