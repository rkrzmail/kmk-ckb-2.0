package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.BranchAreaMapping;
import com.kmkbe.modules.user.entity.MstBranch;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface BranchAreaMappingRepository extends JpaRepository<BranchAreaMapping, String>, JpaSpecificationExecutor<BranchAreaMapping> {
    List<BranchAreaMapping> findAllByMstBranch(MstBranch branch);

    Optional<BranchAreaMapping> findTopByAreaAndCityAndProvince(@Size(max = 50) @NotNull String area, @Size(max = 50) @NotNull String city, @Size(max = 50) @NotNull String province);

    @EntityGraph(attributePaths = "mstBranch")
    Optional<BranchAreaMapping> findFirstByAreaIgnoreCaseAndCityIgnoreCaseAndProvinceIgnoreCaseAndIsActiveTrue(
      String area,
      String city,
      String province
    );

    @EntityGraph(attributePaths = "mstBranch")
    Optional<BranchAreaMapping> findFirstByCityIgnoreCaseAndIsActiveTrue(String city);



    Optional<BranchAreaMapping> findTopByCity(@Size(max = 50) @NotNull String city);


    @Query(value = "SELECT * FROM branch_area_mapping WHERE city ILIKE :city", nativeQuery = true)
    Optional<BranchAreaMapping> findByCityIgnoreCase(@Param("city") String city);

}
