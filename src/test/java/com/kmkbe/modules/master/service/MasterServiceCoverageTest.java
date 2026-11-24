package com.kmkbe.modules.master.service;

import com.kmkbe.core.domain.dto.AreaRemoteDto;
import com.kmkbe.core.domain.dto.BaseMstRemoteResponseDto;
import com.kmkbe.core.domain.dto.InputOptionsRemoteDto;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.modules.remote.request.AreaRemoteRequest;
import com.kmkbe.modules.remote.request.ZipCodeRequest;
import com.kmkbe.modules.remote.service.MstRemoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MasterServiceCoverageTest {
  @Mock MstRemoteService mstRemoteService;
  MasterService service;

  @BeforeEach
  void setUp() {
    service = new MasterService(mstRemoteService);
  }

  @Test
  void getOptionsValidatesTypeAndDelegates() {
    BaseMstRemoteResponseDto<List<InputOptionsRemoteDto>> response = new BaseMstRemoteResponseDto<>();
    response.setData(List.of(new InputOptionsRemoteDto()));
    when(mstRemoteService.refMasterInputOption(any(), any())).thenReturn(response);

    assertThat(service.getOptions(BaseRemoteService.RefMasterTypeCode.TipeID, null).getData()).hasSize(1);
    assertThat(service.getOptions(BaseRemoteService.RefMasterTypeCode.TipeID, "personal").getData()).hasSize(1);
    assertThat(service.getOptions(BaseRemoteService.RefMasterTypeCode.TipeID, "COMPANY").getData()).hasSize(1);

    assertThatThrownBy(() -> service.getOptions(BaseRemoteService.RefMasterTypeCode.TipeID, "invalid"))
      .isInstanceOf(RuntimeException.class);

  }

  @Test
  void getAreaBuildsAllCriteriaAndFallsBackToDummy() throws Exception {
    ZipCodeRequest request = new ZipCodeRequest();
    request.setPageNo(1);
    request.setRowPerPage(10);
    request.setCity("Jakarta");
    request.setKecamatan("Pusat");
    request.setKelurahan("Kebon");
    request.setZipCode("10110");

    BaseMstRemoteResponseDto<List<AreaRemoteDto>> response = new BaseMstRemoteResponseDto<>();
    response.setData(List.of(new AreaRemoteDto()));
    when(mstRemoteService.areaQuery(any(), any(), any())).thenReturn(response);

    assertThat(service.getArea(request).getData()).hasSize(1);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<AreaRemoteRequest>> captor = ArgumentCaptor.forClass(List.class);
    verify(mstRemoteService).areaQuery(org.mockito.ArgumentMatchers.eq(1),
      org.mockito.ArgumentMatchers.eq(10), captor.capture());
    assertThat(captor.getValue()).hasSize(4);

    when(mstRemoteService.areaQuery(any(), any(), any())).thenReturn(null);
    BaseMstRemoteResponseDto<List<AreaRemoteDto>> dummy = new BaseMstRemoteResponseDto<>();
    dummy.setData(List.of(new AreaRemoteDto()));
    when(mstRemoteService.dummyZipCode()).thenReturn(dummy);
    assertThat(service.getArea(request).getData()).hasSize(1);
  }
}
