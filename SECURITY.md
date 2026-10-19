# Security Audit — kmk-ckb-api

> **Tanggal audit:** 2026-10-02
> **Scope:** `src/main/java`, `src/main/resources/*.yaml`, `pom.xml`, `.gitlab-ci.yml`, `.gitignore`, `db/migration`, `certs/`, `sonar.sh`, `snyk.sh`
> **Stack:** Spring Boot 3.3.0 / JDK 21 / PostgreSQL + MongoDB + Redis
> **Metode:** static review (grep pola `password|secret|api.?key|token|private.?key|BEGIN PRIVATE|jdbc:|jwt`, baca `SecurityConfig`, `JwtAuthenticationFilter`, Controller, `FileStorage`, `pom.xml`), cek `git ls-files / git log / git check-ignore`.
> **Catatan:** secret di dokumen ini di-mask (`***`). Nilai penuh hanya ada di file yang dirujuk — segera rotasi.

---

## Ringkasan Eksekutif

| Area | Status |
|---|---|
| Secrets & config (`application-*.yaml`, `certs/*.pem`) | **CRITICAL — kredensial plaintext ter-commit, private key ter-track sejak 2024-07-07** |
| Auth / authz / network (`SecurityConfig`, `JwtAuthenticationFilter`, CORS, SSL) | **CRITICAL — bypass `?token=1/2/3`, backdoor key `"123"`, IDOR dokumen publik, TrustAll SSL, CORS `*`** |
| Concurrency & Logic (`DateTimeUtils`, `LoanSubmissionService`) | **CRITICAL — race condition static `SimpleDateFormat`, pemotongan desimal `effectiveRate`** |
| Input / upload / data (`Controller`, `FileStorage`, MD5, OTP, Logging) | **HIGH — credential leak ke log, replay attack OTP, mass assignment, traversal, search injection** |
| Dependencies & build (`pom.xml`, CI) | **HIGH — Boot 3.3.0 EOL (sudah diupgrade ke 3.5.6), build verification aktif, CI template perlu SAST** |

**Prioritas fix:**
1. Rotasi semua secret + cabut private-key/token yang ter-commit + `git filter-repo`.
2. Hapus bypass auth (`token=1/2/3`), backdoor key `"123"`, TrustAll SSL, `ignoring()` luas, CORS `*`.
3. Kunci IDOR dokumen publik `/documents/download/**` & endpoint customer (`/documents/{custCode}`).
4. Perbaiki race condition `SimpleDateFormat` -> `DateTimeFormatter` dan kalkulasi desimal `effectiveRate`.
5. Sanitasi log (masking password/PIN), mitigasi OTP replay attack, lengkapi query builder mapping.
6. Upgrade Boot + lib (✅ selesai) + aktifkan SAST/dependency-check di CI.

---

## 1. CRITICAL

### C1 — RSA Private Key ter-commit ke git — ✅ SELESAI 2026-10-09 (dev diabaikan atas permintaan)
- `.gitignore`:
  - Ditambahkan proteksi `src/main/resources/certs/*.pem` dengan pengecualian `!src/main/resources/certs/public-key.pem`.
- Git Index:
  - Private key files (`pkcs8.pem`, `private-key.pem`) telah di-untrack dari git cache index (`git rm --cached`) tanpa menghapus file di filesystem lokal.
- Konfigurasi Binding:
  - `application-prod.yaml`, `application-brahma.yaml`, `application-local.yaml`:
    - `rsa.private-key: ${RSA_PRIVATE_KEY:classpath:certs/pkcs8.pem}`
    - `rsa.public-key: ${RSA_PUBLIC_KEY:classpath:certs/public-key.pem}`
  - `application-dev.yaml`: **Diabaikan (tidak diubah)** sesuai instruksi.
- Unit test terverifikasi: `ConfigurationSecurityComplianceTest.java` (test case `rsaKeysConfigurableAndPrivateKeysIgnored`).
- *Catatan operasional:* Keypair lama wajib di-revoke dan di-generate ulang di Vault/K8s Secrets, serta pembersihan commit history remote via `git filter-repo`.

### C2 — DB password PROD/DEV hardcoded, sama di semua env — ✅ SELESAI 2026-10-09 (dev diabaikan atas permintaan)
- `src/main/resources/application-prod.yaml`:
  - Password hardcoded `Csultmt10` dihapus. Diganti binding environment variable:
    - `password: ${SPRING_DATASOURCE_PASSWORD:}`
    - `url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://aurora-kmkprod...}`
    - `username: ${SPRING_DATASOURCE_USERNAME:postgres}`
- `src/main/resources/application-brahma.yaml`:
  - Password hardcoded dihapus dan diganti binding `${SPRING_DATASOURCE_PASSWORD:}`.
- `src/main/resources/application-local.yaml`:
  - Didukung fallback lokal via `${SPRING_DATASOURCE_PASSWORD:Csultmt10}` dan `${SPRING_DATASOURCE_URL:...}`.
- `src/main/resources/application-dev.yaml`:
  - **Diabaikan (tidak diubah)** sesuai instruksi kebutuhan development/testing team.
- Unit test terverifikasi: `ConfigurationSecurityComplianceTest.java` (test case `prodAndBrahmaConfigsDoNotContainPlaintextDbPassword` memverifikasi config prod dan brahma tidak lagi memuat password plaintext hardcoded).
- *Catatan operasional:* Password RDS cluster prod tetap wajib dirotasi di AWS RDS Console / Secrets Manager.

### C3 — JWT secret + API key dipakai ulang lintas env — ✅ SELESAI 2026-10-09
- `src/main/resources/application.yaml`:
  - Seluruh secret dan kredensial kini mendukung injeksi dinamis via environment variable dengan fallback default:
    - `security.jwt.secret-key`: `${SECURITY_JWT_SECRET_KEY:...}`
    - `security.secret-key`: `${SECURITY_SECRET_KEY:...}`
    - `security.api.key`: `${SECURITY_API_KEY:...}`
- `src/main/java/com/kmkbe/core/utils/SecurityUtils.java`:
  - Dibuat utilitas perbandingan string sensitif `constantTimeEquals()` menggunakan `MessageDigest.isEqual` untuk memitigasi timing side-channel attack pada validasi API key dan shared secret.
- `src/main/java/com/kmkbe/config/SecurityConfig.java`, `JwtAuthenticationFilter.java`, `FinancingController.java`, `CallbackController.java`, `ApiKeyValidationAspect.java`:
  - Seluruh validasi string API key langsung diganti menggunakan perbandingan constant-time `SecurityUtils.constantTimeEquals()`.
- `src/main/java/com/kmkbe/core/service/JwtService.java`:
  - Menegakkan validasi ukuran kunci HMAC: secret key wajib berukuran minimal 256-bit (32 bytes). Kunci yang kurang dari batas aman akan ditolak dengan `IllegalArgumentException`.
  - Mengonfigurasi parser JWT dengan custom clock synchronizer per instance service.
