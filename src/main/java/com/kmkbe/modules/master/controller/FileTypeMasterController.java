package com.kmkbe.modules.master.controller;

import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.modules.master.request.FileTypeRequest;
import com.kmkbe.modules.master.response.FileTypeResponse;
import com.kmkbe.modules.master.service.FileTypeMasterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;

@RestController
@RequestMapping("/api/v1/mst/file-types")
@RequiredArgsConstructor
public class FileTypeMasterController {
    private final FileTypeMasterService service;
    private final CurrentUserService currentUserService;

    @GetMapping
    public CommonResult<PaginationResult<FileTypeResponse>> list(@ParameterObject BasePaginationRequest request) throws SignatureException {
        currentUserService.authenticatedInternalUser();
        return new CommonResult<PaginationResult<FileTypeResponse>>().success(service.list(request));
    }

    @GetMapping("/{code}")
    public CommonResult<FileTypeResponse> get(@PathVariable String code) throws SignatureException {
        currentUserService.authenticatedInternalUser();
        return new CommonResult<FileTypeResponse>().success(service.get(code));
    }

    @PostMapping
    public CommonResult<FileTypeResponse> create(@Valid @RequestBody FileTypeRequest request) throws SignatureException {
        currentUserService.authenticatedInternalUser();
        return new CommonResult<FileTypeResponse>().success(service.create(request));
    }

    @PutMapping("/{code}")
    public CommonResult<FileTypeResponse> update(@PathVariable String code, @Valid @RequestBody FileTypeRequest request) throws SignatureException {
        currentUserService.authenticatedInternalUser();
        return new CommonResult<FileTypeResponse>().success(service.update(code, request));
    }

    @DeleteMapping("/{code}")
    public CommonResult<Void> delete(@PathVariable String code) throws SignatureException {
        currentUserService.authenticatedInternalUser();
        service.delete(code);
        return new CommonResult<Void>().success(null);
    }
}
