package com.kmkbe.modules.user.service;

import com.kmkbe.modules.user.entity.MstAppRoleForm;
import com.kmkbe.modules.user.entity.MstAppRoleFormUser;
import com.kmkbe.modules.user.entity.MstApplicationRole;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.entity.MstEmployee;
import com.kmkbe.modules.user.entity.MstForm;
import com.kmkbe.modules.user.entity.MstRole;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstAppRoleFormRepository;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import com.kmkbe.modules.user.repository.MstApplicationRepository;
import com.kmkbe.modules.user.repository.MstApplicationRoleRepository;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import com.kmkbe.modules.user.repository.MstEmployeeRepository;
import com.kmkbe.modules.user.repository.MstFormRepository;
import com.kmkbe.modules.user.repository.MstRoleRepository;
import com.kmkbe.modules.user.repository.MstUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserInternalSeederServiceCoverageTest {
  @Mock BCryptPasswordEncoder encoder;
  @Mock MstRoleRepository roleRepository;
  @Mock MstFormRepository formRepository;
  @Mock MstAppRoleFormRepository appRoleFormRepository;
  @Mock MstApplicationRepository applicationRepository;
  @Mock MstApplicationRoleRepository applicationRoleRepository;
  @Mock MstBranchRepository branchRepository;
  @Mock MstEmployeeRepository employeeRepository;
  @Mock MstUserRepository userRepository;
  @Mock MstAppRoleFormUserRepository appRoleFormUserRepository;
  UserInternalSeederService service;
  MstApplicationRole appRole;
  MstForm form;

  @BeforeEach
  void setUp() {
    service = new UserInternalSeederService(encoder, roleRepository, formRepository, appRoleFormRepository,
      applicationRepository, applicationRoleRepository, branchRepository, employeeRepository, userRepository,
      appRoleFormUserRepository);
    when(encoder.encode("admin123")).thenReturn("hash");
    appRole = MstApplicationRole.builder().roleCode(MstRole.builder().roleCode("brnch_adm").build()).build();
    form = MstForm.builder().formCode("mnu_brnch_adm").build();
    when(roleRepository.findByRoleCode(anyString())).thenReturn(Optional.of(MstRole.builder().roleCode("role").build()));
    when(applicationRoleRepository.findByRoleCode(any())).thenReturn(Optional.of(appRole));
    when(formRepository.findById(anyString())).thenReturn(Optional.of(form));
    lenient().when(branchRepository.findByBranchCode("412"))
      .thenReturn(Optional.of(MstBranch.builder().branchCode("412").build()));
    lenient().when(branchRepository.findByBranchCode("413"))
      .thenReturn(Optional.of(MstBranch.builder().branchCode("413").build()));
    lenient().when(branchRepository.findByBranchCode("414"))
      .thenReturn(Optional.of(MstBranch.builder().branchCode("414").build()));
    lenient().when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
    lenient().when(employeeRepository.findById(anyString())).thenReturn(Optional.empty());
    lenient().when(appRoleFormUserRepository.findAllByUser(any())).thenReturn(List.of());
    lenient().when(employeeRepository.save(any())).thenAnswer(call -> call.getArgument(0));
    lenient().when(userRepository.save(any())).thenAnswer(call -> call.getArgument(0));
    lenient().when(appRoleFormRepository.save(any())).thenAnswer(call -> call.getArgument(0));
  }

  @Test
  void seedAdminWithExistingRecordsAndEmptyRoleForms() {
    when(userRepository.findByUsername("Admin Jakarta 1"))
      .thenReturn(Optional.of(MstUser.builder().username("Admin Jakarta 1").build()));
    when(employeeRepository.findById("100002"))
      .thenReturn(Optional.of(MstEmployee.builder().employeeCode("100002").build()));
    when(appRoleFormRepository.findAllByApplicationRoleAndForm(appRole, form)).thenReturn(new ArrayList<>());

    service.seedAdmin();

    verify(userRepository).delete(any(MstUser.class));
    verify(employeeRepository).delete(any(MstEmployee.class));
    verify(appRoleFormRepository).save(any(MstAppRoleForm.class));
    verify(userRepository, times(3)).save(any(MstUser.class));
    verify(appRoleFormUserRepository, times(3)).save(any(MstAppRoleFormUser.class));
  }

  @Test
  void seedMajorAccountWithExistingRoleFormsAndClearBranches() {
    when(userRepository.findByUsername("mjr.acc.1"))
      .thenReturn(Optional.of(MstUser.builder().username("mjr.acc.1").build()));
    when(userRepository.findByUsername("mjr.acc.2"))
      .thenReturn(Optional.of(MstUser.builder().username("mjr.acc.2").build()));
    when(employeeRepository.findById("1000011"))
      .thenReturn(Optional.of(MstEmployee.builder().employeeCode("1000011").build()));
    MstAppRoleForm existing = MstAppRoleForm.builder().build();
    when(appRoleFormRepository.findAllByApplicationRoleAndForm(appRole, form)).thenReturn(List.of(existing));

    service.seedMajorAcc();

    verify(userRepository, times(2)).delete(any(MstUser.class));
    verify(employeeRepository).delete(any(MstEmployee.class));
    verify(appRoleFormUserRepository, times(2)).save(any(MstAppRoleFormUser.class));
  }

  @Test
  void seedFailuresAreRethrown() {
    when(branchRepository.findByBranchCode("412")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.seedAdmin()).isInstanceOf(Exception.class);
    assertThatThrownBy(() -> service.seedMajorAcc()).isInstanceOf(Exception.class);
  }
}