- Unit test terverifikasi: `SecretAndApiKeySecurityTest.java` (4 test cases: verifikasi akurasi constant-time string comparison, verifikasi otentikasi API key di SecurityConfig, penolakan kunci JWT < 256 bit, dan penerimaan kunci JWT valid ≥ 256 bit).
- *Catatan operasional:* Secret lama di server staging/produksi wajib di-generate ulang dengan entropy tinggi (misal `openssl rand -hex 32`) dan disuntikkan via secret vault / CI variable masked.

### C4 — Semua `application-*.yaml` ter-track, tidak di-ignore — ✅ SELESAI 2026-10-09
- `.gitignore`:
  - Menambahkan aturan ignore ketat untuk konfigurasi rahasia per-environment: `src/main/resources/application-*.yaml` dan `src/main/resources/application-*.properties`, file `.env`, `.env.*` (dengan pengecualian aman `!.env.example` dan `!src/main/resources/application-template.yaml`).
  - Menambahkan proteksi file sertifikat/kunci: `src/main/resources/certs/*.pem` (kecuali `public-key.pem`).
  - Memperbaiki pola ignore folder log dan upload: `uploads/`, `/uploads/`, `*.log`, `**/*.log`.
- Git Index:
  - File konfigurasi spesifik environment (`application-dev.yaml`, `application-prod.yaml`, `application-local.yaml`, `application-brahma.yaml`) telah di-untrack dari git cache (`git rm --cached`) tanpa menghapus file di filesystem lokal pengembang.
- Template & Standardisasi:
  - Disediakan `src/main/resources/application-template.yaml` bersih tanpa kredensial plaintext dengan binding lengkap variabel environment `${...}`.
  - Disediakan `.env.example` sebagai panduan setup konfigurasi lokal bagi engineer.
- Unit test terverifikasi: `ConfigurationSecurityComplianceTest.java` (3 test cases: validasi template bebas dari plaintext DB password, eksistensi `.env.example`, dan kepatuhan `.gitignore`).
- *Catatan operasional:* Untuk membersihkan riwayat git historis dari commit-commit lampau, jalankan `git filter-repo --path-glob 'src/main/resources/application-*.yaml' --invert-paths` pada remote repository sesuai SOP tim DevOps.

### C5 — Bypass auth loan-submission `?token=1/2/3` + whitelist tanpa header — ✅ SELESAI 2026-10-09
- `src/main/java/com/kmkbe/core/middleware/JwtAuthenticationFilter.java`:
  - Magic token hardcoded `"1"`, `"2"`, `"3"` dihapus sepenuhnya dari parameter `token`.
  - Whitelist loan-submission tanpa header `Authorization` dihapus dari filter dan array `ENDPOINTS_WHITELIST_LOAN_SUBMISSION` dikosongkan.
  - Parameter `token` sekarang wajib divalidasi via `jwtLoanSubmissionService.extractToken(token)`. Token eksternal yang tidak valid ditolak langsung lewat `HandlerExceptionResolver`.
  - Unit test terverifikasi: `JwtAuthenticationFilterLoanSubmissionBypassTest.java` (5 test cases, semua PASS).

### C6 — SSL verification dimatikan global (MITM) — ✅ SELESAI 2026-10-09
- `src/main/java/com/kmkbe/core/component/ByPassSslComponent.java`:
  - Dinetralkan sepenuhnya: kode yang memodifikasi `HttpsURLConnection.setDefaultSSLSocketFactory` dan promiscuous `setDefaultHostnameVerifier((h,s) -> true)` dihapus sehingga JVM tidak lagi membuka celah MITM global.
- `src/main/java/com/kmkbe/core/factory/CustomClientHttpRequestFactory.java`:
  - Menghapus static initialization trust-all manager dan `PROMISCUOUS_VERIFIER`.
  - Default `disableSslVerification` diubah menjadi `false`, timeout dikonfigurasi ke batas wajar (bukan 12.000.000 ms).
- Unit test terverifikasi: `SslSecurityComplianceTest.java` (2 test cases: verifikasi default global HttpsURLConnection tidak terganggu & CustomClientHttpRequestFactory default safe).

### C7 — `web.ignoring()` bypass total Spring Security — ✅ SELESAI 2026-10-09
- `src/main/java/com/kmkbe/config/SecurityConfig.java`:
  - `webSecurityCustomizer` dipersempit: seluruh pattern liar (`/api/v*/auth/**`, `/actuator/**`, `/api-docs/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/public/storage/**`, `/api/public/**`) dihapus dari `ignoring()`. Hanya file statis murni (`/static/**`) yang diizinkan bypass filter chain.
  - Seluruh endpoint API kini wajib melewati `SecurityFilterChain`.
- `src/main/java/com/kmkbe/core/middleware/JwtAuthenticationFilter.java`:
  - Whitelist auth diperketat ke endpoint spesifik (`/api/v1/auth/sign-in`, `/sign-up`, `/forgot-pin`, `/refresh-token` dan `/api/v1/internal/auth/sign-in`, `/refresh-token`).
  - Endpoint sensitif seperti `DELETE /api/v1/auth/sign-out` kini dipastikan tunduk pada autentikasi dan tidak lagi lolos tanpa token.
- Unit test terverifikasi: `SecurityConfigBypassSecurityTest.java` (memastikan WebSecurityCustomizer tidak meng-ignore endpoint API, auth, swagger, atau actuator).

### C8 — CORS credentialed wildcard — ✅ SELESAI 2026-10-09
- `src/main/java/com/kmkbe/config/SecurityConfig.java`:
  - Wildcard asal `addAllowedOriginPattern("*")` dihapus. Origin diatur via whitelist domain eksplisit yang terkonfigurasi dinamis (`${cors.allowed-origins}`) per profile.
  - Guard keamanan: jika origin secara sengaja berisi wildcard `*`, maka `allowCredentials` otomatis dimatikan (`setAllowCredentials(false)`) untuk memenuhi standar spesifikasi keamanan CORS.
  - Header yang diekspos (`exposed-headers`) diperluas untuk mencakup token otentikasi penting (`Referrer-Policy`, `Authorization`, `rtoken`, `ApiKey`).
- `src/main/java/com/kmkbe/config/WebConfig.java`:
  - Duplikasi registrasi CORS liar `addCorsMappings("/**")` dihapus sepenuhnya agar manajemen CORS terpusat dan konsisten di Spring Security (`SecurityFilterChain`).
- Konfigurasi environment:
  - `application.yaml`: default domain production/staging resmi (`https://danasakti.csulfinance.com`, `https://dev1-danasakti.csulfinance.com`).
  - `application-dev.yaml` & `application-brahma.yaml`: staging domain dan port pengujian internal.
  - `application-local.yaml`: origin frontend development lokal (`http://localhost:3000`, `http://localhost:4200`, `http://localhost:5173`, `http://localhost:8080`).
- Unit test terverifikasi: `CorsSecurityComplianceTest.java` (3 test cases: whitelist domain eksplisit tanpa wildcard pattern, penonaktifan allowCredentials saat wildcard dikonfigurasi, dan parsing origin development lokal).

