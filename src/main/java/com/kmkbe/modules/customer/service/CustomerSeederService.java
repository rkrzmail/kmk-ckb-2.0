package com.kmkbe.modules.customer.service;

import com.kmkbe.core.constants.CommonConstants;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.domain.constant.CustomerIdType;
import com.kmkbe.core.domain.constant.CustomerType;
import com.kmkbe.core.domain.constant.GenderType;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.CustomerCompany;
import com.kmkbe.core.domain.entity.CustomerPersonal;
import com.kmkbe.core.domain.repository.CustomerCompanyRepository;
import com.kmkbe.core.domain.repository.CustomerPersonalRepository;
import com.kmkbe.core.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerSeederService implements CommandLineRunner {
    private final CustomerRepository customerRepository;
    private final CustomerCompanyRepository companyRepository;
    private final CustomerPersonalRepository personalRepository;
    private final BCryptPasswordEncoder bcryptEncoder;

    //@Transactional
    private void seed() {
        final String emailCompany = null;
        final String emailPersonal = "genzha112233@gmail.com";
        try {
           /* Optional<Customer> findCompany = customerRepository.findByCustEmail(emailCompany);
            if (findCompany.isEmpty()) {
                seedCustomerCompany(emailCompany);
            }*/

            Optional<Customer> findPersonal = customerRepository.findByCustEmail(emailPersonal);
            if (findPersonal.isEmpty()) {
                seedCustomerPersonal(emailPersonal);
            }

        } catch (Exception e) {
            log.error("Error seeding customer personal data", e);
        }
    }

    private Customer seedCustomer(CustomerType type, String email) {
        try {
            Customer customer = Customer.builder()
                    .custCode(UUID.randomUUID())
                    .custName("Customer " + type.name())
                    .custTypeCode(type.name())
                    .custIdTypeCode((type == CustomerType.Company ? CustomerIdType.NPWP : CustomerIdType.KTP).name())
                    .custIdNo("1234567890")
                    .custEmail(email)
                    .isEmailValid(true)
                    .custMobilePhone("085156032859")
                    .isPhoneValid(false)
                    .isWaActive(false)
                    .custPin(bcryptEncoder.encode("123456"))
                    .agreeTc(true)
                    .isActive(true)
                    .usrCrt("SYSTEM")
                    .dtmCrt(Instant.now())
                    .usrUpd("SYSTEM")
                    .dtmUpd(null)
                    .custExternalCode("0002004099")
                    .agreeLegalShare(true)
                    .build();

            customer = customerRepository.save(customer);
            return customer;
        } catch (Exception e) {
            log.error("Error seeding customer personal data", e);
            throw e;
        }
    }

    private void seedCustomerPersonal(String email) throws ParseException {
        try {
            Map<String, Object> staySinceLength = staySinceLength();
            Customer customer = seedCustomer(CustomerType.Personal, email);
            CustomerPersonal personal = CustomerPersonal.builder()
                    .custPersonalCode(UUID.randomUUID())
                    .customer(customer)
                    .birthPlace("Jakarta")
                    .birthDate(DateTimeUtils.SDF_STANDARD_DATE.parse("1998-01-01").toInstant())
                    .gender(GenderType.PEREMPUAN.toString())
                    .identityType(CustomerIdType.KTP.name())
                    .identityNo("1234567890")
                    .motherMaidenName("Fatimah")
                    .maritalStatus("Single")
                    .custModel("Pegawai") // Pegawai, Pengusaha, Profesional
                    .phone("085156032859")
                    .legalAddress("Jl. Raya Citayam")
                    .rt("08")
                    .rw("06")
                    .kelurahan("Citayam")
                    .kecamatan("Citayam")
                    .city("Depok")
                    .province("Jawa Barat")
                    .zipCode("1990")
                    .area("CM")
                    .ownershipStatus("Milik Keluarga") // Milik Keluarga, Milik Sendiri, Sewa, Kost, Dinas, Mess, Saudara Kandung
                    .staySince(((Date) staySinceLength.get("staySince")).toInstant())
                    .stayLength(BigDecimal.valueOf((double) staySinceLength.get("stayLength")).setScale(2, RoundingMode.CEILING).doubleValue())
                    .usrCrt(customer.getUsrCrt())
                    .dtmCrt(customer.getDtmCrt())
                    .usrUpd(customer.getUsrUpd())
                    .dtmUpd(customer.getDtmUpd())
                    .build();

            personalRepository.save(personal);
        } catch (Exception e) {
            log.error("Error seeding customer personal data", e);
            throw e;
        }
    }

    private void seedCustomerCompany(String email) throws ParseException {
        try {
            Map<String, Object> staySinceLength = staySinceLength();
            Customer customer = seedCustomer(CustomerType.Company, email);
            CustomerCompany company = CustomerCompany.builder()
                    .custCompanyCode(UUID.randomUUID())
                    .customer(customer)
                    .custCompanyType("Perseroan Terbatas") // Commanditer Venotschap, Yayasan, Firma, Koperasi, Usaha Dagang, Badan Usaha Milik Desa, Debitur Kelompok, Expedisi Muatan Kapal Laut
                    .companyModel("Perseroan Terbatas") // Yayasan, Koperasi
                    .identityType(CustomerIdType.NPWP.name())
                    .identityNo("1234567890")
                    .identityIssuedDate(Instant.now())
                    .identityExpiredDate(Instant.now().plus(365, ChronoUnit.DAYS))
                    .phone("0251560324")
                    .companyAddress("Jl. Raya Citayam")
                    .rt("08")
                    .rw("06")
                    .kelurahan("Citayam")
                    .kecamatan("Citayam")
                    .city("Depok")
                    .province("Jawa Barat")
                    .zipCode("1990")
                    .area("CM")
                    .ownershipStatus("Milik Keluarga") // Milik Keluarga, Milik Sendiri, Sewa, Kost, Dinas, Mess, Saudara Kandung
                    .staySince(((Date) staySinceLength.get("staySince")).toInstant())
                    .stayLength(BigDecimal.valueOf((double) staySinceLength.get("stayLength")).setScale(2, RoundingMode.CEILING).doubleValue())
                    .usrCrt(customer.getUsrCrt())
                    .dtmCrt(customer.getDtmCrt())
                    .usrUpd(customer.getUsrUpd())
                    .dtmUpd(customer.getDtmUpd())
                    .build();
            companyRepository.save(company);
        } catch (Exception e) {
            log.error("Error seeding customer personal data", e);
            throw e;
        }
    }

    private Map<String, Object> staySinceLength() throws ParseException {
        final Date staySince = DateTimeUtils.SDF_STANDARD_DATE.parse("2021-01-01");

        final double differenceDays = Duration.between(
                staySince.toInstant(),
                Instant.now()
        ).toMinutes() / (double) CommonConstants.MONTH_IN_MINUTES;

        return Map.of("staySince", staySince, "stayLength", differenceDays);
    }

    @Override
    public void run(String... args) {
        seed();
    }
}
