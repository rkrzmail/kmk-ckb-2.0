package com.kmkbe.modules.user.service;

import com.kmkbe.modules.user.entity.*;
import com.kmkbe.modules.user.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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
        seedBranchAdmin();
    }

    private void clearAdmin() {
        Optional<MstUser> admin1 = mstUserRepository.findByUsername("Admin Jakarta 1");
        Optional<MstUser> admin2 = mstUserRepository.findByUsername("Admin Jakarta 2");
        Optional<MstUser> admin3 = mstUserRepository.findByUsername("Admin Jakarta 3");

        if (admin1.isPresent()) {
            mstAppRoleFormUserRepository
                    .deleteAll(mstAppRoleFormUserRepository.findAllByUser(admin1.get()));
            mstUserRepository.delete(admin1.get());

            mstAppRoleFormUserRepository
                    .deleteAll(mstAppRoleFormUserRepository.findAllByUser(admin2.get()));
            mstUserRepository.delete(admin2.get());

            mstAppRoleFormUserRepository
                    .deleteAll(mstAppRoleFormUserRepository.findAllByUser(admin3.get()));
            mstUserRepository.delete(admin3.get());
        }

        mstEmployeeRepository.findById("100001").ifPresent(mstEmployeeRepository::delete);
        mstEmployeeRepository.findById("100002").ifPresent(mstEmployeeRepository::delete);
        mstEmployeeRepository.findById("100003").ifPresent(mstEmployeeRepository::delete);
    }

    private void seedBranchAdmin() {
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
                mstAppRoleForms.add(seedRoleForm(appRole, form));
            }

            MstEmployee admin1Employee = employeeBranchAdmin(jakarta1, 1);
            MstUser admin1 = userBranchAdmin(admin1Employee, 1);
            mstEmployeeRepository.save(admin1Employee);
            mstUserRepository.save(admin1);

            MstEmployee admin2Employee = employeeBranchAdmin(jakarta2, 2);
            MstUser admin2 = userBranchAdmin(admin2Employee, 2);
            mstEmployeeRepository.save(admin2Employee);
            mstUserRepository.save(admin2);

            MstEmployee admin3Employee = employeeBranchAdmin(jakarta3, 3);
            MstUser admin3 = userBranchAdmin(admin3Employee, 3);
            mstEmployeeRepository.save(admin3Employee);
            mstUserRepository.save(admin3);

            for (MstAppRoleForm appRoleForm : mstAppRoleForms) {
                seedAppRoleFormUser(admin1, appRoleForm);
                seedAppRoleFormUser(admin2, appRoleForm);
                seedAppRoleFormUser(admin3, appRoleForm);
            }
        } catch (Exception e) {
            log.error("seedInternalUser, error {}", e.getMessage());
            throw e;
        }
    }

    private void seedAppRoleFormUser(
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

    private MstUser userBranchAdmin(MstEmployee employee, int index) {
        return MstUser.builder()
                .employee(employee)
                .username("Admin Jakarta " + index)
                .password(bcryptEncoder.encode("admin123"))
                .isUserAd(false)
                .isUserNonad(true)
                .isActive(true)
                .build();
    }

    private MstUser userMajorAccount(MstEmployee employee, int index) {
        return MstUser.builder()
                .employee(employee)
                .username("major.acc." + index)
                .password(bcryptEncoder.encode("admin123"))
                .isUserAd(false)
                .isUserNonad(true)
                .isActive(true)
                .build();
    }

    private MstEmployee employeeBranchAdmin(MstBranch branch, int index) {
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

    private MstEmployee employeeMajorAccount(MstBranch branch, int index) {
        return MstEmployee.builder()
                .branch(branch)
                .employeeName("Major Account " + index)
                .employeeType("Contract")
                .employeeCode("100001" + index)
                .email("vandikalvandi@gmail.com")
                .phone("08131")
                .isActive(true)
                .build();
    }

    private MstAppRoleForm seedRoleForm(
            MstApplicationRole appRole,
            MstForm form
    ) {
        MstAppRoleForm appRoleForm = MstAppRoleForm.builder()
                .applicationRole(appRole)
                .form(form)
                .build();

        return mstAppRoleFormRepository.save(appRoleForm);
    }

    private void seedMajorAccount() {
        try {
            MstApplicationRole appRole = mstApplicationRoleRepository
                    .findById(UUID.fromString("4ab9812c-aba1-cdbf-111b-558c2b20e2aa"))
                    .orElse(null);

            MstForm form = mstFormRepository.findById("menu_mjr_account")
                    .orElse(null);

            List<MstAppRoleForm> mstAppRoleForms = mstAppRoleFormRepository.findAllByApplicationRoleAndForm(
                    appRole,
                    form
            );

            if (mstAppRoleForms.isEmpty()) {
                mstAppRoleForms.add(seedRoleForm(appRole, form));
            }

            MstBranch jakarta1 = mstBranchRepository.findByBranchCode("412").orElseThrow();
            MstBranch jakarta2 = mstBranchRepository.findByBranchCode("413").orElseThrow();

            MstEmployee mjr1Employee = employeeMajorAccount(jakarta1, 1);
            MstUser mjr1 = userBranchAdmin(mjr1Employee, 1);

            MstEmployee mjr2Employee = employeeMajorAccount(jakarta2, 1);
            MstUser mjr2 = userBranchAdmin(mjr2Employee, 1);

            for (MstAppRoleForm appRoleForm : mstAppRoleForms) {
                seedAppRoleFormUser(mjr1, appRoleForm);
                seedAppRoleFormUser(mjr2, appRoleForm);
            }
        } catch (Exception e) {
            log.error("seedMajorAccount, error {}", e.getMessage());
            throw e;
        }
    }
}
