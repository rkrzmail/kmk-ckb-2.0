package com.kmkbe.modules.kredit.service;

import com.kmkbe.modules.kredit.entity.MstFileType;
import com.kmkbe.modules.kredit.repository.LegalFileRepository;
import com.kmkbe.modules.kredit.repository.MstFileTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DokumenService {
    private final MstFileTypeRepository mstFileTypeRepository;
    private final LegalFileRepository legalFileRepository;

    public List<MstFileType> fetchAll() throws Exception {
        throw new Exception("Api not implemented yet");
    }
}
