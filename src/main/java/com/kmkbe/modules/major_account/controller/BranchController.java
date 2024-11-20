package com.kmkbe.modules.major_account.controller;

import com.kmkbe.core.domain.dto.BranchAreaMappingDto;
import com.kmkbe.core.domain.dto.BranchDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.modules.major_account.service.BranchAreaMappingService;
import com.kmkbe.modules.major_account.service.MstBranchService;
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
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/branches")
@Tag(
        name = "Penempatan cabang API",
        description = "Berisi endpoints data penembatan cabang / branch area mapping"
)
@RequiredArgsConstructor
public class BranchController {
    private final MstBranchService mstBranchService;
    private final BranchAreaMappingService branchAreaMappingService;

    @GetMapping("/master")
    public CommonResult<List<BranchDto>> getBranchListA() {
        return new CommonResult<List<BranchDto>>().success(
                mstBranchService.branchList("")
        );
    }

    @GetMapping("/list")
    public CommonResult<PaginationResult<BranchAreaMappingDto>> getList(
            Authentication authentication, PaginationRequest request
    ) throws SignatureException {
        UserInternalUtils.authenticated(authentication);
        return new CommonResult<PaginationResult<BranchAreaMappingDto>>().success(
                branchAreaMappingService.listBranch(request)
        );
    }

    @GetMapping("/placement")
    public CommonResult<PaginationResult<BranchAreaMappingDto>> getBranchList(
            Authentication authentication, PaginationRequest request
    ) throws SignatureException {
        UserInternalUtils.authenticated(authentication);
        return new CommonResult<PaginationResult<BranchAreaMappingDto>>().success(
                branchAreaMappingService.placementBranch(request)
        );
    }

    @PostMapping(
            name = "/placement/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public CommonResult<Object> postUploadPlacementBranch(
            HttpServletRequest httpServletRequest,
            Authentication authentication,
            @Valid @RequestPart MultipartFile file
    ) {
        return new CommonResult<>().success(
                null
        );
       // return   branchAreaMappingService.updateBranch(httpServletRequest, file);

    }
}
