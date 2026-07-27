package com.kmkbe.modules.branch_admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.mapper.CwrMapper;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.CustomerRepository;
import com.kmkbe.core.domain.repository.CwrRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.branch_admin.request.CreateInquiryCwrRequest;
import com.kmkbe.modules.remote.request.InquiryCwrRemoteRequest;
import com.kmkbe.modules.remote.service.CwrRemoteService;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.utils.UserInternalUtils;
import com.kmkbe.nikita.utils.SpecPagination;
import com.kmkbe.nikita.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.security.SignatureException;
import java.text.ParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CwrService {
    private final CustomerRepository customerRepository;
    private final CwrRemoteService cwrRemoteService;
    private final CwrRepository cwrRepository;
    private final FinancingHdrRepository financingHdrRepository;
    private final ObjectMapper objectMapper;
    private final AgreementRepository agreementRepository;

    public PaginationResult<CwrListDto> list(
            String custCode,
            PaginationRequest request
    ) {
        try {
            int pageNo = 0, pageSize = 10;

            if (request.getPageNo() != null) {
                pageNo = request.getPageNo();
            }
            if (request.getPageSize() != null) {
                pageSize = request.getPageSize();
            }

            if (pageNo > 0) {
                pageNo = pageNo - 1;
            }

            Customer customer = customerRepository.findByCustCode(UUID.fromString(custCode))
                    .orElseThrow(() -> new IllegalStateException("Customer not found or not valid"));

            /*Page<Cwr> page = cwrRepository.findAllByCustomerOrderByDtmUpdDescUsrCrtDesc(
                    customer,
                    PageRequest.of(pageNo, pageSize)
            );

            List<CwrListDto> result = page.stream()
                    .map(CwrMapper.INSTANCE::toDto)
                    .toList();
            return PaginationResult.<CwrListDto>builder()
                    .currentPage(pageNo + 1)
                    .totalData(page.getTotalElements())
                    .totalPage(page.getTotalPages())
                    .list(result)
                    .build();*/

            List<Cwr> cwrs = cwrRepository.findAllByCustomerOrderByDtmUpdDesc(customer);


            List<CwrListDto> result = cwrs.stream()
                    .map(CwrMapper.INSTANCE::toDto)
                    .peek(dto -> {
                        List<Agreement> agreements = agreementRepository.findByCwr_CwrCode(dto.getCwrCode());
                        if (!agreements.isEmpty()) {
                            Agreement agreement = agreements.get(0);
                            if (agreement.getFinancingHdr() != null) {
                                dto.setFinancingAmt(agreement.getFinancingAmt());
                            }
                        }
                    })
                    .toList();
            return SpecPagination.paginationData(new SpecPagination<CwrListDto, CwrListDto>(result, request){
                @Override
                public CwrListDto search(CwrListDto data) {
                    
                    if (isSearchBy("office") && like(data.getBouwheerName())  ){
                        return data;
                    }else if (isSearchBy("cwrNo") && like(data.getCwrCode())  ){
                        return data;
                    }else if (isSearchBy("cwrStartDate") && equalDate(data.getCwrStartDate())  ){
                        return data;
                    }else if (isSearchBy("cwrEndDate") && equalDate(data.getCwrEndDate())  ){
                        return data;
                    }else if (isSearchBy("typeCurrency") && like(data.getCurrency())  ){
                        return data;
                    }else if (isSearchBy("plafondValue") && equalNumber(data.getPlafondAmt())  ){
                        return data;
                    }else if (isSearchBy("submissionValue") && equalNumber(data.getRealisationAmt())  ){
                        return data;


                    }

                    return null;
                }

                @Override
                public CwrListDto eval(CwrListDto data) {
                    return data;
                }
            });
        } catch (Exception e) {
            log.error("list: error {}", e.getMessage());
            throw e;
        }
    }

    public DetailCwrDto detail(String cwrCode, String financingHdrCode) {
        try {
            Cwr cwr = cwrRepository.findById(cwrCode).orElseThrow(
                    () -> new IllegalStateException("CWR not found")
            );

            Customer customer = cwr.getCustomer();
            if (customer == null) {
                throw new IllegalStateException("Customer not found or not valid");
            }

            final FinancingHdr financingHdr = financingHdrRepository.findByFinancingHdrCode(UUID.fromString(financingHdrCode))
                    .orElseThrow(() -> new IllegalStateException("Financing not found or not valid"));

            return DetailCwrDto.builder()
                    .cwrCode(cwr.getCwrCode())
                    .cwrStartDate(Utils.fromInstant(cwr.getCwrStartDate()))
                    .cwrEndDate(Utils.fromInstant(cwr.getCwrEndDate()))
                    .currency(cwr.getCurrency())
                    .plafondAmt(new BigDecimal(cwr.getPlafondAmt(), MathContext.DECIMAL64))
                    .realisationAmt(new BigDecimal(cwr.getRealisationAmt(), MathContext.DECIMAL64))
                    .remainingPlafondAmt(new BigDecimal(cwr.getPlafondAmt() - cwr.getRealisationAmt(), MathContext.DECIMAL64))
                    .custCode(customer.getCustCode())
                    .custTypeCode(customer.getCustTypeCode())
                    .custIdTypeCode(customer.getCustIdTypeCode())
                    .custIdNo(customer.getCustIdNo())
                    .custName(customer.getCustName())
                    .custEmail(customer.getCustEmail())
                    .financingAmt(BigDecimal.valueOf(financingHdr.getFinancingAmt()))
                    .build();
        } catch (Exception e) {
            log.error("detail: error {}", e.getMessage());
            throw e;
        }
    }




    public InquiryCwrDto inquiryCwr(String cwrNo) throws JsonProcessingException, ParseException {
        try {
            validateCwr(cwrNo);
            CommonInvalidException ex = CommonInvalidException.builder()
                    .title("Peringatan")
                    .message("Harap input CWR aktif di Confins terlebih dahulu")
                    .build();
            final List<InquiryCwrRemoteDto> data;
            try {
                BaseMstRemoteResponseDto<List<InquiryCwrRemoteDto>> response = cwrRemoteService.inquiryCwr(
                        InquiryCwrRemoteRequest.builder()
                                .cwrNo(cwrNo)
                                .build()
                );
                data = response.getData();
            } catch (Exception e) {
                throw ex;
            }


            if (data != null && !data.isEmpty()) {
                return InquiryCwrDto.builder()
                        .cwrStartDate(DateTimeUtils.cSharpTimeStampToDate(data.getFirst().getStartDt()))
                        .cwrEndDate(DateTimeUtils.cSharpTimeStampToDate(data.getFirst().getEndDt()))
                        .cwrCode(cwrNo)
                        .loanAmt(BigDecimal.valueOf(data.getFirst().getRealisationAmt()))
                        .plafondAmt(BigDecimal.valueOf(data.getFirst().getPlafondAmt()))
                        .currency(data.getFirst().getCurrency())


                        .realisationAmt(BigDecimal.valueOf(data.getFirst().getRealisationAmt()))
                        .status(data.getFirst().getCwrStatDescr())
                        .build();
            }

            throw ex;
        } catch (Exception e) {
            log.error("inquiryCwr: error {}", e.getMessage());
            throw e;
        }
    }

    public void createInquiryCwr(
            Authentication authentication,
            CreateInquiryCwrRequest request
    ) throws JsonProcessingException, SignatureException, ParseException {
        try {
            validateCwr(request.getCwrNo());
            final List<InquiryCwrRemoteDto> data;
            try {
                BaseMstRemoteResponseDto<List<InquiryCwrRemoteDto>> response = cwrRemoteService.inquiryCwr(
                        InquiryCwrRemoteRequest.builder()
                                .cwrNo(request.getCwrNo())
                                .build()
                );

                data = response.getData();
            } catch (Exception e) {
                throw CommonInvalidException.builder()
                        .title("Peringatan")
                        .message("Harap input CWR aktif di Confins terlebih dahulu")
                        .build();
            }


            final MstUser user = UserInternalUtils.authenticateUser(authentication);
            final FinancingHdr financingHdr = financingHdrRepository.findByFinancingHdrCode(UUID.fromString(request.getFinancingHdrCode()))
                    .orElseThrow(() -> new IllegalStateException("Financing not found or not valid"));

            final Bouwheer bouwheer = financingHdr.getBouwheer();
            if (bouwheer == null) {
                throw new IllegalStateException("Bouwheer not found or not valid");
            }

            final Customer customer = financingHdr.getCustomer();
            if (customer == null) {
                throw new IllegalStateException("Customer not found or not valid");
            }

            if (!data.isEmpty()) {
                for (InquiryCwrRemoteDto inquiryCwr : data) {
                    Cwr cwr = Cwr.builder()
                            .cwrCode(inquiryCwr.getCwrNo())
                            .bouwheer(bouwheer)
                            .customer(customer)
                            .branchCode(inquiryCwr.getOfficeCode())
                            .cwrType(inquiryCwr.getCwrType())
                            .cwrTypeDesc(inquiryCwr.getCwrTypeDesc())
                            .facility(inquiryCwr.getFacility())
                            .isRevolving(inquiryCwr.getIsRevolving())
                            .currency(inquiryCwr.getCurrency())
                            .cwrStartDate(Utils.toInstant(DateTimeUtils.cSharpTimeStampToDate(inquiryCwr.getStartDt())))
                            .cwrEndDate(Utils.toInstant((DateTimeUtils.cSharpTimeStampToDate(inquiryCwr.getEndDt()))))
                            .plafondAmt(inquiryCwr.getPlafondAmt())
                            .realisationAmt(inquiryCwr.getRealisationAmt())
                            .status(inquiryCwr.getCwrStatDescr())
                            .usrCrt(user.getUsername())
                            .dtmCrt(DateTimeUtils.now())

                            .usrUpd(user.getUsername())
                            .dtmUpd(DateTimeUtils.now())
                            .build();
                    cwrRepository.save(cwr);
                }
            }
        } catch (Exception e) {
            log.error("agreementCredit: error {}", e.getMessage());
            throw e;
        }
    }


    private InquiryCwrRemoteDto sample() throws JsonProcessingException {
        String sample = "{\"rn\":1,\"CwrNo\":\"41450CWR2024626\",\"DebtorType\":\"SINGLE\",\"CustName\":\"JOMON PERSADA NUSANTARA\",\"CustNo\":\"41400001208\",\"StartDt\":\"2023-08-29T00:00:00\",\"EndDt\":\"2024-08-29T00:00:00\",\"CurrStep\":\"Active\",\"LastStep\":\"CWR Activation\",\"CwrTypeDesc\":\"FACTORING\",\"CwrType\":\"FACTORING\",\"CwrStat\":\"ACT\",\"PlafondAmt\":9000000000,\"MrCwrTypeCode\":\"FACTORING\",\"Version\":1,\"AFVersion\":null,\"OfficeCode\":\"414\",\"OfficeName\":\"JAKARTA 3\",\"CwrStatDescr\":\"ACTIVE\",\"Facility\":\"MODAL KERJA\",\"IsRevolving\":true,\"Currency\":\"IDR\",\"RealisationAmt\":1719717561,\"LastApprover\":\"-\",\"GroupName\":null,\"GroupNo\":null,\"IsSuspend\":false,\"ChangeCwrTrxNo\":null}";
        return objectMapper.readValue(sample, new TypeReference<>() {
        });
    }

    public List<String> inquiryListAggr(String cwrCode) {
        try {


            final List<InquiryCwrRemoteDto> data;
            List<String> list = new ArrayList<>();
            try {

                final BaseMstRemoteResponseDto<List<InquiryAgreementByNoCwrRemoteDto>> agreementResponse =
                        cwrRemoteService
                                .inquiryAgreementByNoCwr(cwrCode);

//                if (  agreementResponse.getData() != null ) {
//                    for (int i = 0; i < agreementResponse.getData().size(); i++) {
//                        InquiryAgreementByNoCwrRemoteDto agreement = agreementResponse.getData().get(i);
//                        if (String.valueOf(agreement.agrmntStat).equalsIgnoreCase("prospect")||
//                                String.valueOf(agreement.agrmntStat).trim().isEmpty()||
//                                String.valueOf(agreement.agrmntStat).equalsIgnoreCase("Go Live")||
//                                String.valueOf(agreement.agrmntStat).equalsIgnoreCase("Live")){
//                            list.add(agreement.agrmntNo);
//                        }
//                    }
//                }
                if (agreementResponse.getData() != null) {
                    for (InquiryAgreementByNoCwrRemoteDto agreement : agreementResponse.getData()) {
                        String status = agreement.agrmntStat; // bisa null

                        if (status == null
                                || status.trim().isEmpty()
                                || status.equalsIgnoreCase("prospect")
                                || status.equalsIgnoreCase("Go Live")
                                || status.equalsIgnoreCase("Live")) {

                            list.add(agreement.agrmntNo);
                        }
                    }
                }




            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 10; i++) {

               // list.add("11111111112");
            }

           //[
            //  "11111111112",
            //  "41350240356",
            //  "41350240346",
            //  "41450241703",
            //  "41350240368",
            //  "41450241700",
            //  "41350240403",
            //  "41350240304",
            //  "41350240371",
            //  "41450241710",
            //  "41350240359",
            //  "41350240363",
            //  "41250241657",
            //  "41350240397",
            //  "41350240405",
            //  "41350240364",
            //  "41350240372",
            //  "41350241713",
            //  "41350240370",
            //  "41350240347",
            //  "41450241666",
            //  "41950241610",
            //]

            return list;

        } catch (Exception e) {
            log.error("inquiryCwr: error {}", e.getMessage());
            throw e;
        }
    }

    public void validateCwr(String cwrNo) {
        final Cwr existingCwr = cwrRepository.findTopByCwrCode(cwrNo)
                .orElse(null);
        if (existingCwr != null) {
            throw CommonInvalidException.builder()
                    .title("Peringatan")
                    .message("Nomor CWR sudah di input sebelumnya")
                    .build();
        }
    }
}