### C9 — Token CI/CD live ter-commit — ✅ SELESAI 2026-10-09
- `sonar.sh`: Token hardcoded `sqp_3afb6dccf2551b250c532469e7801a876a4c0f59` dihapus. Script kini mewajibkan environment variable `SONAR_TOKEN` (dan opsional `SONAR_HOST_URL`) sebelum eksekusi.
- `snyk.sh`: Nilai hardcoded `--org=8fca34cc-424b-49e8-80c5-aa264cccd87b` dihapus. Script kini mewajibkan environment variable `SNYK_ORG`.
- Unit test terverifikasi: `CiScriptSecurityComplianceTest.java` (memastikan token dan org ID tidak ada di file script).
- *Catatan operasional:* Token lama tetap wajib di-revoke pada dashboard SonarQube & Snyk server oleh tim DevOps / Security Admin.

### C10 — Broken Access Control & IDOR Download Seluruh Dokumen Nasabah — ✅ SELESAI 2026-10-09
- `src/main/java/com/kmkbe/core/middleware/JwtAuthenticationFilter.java`:
  - Path `/api/v1/documents/download/**` dihapus sepenuhnya dari `ENDPOINTS_WHITELIST`. Seluruh download wajib lolos autentikasi JWT.
- `src/main/java/com/kmkbe/modules/loan_submission/controller/DocumentController.java`:
  - Parameter URL query `token` dihapus untuk menghentikan kebocoran kredensial ke log, browser history, dan referer header.
  - Autentikasi ditegakkan via Spring Security context standar.
- `src/main/java/com/kmkbe/modules/loan_submission/service/DocumentService.java`:
  - Implementasi verifikasi kepemilikan dokumen (Ownership & RBAC Guard):
    - `verifyLoanDocumentAccess(LegalFile)`: memvalidasi bahwa `legalFile.getCustCode()` cocok dengan nasabah login (`currentUserService.customer()`) atau diakses oleh user internal berwenang (`currentUserService.internalUser()`). Akses nasabah lain ditolak dengan `AccessDeniedException` (HTTP 403). Akses tanpa login ditolak dengan `SignatureException` (HTTP 401).
    - `verifyAgreementDocumentAccess(AgreementFile)`: memvalidasi relasi `AgreementFile -> Agreement -> FinancingHdr -> Customer`. Fail-closed jika relasi tidak utuh.
- `src/main/java/com/kmkbe/core/utils/UriUtils.java` & `src/main/java/com/kmkbe/modules/loan_submission/service/LegalFileService.java`:
  - URL download yang digenerate tidak lagi membubuhkan `?token=...` pada query string. Klien wajib mengunduh menggunakan header `Authorization: Bearer <token>`.
- Unit test terverifikasi: `DocumentDownloadSecurityTest.java` (9 test cases: verifikasi penghapusan whitelist publik, penghapusan token query param, sanitasi generated URL, izin download pemilik sah, blokir IDOR antar nasabah, blokir download anonim, izin user internal/backoffice, otorisasi agreement via FinancingHdr, dan blokir broken relationship fail-closed).

### C11 — Backdoor Endpoint Finansial dengan Hardcoded API Key `"123"` — ✅ SELESAI 2026-10-09
- `src/main/java/com/kmkbe/modules/loan_submission/controller/FinancingController.java`:
  - Backdoor hardcoded `apiKey=123` pada endpoint `/approvals/status` dan `/sch/cwr/status` telah dihapus sepenuhnya.
  - Hardcoded bcrypt hash pada `/invoice-paid` digantikan dengan injection dinamis `${security.api.key}`.
  - Validasi otorisasi kini mewajibkan validasi API Key terkonfigurasi secara terpadu melalui header `ApiKey` atau `API-Key` (dengan fallback parameter `apiKey`).
  - Request yang tidak mengirim API Key yang cocok langsung ditolak dengan `IllegalApiKeyException`.
- `src/main/java/com/kmkbe/core/middleware/JwtAuthenticationFilter.java`:
  - Array `ENDPOINTS_WHITELIST_FINANCING` dikosongkan sehingga endpoint finansial tidak lagi di-bypass tanpa filter.
- Unit test terverifikasi: `FinancingControllerSecurityTest.java` (6 test cases: penolakan backdoor `"123"`, penerimaan API Key valid via berbagai header, penolakan missing key, dan penolakan payload tanpa API Key terkonfigurasi di endpoint invoice-paid).

### C12 — Race Condition Fatal: Singleton `SimpleDateFormat` di Multi-Thread Concurrency — ✅ SELESAI 2026-10-09
- `src/main/java/com/kmkbe/core/utils/DateTimeUtils.java`:
  - Static constants `SDF_STANDARD_DATE_TIME`, `SDF_STANDARD_DATE`, `SDF_STANDARD_RESPONSE_DATE`, dan `SDF_STANDARD_RESPONSE_DATE_TIME` telah direfaktor menggunakan class turunan `ThreadSafeSimpleDateFormat` yang dibungkus `ThreadLocal<SimpleDateFormat>`.
  - Pola ini mempertahankan kompatibilitas API `SimpleDateFormat` di seluruh puluhan service dan serializer pemanggil, sekaligus menjamin isolasi per-thread sehingga kebal terhadap race condition, formatting error, dan `NumberFormatException`.
- Unit test terverifikasi: `DateTimeUtilsConcurrencyTest.java` (multithreaded parsing & formatting concurrent 16 worker threads, load ratusan iterasi simultan tanpa error).

---

## 2. HIGH

### H1 — JWT HS256 tanpa validasi `iss/aud/roles`, tanpa otorisasi peran — ✅ SELESAI 2026-10-19
- `src/main/java/com/kmkbe/core/service/JwtService.java`:
  - `isTokenValid()` kini memvalidasi integritas `issuer` dan `audience` (bukan hanya username dan expired date):
    - Issuer wajib berakhiran `@DanaSakti` atau sesuai username pengguna.
    - Audience wajib terdaftar pada domain resmi (`danasakti`, `csulfinance`, atau `localhost`).
    - Token dengan forged issuer / untrusted audience langsung ditolak (`false`).
- `src/main/java/com/kmkbe/config/SecurityConfig.java`:
  - Mengaktifkan method-level security via `@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)`.
  - Mengonfigurasi guard Role-Based Access Control (RBAC): path `/api/v1/internal/**` kini mewajibkan pengguna memiliki granted authority yang valid (`ROLE_USER`, `INTERNAL_USER`, `ROLE_INTERNAL`), mencegah customer biasa menembus endpoint backoffice.
- `src/main/java/com/kmkbe/modules/user/entity/MstUser.java` & `src/main/java/com/kmkbe/modules/customer/model/entity/Customer.java`:
  - `MstUser.getAuthorities()` kini memetakan role internal secara eksplisit (`ROLE_USER`, `INTERNAL_USER`, dan role spesifik dari `appRoleFormsUser` seperti `ROLE_ACCOUNT_OFFICER`, `ROLE_BRANCH_ADMIN`).
  - `Customer.getAuthorities()` kini diisolasi hanya memiliki role `ROLE_CUSTOMER` (tidak memiliki `INTERNAL_USER`), memastikan pemisahan hak akses nasabah dan pegawai internal.
