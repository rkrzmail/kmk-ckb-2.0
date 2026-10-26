package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.entity.DokumentHistoryUpload;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.LegalFile;
import com.kmkbe.core.domain.entity.MstFileType;
import com.kmkbe.core.domain.repository.DokumentHistoryUploadRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.LegalFileRepository;
import com.kmkbe.core.domain.repository.MstFileTypeRepository;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.entity.MstEmployee;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class DocumentUploadServiceTest {
  @Mock CurrentUserService currentUserService;
  @Mock MstAppRoleFormUserRepository roleRepository;
  @Mock FinancingHdrRepository financingHdrRepository;
  @Mock MstFileTypeRepository fileTypeRepository;
  @Mock DokumentHistoryUploadRepository uploadRepository;
  @Mock LegalFileRepository legalFileRepository;
  @Mock FileStorageService fileStorageService;
  @InjectMocks DocumentUploadService service;

  private final UUID code = UUID.randomUUID();
  private final UUID userCode = UUID.randomUUID();
  private final UUID bouwheerCode = UUID.randomUUID();
  private FinancingHdr hdr;

  @BeforeEach
  void setup() throws Exception {
    MstBranch branch = new MstBranch();
    branch.setBranchCode("412");
    MstEmployee employee = new MstEmployee();
    employee.setBranch(branch);
    MstUser user = new MstUser();
    user.setUserCode(userCode);
    user.setUsername("admin412");
    user.setEmployee(employee);
    when(currentUserService.internalUser()).thenReturn(user);

    hdr = new FinancingHdr();
    hdr.setFinancingHdrCode(code);
    hdr.setMstBranch(branch);
    Bouwheer bouwheer = new Bouwheer();
    bouwheer.setBouwheerCode(bouwheerCode);
    hdr.setBouwheer(bouwheer);
    Customer customer = new Customer();
    customer.setCustCode(UUID.randomUUID());
    hdr.setCustomer(customer);
  }

  @Test
  void rejectsUserWithoutBranchAdminRole() {
    assertThatThrownBy(() -> service.latest(code, "DOC006")).isInstanceOf(BusinessException.class);
    verify(financingHdrRepository, never()).findByFinancingHdrCode(any());
  }

  @Test
  void rejectsOtherBranch() {
    when(roleRepository.hasActiveBranchAdminRole(userCode)).thenReturn(true);
    MstBranch other = new MstBranch();
    other.setBranchCode("413");
    hdr.setMstBranch(other);
    when(financingHdrRepository.findByFinancingHdrCode(code)).thenReturn(Optional.of(hdr));
    assertThatThrownBy(() -> service.latest(code, "DOC006")).isInstanceOf(BusinessException.class);
    verify(uploadRepository, never()).findTopByFinancingHdr_FinancingHdrCodeAndFileType_FileTypeCodeOrderByUploadedAtDescIdDesc(any(), any());
  }

  @Test
  void uploadAppendsHistoryForLead() throws Exception {
    when(roleRepository.hasActiveBranchAdminRole(userCode)).thenReturn(true);
    when(financingHdrRepository.findByFinancingHdrCode(code)).thenReturn(Optional.of(hdr));
    MstFileType type = MstFileType.builder().fileTypeCode("DOC006").bouwheerCode(bouwheerCode).maxSizeMb(10L).build();
    when(fileTypeRepository.findByFileTypeCodeAndBouwheerCode("DOC006", bouwheerCode)).thenReturn(Optional.of(type));
    when(fileStorageService.save(any(), any(), eq("signed.pdf"), eq("pdf"))).thenReturn("stored/signed.pdf");
    when(currentUserService.internalUsername()).thenReturn("admin412");
    when(uploadRepository.saveAndFlush(any())).thenAnswer(call -> {
      DokumentHistoryUpload upload = call.getArgument(0);
      upload.setId(7L);
      return upload;
    });
    MockMultipartFile file = new MockMultipartFile("file", "signed.pdf", "application/pdf", "%PDF-1.7".getBytes());

    var result = service.upload(code, "DOC006", file, LocalDate.of(2027, 12, 31), "SIT-001");

    assertThat(result.id()).isEqualTo(7L);
    assertThat(result.financingHdrCode()).isEqualTo(code);
    assertThat(result.expiredDate()).isEqualTo(LocalDate.of(2027, 12, 31));
    ArgumentCaptor<LegalFile> activeFile = ArgumentCaptor.forClass(LegalFile.class);
    verify(legalFileRepository).saveAndFlush(activeFile.capture());
    assertThat(activeFile.getValue().getFinancingHdr().getFinancingHdrCode()).isEqualTo(code);
    assertThat(activeFile.getValue().getFileTypeCode().getFileTypeCode()).isEqualTo("DOC006");
    assertThat(activeFile.getValue().getFilePath()).isEqualTo("stored/signed.pdf");
    verify(uploadRepository).saveAndFlush(any());
  }

  @Test
  void nextSitUploadUpdatesActiveFileAndAppendsHistory() throws Exception {
    when(roleRepository.hasActiveBranchAdminRole(userCode)).thenReturn(true);
    when(financingHdrRepository.findByFinancingHdrCode(code)).thenReturn(Optional.of(hdr));
    MstFileType type = MstFileType.builder().fileTypeCode("DOC006").bouwheerCode(bouwheerCode).build();
    when(fileTypeRepository.findByFileTypeCodeAndBouwheerCode("DOC006", bouwheerCode)).thenReturn(Optional.of(type));
    LegalFile active = new LegalFile();
    active.setFileId(42L);
    active.setFilePath("stored/old.pdf");
    when(legalFileRepository.findByFinancingHdr_FinancingHdrCodeAndFileTypeCode_FileTypeCode(code, "DOC006"))
      .thenReturn(Optional.of(active));
    when(fileStorageService.save(any(), any(), eq("new.pdf"), eq("pdf"))).thenReturn("stored/new.pdf");
    when(currentUserService.internalUsername()).thenReturn("admin412");
    when(uploadRepository.saveAndFlush(any())).thenAnswer(call -> {
      DokumentHistoryUpload upload = call.getArgument(0);
      upload.setId(8L);
      return upload;
    });
    MockMultipartFile file = new MockMultipartFile("file", "new.pdf", "application/pdf", "%PDF-1.7".getBytes());

    service.upload(code, "DOC006", file, LocalDate.of(2028, 12, 31), "versi 2");

    assertThat(active.getFileId()).isEqualTo(42L);
    assertThat(active.getFilePath()).isEqualTo("stored/new.pdf");
    verify(legalFileRepository).saveAndFlush(active);
    verify(uploadRepository).saveAndFlush(any());
  }

  @Test
  void latestSitDownloadReadsActiveLegalFile() throws Exception {
    when(roleRepository.hasActiveBranchAdminRole(userCode)).thenReturn(true);
    when(financingHdrRepository.findByFinancingHdrCode(code)).thenReturn(Optional.of(hdr));
    LegalFile active = new LegalFile();
    active.setFilePath("stored/current.pdf");
    active.setFileName("current.pdf");
    when(legalFileRepository.findByFinancingHdr_FinancingHdrCodeAndFileTypeCode_FileTypeCode(code, "DOC006"))
      .thenReturn(Optional.of(active));
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("cd", "attachment");

    service.downloadLatest(code, "DOC006", request);

    verify(fileStorageService).downloadUploadFile(request, "stored/current.pdf", "current.pdf", "attachment");
    verify(uploadRepository, never())
      .findTopByFinancingHdr_FinancingHdrCodeAndFileType_FileTypeCodeOrderByUploadedAtDescIdDesc(any(), any());
  }

  @Test
  void rejectsNonPdfContentBeforeSaving() throws Exception {
    when(roleRepository.hasActiveBranchAdminRole(userCode)).thenReturn(true);
    when(financingHdrRepository.findByFinancingHdrCode(code)).thenReturn(Optional.of(hdr));
    MstFileType type = MstFileType.builder().fileTypeCode("DOC006").bouwheerCode(bouwheerCode).maxSizeMb(10L).build();
    when(fileTypeRepository.findByFileTypeCodeAndBouwheerCode("DOC006", bouwheerCode)).thenReturn(Optional.of(type));
    MockMultipartFile file = new MockMultipartFile("file", "wrong.pdf", "application/pdf", "not a PDF".getBytes());

    assertThatThrownBy(() -> service.upload(code, "DOC006", file, LocalDate.of(2027, 12, 31), null))
      .isInstanceOf(BusinessException.class);
    verify(fileStorageService, never()).save(any(), any(), any(), any());
  }

  @Test
  void sitRequiresExpiredDate() throws Exception {
    when(roleRepository.hasActiveBranchAdminRole(userCode)).thenReturn(true);
    when(financingHdrRepository.findByFinancingHdrCode(code)).thenReturn(Optional.of(hdr));
    MstFileType type = MstFileType.builder().fileTypeCode("DOC006").bouwheerCode(bouwheerCode).build();
    when(fileTypeRepository.findByFileTypeCodeAndBouwheerCode("DOC006", bouwheerCode)).thenReturn(Optional.of(type));
    MockMultipartFile file = new MockMultipartFile("file", "signed.pdf", "application/pdf", "%PDF-1.7".getBytes());

    assertThatThrownBy(() -> service.upload(code, "DOC006", file, null, null))
      .isInstanceOf(BusinessException.class);
    verify(fileStorageService, never()).save(any(), any(), any(), any());
  }

  @Test
  void otherFileTypeCanUseSameUploadContractWithoutExpiry() throws Exception {
    when(roleRepository.hasActiveBranchAdminRole(userCode)).thenReturn(true);
    when(financingHdrRepository.findByFinancingHdrCode(code)).thenReturn(Optional.of(hdr));
    MstFileType type = MstFileType.builder().fileTypeCode("DOC007").bouwheerCode(bouwheerCode).build();
    when(fileTypeRepository.findByFileTypeCodeAndBouwheerCode("DOC007", bouwheerCode)).thenReturn(Optional.of(type));
    when(fileStorageService.save(any(), any(), eq("document.pdf"), eq("pdf"))).thenReturn("stored/document.pdf");
    when(currentUserService.internalUsername()).thenReturn("admin412");
    when(uploadRepository.saveAndFlush(any())).thenAnswer(call -> {
      DokumentHistoryUpload upload = call.getArgument(0);
      upload.setId(8L);
      return upload;
    });
    MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", "%PDF-1.7".getBytes());

    var result = service.upload(code, "DOC007", file, null, "Catatan");

    assertThat(result.fileTypeCode()).isEqualTo("DOC007");
    assertThat(result.expiredDate()).isNull();
    verify(uploadRepository).saveAndFlush(any());
  }
}
