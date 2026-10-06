package com.kmkbe.modules.master.service;

import com.kmkbe.core.domain.entity.MstFileType;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.AgreementFileRepository;
import com.kmkbe.core.domain.repository.AgreementFileSigningRepository;
import com.kmkbe.core.domain.repository.LegalFileRepository;
import com.kmkbe.core.domain.repository.MstFileTypeRepository;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.modules.master.request.FileTypeRequest;
import com.kmkbe.modules.master.response.FileTypeResponse;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class FileTypeMasterService {
    private static final Map<String, String> FIELDS = Map.of(
        "fileTypeCode", "fileTypeCode", "fileTypeId", "fileTypeId",
        "fileTypeName", "fileTypeName", "fileTypeDesc", "fileTypeDesc",
        "fileAllocation", "fileAllocation", "bouwheerCode", "bouwheerCode",
        "maxSizeMb", "maxSizeMb", "isMandatory", "isMandatory",
        "dtmCrt", "dtmCrt", "dtmUpd", "dtmUpd"
    );
    private static final Set<String> SEARCH_FIELDS = Set.of(
        "fileTypeCode", "fileTypeId", "fileTypeName", "fileTypeDesc",
        "fileAllocation", "bouwheerCode", "maxSizeMb", "isMandatory"
    );

    private final MstFileTypeRepository repository;
    private final BouwheerRepository bouwheerRepository;
    private final LegalFileRepository legalFileRepository;
    private final AgreementFileRepository agreementFileRepository;
    private final AgreementFileSigningRepository signingRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public PaginationResult<FileTypeResponse> list(BasePaginationRequest request) {
        int pageNo = request.getPageNo() == null ? 1 : request.getPageNo();
        int pageSize = request.getPageSize() == null ? 10 : request.getPageSize();
        if (pageNo < 1 || pageSize < 1) throw badRequest("pageNo dan pageSize harus minimal 1.");
        String sortBy = request.getSortBy() == null || request.getSortBy().isBlank()
            ? "fileTypeCode" : request.getSortBy().trim();
        if (!FIELDS.containsKey(sortBy)) throw badRequest("sortBy tidak didukung: " + sortBy);
        Sort.Direction direction;
        try {
            direction = request.getSortType() == null || request.getSortType().isBlank()
                ? Sort.Direction.ASC : Sort.Direction.fromString(request.getSortType().trim());
        } catch (IllegalArgumentException ex) {
            throw badRequest("sortType harus ASC atau DESC.");
        }
        Specification<MstFileType> filter = (root, query, cb) -> cb.conjunction();
        if (request.getSearchValue() != null && !request.getSearchValue().isBlank()) {
            String searchBy = request.getSearchBy() == null ? "" : request.getSearchBy().trim();
            if (!SEARCH_FIELDS.contains(searchBy)) throw badRequest("searchBy tidak didukung: " + searchBy);
            String value = request.getSearchValue().trim();
            filter = (root, query, cb) -> switch (searchBy) {
                case "fileTypeId", "maxSizeMb" -> {
                    try { yield cb.equal(root.get(searchBy), Long.parseLong(value)); }
                    catch (NumberFormatException ex) { yield cb.disjunction(); }
                }
                case "isMandatory" -> {
                    if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) yield cb.disjunction();
                    yield cb.equal(root.get(searchBy), Boolean.parseBoolean(value));
                }
                case "bouwheerCode" -> {
                    try { yield cb.equal(root.get(searchBy), java.util.UUID.fromString(value)); }
                    catch (IllegalArgumentException ex) { yield cb.disjunction(); }
                }
                default -> cb.like(cb.lower(root.get(searchBy)), "%" + value.toLowerCase(Locale.ROOT) + "%");
            };
        }
        Sort sort = Sort.by(direction, sortBy);
        if (!sortBy.equals("fileTypeCode")) sort = sort.and(Sort.by("fileTypeCode"));
        Page<MstFileType> page = repository.findAll(filter, PageRequest.of(pageNo - 1, pageSize, sort));
        Set<UUID> bouwheerCodes = page.getContent().stream()
            .map(MstFileType::getBouwheerCode).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, String> bouwheerNames = bouwheerCodes.isEmpty() ? Map.of() : StreamSupport
            .stream(bouwheerRepository.findAllById(bouwheerCodes).spliterator(), false)
            .collect(Collectors.toMap(Bouwheer::getBouwheerCode, Bouwheer::getBouwheerName));
        return new PaginationResult<>(pageNo, page.getTotalPages(), page.getTotalElements(),
            page.map(entity -> FileTypeResponse.from(entity, entity.getBouwheerCode() == null
                ? null : bouwheerNames.get(entity.getBouwheerCode()))).getContent());
    }

    @Transactional(readOnly = true)
    public FileTypeResponse get(String code) {
        return toResponse(find(code));
    }

    @Transactional
    public FileTypeResponse create(FileTypeRequest request) {
        String code = request.fileTypeCode().trim();
        if (repository.existsById(code)) throw new BusinessException(HttpStatus.CONFLICT, 409, "File type code sudah digunakan.");
        MstFileType entity = new MstFileType();
        entity.setFileTypeCode(code);
        apply(entity, request);
        entity.setUsrCrt(currentUserService.usernameOrDefault("SYSTEM"));
        entity.setDtmCrt(LocalDateTime.now());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public FileTypeResponse update(String code, FileTypeRequest request) {
        MstFileType entity = find(code);
        if (!entity.getFileTypeCode().equals(request.fileTypeCode().trim())) {
            throw badRequest("fileTypeCode tidak dapat diubah.");
        }
        apply(entity, request);
        entity.setUsrUpd(currentUserService.usernameOrDefault("SYSTEM"));
        entity.setDtmUpd(LocalDateTime.now());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(String code) {
        MstFileType entity = find(code);
        if (legalFileRepository.existsByFileTypeCode_FileTypeCode(code)
            || agreementFileRepository.existsByMstFileType_FileTypeCode(code)
            || signingRepository.existsByFileTypeCode(code)) {
            throw new BusinessException(HttpStatus.CONFLICT, 409, "File type sudah digunakan dan tidak dapat dihapus.");
        }
        try {
            repository.delete(entity);
            repository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(HttpStatus.CONFLICT, 409, "File type masih direferensikan dan tidak dapat dihapus.");
        }
    }

    private MstFileType find(String code) {
        return repository.findById(code).orElseThrow(() ->
            new BusinessException(HttpStatus.NOT_FOUND, 404, "File type tidak ditemukan: " + code));
    }

    private FileTypeResponse toResponse(MstFileType entity) {
        UUID bouwheerCode = entity.getBouwheerCode();
        String bouwheerName = bouwheerCode == null ? null : bouwheerRepository.findById(bouwheerCode)
            .map(Bouwheer::getBouwheerName).orElse(null);
        return FileTypeResponse.from(entity, bouwheerName);
    }

    private void apply(MstFileType entity, FileTypeRequest request) {
        entity.setFileTypeName(request.fileTypeName().trim());
        entity.setFileTypeDesc(request.fileTypeDesc());
        entity.setFileAllocation(request.fileAllocation().trim());
        entity.setIsMandatory(request.isMandatory());
        entity.setMaxSizeMb(request.maxSizeMb());
        entity.setBouwheerCode(request.bouwheerCode());
    }

    private static BusinessException badRequest(String message) {
        return new BusinessException(HttpStatus.BAD_REQUEST, 400, message);
    }
}