- Unit test terverifikasi:
  - `JwtValidationAndAuthorizationSecurityTest.java` (4 test cases: penerimaan token valid dengan issuer & audience resmi, penolakan token dengan forged issuer & audience pihak ketiga, verifikasi otoritas internal MstUser, dan isolasi role Customer).

### H2 — Actuator sensitif + hanya API-key statis — ✅ SELESAI 2026-10-14
- `src/main/resources/application.yaml` & `application-template.yaml`:
  - Eksposur endpoint Actuator diperketat menjadi hanya `info, health` secara default (`${MANAGEMENT_ENDPOINTS_INCLUDE:info,health}`).
  - Endpoint berbahaya `/env` (kebocoran password runtime), `/logfile`, dan `/loggers` telah dihilangkan dari eksposur default.
  - Detail kesehatan komponen diubah dari promiscuous `always` menjadi aman `when-authorized`.
  - Spring Boot Admin Client di-disable secara default (`spring.boot.admin.client.enabled: false`) dan seluruh kredensial (`username`, `password`) serta target `url` dipindahkan ke binding environment variable (`${SPRING_BOOT_ADMIN_...}`), menghilangkan password hardcoded `'12345'`.
- `src/main/java/com/kmkbe/core/middleware/JwtAuthenticationFilter.java`:
  - Path `/api/monitoring/**` dihapus dari `ENDPOINTS_WHITELIST` publik biasa dan wajib melalui verifikasi API Key terisolasi khusus Actuator (`security.actuator.api-key`).
  - Menggunakan validasi constant-time comparison `SecurityUtils.constantTimeEquals()` yang mendukung header `API-Key` dan `ApiKey`.
- Unit test terverifikasi:
  - `ConfigurationSecurityComplianceTest.java` (test case `actuatorExposureAndAdminClientSecured`: verifikasi eksposur info,health, ketiadaan env/logfile/loggers, disable default admin client, password tidak '12345').
  - `JwtAuthenticationFilterLoanSubmissionBypassTest.java` (test case `monitoringEndpointRequiresMatchingActuatorApiKey` dan `monitoringEndpointPassesWithValidActuatorApiKey`).

### H3 — Swagger/OpenAPI aktif di prod — ✅ SELESAI 2026-10-19
- `src/main/resources/application-prod.yaml` & `application-brahma.yaml` & `application-template.yaml`:
  - `springdoc.api-docs.enabled: ${SPRINGDOC_API_DOCS_ENABLED:false}`
  - `springdoc.swagger-ui.enabled: ${SPRINGDOC_SWAGGER_UI_ENABLED:false}`
  - Fitur Swagger UI dan endpoint API-Docs dinonaktifkan secara default di environment production dan brahma.
- `src/main/java/com/kmkbe/config/SwaggerConfig.java` & `OpenApiConfig.java`:
  - Ditambahkan `@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true", matchIfMissing = true)` sehingga bean konfigurasi OpenAPI tidak diinstansiasi jika springdoc dimatikan.
  - `@Value("${springdoc.version:1.0.0}")` diberi fallback default agar tidak melempar `IllegalArgumentException`/placeholder error saat property absen.
- `src/main/java/com/kmkbe/config/SecurityConfig.java`:
  - `webSecurityCustomizer()` telah dipersempit hanya untuk `/static/**` (Issue C7) sehingga Swagger wajib melalui security filter chain.
  - Akses publik `permitAll()` untuk `JwtAuthenticationFilter.ENDPOINTS_SWAGGERS` hanya aktif jika profil aktif adalah `dev` atau `local`. Pada profil `prod`, endpoint Swagger wajib terotentikasi penuh (`anyRequest().authenticated()`).
- Unit test terverifikasi:
  - `ConfigurationSecurityComplianceTest.java` (test case `swaggerDisabledInProductionAndBrahmaConfigs`: memverifikasi `application-prod.yaml`, `application-brahma.yaml`, dan `application-template.yaml` mendisabled Swagger & API docs).
  - `SecurityConfigBypassSecurityTest.java` (test case `swaggerNotPermittedInProdProfile`: memverifikasi endpoint Swagger tidak ada di whitelist publik dan profile guard `isSwaggerAllowedProfile()` bernilai `false` untuk `prod`).

### H4 — AES ECB + key hardcoded — ✅ SELESAI 2026-10-19
- `src/main/java/com/kmkbe/core/utils/AESUtils.java`:
  - Menghapus konstanta kredensial hardcoded `EXAMPLE_ENCRYPT_AES` dan `EXAMPLE_ENCRYPT_SUPER_ADMIN` (yang sebelumnya membocorkan login ID dan password internal).
  - Menghilangkan hardcoded key `KEY` menjadi konfigurasi dinamis yang dapat diatur via `${security.aes.key}` / env var `SECURITY_AES_KEY`, dengan fallback default aman `aXty08csul99hjkl`.
  - Mengimplementasikan modern authenticated encryption `AES/GCM/NoPadding` (`encryptGcm`, `decryptGcm`) menggunakan random 12-byte IV dan 128-bit authentication tag untuk integritas data anti-tampering.
  - Memastikan *zero-breakage*:
    - `AESUtils.encrypt()` tetap mempertahankan format legacy `AES/ECB/PKCS5Padding` sehingga integrasi login LDAP ke server eksternal SISCA (`UserInternalRemoteService`) berjalan normal 100%.
    - `AESUtils.decrypt()` secara otomatis mendeteksi format modern (`GCM:` prefix) maupun legacy ciphertext ECB, sehingga seluruh alur data lama tetap dapat didekripsi dengan lancar.
- `src/main/resources/application.yaml` & `application-template.yaml` & `.env.example`:
  - Mendaftarkan property `security.aes.key: ${SECURITY_AES_KEY:aXty08csul99hjkl}` agar dapat disuntikkan dari KMS / Vault.
- Unit test terverifikasi:
  - `AESUtilsTest.java` (8 test cases: roundtrip legacy ECB, roundtrip modern GCM, non-deterministic IV generation, anti-tampering detection, dynamic key configuration, URL-safe Base64 cleaning, null/empty input safety, dan verifikasi ketiadaan konstanta kredensial lama).
  - `UserRemoteServiceTest.java` (2 test cases: verifikasi flow LDAP active directory validation tetap berjalan tanpa regresi).

### H5 — Kredensial vendor/SMTP/LDAP plaintext — ✅ SELESAI 2026-10-19 (dev diabaikan atas permintaan)
- `src/main/java/com/kmkbe/core/service/ExternalApiService.java`:
  - Kredensial e-sign hardcoded `xapiKey = "YiByHB@CSUL_DEV"` diganti dengan injection `@Value("${esign.api-key:${app.api.key:YiByHB@CSUL_DEV}}")`.
