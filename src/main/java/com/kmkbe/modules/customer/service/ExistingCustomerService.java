package com.kmkbe.modules.customer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.entity.Cwr;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.core.domain.repository.CwrRepository;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.remote.request.ExistingCustomerRequest;
import com.kmkbe.modules.remote.request.InquiryCwrRemoteRequest;
import com.kmkbe.modules.remote.request.PropCriteriaGenericTypeRequest;
import com.kmkbe.modules.remote.service.CustomerRemoteService;
import com.kmkbe.modules.remote.service.CwrRemoteService;
import com.kmkbe.helpers.utils.Utils;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service
@Slf4j
public class ExistingCustomerService {
  private final JdbcTemplate jdbcTemplate;
  private final CustomerRemoteService customerRemoteService;
  private final CwrRemoteService cwrRemoteService;
  private final CwrRepository cwrRepository;
  private final CustomerRepository customerRepository;

  public ExistingCustomerService(
    JdbcTemplate jdbcTemplate,
    CustomerRemoteService customerRemoteService,
    CwrRemoteService cwrRemoteService,
    CwrRepository cwrRepository,
    CustomerRepository customerRepository
  ) {
    this.jdbcTemplate = jdbcTemplate;
    this.customerRemoteService = customerRemoteService;
    this.cwrRemoteService = cwrRemoteService;
    this.customerRepository = customerRepository;
    this.cwrRepository = cwrRepository;
  }

  public ExistingCustomerDto createOrUpdate(
    String vendorCode,
    boolean isExisting,
    String identityType,
    String identityNo
  ) {
    try {
      Optional<ExistingCustomerDto> find = findLastByVendorCode(vendorCode);
      final ExistingCustomerDto result;

      if (find.isEmpty()) {
        jdbcTemplate.update(
          "insert into public._existing_customer (vendor_code, is_existing, identity_type, identity_no, dtm_crt) values (?, ?, ?, ?, ?)",
          vendorCode,
          isExisting,
          identityType,
          identityNo,
          Timestamp.from(Utils.fromInstant(DateTimeUtils.nowLocal()).toInstant())
        );
      } else {
        jdbcTemplate.update(
          "update public._existing_customer set is_existing = ?, identity_type = ?, identity_no = ?, dtm_upd = ? where vendor_code = ?",
          isExisting,
          identityType,
          identityNo,
          Timestamp.from(Utils.fromInstant(DateTimeUtils.nowLocal()).toInstant()),
          vendorCode
        );
      }

      result = findLastByVendorCode(vendorCode).get();

      return result;
    } catch (Exception e) {
      log.error("create: error {}", e.getMessage());
      throw e;
    }
  }

