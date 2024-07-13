package com.kmkbe.modules.loan_submission.service;


import com.kmkbe.modules.loan_submission.repository.BouwheerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BouwheerService {
    private final BouwheerRepository bouwheerRepository;
}