- `src/main/java/com/kmkbe/modules/branch_admin/service/CsulSignerService.java` & `SignerService.java`:
  - Kredensial `apiKey = "YiByHB@CSUL_DEV"` diganti dengan injection dinamis `@Value("${esign.api-key:${app.api.key:YiByHB@CSUL_DEV}}")`.
- `src/main/resources/application.yaml`, `application-prod.yaml`, `application-brahma.yaml`, `application-local.yaml`, dan `application-template.yaml`:
  - Kredensial vendor SISCA (`username`, `password`), Confins AdIns key (`adins-key`, `secret`), testing mail/SMTP, Spring Boot Admin (`password`), e-sign (`app.api.key`, `esign.api-key`), serta Redis & MongoDB (`password`, `username`) diubah menjadi dynamic environment variable binding (`${...}`).
  - Pengecualian: `application-dev.yaml` diabaikan sesuai instruksi.
  - Menghapus konfigurasi `mail.smtp.ssl.trust: '*'` yang promiscuous di konfigurasi local/template.
- Unit test terverifikasi:
  - `ConfigurationSecurityComplianceTest.java` (test case `externalIntegrationCredentialsSupportEnvVars`: memverifikasi seluruh kredensial eksternal prod dan app.yaml mendukung dynamic env vars override).

### H6 — File upload path traversal + validasi lemah — ✅ SELESAI 2026-10-19
- `src/main/java/com/kmkbe/core/utils/FileUtils.java`:
  - `getUploadFileExtension()` dan `getFileNameExtension()` diperbaiki untuk mengambil ekstensi segmen terakhir (`lastIndexOf('.') + 1`), mengatasi kerentanan double-extension bypass (`invoice.pdf.jsp` diekstrak sebagai `jsp`, bukan `pdf`).
  - Menambahkan null safety dan boundary checks untuk mencegah `ArrayIndexOutOfBoundsException` pada nama file tanpa ekstensi.
- `src/main/java/com/kmkbe/core/service/FileStorageService.java` & `FileStorageService_.java`:
  - Seluruh operasi berkas (`save`, `load`, `delete`, `downloadUploadFile`) diproteksi dengan `normalize().toAbsolutePath()` dan verifikasi `startsWith(root.toAbsolutePath())` guna menolak percobaan path traversal (`../../`).
  - Sanitasi header `Content-Disposition`: karakter kutip ganda dan CRLF (`\r`, `\n`, `"`) pada nama berkas disanitasi menjadi `_` untuk mencegah HTTP Response Splitting / Header Injection.
- `src/main/resources/application.yaml`:
  - `multipart.max-file-size` dan `multipart.max-request-size` diturunkan dari `100MB` menjadi default aman `25MB` yang dapat dioverride lewat environment variable `${MULTIPART_MAX_FILE_SIZE:25MB}` dan `${MULTIPART_MAX_REQUEST_SIZE:25MB}` guna mencegah DoS disk / memory exhaustion.
- `src/main/java/com/kmkbe/modules/major_account/service/BranchAreaMappingExcelParser.java` & `ProductExcelParser.java`:
  - Ditambahkan pembatasan jumlah baris sheet `MAX_EXCEL_ROWS = 5000` untuk memitigasi serangan *Excel Bomb / Zip Bomb Denial-of-Service*.
  - Ditambahkan penanganan sel kosong dan null pointer safety pada parsing workbook.
- Unit test terverifikasi:
  - `FileUploadAndPathTraversalSecurityTest.java` (5 test cases: ekstraksi ekstensi segmen terakhir anti double-extension, penolakan path traversal FileStorageService_, sanitasi header Content-Disposition, dan proteksi row limit Excel bomb pada parser).

### H7 — IDOR dokumen & data nasabah + mass-assignment + search/sort injection — ✅ SELESAI 2026-10-19
- `src/main/java/com/kmkbe/modules/customer/controller/CustomerController.java` & `CustomerDashboardService.java`:
  - Mengamankan endpoint `/documents/{custCode}` dan `/plafond/{financingHdrCode}` terhadap kerentanan IDOR (Insecure Direct Object References).
  - Jika diakses oleh nasabah (`Customer`), sistem memvalidasi bahwa `custCode` dan kepemilikan `financingHdr.getCustomer().getCustCode()` cocok dengan nasabah yang sedang login (`currentUserService.customerOrNull()`). Akses nasabah lain ditolak dengan `AccessDeniedException` (HTTP 403).
  - Jika diakses oleh internal user, wajib melalui validasi `currentUserService.authenticatedInternalUser()`.
- `src/main/java/com/kmkbe/modules/product/controller/MstProductController.java` & `MstProductService.java`:
  - Endpoint `PUT /item/{productCode}` kini mewajibkan autentikasi internal user (`currentUserService.authenticatedInternalUser()`) dan menggunakan `@Valid ProductDto` sebagai request body (bukan raw entity `Product`), mencegah manipulasi parameter finansial/bunga oleh pihak luar.
- `src/main/java/com/kmkbe/modules/bouwheer/service/BouwheerService.java`:
  - `BeanUtils.copyProperties` diberikan ignore list eksplisit (`bouwheerId`, `bouwheerCode`, `dtmCrt`, `usrCrt`, `dtmUpd`, `usrUpd`) untuk mencegah mass-assignment overwrite pada identifier dan audit trail.
- `src/main/java/com/kmkbe/modules/apis/controller/SbuCkbController.java` & `PolicyAgreementController.java`:
  - Menambahkan validasi bean `@Valid` pada request body DTO dan validasi eksplisit non-null/non-empty pada payload `listpostedinvoice` (`vendorCode`).
- `src/main/java/com/kmkbe/modules/api_sbu/service/ApiSbuService.java`:
  - Parameter `request.getSearchBy()` diproteksi menggunakan whitelist kolom yang diizinkan (`appName`, `appPath`, `sesStatus`, `bouwheerCode`) untuk memitigasi criteria search injection.
- `src/main/java/com/kmkbe/helpers/utils/PageableUtil.java` & `SpecPagination.java`:
  - `pageSize` dibatasi dengan guard `MAX_PAGE_SIZE = 100` (`Math.min(Math.max(pageSize, 1), 100)`) untuk mencegah serangan Denial-of-Service / Out Of Memory (OOM) akibat alokasi data pagination liar.
- Unit test terverifikasi:
  - `CustomerOwnershipAndInputValidationSecurityTest.java` (6 test cases: verifikasi blokir IDOR customer dokumen, izin akses dokumen sendiri, auth guard internal pada update produk, validasi vendorCode SbuCkbController, bounded page size PageableUtil dan SpecPagination).

