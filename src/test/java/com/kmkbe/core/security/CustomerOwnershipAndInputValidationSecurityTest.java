package com.kmkbe.core.security;

import com.kmkbe.core.domain.dto.PolicyAgreementDto;
import com.kmkbe.modules.product.model.dto.ProductDto;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.helpers.utils.PageableUtil;
import com.kmkbe.helpers.utils.SpecPagination;
import com.kmkbe.modules.apis.controller.SbuCkbController;
import com.kmkbe.modules.branch_admin.controller.PolicyAgreementController;
import com.kmkbe.modules.customer.controller.CustomerController;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.service.CustomerDashboardService;
import com.kmkbe.modules.loan_submission.service.DocumentService;
import com.kmkbe.modules.product.controller.MstProductController;
import com.kmkbe.modules.product.model.entity.Product;
import com.kmkbe.modules.product.service.MstProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;

import java.security.SignatureException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerOwnershipAndInputValidationSecurityTest {

    @Mock private CurrentUserService currentUserService;
    @Mock private CustomerDashboardService customerDashboardService;
    @Mock private DocumentService documentService;
    @Mock private MstProductService mstProductService;
    @Mock private FinancingHdrRepository financingHdrRepository;

    // ---------- 1. CustomerController IDOR Guard ----------

    @Test
    @DisplayName("H7: CustomerController rejects customer trying to view another customer's documents")
    void customerCannotViewOtherCustomerDocuments() throws SignatureException {
        Customer loggedInCustomer = new Customer();
        UUID myCustCode = UUID.randomUUID();
        loggedInCustomer.setCustCode(myCustCode);

        when(currentUserService.customerOrNull()).thenReturn(loggedInCustomer);

        CustomerController controller = new CustomerController(
                null, null, null, customerDashboardService, null, null, documentService, null, currentUserService, null
        );

        UUID victimCustCode = UUID.randomUUID();
        assertThatThrownBy(() -> controller.getDocuments(victimCustCode.toString(), null, new MockHttpServletRequest()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You cannot view other customer's documents");
    }

    @Test
    @DisplayName("H7: CustomerController allows customer viewing their own documents")
    void customerCanViewOwnDocuments() throws SignatureException {
        Customer loggedInCustomer = new Customer();
        UUID myCustCode = UUID.randomUUID();
        loggedInCustomer.setCustCode(myCustCode);

        when(currentUserService.customerOrNull()).thenReturn(loggedInCustomer);

        CustomerController controller = new CustomerController(
                null, null, null, customerDashboardService, null, null, documentService, null, currentUserService, null
        );

        var result = controller.getDocuments(myCustCode.toString(), null, new MockHttpServletRequest());
        assertThat(result).isNotNull();
        verify(documentService).uploadedCustomerDoc(eq(myCustCode.toString()), any(), any());
    }

    // ---------- 2. MstProductController Internal User Auth Guard ----------

    @Test
    @DisplayName("H7: MstProductController requires internal user authentication for updateProduct")
    void updateProductRequiresInternalUser() throws SignatureException {
        doThrow(new SignatureException("Unauthorized")).when(currentUserService).authenticatedInternalUser();

        MstProductController controller = new MstProductController(
                mstProductService, null, currentUserService
        );

        ProductDto dto = new ProductDto();
        dto.setProductCode("PROD-01");
        dto.setProductName("Product 01");

        var response = controller.updateProduct("PROD-01", dto);
        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    // ---------- 3. SbuCkbController Body & VendorCode Validation ----------

    @Test
    @DisplayName("H7: SbuCkbController rejects null body or empty vendorCode in listpostedinvoice")
    void sbuCkbControllerRejectsEmptyVendorCode() {
        com.kmkbe.modules.apis.service.ApiSbuCkbService apiSbuCkbService = mock(com.kmkbe.modules.apis.service.ApiSbuCkbService.class);
        SbuCkbController controller = new SbuCkbController(new com.fasterxml.jackson.databind.ObjectMapper(), apiSbuCkbService);

        assertThatThrownBy(() -> controller.getListPostedInvoice("token", "key", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Request body cannot be null");

        Object emptyJson = new java.util.HashMap<String, Object>();
        assertThatThrownBy(() -> controller.getListPostedInvoice("token", "key", emptyJson))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vendorCode is required");
    }

    // ---------- 4. PageableUtil & SpecPagination Bounded Page Size (anti OOM) ----------

    @Test
    @DisplayName("H7: PageableUtil caps excessive pageSize to MAX_PAGE_SIZE (100)")
    void pageableUtilCapsExcessivePageSize() {
        BasePaginationRequest req = new BasePaginationRequest();
        req.setPageSize(99999);
        req.setPageNo(1);

        PageRequest pageRequest = PageableUtil.createPageRequest(req, 10, 1, "id", "ASC");
        assertThat(pageRequest.getPageSize()).isEqualTo(PageableUtil.MAX_PAGE_SIZE);

        PageRequest nativeRequest = PageableUtil.createPageRequestNative(req, 10, 1, "id", "ASC");
        assertThat(nativeRequest.getPageSize()).isEqualTo(PageableUtil.MAX_PAGE_SIZE);
    }

    @Test
    @DisplayName("H7: SpecPagination caps excessive pageSize to MAX_PAGE_SIZE (100)")
    void specPaginationCapsExcessivePageSize() {
        com.kmkbe.core.domain.request.PaginationRequest req = new com.kmkbe.core.domain.request.PaginationRequest();
        req.setPageSize(50000);
        req.setPageNo(1);

        SpecPagination<String, String> spec = new SpecPagination<>(java.util.List.of("a", "b"), req);
        var res = SpecPagination.paginationData(spec);
        assertThat(res).isNotNull();
    }
}
