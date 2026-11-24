package com.kmkbe.modules.master.controller;

import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.modules.confinsr3.service.ConfinsR3Service;
import com.kmkbe.modules.master.request.AreaPageRequest;
import com.kmkbe.modules.master.request.FileTypeRequest;
import com.kmkbe.modules.master.response.FileTypeResponse;
import com.kmkbe.modules.master.service.FileTypeMasterService;
import com.kmkbe.modules.master.service.MasterService;
import com.kmkbe.modules.remote.request.ZipCodeRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MasterControllersCoverageTest {
  @Mock FileTypeMasterService fileTypeService;
  @Mock MasterService masterService;
  @Mock ConfinsR3Service confinsR3Service;

  @Test
  void fileTypeControllerDelegates() throws Exception {
    com.kmkbe.core.security.CurrentUserService currentUserService = mock(com.kmkbe.core.security.CurrentUserService.class);
    FileTypeMasterController controller = new FileTypeMasterController(fileTypeService, currentUserService);
    FileTypeRequest request = new FileTypeRequest("DOC001", "Doc", "Desc", "Legal", true, false, 10L, null);
    FileTypeResponse response = new FileTypeResponse("DOC001", 1L, "Doc", "Desc", "Legal", true, false, 10L, null, null, null, null, null, null);

    when(fileTypeService.list(any())).thenReturn(com.kmkbe.core.domain.model.PaginationResult.empty(1));
    when(fileTypeService.get("DOC001")).thenReturn(response);
    when(fileTypeService.create(request)).thenReturn(response);
    when(fileTypeService.update("DOC001", request)).thenReturn(response);

    assertThat(controller.list(new com.kmkbe.helpers.base.BasePaginationRequest()).isSuccess()).isTrue();
    assertThat(controller.get("DOC001").isSuccess()).isTrue();
    assertThat(controller.create(request).isSuccess()).isTrue();
    assertThat(controller.update("DOC001", request).isSuccess()).isTrue();
    assertThat(controller.delete("DOC001").isSuccess()).isTrue();
    verify(fileTypeService).delete("DOC001");
    verify(currentUserService, org.mockito.Mockito.times(5)).authenticatedInternalUser();
  }

  @Test
  void mstControllerDelegates() throws Exception {
    MstController controller = new MstController(masterService, confinsR3Service);
    when(masterService.getOptions(any(), any())).thenReturn(
      new com.kmkbe.core.domain.model.CommonResult<List<com.kmkbe.core.domain.dto.InputOptionsRemoteDto>>()
        .success(List.of()));
    when(masterService.getArea(any())).thenReturn(
      new com.kmkbe.core.domain.model.CommonResult<List<com.kmkbe.core.domain.dto.AreaRemoteDto>>()
        .success(List.of()));
    when(confinsR3Service.pageZipcode(any())).thenReturn(new BaseResponseBuilder<>(true, 200, "OK"));
    when(confinsR3Service.allZipcode(1, 10)).thenReturn(new BaseResponseBuilder<>(true, 200, "OK"));

    assertThat(controller.getOptions(BaseRemoteService.RefMasterTypeCode.TipeID, "personal").isSuccess()).isTrue();
    assertThat(controller.getPageArea(new AreaPageRequest())).isNotNull();
    assertThat(controller.getAllArea(1, 10)).isNotNull();
    assertThat(controller.getArea(new ZipCodeRequest()).isSuccess()).isTrue();
  }
}