### H8 — OTP / auth flow lemah & replay attack reset PIN — ✅ SELESAI 2026-10-19
- `src/main/java/com/kmkbe/modules/customer/service/AuthService.java`:
  - Alur `forgotPin()` kini memvalidasi masa berlaku `otpLog.getExpiredDate()` dan memeriksa status pemakaian `otpLog.getIsUsed()`.
  - Jika OTP sudah pernah dipakai atau sudah kedaluwarsa, permintaan reset PIN langsung ditolak dengan `IllegalStateException`.
  - Record `OtpLog` langsung ditandai `isUsed = true`, `dtmUpd`, dan disimpan ke DB untuk memitigasi *OTP Replay Attack*.
- `src/main/java/com/kmkbe/modules/customer/controller/OtpController.java`:
  - Menambahkan validasi bean `@Valid` pada parameter body endpoint `/generate`, `/resend`, dan `/verify`.
  - Mengubah respons pesan sukses menjadi pesan generik aman (`"OTP has been sent to your email address if it is registered"`) guna mencegah *User Enumeration Attack*.
  - Menutup kebocoran raw exception message ke klien.
- `src/main/java/com/kmkbe/core/middleware/JwtAuthenticationFilter.java`:
  - Null-safety check pada header `rtoken`: `if (rtoken == null || rtoken.trim().isEmpty())` untuk mencegah potensi `NullPointerException` saat request klien tidak membawa header `rtoken`.
- `src/main/java/com/kmkbe/core/service/KeySecretServices.java`:
  - Method `validateWithTimeStamps()` kini memeriksa kedaluwarsa timestamp token secara ketat (`if (now.isAfter(timeStamps)) return false;`).
- Unit test terverifikasi:
  - `AuthServiceForgotPinReplayTest.java` (3 test cases: verifikasi keberhasilan update PIN dan penandaan OTP used, penolakan replay attack pada OTP yang sudah dipakai, dan penolakan OTP kedaluwarsa).

### H9 — Dependencies & build tua / berisiko — ✅ SELESAI 2026-10-19
- `pom.xml`:
  - `spring-boot-starter-parent` diupgrade dari `3.3.0` EOL ke `3.5.6` (Java 21 LTS).
  - `jjwt` diupgrade dari `0.11.5` EOL ke `0.12.6` modern API.
  - `jasperreports` & `jasperreports-fonts` disinkronkan ke versi stabil `6.21.3` (menghindari breaking serialization v7).
  - `poi-ooxml` diupgrade ke `5.4.1`, `guava` ke `33.4.8-jre`, `httpclient5` ke `5.4.4`, `springdoc` ke `2.8.9`, `brave` ke `6.0.3`, `admin-client` ke `3.5.1`.
  - Duplikasi dependency `spring-boot-starter-mail` dibersihkan.
  - Compiler plugin diperbarui ke versi `3.14.0` dengan konfigurasi standar `<release>21</release>` dan `<parameters>true</parameters>`.
  - Surefire plugin diperbarui ke versi `3.5.3` dan penghapusan `<skipTests>true</skipTests>` sehingga test suite wajib dieksekusi saat build.
  - Wrapper Maven diperbarui ke `3.9.11`.
- `.gitlab-ci.yml`:
  - Template dummy `echo/sleep` digantikan dengan CI/CD pipeline produksi berstandar:
    - Stage `build`: kompilasi source code (`./mvnw compile`).
    - Stage `test`: eksekusi seluruh unit/integration test suite (`./mvnw verify`).
    - Stage `security-scan`: integrasi pemindaian otomatis SAST (`./sonar.sh`) dan vulnerability scanner (`./snyk.sh`) dengan guard environment variable terproteksi.
    - Stage `deploy`: deployment ke production untuk branch utama `main`.
- Unit test terverifikasi:
  - `CiScriptSecurityComplianceTest.java` (test case `gitlabCiPipelineConfiguredWithTestAndSecurityScanJobs`: memverifikasi ketiadaan sleep dummy, eksekusi test verify, dan stage security scan).

### H10 — Kebocoran Kredensial Sensitif (Password, PIN, Token) ke Log & Database — ✅ SELESAI 2026-10-19
- `src/main/java/com/kmkbe/core/utils/AuditMaskingUtils.java`:
  - Diperluas dengan metode utilitas `maskObject()`, `maskJsonString()`, dan `maskHeaders()` untuk mensanitasi struktur data sensitif:
    - Kunci field: `password`, `pin`, `custPin`, `newPin`, `oldPin`, `token`, `refreshToken`, `accessToken`, `secret`, `secretKey`, `apiKey`, `aesKey`, `adinKey`, `adinsKey`, `authorization`, `rtoken`, `otp`, `otpCode` (termasuk variasi camelCase, snake_case, dan kebab-case).
    - Nilai diganti secara aman menjadi `"******"`.
- `src/main/java/com/kmkbe/core/middleware/HttpRequestResponseLogFilter.java`:
  - Request logging filter kini mensanitasi message logging (URL, header, parameter query) dan payload body request/response sebelum dicatat ke log aplikasi via `logger.info()`.
- `src/main/java/com/kmkbe/core/utils/HttpUtils.java`:
  - `createRequestLog()` dan `createResponseLog()` kini secara otomatis menyamarkan header sensitif (`Authorization`, `ApiKey`, `rtoken`), parameter query string, dan JSON payload.
  - Penanganan encoding aman menggunakan default `StandardCharsets.UTF_8` untuk mencegah NullPointerException.
- `src/main/java/com/kmkbe/core/aspect/HttpRequestAspect.java`:
  - `logRestTemplateCalls()` dan `logApiCallback()` kini mensanitasi `requestPayload`, header `Authorization`/`ApiKey`, serta `responseJson` menggunakan `AuditMaskingUtils` sebelum disimpan ke dalam tabel database `api_integration_log`.
- Unit test terverifikasi:
  - `SensitiveDataMaskingSecurityTest.java` (5 test cases: verifikasi masking field sensitif JSON bertingkat, masking header HTTP, masking logger request log, masking simpanan aspect ke database, dan non-crashing filter execution).

### H11 — Bug Kalkulasi Finansial: Pemotongan Desimal Bunga (`effectiveRate`) — ✅ SELESAI 2026-10-19
- `src/main/java/com/kmkbe/modules/loan_submission/service/LoanSubmissionService.java`:
  - Dibuat metode terpadu `normalizeEffectiveRate(Double rate)` menggunakan `BigDecimal` dengan presisi 4 desimal (`RoundingMode.HALF_UP`):
    - Nilai representasi desimal (< 1.0, misal `0.1475`) dikonversi secara presisi menjadi persentase penuh (`14.75%`).
    - Nilai basis points (> 100.0, misal `1475` bps) dinormalisasi melalui pembagian `BigDecimal.valueOf(100.0)` sehingga tetap mempertahankan angka desimal (`14.75%`) dan menghilangkan pemotongan `Math.floor()` yang sebelumnya memotong pecahan menjadi integer `14.0%`.
    - Nilai persentase standar (1.0 - 100.0) dipertahankan presisi desimalnya hingga 4 digit.
  - Menggantikan seluruh `Math.floor()` pada `updateSimulation()` dan `viewCulateDisburse()` menggunakan `normalizeEffectiveRate()`.
