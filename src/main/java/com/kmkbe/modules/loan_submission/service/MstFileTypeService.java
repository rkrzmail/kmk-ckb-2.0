package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.entity.MstFileType;
import com.kmkbe.core.domain.repository.MstFileTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MstFileTypeService {
    private final MstFileTypeRepository mstFileTypeRepository;

    public List<MstFileType> getAll() {
        return mstFileTypeRepository.findAll();
    }

    public List<MstFileType> getAllMandatory() {
        return mstFileTypeRepository.findAllMandatory();
    }

    public void getByCode() {
    }

    public void create() {
    }

    public void update() {
    }

    public void delete() {
    }
}