  public Optional<ExistingCustomerDto> findLastByVendorCode(String vendorCode) {
    try {
      ExistingCustomerDto result = jdbcTemplate.queryForObject(
        "select vendor_code, is_existing, identity_type, identity_no, dtm_crt, dtm_upd from public._existing_customer where vendor_code = ? order by id desc limit 1",
        (rs, rowNum) -> ExistingCustomerDto.builder()

          .dtmCrt(Utils.toInstant(new Date(rs.getTimestamp("dtm_crt").getTime())))
          .dtmUpd(rs.getTimestamp("dtm_upd") != null ? Utils.toInstant(new Date(rs.getTimestamp("dtm_upd").getTime())) : null)
          .build(),
        vendorCode
      );

      return Optional.ofNullable(result);
    } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
      return Optional.empty();
    } catch (Exception e) {
      log.error("findLastByVendorCode, error {}", e.getMessage());
      throw e;
    }
  }

  public Cwr inquiryAndDecideExistingCustomer(
    ExistingCustomerRequest.KeyType identityType,
    String identityNo,
    Customer customer,
    Bouwheer bouwheer
  ) throws JsonProcessingException {
    if (
      identityType == null
        || identityNo == null
    ) {
      return null;
    }

    try {
      CustomerRemoteDto existingCustomer = null;
      try {
        existingCustomer = customerRemoteService.validateExisting(
          ExistingCustomerRequest.builder()
            .args(
              ExistingCustomerRequest.Args.builder()
                .key(identityType)
                .value(identityNo)
                .build()
            )
            .includeProperties(new ArrayList<>())
            .requestDateTime(DateTimeUtils.SDF_STANDARD_DATE.format(new Date()))
            .build()
        );
      } catch (Exception ignored) {
      }

      if (existingCustomer == null) {
        return null;
      } else {
        if (StringUtil.isNullOrEmpty(customer.getCustNo())) {
          final Customer loadedCust = customerRepository.findByCustCode(customer.getCustCode())
            .orElseThrow();
          loadedCust.setCustNo(existingCustomer.getCustNo());
          loadedCust.setUsrUpd(customer.getCustName());
          loadedCust.setDtmUpd(DateTimeUtils.now());
          customerRepository.save(loadedCust);
        }
      }

      BaseMstRemoteResponseDto<List<InquiryCwrRemoteDto>> cwrResponse = null;
      try {
        cwrResponse = cwrRemoteService.inquiryCwr(
          InquiryCwrRemoteRequest.builder()
            .name(PropCriteriaGenericTypeRequest.CwrPropName.custNo)
            .custNo(existingCustomer.getCustNo())
            .build()
        );
      } catch (Exception ignored) {
      }

      if (cwrResponse == null) {
        return null;
      }

      final List<InquiryCwrRemoteDto> cwrs = cwrResponse.getData();
      final List<InquiryCwrRemoteDto> cwrActives = new ArrayList<>();
      InquiryCwrRemoteDto firstCwr = null;
      if (cwrs != null && !cwrs.isEmpty()) {
        firstCwr = cwrs.getFirst();
        cwrs.sort((o1, o2) -> o2.getVersion().compareTo(o1.getVersion())); // sort by high version

        Date now = DateTimeUtils.setDateZeroTime(new Date());
        cwrs.forEach((cwr) -> {
          Date startDt = DateTimeUtils.cSharpTimeStampToDate(cwr.getStartDt());
          Date endDt = DateTimeUtils.cSharpTimeStampToDate(cwr.getEndDt());
          if (startDt != null && endDt != null && startDt.compareTo(endDt) < 0) {
            if (now.compareTo(startDt) >= 0 && now.compareTo(endDt) <= 0) {
              cwrActives.add(cwr);
            }
          }

          if (
            cwr.getCurrStep().equalsIgnoreCase("active")
              &&
              (
                startDt != null && (DateUtils.isSameDay(startDt, now) || startDt.after(now))
              )
              &&
              (
                endDt != null && (DateUtils.isSameDay(endDt, now) || endDt.before(now))
              )
          ) {
            cwrActives.add(cwr);
          }
        });
      }

      if (firstCwr != null) {
        final BaseMstRemoteResponseDto<List<InquiryAgreementByNoCwrRemoteDto>> agreementResponse =
          cwrRemoteService
            .inquiryAgreementByNoCwr(firstCwr.getCwrNo());

        if (
          agreementResponse.getData() != null
            && agreementResponse.getData().isEmpty()
        ) {
          Cwr result = Cwr.builder()
            .cwrCode(firstCwr.getCwrNo())
            .bouwheer(bouwheer)
            .customer(customer)
            .branchCode(firstCwr.getOfficeCode())
            .cwrType(firstCwr.getCwrType())
            .cwrTypeDesc(firstCwr.getCwrTypeDesc())
            .facility(firstCwr.getFacility())
            .isRevolving(firstCwr.getIsRevolving())
            .currency(firstCwr.getCurrency())
            .cwrStartDate(Utils.toInstant(DateTimeUtils.cSharpTimeStampToDate(firstCwr.getStartDt())))
            .cwrEndDate(Utils.toInstant(DateTimeUtils.cSharpTimeStampToDate(firstCwr.getEndDt())))
            .plafondAmt(firstCwr.getPlafondAmt())
            .realisationAmt(firstCwr.getRealisationAmt())
            .status(firstCwr.getCwrStatDescr())
            .usrCrt(customer.getCustName())
            .dtmCrt(DateTimeUtils.now())
            .build();
          if (bouwheer != null) {
            final Cwr cwr = cwrRepository.findTopByCwrCode(firstCwr.getCwrNo())
              .orElse(null);

            if (cwr == null) {
              cwrRepository.save(result);
            }
          }

          return result;
        }
      }

      return null;
    } catch (Exception e) {
      log.error("inquiryAndDecideExistingCustomer: error {}", e.getMessage());
      throw e;
    }
  }
}