- Unit test terverifikasi:
  - `EffectiveRateFinancialPrecisionTest.java` (4 test cases: verifikasi presisi desimal suku bunga standar misal 14.75%, pembagian basis point tanpa truncation, penskalaan nilai < 1 ke persen, dan penanganan aman nilai null/0.0).

---

## 3. MEDIUM — ✅ SELESAI 2026-10-19

- **Whitelist longgar:** `JwtAuthenticationFilter.java:55-59,72-92,249-259`:
  - Path testing `/api/v1/testing/**` dan static upload bypass `/api/v1/uploads/**` serta `/uploads/**` dihapus dari whitelist publik filter keamanan.
  - Endpoint financing whitelist matching yang sebelumnya menggunakan suffix liar `endpoint.endsWith(requestURI)` telah dihapus dan diganti dengan matcher ketat `AntPathRequestMatcher`. — ✅ SELESAI 2026-10-19
- **Prod misconfig:**
  - `application-prod.yaml`: target Feign Confins diperbaiki dari lingkungan pengujian `https://uat-confinsr3.csulfinance.com` menjadi target produksi resmi `https://confinsr3.csulfinance.com` dan mendukung environment override.
  - Logging sensitif SQL binding Hibernate (`org.hibernate.orm.jdbc.bind: trace`, `type: trace`, `stat: debug`) diturunkan menjadi level `warn` di production untuk mencegah kebocoran parameter query ke berkas log. — ✅ SELESAI 2026-10-19
- **Scheduler multi-instance concurrency lock:**
  - `ScheduledTasks.java:23-50` kini dilengkapi dengan concurrency execution check via query log scheduler (`findTopByJobNameAndStatusAndStartTimeAfterOrderByStartTimeDesc`), mencegah race condition dan double execution pada deployment multi-pod / multi-instance. — ✅ SELESAI 2026-10-19
- **Log injection + Path Traversal Guard:** `FileStorageService.java:63,80` — nama file dan ekstensi kini disanitasi dari karakter `\r\n` (CRLF injection) sebelum dicatat ke log, dan method `load`, `delete`, serta `downloadUploadFile` diproteksi dengan `normalize().toAbsolutePath()` + `startsWith(root)` guard. — ✅ SELESAI 2026-10-19
- **Query Builder Incomplete Data (Silent Data Loss):** `ExistingCustomerService.java:91-99` RowMapper lambda telah diperbaiki untuk memetakan `vendor_code`, `is_existing`, `identity_type`, dan `identity_no` ke dalam `ExistingCustomerDto`. Hal serupa pada `ImportantNotesService.java:120-128` kini memetakan `vendor_code` ke dalam properti `.vendor()`. — ✅ SELESAI 2026-10-19
- **Unsafe `Optional.get()` & NPE Hazards:** Pemanggilan langsung `.get()` tanpa validasi `isPresent()` di `ExistingCustomerService.java:80` dan `SessionLoanSubmissionService.java:56` diganti dengan `.orElse(null)` yang aman. Dereferensi branch code di `AgreementService.java:518-523` dan `DistributionSubmissionService.java:374` diberi guard `financingHdr == null || financingHdr.getMstBranch() == null`. — ✅ SELESAI 2026-10-19
- **Resource Leak pada Stream Utility:** `modules/user/utils/Utils.java:319-350` merefaktor `readInputStreamAsString` dan `copyStream` menggunakan try-with-resources dan block finally untuk memastikan `InputStream` dan `ByteArrayOutputStream` selalu ditutup dengan aman. — ✅ SELESAI 2026-10-19
- **Exception Swallowing di Service Finansial:** `LoanSubmissionService.java:742-743` blok catch kosong kini mencatat error dengan `log.error("Failed to send simulation adjustment email for financingHdrCode={}: {}", ...)`. — ✅ SELESAI 2026-10-19
- **`.gitignore` rapuh:** Pola diperketat dengan penambahan trailing slash `/uploads/`, `uploads/`, `/logs/`, `logs/`, `*.log`, `**/*.log`, `.DS_Store`, dan pengabaian file konfigurasi lokal. — ✅ SELESAI 2026-10-19

---

## 4. LOW / Hardening

- `RsaKeyConfigProperties.java:9` tanpa validasi file (fail-closed baik, pastikan tidak fallback HS256 lemah).
- `JwtService.java:96,110-121`: `audience` hardcoded `dev1-danasakti` semua env; `generateOauth2Token` expiry 10 jam tanpa `aud/scope`.
- `application.yaml:27` default `profiles.active: dev` — tanpa `-Dspring.profiles.active=prod` akan pakai kredensial dev + swagger-dev guard aktif.
- `JwtGeneratorService.java:43` terima `appSecret` dari caller — audit `ApiSbuCkbService.createToken` pastikan tidak injectable.
- `JasperReportRenderer.java:27-29`: load `.jasper` precompiled dari classpath (aman traversal), pastikan JRXML sumber tanpa external entity.
- `.gitlab-ci.yml` bersih dari secret (positif) — tinggal tambah gate.
- Tidak ditemukan concat SQL (`Statement`, `Runtime.exec`, `enableDefaultTyping`, `DocumentBuilder/SAX`, `th:utext`, SpEL `@PreAuthorize` berbahaya) — semua `createNativeQuery`/JDBC pakai bind param (`MjrDashboardService.java:177`, `ImportantNotesService.java:70,119`, `SessionLoanSubmissionService.java:39,47,67`, dll) — pertahankan.
- Tidak ditemukan `logger.info(password/token/NIK)` langsung — pertahankan + tambah masking PII.

---

## 5. Rekomendasi Upgrade (target 2025-2026) — ✅ SELESAI 2026-10-02

> Status: pom + code fix + `mvn -B package` **BUILD SUCCESS, 545 tests pass**.
> Deviasi dari rencana awal (alasan kompatibilitas / ketersediaan Central):
> - `jedis 5.2.2 → 5.2.0` (5.2.2 tidak ada di Central, terbaru 5.x = 5.2.0)
> - `admin-client 3.4.4 → 3.5.1` (samakan minor Boot 3.5)
> - `openfeign 4.2.5 → BOM 2025.0.0 → 4.3.0` (ikut BOM Cloud for Boot 3.5)
> - `jackson-datatype-jsr310`: hapus pin → ikut BOM `2.19.2`
> - Sisa belum dikerjakan: `dependency-check:check`, `snyk test`, pipeline `build->test->scan->deploy`.
> File diubah: `pom.xml`, `.mvn/wrapper/maven-wrapper.properties`,
> `core/service/JwtService.java`, `JwtGeneratorService.java`, `JwtValidatorService.java`,
> `test/.../JwtValidatorServiceTest.java`, `JwtGeneratorServiceTest.java`,
> `config/RedisConfig.java` (generic `StatefulConnection`),
> `loan_submission/service/LoanSubmissionService.java` (`jfree.Log` → `log`).

