package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.dto.ProyeksiReportDto;
import com.kmkbe.core.domain.dto.VisitorDto;
import com.kmkbe.core.domain.entity.Visitor;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {

    @Query("SELECT new com.kmkbe.core.domain.dto.VisitorDto(v.debtorName, v.debtorStatus, v.bouwheerName, MIN(v.visitDate), MAX(v.visitDate), COUNT(v.visitDate))" +
            "FROM Visitor v " +
            "WHERE DATE(v.visitDate) BETWEEN :startDate AND :endDate " +
            "GROUP BY v.debtorName, v.debtorStatus, v.bouwheerName " +
            "ORDER BY v.debtorName")
    Page<VisitorDto> getDebtorVisitStats(Pageable pageable, @Param("startDate") String startDate, @Param("endDate") String endDate);
}

