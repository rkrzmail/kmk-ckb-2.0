package com.kmkbe.modules.user.service;

import com.kmkbe.modules.user.entity.*;
import com.kmkbe.modules.user.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserInternalSeederService {
    private final BCryptPasswordEncoder bcryptEncoder;

    private final MstRoleRepository mstRoleRepository;
    private final MstFormRepository mstFormRepository;
    private final MstAppRoleFormRepository mstAppRoleFormRepository;
    private final MstApplicationRepository mstApplicationRepository;
    private final MstApplicationRoleRepository mstApplicationRoleRepository;
    private final MstBranchRepository mstBranchRepository;
    private final MstEmployeeRepository mstEmployeeRepository;
    private final MstUserRepository mstUserRepository;

    @Transactional
    public void seed() {
        seedApplicationRole();
        seedAdmin();
    }

    private void seedApplicationRole() {
        try {
            /*MstRole spAdmin = mstRoleRepository.findByRoleCode("sp_admin").orElseThrow();
            MstRole mjrAccount = mstRoleRepository.findByRoleCode("mjr_account").orElseThrow();
            MstRole finance = mstRoleRepository.findByRoleCode("finance").orElseThrow();
            MstRole management = mstRoleRepository.findByRoleCode("mgmnt").orElseThrow();*/

            MstApplication application = mstApplicationRepository.findByApplicationCode("danasakti").orElseThrow();
            List<MstRole> roles = mstRoleRepository.findAllByIsActive();

            for (MstRole role : roles) {
                int count = mstApplicationRoleRepository.countByRoleCode(role);
                if (count == 0) {
                    MstApplicationRole applicationRole = MstApplicationRole.builder()
                            .applicationCode(application)
                            .roleCode(role)
                            .isActive(true)
                            .build();

                    mstApplicationRoleRepository.save(applicationRole);

                    MstAppRoleForm roleForm = MstAppRoleForm.builder()
                            .applicationRoleCode(applicationRole)
                            .isActive(true)
                            .build();
                }
            }
        } catch (Exception e) {
            log.error("seedRole, error {}", e.getMessage());
            throw e;
        }
    }

    private void seedRoleFormUser() {
        try {

        } catch (Exception e) {
            log.error("seedRoleForm, error {}", e.getMessage());
            throw e;
        }
    }

    private void seedAdmin() {
        try {
            MstBranch jakarta1 = mstBranchRepository.findByBranchCode("412").orElseThrow();
            MstBranch jakarta2 = mstBranchRepository.findByBranchCode("413").orElseThrow();
            MstBranch jakarta3 = mstBranchRepository.findByBranchCode("414").orElseThrow();

            MstEmployee admin1Employee = MstEmployee.builder()
                    .branchCode(jakarta1)
                    .employeeName("Admin Jakarta 1")
                    .employeeType("Contract")
                    .employeeCode("100001")
                    .email("vandikalvandi@gmail.com")
                    .phone("08131")
                    .isActive(true)
                    .build();

            MstUser admin1 = MstUser.builder()
                    .employeeCode(admin1Employee)
                    .username("Admin Jakarta 1")
                    .password(bcryptEncoder.encode("admin12345"))
                    .isUserAd(false)
                    .isUserNonad(true)
                    .isActive(true)
                    .build();

            mstEmployeeRepository.save(admin1Employee);
            mstUserRepository.save(admin1);
        } catch (Exception e) {
            log.error("seedInternalUser, error {}", e.getMessage());
            throw e;
        }
    }
}
