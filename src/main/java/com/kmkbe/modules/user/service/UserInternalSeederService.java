package com.kmkbe.modules.user.service;

import com.kmkbe.modules.user.entity.*;
import com.kmkbe.modules.user.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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
    private final MstAppRoleFormUserRepository mstAppRoleFormUserRepository;

    //@Transactional
    public void seed() {
        clearAdmin();
        seedAdmin();
    }

    private void clearAdmin() {
        mstUserRepository.findByUsername("Admin Jakarta 1").ifPresent(mstUserRepository::delete);
        mstUserRepository.findByUsername("Admin Jakarta 2").ifPresent(mstUserRepository::delete);
        mstUserRepository.findByUsername("Admin Jakarta 3").ifPresent(mstUserRepository::delete);

        mstEmployeeRepository.findById("100001").ifPresent(mstEmployeeRepository::delete);
        mstEmployeeRepository.findById("100002").ifPresent(mstEmployeeRepository::delete);
        mstEmployeeRepository.findById("100003").ifPresent(mstEmployeeRepository::delete);
    }

    private void seedAdmin() {
        try {
            MstBranch jakarta1 = mstBranchRepository.findByBranchCode("412").orElseThrow();
            MstBranch jakarta2 = mstBranchRepository.findByBranchCode("413").orElseThrow();
            MstBranch jakarta3 = mstBranchRepository.findByBranchCode("414").orElseThrow();

            MstApplicationRole appRole = mstApplicationRoleRepository
                    .findById(UUID.fromString("ecf84049-ed3c-7622-e88c-f44d50e4cb9d"))
                    .orElse(null);

            MstForm form = mstFormRepository.findById("mnu_brnch_adm")
                    .orElse(null);

            List<MstAppRoleForm> mstAppRoleForms = mstAppRoleFormRepository.findAllByApplicationRoleAndForm(
                    appRole,
                    form
            );

            if (mstAppRoleForms.isEmpty()) {
                mstAppRoleForms.add(seedAdminBranchRoleForm(appRole, form));
            }

            MstEmployee admin1Employee = employee(jakarta1, 1);
            MstUser admin1 = user(admin1Employee, 1);
            mstEmployeeRepository.save(admin1Employee);
            mstUserRepository.save(admin1);

            MstEmployee admin2Employee = employee(jakarta2, 2);
            MstUser admin2 = user(admin2Employee, 2);
            mstEmployeeRepository.save(admin2Employee);
            mstUserRepository.save(admin2);

            MstEmployee admin3Employee = employee(jakarta3, 3);
            MstUser admin3 = user(admin3Employee, 3);
            mstEmployeeRepository.save(admin3Employee);
            mstUserRepository.save(admin3);

            for (MstAppRoleForm appRoleForm : mstAppRoleForms) {
                seedJakarta(admin1, appRoleForm);
                seedJakarta(admin2, appRoleForm);
                seedJakarta(admin3, appRoleForm);
            }
        } catch (Exception e) {
            log.error("seedInternalUser, error {}", e.getMessage());
            throw e;
        }
    }

    private void seedJakarta(
            MstUser user,
            MstAppRoleForm appRoleForm
    ) {
        MstAppRoleFormUser appRoleFormUser = MstAppRoleFormUser.builder()
                .appRoleForm(appRoleForm)
                .user(user)
                .isActive(true)
                .build();

        mstAppRoleFormUserRepository.save(appRoleFormUser);
    }

    private MstUser user(MstEmployee employee, int index) {
        return MstUser.builder()
                .employee(employee)
                .username("Admin Jakarta " + index)
                .password(bcryptEncoder.encode("admin123"))
                .isUserAd(false)
                .isUserNonad(true)
                .isActive(true)
                .build();
    }

    private MstEmployee employee(MstBranch branch, int index) {
        return MstEmployee.builder()
                .branch(branch)
                .employeeName("Admin Jakarta " + index)
                .employeeType("Contract")
                .employeeCode("10000" + index)
                .email("mail@gmail.com")
                .phone("08131")
                .isActive(true)
                .build();
    }

    private MstAppRoleForm seedAdminBranchRoleForm(
            MstApplicationRole appRole,
            MstForm form
    ) {
        MstAppRoleForm appRoleForm = MstAppRoleForm.builder()
                .applicationRole(appRole)
                .form(form)
                .build();

       return mstAppRoleFormRepository.save(appRoleForm);
    }
}