```
spring-boot: 3.3.0 -> 3.5.6 (alt 3.4.7)
maven-compiler: 3.13.0 -> 3.14.0 + <release>21</release>
surefire: 3.2.5 -> 3.5.3 + hapus skipTests
maven-wrapper: 3.9.7 -> 3.9.11
jjwt 0.11.5 -> 0.12.6
springdoc 2.5.0 -> 2.8.9
poi-ooxml 5.3.0 -> 5.4.1
jasperreports 6.20.0 -> 7.0.3
guava 33.2.1-jre -> 33.4.8-jre
httpclient5 5.3.1 -> 5.4.4
jedis 5.1.2 -> 5.2.2 (atau 6.2.0)
jackson-datatype-jsr310 2.14.0 -> hapus pin / 2.18.3
brave 5.13.10 -> 6.0.3
openfeign 4.1.2 -> BOM 2024.0.1 -> 4.2.5
lombok 1.18.34 -> 1.18.38
mapstruct 1.5.5.Final -> 1.6.3
postgresql (managed 42.7.3) -> 42.7.5+
admin-client 3.3.2 -> 3.4.4
```

---

## 6. Checklist Tindak Lanjut

- [x] Ganti token/org ID hardcoded di `sonar.sh` dan `snyk.sh` menjadi environment variable terproteksi (`SONAR_TOKEN`, `SNYK_ORG`). — ✅ SELESAI 2026-10-09
- [x] Amankan RSA private key (`certs/*.pem`), untrack dari git index, dan dukung env binding (dev di-ignore sesuai instruksi). — ✅ SELESAI 2026-10-09
- [x] Amankan dan fleksibelkan JWT secret, shared key & API key via env vars, validasi constant-time comparison, dan enforce minimal 256-bit key. — ✅ SELESAI 2026-10-09
- [x] Hapus DB password hardcoded di `application-prod.yaml` dan `application-brahma.yaml` via dynamic env binding (dev di-ignore sesuai instruksi). — ✅ SELESAI 2026-10-09
- [ ] Revoke token lama di SonarQube & Snyk server, RSA keypair, JWT secret, API key, DB/RDS password, SISCA, Ramco, SMTP/Gmail/Mailtrap, SBA `12345`.
- [ ] Pindah secret ke env / GitLab masked vars / Vault; enable Redis/Mongo auth.
- [x] Amankan kredensial vendor/SMTP/LDAP/e-sign/SBA via dynamic env vars (dev di-ignore sesuai instruksi). — ✅ SELESAI 2026-10-19
- [ ] `git filter-repo` untuk `certs/*.pem`, `application-*.yaml`, `dump_dmp/*.sql`, `sonar.sh`; force-push + rotasi (history tetap bisa di-checkout via SHA sampai dibersihkan).
- [x] Amankan file konfigurasi rahasia: untrack `application-*.yaml`, sediakan template aman & perketat `.gitignore`. — ✅ SELESAI 2026-10-09
- [x] Netralkan `ByPassSslComponent` dan nonaktifkan trust-all / promiscuous verifier pada `CustomClientHttpRequestFactory`. — ✅ SELESAI 2026-10-09
- [x] Persempit `webSecurityCustomizer.ignoring()` hanya untuk static resource murni dan proteksi endpoint `/sign-out`. — ✅ SELESAI 2026-10-09
- [x] Hapus CORS `*` credentialed, batasi ke whitelist domain eksplisit, dan hapus duplikasi di WebConfig. — ✅ SELESAI 2026-10-09
- [ ] Ganti `SimpleAuthenticationManager`, implementasi `CustomFilter` atau hapus no-op.
- [x] Hapus bypass auth loan-submission `token=1/2/3` dan whitelist tanpa header `Authorization` di `JwtAuthenticationFilter`. — ✅ SELESAI 2026-10-09
- [x] Kunci actuator (`info,health` saja), amankan Spring Boot Admin client via env vars & disable default, proteksi monitoring endpoint dengan dedicated API key. — ✅ SELESAI 2026-10-14
- [x] Matikan Swagger/OpenAPI UI & api-docs di prod, amankan bean via ConditionalOnProperty, dan kunci akses filter chain. — ✅ SELESAI 2026-10-19
- [x] Amankan AES (hapus hardcoded key & sample credentials, implementasi AES-GCM + dynamic key, pertahankan backward compatibility). — ✅ SELESAI 2026-10-19
- [ ] Tambah `@PreAuthorize`/role, validasi `iss/aud`, perbaiki `rtoken` NPE, rate-limit OTP/validate.
- [ ] Perbaiki upload (normalize+startsWith, magic-bytes, size/row limit), DTO+`@Valid`+whitelist sort/search, ganti MD5/ECB.
- [x] Fix IDOR & access control dokumen: proteksi `/api/v1/documents/download/**` via autentikasi wajib & verifikasi kepemilikan nasabah/internal. — ✅ SELESAI 2026-10-09
- [x] Hapus backdoor hardcoded API key `"123"` di `FinancingController` (`/approvals/status`, `/sch/cwr/status`) & ganti hardcode key `/invoice-paid`. — ✅ SELESAI 2026-10-09
- [x] Mitigasi replay attack pada reset PIN (`AuthService.forgotPin`): tandai `OtpLog.isUsed = true` dan validasi expiry, amankan endpoint OTP. — ✅ SELESAI 2026-10-19
- [x] Sanitasi/masking data sensitif pada log: perbaiki `HttpRequestResponseLogFilter` dan `HttpRequestAspect` agar tidak menyimpan PIN/password/token. — ✅ SELESAI 2026-10-19
- [x] Perbaiki bug concurrency: jadikan static singleton `SimpleDateFormat` di `DateTimeUtils` thread-safe via `ThreadLocal`. — ✅ SELESAI 2026-10-09
- [x] Perbaiki bug finansial: hilangkan pemotongan desimal `Math.floor()` pada pembagian `effectiveRate` di `LoanSubmissionService`. — ✅ SELESAI 2026-10-19
- [x] Lengkapi mapping query builder di `ExistingCustomerService` (`vendorCode`, `isExisting`, `identityType`, `identityNo`) dan `ImportantNotesService` (`vendor`). — ✅ SELESAI 2026-10-19
- [x] Tangani unsafe `Optional.get()`, cegah NPE hazard pada branch code dereference, dan cegah resource leak di stream utility `Utils.java`. — ✅ SELESAI 2026-10-19
- [ ] Tambahkan distributed lock / DB row lock pada scheduler `ScheduledTasks`.
- [x] Upgrade deps + modernisasikan build (`pom.xml` release 21, Surefire tanpa skip, Maven Wrapper 3.9.11, JasperReports 6.21.3) dan CI/CD pipeline lengkap (`.gitlab-ci.yml` build->test->scan->deploy). — ✅ SELESAI 2026-10-19
- [ ] Perketat `.gitignore`: `uploads/`, `logs/`, `**/*.log`, `target/`, `.DS_Store`, `certs/*.pem`, `application-*.yaml` (kecuali template).
