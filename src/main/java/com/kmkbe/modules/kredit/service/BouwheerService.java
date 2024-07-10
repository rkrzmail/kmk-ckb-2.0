package com.kmkbe.modules.kredit.service;


import com.kmkbe.modules.kredit.repository.BouwheerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BouwheerService {
    private final BouwheerRepository bouwheerRepository;
}
