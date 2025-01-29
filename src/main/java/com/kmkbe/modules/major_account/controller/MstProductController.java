package com.kmkbe.modules.major_account.controller;

import com.kmkbe.core.domain.dto.ProductDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.major_account.service.BranchAreaMappingService;
import com.kmkbe.modules.major_account.service.MstProductService;

import com.kmkbe.modules.user.utils.UserInternalUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.SignatureException;

@Validated
@RestController
@RequestMapping("/api/v1/product")
@Tag(
        name = "Penempatan cabang API",
        description = "Berisi endpoints data penembatan cabang / branch area mapping"
)
@RequiredArgsConstructor
public class MstProductController {
    private final MstProductService productService;
    private final BranchAreaMappingService branchAreaMappingService;


    @GetMapping("/list")
    public CommonResult<PaginationResult<ProductDto>> getList(
            Authentication authentication, PaginationRequest request
    ) throws SignatureException {
        UserInternalUtils.authenticated(authentication);
        return new CommonResult<PaginationResult<ProductDto>>().success(
                productService.listProduct(request)
        );
    }

    @GetMapping("/listitem/{id}")
    public CommonResult<PaginationResult<ProductDto>> getListItem(
            Authentication authentication, PaginationRequest request,
              @PathVariable("id") Long id
    ) throws SignatureException {
        UserInternalUtils.authenticated(authentication);
        return new CommonResult<PaginationResult<ProductDto>>().success(
                productService.listProductItem(request, id)
        );
    }

    @PostMapping(
            value = "/update/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public CommonResult<Object> postUploadPlacementBranch(
            HttpServletRequest httpServletRequest,
            Authentication authentication,
            @Valid @RequestPart MultipartFile file
    ) throws SignatureException {
        UserInternalUtils.authenticated(authentication);

        productService.uploadProduct(httpServletRequest, authentication, file);
        return new CommonResult<>().success(   null );
    }
}
