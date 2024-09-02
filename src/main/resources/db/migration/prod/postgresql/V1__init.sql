-- region V1
DROP TABLE IF EXISTS customer CASCADE;
CREATE TABLE customer
(
    cust_id            BIGSERIAL                             NOT NULL,
    cust_code          UUID UNIQUE primary key,
    cust_no            VARCHAR(20)                           NULL,
    cust_name          VARCHAR(500),
    cust_type_code     VARCHAR(50),
    cust_id_type_code  VARCHAR(4),
    cust_id_no         VARCHAR(20),
    cust_email         VARCHAR(100),
    is_email_valid     BOOLEAN     DEFAULT FALSE             NOT NULL,
    cust_mobile_phone  VARCHAR(20),
    is_phone_valid     BOOLEAN     DEFAULT FALSE             NOT NULL,
    is_wa_active       BOOLEAN     DEFAULT FALSE             NOT NULL,
    cust_pin           VARCHAR(250),
    agree_tc           BOOLEAN     DEFAULT FALSE             NOT NULL,
    agree_legal_share  boolean     default false             not null NOT NULL,
    cust_external_code varchar(50)                           null,
    is_active          BOOLEAN     DEFAULT FALSE             NOT NULL,
    usr_crt            VARCHAR(50) DEFAULT 'system'::text    NOT NULL,
    dtm_crt            TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd            VARCHAR(50)                           NULL,
    dtm_upd            TIMESTAMP                             NULL
);

DROP TABLE IF EXISTS customer_company CASCADE;
CREATE TABLE customer_company
(
    cust_company_id       BIGSERIAL                             NOT NULL,
    cust_company_code     UUID primary key,
    cust_code             UUID                                  NOT NULL,
    cust_company_type     VARCHAR(50),
    company_model         VARCHAR(200),
    identity_type         VARCHAR(50),
    identity_no           VARCHAR(50),
    identity_issued_date  TIMESTAMP,
    identity_expired_date TIMESTAMP,
    company_address       VARCHAR(1000),
    rt                    VARCHAR(5),
    rw                    VARCHAR(5),
    kelurahan             VARCHAR(50),
    kecamatan             VARCHAR(50),
    city                  VARCHAR(50),
    province              VARCHAR(50),
    zipcode               VARCHAR(10),
    area                  VARCHAR(5),
    phone                 VARCHAR(20),
    ownership_status      VARCHAR(50),
    stay_since            TIMESTAMP,
    stay_length           NUMERIC(10, 2),
    usr_crt               VARCHAR(50) DEFAULT 'system'::text    NOT NULL,
    dtm_crt               TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd               VARCHAR(50)                           NULL,
    dtm_upd               TIMESTAMP                             NULL,
    CONSTRAINT fk_customer_personal_to_customer FOREIGN KEY (cust_code) REFERENCES customer (cust_code)
);

DROP TABLE IF EXISTS customer_personal CASCADE;
CREATE TABLE customer_personal
(
    cust_personal_id   BIGSERIAL                             NOT NULL,
    cust_personal_code UUID primary key,
    cust_code          UUID                                  NOT NULL,
    birthplace         VARCHAR(50),
    birthdate          TIMESTAMP,
    gender             VARCHAR(15),
    identity_type      VARCHAR(50)                           NULL,
    identity_no        VARCHAR(50)                           NULL, -- need to ask
    expired_date       TIMESTAMP                             NULL,
    mother_maiden_name VARCHAR(50),
    marital_status     VARCHAR(20),
    cust_model         VARCHAR(50),
    legal_address      VARCHAR(1000),
    rt                 VARCHAR(5),
    rw                 VARCHAR(5),
    kelurahan          VARCHAR(50),
    kecamatan          VARCHAR(50),
    city               VARCHAR(50),
    province           VARCHAR(50),
    zipcode            VARCHAR(10),
    area               VARCHAR(5),
    phone              VARCHAR(20),
    ownership_status   VARCHAR(50),
    stay_since         TIMESTAMP,
    stay_length        NUMERIC(10, 2),                             -- DOC NUMERIC(5, 2)
    usr_crt            VARCHAR(50) DEFAULT 'system'::text    NOT NULL,
    dtm_crt            TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd            VARCHAR(50)                           NULL,
    dtm_upd            TIMESTAMP                             NULL,
    CONSTRAINT fk_customer_personal_to_customer FOREIGN KEY (cust_code) REFERENCES customer (cust_code)
);

DROP TABLE IF EXISTS login_log CASCADE;
CREATE TABLE login_log
(
    login_log_id   BIGSERIAL NOT NULL,
    login_log_code UUID PRIMARY KEY,
    login_role     VARCHAR(50),
    cust_code      UUID      NULL,
    login_date     TIMESTAMP,
    is_logout      BOOLEAN DEFAULT FALSE,
    logout_date    TIMESTAMP NULL,
    usr_logout     TIMESTAMP NULL,
    CONSTRAINT fk_login_log_to_customer FOREIGN KEY (cust_code) REFERENCES customer (cust_code)
);

DROP TABLE IF EXISTS change_password_log CASCADE;
CREATE TABLE change_password_log
(
    change_password_id BIGSERIAL PRIMARY KEY,
    cust_code          UUID                                  NOT NULL,
    old_pin            VARCHAR(250),
    new_pin            VARCHAR(250),
    usr_crt            VARCHAR(50) DEFAULT 'system'::text    NOT NULL,
    dtm_crt            TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_change_password_log_to_customer FOREIGN KEY (cust_code) REFERENCES customer (cust_code)
);

DROP TABLE IF EXISTS otp_log CASCADE;
CREATE TABLE otp_log
(
    otp_log_id     BIGSERIAL PRIMARY KEY,
    otp_code       VARCHAR(10)                           NOT NULL,
    mobile_phone   VARCHAR(20),
    email          VARCHAR(50),
    generated_date TIMESTAMP,
    expired_date   TIMESTAMP,
    is_used        BOOLEAN     DEFAULT FALSE,
    usr_crt        VARCHAR(50) DEFAULT 'system'::text    NOT NULL,
    dtm_crt        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd        VARCHAR(50)                           NULL,
    dtm_upd        TIMESTAMP                             NULL
    -- ,UNIQUE (otp_code, email, mobile_phone)
);

DROP TABLE IF EXISTS email_template CASCADE;
CREATE TABLE email_template
(
    email_template_id   BIGSERIAL                             NOT NULL,
    email_template_code VARCHAR(20) UNIQUE PRIMARY KEY,
    subject_mail        VARCHAR(1000)                         NULL,
    body_mail           VARCHAR(8000)                         NULL,
    mail_to             VARCHAR(1000)                         NULL,
    mail_cc             VARCHAR(1000)                         NULL,
    mail_bcc            VARCHAR(1000)                         NULL,
    is_active           BOOLEAN     DEFAULT FALSE             NOT NULL,
    usr_crt             VARCHAR(50) DEFAULT 'system'::text    NOT NULL,
    dtm_crt             TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd             VARCHAR(50)                           NULL,
    dtm_upd             TIMESTAMP                             NULL
);

DROP TABLE IF EXISTS general_setting_hdr CASCADE;
CREATE TABLE general_setting_hdr
(
    gs_hdr_id      BIGSERIAL                             NOT NULL,
    gs_hdr_code    VARCHAR(20) UNIQUE PRIMARY KEY,
    gs_description VARCHAR(100),
    is_active      BOOLEAN     DEFAULT FALSE,
    usr_crt        VARCHAR(50) DEFAULT 'system'::text    NOT NULL,
    dtm_crt        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd        VARCHAR(50)                           NULL,
    dtm_upd        TIMESTAMP                             NULL
);

DROP TABLE IF EXISTS general_setting_dtl CASCADE;
CREATE TABLE general_setting_dtl
(
    gs_dtl_id    BIGSERIAL                             NOT NULL,
    gs_dtl_code  varchar(20) PRIMARY KEY,
    gs_hdr_code  VARCHAR(20),
    gs_dtl_value VARCHAR(250),
    is_active    BOOLEAN     DEFAULT FALSE             NOT NULL,
    usr_crt      VARCHAR(50) DEFAULT 'system'::text    NOT NULL,
    dtm_crt      TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd      VARCHAR(50)                           NULL,
    dtm_upd      TIMESTAMP                             NULL,
    CONSTRAINT fk_general_setting_dtl_to_hdr FOREIGN KEY (gs_hdr_code) REFERENCES general_setting_hdr (gs_hdr_code)
);

INSERT INTO
    email_template
(email_template_code, subject_mail, body_mail, is_active, usr_crt, dtm_crt)
VALUES
    ('M_CUST_NEW_OTP', 'Aktifasi Akun Dana Sakit', '<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta
            name="viewport"
            content="width=device-width, initial-scale=1.0"/>
</head>

<body>
    <p>Hi <span>{name}</span></p>
    <p>Selamat akun anda telah terdaftar dengan detail informasi:</p>
    <br/>
    <p>Email: <span>{email}</span></p>
    <p>No. KTP/NPWP: <span>{id_no}</span></p>
    <br/>
    <p>Jika Informasi di atas telah sesuai, harap memasukkan 4 digit kode OTP di bawah ini di website Dana Sakti untuk
        memverifikasi akun anda</p>
    <br/>
    <b style="font-size: 20px">{otp_code}</b>
    <br/>
    <br/>
    <p>Pada saat akun anda telah aktif, silahkan melanjutkan proses transaksi anda.</p>
    <p>
        Jika anda membutuhkan bantuan, harap hubungi customer service kamu melalui email
        <a href="mailto:help.danasakti@csul.com">help.danasakti@csul.com</a>
    </p>
    <br/>
    <br/>
    <p>Hormat Kami,</p>
    <br/>
    <p style="color: rgb(14, 193, 14); font-weight: bold">PT. Candra Sakti Utama Leasing</p>
    <img src="https://www.csulfinance.com/cfind/source/images/logo.png"/>
</body>
</html>', true, 'SYSTEM', NOW());

INSERT INTO
    email_template
(email_template_code, subject_mail, body_mail, is_active, usr_crt, dtm_crt)
VALUES
    ('M_CUST_ACTIVE', 'Konfirmasi Akun Dana Sakti Behasil di Aktifasi', '<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta
            name="viewport"
            content="width=device-width, initial-scale=1.0"/>
</head>
<body>
    <p>Hi <span>{name}</span></p>
    <p>
        Selamat akun anda telah berhasil di aktifasi,
        kini anda sudah dapat melanjutkan proses pembiayaan Dana Sakti anda.
    </p>
    <br/>
    <p>
        Jika anda membutuhkan bantuan, harap hubungi customer service kamu melalui email
        <a href="mailto:help.danasakti@csul.com">help.danasakti@csul.com</a>
    </p>
    <br/>
    <br/>
    <p>Hormat Kami,</p>
    <br/>
    <p style="color: rgb(14, 193, 14); font-weight: bold">PT. Candra Sakti Utama Leasing</p>
    <img src="https://www.csulfinance.com/cfind/source/images/logo.png"/>
</body>
</html>', true, 'SYSTEM', NOW());

INSERT INTO
    email_template
(email_template_code, subject_mail, body_mail, is_active, usr_crt, dtm_crt)
VALUES
    ('M_CUST_CHANGE_OTP', 'Pengajuan Pergantian Kode Akses (PIN) Dana Sakti', '<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta
            name="viewport"
            content="width=device-width, initial-scale=1.0"/>
</head>
<body>
    <p>Hi <span>{name}</span></p>
    <p>
        Terdapat permintaan pergantian Kode Akses
        untuk akun "{email}" di Website Dana Sakti
    </p>
    <p>
        Untuk mengkonfirmasi perubahan kode akses (PIN),
        harap memasukkan 4 digit kode OTP ini di Website Dana Sakti anda
    </p>
    <br/>
    <b style="font-size: 20px">{otp_code}</b>
    <br/>
    <br/>
    <p>
        Jika permintaan perubahan kode akses (PIN) tidak dilakukan oleh anda,
        maka harap abaikan pesan ini.
    </p>
    <p>
        Jika anda membutuhkan bantuan, harap hubungi customer service kamu melalui email
        <a href="mailto:help.danasakti@csul.com">help.danasakti@csul.com</a>
    </p>
    <br/>
    <br/>
    <p>Hormat Kami,</p>
    <br/>
    <p style="color: rgb(14, 193, 14); font-weight: bold">PT. Candra Sakti Utama Leasing</p>
    <img src="https://www.csulfinance.com/cfind/source/images/logo.png"/>
</body>
</html>', true, 'SYSTEM', NOW());

-- endregion

-- region V2
DROP TABLE IF EXISTS bouwheer CASCADE;
CREATE TABLE bouwheer
(
    bouwheer_id      BIGSERIAL                             NOT NULL,
    bouwheer_code    UUID PRIMARY KEY,
    bouwheer_name    VARCHAR(100),
    legal_address    VARCHAR(1000),
    rt               VARCHAR(5),
    rw               VARCHAR(5),
    kelurahan        VARCHAR(50),
    kecamatan        VARCHAR(50),
    city             VARCHAR(50),
    province         VARCHAR(50),
    zipcode          VARCHAR(10),
    area             VARCHAR(5)                            NULL,
    phone            VARCHAR(20),
    is_sbu           BOOLEAN,
    pic_name         VARCHAR(50),
    pic_email        VARCHAR(50),
    pic_mobile_phone VARCHAR(20),
    is_wa_active     BOOLEAN,
    term_of_payment  INT8,
    grace_period     INT8,
    aes_key          VARCHAR(16),
    secret_key       VARCHAR(100)                          NULL,
    api_key          VARCHAR(100)                          NULL,
    is_active        BOOLEAN     DEFAULT FALSE             NOT NULL,
    usr_crt          VARCHAR(50) DEFAULT 'system'::text    NOT NULL,
    dtm_crt          TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd          VARCHAR(50)                           NULL,
    dtm_upd          TIMESTAMP                             NULL
);

DROP TABLE IF EXISTS mst_file_type CASCADE;
CREATE TABLE mst_file_type
(
    file_type_id    BIGSERIAL                             NOT NULL,
    file_type_code  VARCHAR(20) PRIMARY KEY,
    file_type_name  VARCHAR(100)                          NOT NULL,
    file_type_desc  VARCHAR(500),
    file_allocation VARCHAR(50),
    is_mandatory    BOOLEAN     DEFAULT FALSE             NOT NULL,
    max_size_mb     INT8,
    cust_type_code  varchar(50)                           null,
    usr_crt         VARCHAR(50) DEFAULT 'system'::text    NOT NULL,
    dtm_crt         TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd         VARCHAR(50)                           NULL,
    dtm_upd         TIMESTAMP                             NULL
);

DROP TABLE IF EXISTS job_log CASCADE;
CREATE TABLE job_log
(
    job_log_id      BIGSERIAL PRIMARY KEY,
    server_name     VARCHAR(100),
    job_name        VARCHAR(100),
    job_type        VARCHAR(50),
    job_description VARCHAR(200)                          NULL,
    frequency       VARCHAR(20), -- OneTime, Daily, Weekly, Monthly
    custom_config   VARCHAR(1000)                         NULL,
    start_date      TIMESTAMP                             NULL,
    end_date        TIMESTAMP                             NULL,
    job_script      VARCHAR(8000),
    is_enabled      BOOLEAN     DEFAULT FALSE             NOT NULL,
    usr_crt         VARCHAR(50) DEFAULT 'system'::text    NOT NULL,
    dtm_crt         TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd         VARCHAR(50)                           NULL,
    dtm_upd         TIMESTAMP                             NULL
);

DROP TABLE IF EXISTS form_visit_log CASCADE;
CREATE TABLE form_visit_log
(
    form_visit_id  BIGSERIAL PRIMARY KEY,
    login_log_code UUID,
    module_code    VARCHAR(20),
    form_code      VARCHAR(20),
    path_access    VARCHAR(1000),
    access_date    TIMESTAMP,
    CONSTRAINT fk_form_visit_log_to_login_log FOREIGN KEY (login_log_code) REFERENCES login_log (login_log_code)
    -- ,CONSTRAINT fk_form_visit_log_to_mst_module FOREIGN KEY (login_log_code) REFERENCES login_log (login_log_code)
    -- ,CONSTRAINT fk_form_visit_log_to_mst_form FOREIGN KEY (login_log_code) REFERENCES login_log (login_log_code)
);

DROP TABLE IF EXISTS rabbitmq_log CASCADE;
CREATE TABLE rabbitmq_log
(
    rabbitmq_log_id BIGSERIAL PRIMARY KEY,
    exchange_name   VARCHAR(100),
    exchange_type   VARCHAR(100),
    last_exchange   VARCHAR(100),
    queue_name      VARCHAR(100),
    route_key       VARCHAR(100),
    payload         VARCHAR(1000),
    is_ack          BOOLEAN     DEFAULT FALSE             NOT NULL,
    dtm_crt         VARCHAR(50) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    dtm_upd         TIMESTAMP                             NULL
);

DROP TABLE IF EXISTS fly_way CASCADE;
CREATE TABLE fly_way
(
    fly_id         BIGSERIAL PRIMARY KEY,
    server_name    VARCHAR(20),
    db_name        VARCHAR(100),
    scheme         VARCHAR(20),
    table_name     VARCHAR(100),
    script_create  VARCHAR(8000),
    execution_time INT8,
    is_production  BOOLEAN     DEFAULT FALSE             NOT NULL,
    usr_crt        VARCHAR(50) DEFAULT 'system'::text    NOT NULL,
    dtm_crt        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd        VARCHAR(50)                           NULL,
    dtm_upd        TIMESTAMP                             NULL
);

DROP TABLE IF EXISTS api_integration_log CASCADE;
CREATE TABLE api_integration_log
(
    api_log_id      BIGSERIAL PRIMARY KEY,
    endpoint_url    VARCHAR(500),
    content_type    VARCHAR(50),
    request_payload VARCHAR(8000),
    response_json   VARCHAR(8000),
    response_status VARCHAR(30),
    usr_crt         VARCHAR(50) DEFAULT 'system'::text    NOT NULL,
    dtm_crt         TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    dtm_upd         TIMESTAMP                             NULL
);

DROP TABLE IF EXISTS error_log CASCADE;
CREATE TABLE error_log
(
    error_log_id   BIGSERIAL PRIMARY KEY,
    login_log_code UUID,
    error_type     VARCHAR(100)                          NULL,
    error_line     VARCHAR(10)                           NULL,
    error_msg      VARCHAR(500)                          NULL,
    page_url       VARCHAR(500)                          NULL,
    method_name    VARCHAR(100)                          NULL,
    request_param  VARCHAR(1000)                         NULL,
    usr_crt        VARCHAR(50) DEFAULT 'system'::text    NOT NULL,
    dtm_crt        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd        VARCHAR(50)                           NULL,
    dtm_upd        TIMESTAMP                             NULL,
    CONSTRAINT fk_error_log_to_login_log FOREIGN KEY (login_log_code) REFERENCES login_log (login_log_code)
);

DROP TABLE IF EXISTS legal_file CASCADE;
CREATE TABLE legal_file
(
    file_id        BIGSERIAL PRIMARY KEY,
    cust_code      UUID                                  NOT NULL,
    file_type_code VARCHAR(20)                           NULL,
    file_name      VARCHAR(500),
    file_path      VARCHAR(8000),
    content_type   VARCHAR(500),
    file_no        varchar(50)                           null,
    usr_crt        VARCHAR(50) DEFAULT 'system'::text    NOT NULL,
    dtm_crt        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd        VARCHAR(50)                           NULL,
    dtm_upd        TIMESTAMP                             NULL,
    CONSTRAINT fk_legal_file_to_customer FOREIGN KEY (cust_code) REFERENCES customer (cust_code),
    CONSTRAINT fk_legal_file_to_mst_file_type FOREIGN KEY (file_type_code) REFERENCES mst_file_type (file_type_code)
);

DROP TABLE IF EXISTS invoice CASCADE;
CREATE TABLE invoice
(
    invoice_id          BIGSERIAL                             NOT NULL,
    invoice_code        UUID PRIMARY KEY,
    cust_code           UUID                                  NOT NULL,
    bouwheer_code       UUID                                  NOT NULL,
    bouwheer_inv_no     VARCHAR(50),
    cust_inv_no         VARCHAR(50),
    invoice_description VARCHAR(250)                          NULL,
    invoice_date        TIMESTAMP,
    invoice_due_date    TIMESTAMP,
    invoice_amt         NUMERIC(17, 2),
    status              varchar(20)                           null,
    po_number           varchar(50)                           null,
    posting_date        timestamp                             null,
    usr_crt             VARCHAR(50) DEFAULT 'system'::text    NOT NULL,
    dtm_crt             TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd             VARCHAR(50)                           NULL,
    dtm_upd             TIMESTAMP                             NULL,
    CONSTRAINT fk_invoice_to_customer FOREIGN KEY (cust_code) REFERENCES customer (cust_code),
    CONSTRAINT fk_invoice_to_bouwheer FOREIGN KEY (bouwheer_code) REFERENCES bouwheer (bouwheer_code)
);

DROP TABLE IF EXISTS product CASCADE;
CREATE TABLE product
(
    product_id      BIGSERIAL PRIMARY KEY,
    branch_code     VARCHAR(3),
    product_name    VARCHAR(100),
    effective_date  TIMESTAMP,
    ntf_from        NUMERIC(17, 2),
    ntf_to          NUMERIC(17, 2),
    effective_rate  NUMERIC(5, 2),
    provision_rate  NUMERIC(5, 2),
    survey_fee      NUMERIC(17, 2),
    legal_fee       NUMERIC(17, 2),
    admin_limit_fee NUMERIC(17, 2),
    admin_rate      NUMERIC(5, 2),
    insurance_rate  NUMERIC(5, 2),
    others_fee      NUMERIC(17, 2),
    is_active       BOOL,
    usr_crt         VARCHAR(50) DEFAULT 'system'::text    NOT NULL,
    dtm_crt         TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd         VARCHAR(50)                           NULL,
    dtm_upd         TIMESTAMP                             NULL
    --,CONSTRAINT fk_product_to_branch FOREIGN KEY (branch_code) REFERENCES ()
);

DROP TABLE IF EXISTS financing_hdr CASCADE;
CREATE TABLE financing_hdr
(
    financing_hdr_id         BIGSERIAL                             NOT NULL,
    financing_hdr_code       UUID PRIMARY KEY,
    cust_code                UUID                                  NOT NULL,
    bouwheer_code            UUID                                  NOT NULL,
    financing_date           TIMESTAMP,
    currency_code            VARCHAR(5),
    invoice_qty              INT8,
    interest_type            VARCHAR(20),
    tenor                    INT8,
    effective_rate           NUMERIC(5, 2),
    interest_amt             NUMERIC(17, 2),
    term_of_payment          INT8,
    grace_period             INT8,
    retention                NUMERIC(5, 2),
    total_invoice_amt        NUMERIC(17, 2),
    provision_fee_percentage NUMERIC(5, 2),
    provision_fee_amt        NUMERIC(17, 2),
    survey_fee_amt           NUMERIC(17, 2),
    survey_fee_amt_nett      NUMERIC(17, 2),
    legal_fee_amt            NUMERIC(17, 2),
    legal_fee_amt_nett       NUMERIC(17, 2),
    admin_limit_amt          NUMERIC(17, 2),
    admin_fee_percentage     NUMERIC(5, 2),
    admin_fee_amt            NUMERIC(17, 2),
    insurance_fee_percentage NUMERIC(5, 2),
    insurance_fee_amt        NUMERIC(17, 2),
    others_fee_amt           NUMERIC(17, 2),
    financing_amt            NUMERIC(17, 2),
    disburse_amt             NUMERIC(17, 2),
    disburse_date            TIMESTAMP,
    financing_due_date       TIMESTAMP,
    financing_status         VARCHAR(50),
    financing_step           VARCHAR(50)                           NULL,
    usr_crt                  VARCHAR(50) DEFAULT 'system'::text    NOT NULL,
    dtm_crt                  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd                  VARCHAR(50)                           NULL,
    dtm_upd                  TIMESTAMP                             NULL,
    CONSTRAINT fk_financing_hdr_to_customer FOREIGN KEY (cust_code) REFERENCES customer (cust_code),
    CONSTRAINT fk_financing_hdr_to_bouwheer FOREIGN KEY (bouwheer_code) REFERENCES bouwheer (bouwheer_code)
);

DROP TABLE IF EXISTS financing_dtl CASCADE;
CREATE TABLE financing_dtl
(
    financing_dtl_id   BIGSERIAL                             NOT NULL,
    financing_dtl_code UUID PRIMARY KEY,
    financing_hdr_code UUID                                  NOT NULL,
    bouwheer_inv_no    VARCHAR(50),
    invoice_code       UUID                                  not null,                                  -- ini adalah improvisasi untuk relasi ke invoice
    paid_to_cust_date  TIMESTAMP                             NULL,
    bouwheer_paid_date TIMESTAMP                             NULL,
    usr_crt            VARCHAR(50) DEFAULT 'system'::text    NOT NULL,
    dtm_crt            TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd            VARCHAR(50)                           NULL,
    dtm_upd            TIMESTAMP                             NULL,
    CONSTRAINT fk_financing_dtl_to_financing_hdr FOREIGN KEY (financing_hdr_code) REFERENCES financing_hdr (financing_hdr_code),
    CONSTRAINT fk_financing_dtl_to_invoice FOREIGN KEY (invoice_code) REFERENCES invoice (invoice_code) -- ini adalah improvisasi untuk relasi ke invoice
    -- ,CONSTRAINT fk_invoice_to_bouwheer FOREIGN KEY (bouwheer_inv_no) REFERENCES invoice (bouwheer_inv_no)
);

INSERT INTO
    bouwheer (bouwheer_id, bouwheer_code, bouwheer_name, legal_address, rt, rw, kelurahan, kecamatan, city,
              province, zipcode, area, phone, is_sbu, pic_name, pic_email, pic_mobile_phone, is_wa_active,
              term_of_payment, grace_period, aes_key, is_active, usr_crt, dtm_crt, usr_upd, dtm_upd)
VALUES
    (660, 'e8f78820-0e47-da11-585c-8bf75dd608f1', 'PT.Truckindo', '863 Elms Rd, Botley', '03', '05', 'Pakansari',
     'Ciluar', 'Bogor', 'Jawa Barat', '16912', 'XyVkE', '(1223) 31 5007', true, 'Xie Yunxi', 'yxi6@gmail.com',
     '(1865) 61 6582', true, 788, 635, 'QY1QP3Izuw', true, '0BOwCDJeVS', '2021-03-05 09:54:37.000000', 'vLWi2Z8Ee7',
     '2013-01-18 15:24:28.000000');

INSERT INTO
    product (branch_code, product_name, effective_date, ntf_from, ntf_to, effective_rate, provision_rate, survey_fee,
             legal_fee, admin_limit_fee, admin_rate, others_fee, is_active, usr_crt, dtm_crt, insurance_rate)
VALUES
    ('414', 'Product Rate 10', '2024-05-25', 1, 2999999999, 16.61, 0.50,
     500000, 500000, 500000, 0.75, 500000, true, 'SYSTEM', now(), 0);

INSERT INTO
    product (branch_code, product_name, effective_date, ntf_from, ntf_to, effective_rate, provision_rate, survey_fee,
             legal_fee, admin_limit_fee, admin_rate, others_fee, is_active, usr_crt, dtm_crt, insurance_rate)
VALUES
    ('414', 'Product Rate 15', '2024-05-26', '3000000000', '4999999999', '16.50', '0.50',
     '500000', '500000', '500000', '0.50', '500000', true, 'SYSTEM', now(), 0);

INSERT INTO
    product (branch_code, product_name, effective_date, ntf_from, ntf_to, effective_rate, provision_rate, survey_fee,
             legal_fee, admin_limit_fee, admin_rate, others_fee, is_active, usr_crt, dtm_crt, insurance_rate)
VALUES
    ('414', 'Product Rate 15', '2024-05-27', '5000000000', '6999999999', '16.00', '0.50',
     '500000', '500000', '500000', '0.25', '500000', true, 'SYSTEM', now(), 0);

INSERT INTO
    product (branch_code, product_name, effective_date, ntf_from, ntf_to, effective_rate, provision_rate, survey_fee,
             legal_fee, admin_limit_fee, admin_rate, others_fee, is_active, usr_crt, dtm_crt, insurance_rate)
VALUES
    ('414', 'Product Rate 10', '2024-05-27', '7000000000', '10000000000', '15.10', '0.50',
     '500000', '500000', '500000', '0.10', '500000', true, 'SYSTEM', now(), 0);


insert into
    mst_file_type (file_type_code, file_type_name, file_type_desc, file_allocation, is_mandatory, max_size_mb, usr_crt,
                   dtm_crt)
values
    ('DOC001', 'Akta Penyesuaian Anggaran Dasar.PDF', 'Akta Penyesuaian Anggaran Dasar terhadap UU 40/2007',
     'Financing', false,
     20, 'SYSTEM', current_timestamp);


insert into
    mst_file_type (file_type_code, file_type_name, file_type_desc, file_allocation, is_mandatory, max_size_mb, usr_crt,
                   dtm_crt)
values
    ('DOC002', 'Akta Perubahan Maksud dan Tujuan Persero.PDF', 'Akta Perubahan Maksud dan Tujuan Persero', 'Financing',
     false,
     20, 'SYSTEM', current_timestamp);

insert into
    mst_file_type (file_type_code, file_type_name, file_type_desc, file_allocation, is_mandatory, max_size_mb, usr_crt,
                   dtm_crt)
values
    ('DOC003', 'Akta Perubahan Susunan Pengurus Perseroan.PDF',
     'Akta Perubahan Terakhir mengenai Perubahan Susunan Pengurus Perseroan', 'Financing', false,
     20, 'SYSTEM', current_timestamp);

insert into
    mst_file_type (file_type_code, file_type_name, file_type_desc, file_allocation, is_mandatory, max_size_mb, usr_crt,
                   dtm_crt)
values
    ('DOC004', 'Rekening Koran.PDF', 'Rekening Koran', 'Financing', false,
     20, 'SYSTEM', current_timestamp);

insert into
    mst_file_type (file_type_code, file_type_name, file_type_desc, file_allocation, is_mandatory, max_size_mb, usr_crt,
                   dtm_crt)
values
    ('DOC005', 'Form Aplikasi Pembiayaan.PDF', 'Form Aplikasi Pembiayaan', 'Financing', true,
     20, 'SYSTEM', current_timestamp);

insert into
    mst_file_type (file_type_code, file_type_name, file_type_desc, file_allocation, is_mandatory, max_size_mb, usr_crt,
                   dtm_crt)
values
    ('DOC006', '', 'Surat Instruksi Transfer', 'Financing', true,
     20, 'SYSTEM', current_timestamp);

DELETE FROM email_template WHERE email_template_code = 'M_CUST_LOAN';
INSERT INTO
    email_template
(email_template_code, subject_mail, body_mail, is_active, usr_crt, dtm_crt)
VALUES
    ('M_CUST_LOAN', 'Pengajuan Dana Sakti Anda Disetujui', '<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
    <style>
        .tbl {
            border-collapse: collapse;
        }

        .tbl th {
            background-color: #083B82;
            color: #fff;
            padding: 0.5rem 0.7rem;
            font-weight: 700;
            font-size: 0.8em;
        }

        .tbl td {
            padding: 0.5rem 0.7rem;
            font-weight: 400;
        }

        .tbl-center td {
            text-align: center;
        }

        .br {
            border: 0.5px solid #000;
        }

        .primary {
            font-weight: bold;
            background-color: #083B82;
            color: #fff;
        }
    </style>
</head>

<body>
    <p style="line-height: 5px;">Selamat!</p>
    <p style="line-height: 5px;">Pengajuan anda untuk {financingCode} telah disetujui!</p>
    <br>
    <table>
        <tr>
            <td>
                Nama Perusahaan
            </td>
            <td>:</td>
            <td>
                {companyName}
            </td>
        </tr>
        <tr>
            <td>
                Email
            </td>
            <td>:</td>
            <td>
                {email}
            </td>
        </tr>
        <tr>
            <td>
                No. Hp
            </td>
            <td>:</td>
            <td>
                {phoneNumber}
            </td>
        </tr>
        <tr>
            <td>
                Tanggal Pengajuan
            </td>
            <td>:</td>
            <td>
                {applicationDate}
            </td>
        </tr>
    </table>
    <br>
    <table class="tbl tbl-center">
        <thead>
            <tr>
                <th>No. Invoice</th>
                <th>Deskripsi</th>
                <th>Pemberi Kerja</th>

                <th>Tanggal Invoice</th>
                <th>Tanggal Jatuh Tempo</th>
                <th>Nilai Tagihan</th>
            </tr>
        </thead>
        <tbody>
            {invoices}
        </tbody>
    </table>
    <br>
    <p>Adapun rincian dari pengajuan ini sebagai berikut:</p>
    <table class="tbl br text-left">
        <tr>
            <td>
                Nilai Transaksi
            </td>
            <td>
                {invoiceAmt}
            </td>
        </tr>
        <tr>
            <td>
                Retensi
            </td>
            <td>
                {retention}
            </td>
        </tr>
        <tr>
            <td>
                Nilai Pembiayaan
            </td>
            <td>
                {financingAmt}
            </td>
        </tr>
        <tr>
            <td>
                Nilai Layaan
            </td>
            <td>
                {totalFeeAmt}
            </td>
        </tr>
        <tr>
            <td>
                Tenor
            </td>
            <td>
                {tenor}
            </td>
        </tr>
        <tr>
            <td>
                Jatuh Tempo
            </td>
            <td>
                {financingDueDate}
            </td>
        </tr>
        <tr class="primary">
            <td>
                Total Pencairan
            </td>
            <td>
                {disburseAmt}
            </td>
        </tr>
    </table>
    <p>
        Demikian informasi ini disampaikan terima kasih atas kepercayaan Anda.
    </p>
    <br />
    <p>Hormat Kami,</p>
    <p style="color: rgb(14, 193, 14); font-weight: bold">PT. Candra Sakti Utama Leasing</p>
    <img src="https://www.csulfinance.com/cfind/source/images/logo.png" />
</body>

</html>', true, 'SYSTEM', NOW());

INSERT INTO
    bouwheer (bouwheer_code, bouwheer_name, legal_address, rt, rw, kelurahan, kecamatan, city, province, zipcode, area,
              phone, is_sbu, pic_name, pic_email, pic_mobile_phone, is_wa_active, term_of_payment, grace_period,
              aes_key, is_active, usr_crt, dtm_crt)
values
    (uuid_generate_v4(), 'PT. Truck Indo', 'Jakarta', '07', '06', 'Jakarta', 'Jakarta', 'Jakarta', 'Jakarta', '123456',
     'JKT', '02515603', true, 'Truck Indo', 'truckindo@truckindo.com', '085156032859', true, 20, 2, '', true,
     'SYSTEM', now());

-- endregion

-- region V3
set search_path = users;

--region users.mst_branch
DROP TABLE IF EXISTS users.mst_branch CASCADE;
CREATE TABLE users.mst_branch
(
    branch_id      BIGSERIAL             NOT NULL,
    branch_code    VARCHAR(20),
    branch_name    varchar(50),
    branch_initial varchar(50),
    business_unit  varchar(50),
    cg_id          varchar(20),
    address        varchar(1000),
    rt             varchar(5),
    rw             varchar(5),
    kelurahan      varchar(50),
    kecamatan      varchar(50),
    city           varchar(50),
    province       varchar(50),
    zipcode        varchar(10),
    area           varchar(5),
    phone          varchar(20),
    is_active      BOOLEAN DEFAULT FALSE NOT NULL,
    usr_crt        VARCHAR(50)           NOT NULL,
    dtm_crt        TIMESTAMP             NOT NULL,
    usr_upd        VARCHAR(50)           NULL,
    dtm_upd        TIMESTAMP             NULL,
    PRIMARY KEY (branch_code)
);

--region insert users.mst_branch
INSERT INTO
    users.mst_branch (branch_code, branch_name, branch_initial, business_unit, cg_id, address, rt, rw, kelurahan,
                      kecamatan,
                      city, province, zipcode, area, phone, is_active, usr_crt, dtm_crt, usr_upd, dtm_upd)
SELECT
    branch_code,
    branch_name,
    branch_initial,
    business_unit,
    cg_id,
    address,
    rt,
    rw,
    kelurahan,
    kecamatan,
    "City",
    null,
    zipcode,
    area,
    phone,
    is_active::BOOLEAN,
    usr_crt,
    dtm_crt::TIMESTAMP,
    usr_upd,
    dtm_upd::TIMESTAMP
FROM
    users.branch;
--endregion

--endregion

--region users.mst_employee
DROP TABLE IF EXISTS users.mst_employee CASCADE;
CREATE TABLE users.mst_employee
(
    employee_id    BIGSERIAL             NOT NULL,
    employee_code  varchar(10),
    employee_name  varchar(50),
    report_to_code varchar(20),
    employee_type  varchar(20),
    branch_code    varchar(20)           NULL,
    email          varchar(50),
    phone          varchar(20),
    is_active      BOOLEAN DEFAULT FALSE NOT NULL,
    usr_crt        VARCHAR(50)           NOT NULL,
    dtm_crt        TIMESTAMP             NOT NULL,
    usr_upd        VARCHAR(50)           NULL,
    dtm_upd        TIMESTAMP             NULL,
    PRIMARY KEY (employee_code),
    CONSTRAINT fk_mst_employee_to_mst_branch FOREIGN KEY (branch_code) REFERENCES users.mst_branch (branch_code)
);

--region insert users.mst_employee
INSERT INTO
    users.mst_employee (employee_code, employee_name, report_to_code, employee_type, branch_code, email,
                        phone, is_active,
                        usr_crt, dtm_crt, usr_upd, dtm_upd)
SELECT
    employee_code,
    employee_name,
    report_to_code,
    "EmployeeType",
    e.branch_code,
    email,
    e.phone,
    e.is_active::BOOLEAN,
    e.usr_crt,
    e.dtm_crt::TIMESTAMP,
    e.usr_upd,
    e.dtm_upd::TIMESTAMP
FROM
    users.employee e
        join users.branch b on e.branch_code = b.branch_code;
--endregion

--endregion

--region users.mst_application
DROP TABLE IF EXISTS users.mst_application CASCADE;
CREATE TABLE users.mst_application
(
    application_id   BIGSERIAL             NOT NULL,
    application_code VARCHAR(20)           NOT NULL,
    application_name VARCHAR(50)           NOT NULL,
    application_desc VARCHAR(250)          NOT NULL,
    path_icon        VARCHAR(1000)         NULL,
    is_active        BOOLEAN DEFAULT FALSE NOT NULL,
    usr_crt          VARCHAR(50)           NOT NULL,
    dtm_crt          TIMESTAMP             NOT NULL,
    usr_upd          VARCHAR(50)           NULL,
    dtm_upd          TIMESTAMP             NULL,
    PRIMARY KEY (application_code)
);

--region insert users.mst_application
INSERT INTO
    users.mst_application (application_code, application_name, application_desc, path_icon, is_active, usr_crt,
                           dtm_crt, usr_upd, dtm_upd)
VALUES
    ('danasakti', 'Dana Sakti', 'Aplikasi Dana Sakti Factoring (KMK Digital 2.0)', '-', '1', 'by system',
     '2024-07-17 15:00', 'by system', '2024-07-17 15:00');
--endregion

--endregion

--region users.mst_module
DROP TABLE IF EXISTS users.mst_module CASCADE;
CREATE TABLE users.mst_module
(
    module_id        BIGSERIAL             NOT NULL,
    module_code      VARCHAR(20)           NOT NULL,
    module_name      VARCHAR(50)           NOT NULL,
    application_code VARCHAR(20)           NOT NULL,
    is_active        BOOLEAN DEFAULT FALSE NOT NULL,
    usr_crt          VARCHAR(50)           NOT NULL,
    dtm_crt          TIMESTAMP             NOT NULL,
    usr_upd          VARCHAR(50)           NULL,
    dtm_upd          TIMESTAMP             NULL,
    PRIMARY KEY (module_code),
    CONSTRAINT fk_mst_module_to_mst_application FOREIGN KEY (application_code) REFERENCES users.mst_application (application_code)
);

--region insert users.mst_module
INSERT INTO
    users.mst_module (module_id, module_code, module_name, application_code, is_active, usr_crt, dtm_crt, usr_upd,
                      dtm_upd)
VALUES
    ('1', 'mdl_mjr_acct', 'Major Account', 'danasakti', '1', 'by system', '2024-07-17 15:00', 'by system',
     '2024-07-17 15:00');
INSERT INTO
    users.mst_module (module_id, module_code, module_name, application_code, is_active, usr_crt, dtm_crt, usr_upd,
                      dtm_upd)
VALUES
    ('2', 'mdl_brch_adm', 'Branch Admin', 'danasakti', '1', 'by system', '2024-07-17 15:00', 'by system',
     '2024-07-17 15:00');
INSERT INTO
    users.mst_module (module_id, module_code, module_name, application_code, is_active, usr_crt, dtm_crt, usr_upd,
                      dtm_upd)
VALUES
    ('3', 'mdl_finance', 'Finance', 'danasakti', '1', 'by system', '2024-07-17 15:00', 'by system', '2024-07-17 15:00');
--endregion

--endregion

--region users.mst_form
DROP TABLE IF EXISTS users.mst_form CASCADE;
CREATE TABLE users.mst_form
(
    form_id     BIGSERIAL             NOT NULL,
    form_code   VARCHAR(20)           NOT NULL,
    form_name   VARCHAR(100),
    form_path   VARCHAR(500),
    form_icon   VARCHAR(50),
    parent_code VARCHAR(20), --VARCHAR(10)
    order_no    INT2,
    module_code VARCHAR(20), --VARCHAR(10)
    is_active   BOOLEAN DEFAULT FALSE NOT NULL,
    usr_crt     VARCHAR(50)           NOT NULL,
    dtm_crt     TIMESTAMP             NOT NULL,
    usr_upd     VARCHAR(50)           NULL,
    dtm_upd     TIMESTAMP             NULL,
    PRIMARY KEY (form_code),
    CONSTRAINT fk_mst_form_to_mst_module FOREIGN KEY (module_code) REFERENCES users.mst_module (module_code)
);

--region insert users.mst_form
INSERT INTO
    users.mst_form (form_id, form_code, form_name, form_path, form_icon, parent_code, order_no, module_code, is_active,
                    usr_crt, dtm_crt, usr_upd, dtm_upd)
VALUES
    ('1', 'menu_mjr_account', 'Major Account', '#', '-', '', '1', 'mdl_mjr_acct', '1', 'by system', '2024-07-17 15:00',
     'by system', '2024-07-17 15:00');
INSERT INTO
    users.mst_form (form_id, form_code, form_name, form_path, form_icon, parent_code, order_no, module_code, is_active,
                    usr_crt, dtm_crt, usr_upd, dtm_upd)
VALUES
    ('2', 'mnu_distr_cbg', 'Distribusi Cabang', '~/distribusi-cabang/index', '-', 'mjr_account', '1', 'mdl_mjr_acct',
     '1', 'by system', '2024-07-17 15:00', 'by system', '2024-07-17 15:00');
INSERT INTO
    users.mst_form (form_id, form_code, form_name, form_path, form_icon, parent_code, order_no, module_code, is_active,
                    usr_crt, dtm_crt, usr_upd, dtm_upd)
VALUES
    ('3', 'mnu_mst_cbg', 'Upload Master Produk', '~/master-cabang/index', '-', 'mjr_account', '2', 'mdl_mjr_acct', '1',
     'by system', '2024-07-17 15:00', 'by system', '2024-07-17 15:00');
INSERT INTO
    users.mst_form (form_id, form_code, form_name, form_path, form_icon, parent_code, order_no, module_code, is_active,
                    usr_crt, dtm_crt, usr_upd, dtm_upd)
VALUES
    ('4', 'mnu_mst_prd', 'Input Master Produk', '~/master-product/index', '-', 'mjr_account', '3', 'mdl_mjr_acct', '1',
     'by system', '2024-07-17 15:00', 'by system', '2024-07-17 15:00');
INSERT INTO
    users.mst_form (form_id, form_code, form_name, form_path, form_icon, parent_code, order_no, module_code, is_active,
                    usr_crt, dtm_crt, usr_upd, dtm_upd)
VALUES
    ('5', 'mnu_brnch_adm', 'Branch Admin', '#', '-', '', '2', 'mdl_brch_adm', '1', 'by system', '2024-07-17 15:00',
     'by system', '2024-07-17 15:00');
INSERT INTO
    users.mst_form (form_id, form_code, form_name, form_path, form_icon, parent_code, order_no, module_code, is_active,
                    usr_crt, dtm_crt, usr_upd, dtm_upd)
VALUES
    ('6', 'mnu_dft_pengajuan', 'Daftar Pengajuan', '~/utilization-list/index', '-', 'dft_pengajuan', '1',
     'mdl_brch_adm', '1', 'by system', '2024-07-17 15:00', 'by system', '2024-07-17 15:00');
INSERT INTO
    users.mst_form (form_id, form_code, form_name, form_path, form_icon, parent_code, order_no, module_code, is_active,
                    usr_crt, dtm_crt, usr_upd, dtm_upd)
VALUES
    ('7', 'mnu_fin', 'Finance', '#', '-', '', '3', 'mdl_finance', '1', 'by system', '2024-07-17 15:00', 'by system',
     '2024-07-17 15:00');
INSERT INTO
    users.mst_form (form_id, form_code, form_name, form_path, form_icon, parent_code, order_no, module_code, is_active,
                    usr_crt, dtm_crt, usr_upd, dtm_upd)
VALUES
    ('8', 'mnu_inv_retensi', 'Invoice & Retensi Settlement', '~/finance/index', '', 'mnu_fin', '1', 'mdl_finance', '1',
     'by system', '2024-07-17 15:00', 'by system', '2024-07-17 15:00');
--endregion

--endregion users.mst_form

--region users.mst_role
DROP TABLE IF EXISTS users.mst_role CASCADE;
CREATE TABLE users.mst_role
(
    role_id   BIGSERIAL             NOT NULL,
    role_code VARCHAR(20)           NOT NULL,
    role_name VARCHAR(50)           NOT NULL,
    is_active BOOLEAN DEFAULT FALSE NOT NULL,
    usr_crt   VARCHAR(50)           NOT NULL,
    dtm_crt   TIMESTAMP             NOT NULL,
    usr_upd   VARCHAR(50)           NULL,
    dtm_upd   TIMESTAMP             NULL,
    PRIMARY KEY (role_code)
);

--region insert data users.mst_role
INSERT INTO
    users.mst_role (role_id, role_code, role_name, is_active, usr_crt, dtm_crt, usr_upd, dtm_upd)
VALUES
    ('1', 'sp_admin', 'Super Admin', '1', 'by system', '2024-07-17 15:00', 'by system', '2024-07-17 15:00');
INSERT INTO
    users.mst_role (role_id, role_code, role_name, is_active, usr_crt, dtm_crt, usr_upd, dtm_upd)
VALUES
    ('2', 'mjr_account', 'Major Account', '1', 'by system', '2024-07-17 15:00', 'by system', '2024-07-17 15:00');
INSERT INTO
    users.mst_role (role_id, role_code, role_name, is_active, usr_crt, dtm_crt, usr_upd, dtm_upd)
VALUES
    ('3', 'finance', 'Finance', '1', 'by system', '2024-07-17 15:00', 'by system', '2024-07-17 15:00');
INSERT INTO
    users.mst_role (role_id, role_code, role_name, is_active, usr_crt, dtm_crt, usr_upd, dtm_upd)
VALUES
    ('4', 'mgmnt', 'Management', '1', 'by system', '2024-07-17 15:00', 'by system', '2024-07-17 15:00');
--endregion

--endregion

--region users.mst_user
DROP TABLE IF EXISTS users.mst_user CASCADE;
CREATE TABLE users.mst_user
(
    user_id       BIGSERIAL             NOT NULL,
    user_code     UUID                  NOT NULL,
    username      VARCHAR(50)           NOT NULL,
    employee_code VARCHAR(20)           NOT NULL,
    is_user_ad    BOOLEAN DEFAULT FALSE NOT NULL,
    is_user_nonad BOOLEAN DEFAULT FALSE NOT NULL,
    password      VARCHAR(250)          NULL,
    is_active     BOOLEAN DEFAULT FALSE NOT NULL,
    usr_crt       VARCHAR(50)           NOT NULL,
    dtm_crt       TIMESTAMP             NOT NULL,
    usr_upd       VARCHAR(50)           NULL,
    dtm_upd       TIMESTAMP             NULL,
    PRIMARY KEY (user_code),
    CONSTRAINT username_unique UNIQUE (username),
    CONSTRAINT fk_mst_user_to_mst_employee FOREIGN KEY (employee_code) REFERENCES users.mst_employee (employee_code)
);

--region insert users.mst_user
INSERT INTO
    users.mst_user (user_id, user_code, username, employee_code, is_user_ad, is_user_nonad, password, is_active,
                    usr_crt,
                    dtm_crt, usr_upd, dtm_upd)
VALUES
    ('1', gen_random_uuid(), 'rizky.permana', '51905', '1', '0',
     '$2b$10$Bmu4F8wv6A.t6LqClODr4uzPJmw8LcsRyfWJzSB0tvCJo.uFVsawu', '1', 'by system', '2024-07-17 15:00', 'by system',
     '2024-07-17 15:00');
--endregion

--endregion

--region users.mst_application_role
DROP TABLE IF EXISTS users.mst_application_role CASCADE;
CREATE TABLE users.mst_application_role
(
    application_role_id   BIGSERIAL             NOT NULL,
    application_role_code UUID                  NOT NULL,
    application_code      VARCHAR(20)           NOT NULL,
    role_code             VARCHAR(20)           NOT NULL,
    is_active             BOOLEAN DEFAULT FALSE NOT NULL,
    usr_crt               VARCHAR(50)           NOT NULL,
    dtm_crt               TIMESTAMP             NOT NULL,
    usr_upd               VARCHAR(50)           NULL,
    dtm_upd               TIMESTAMP             NULL,
    PRIMARY KEY (application_role_code),
    CONSTRAINT fk_mst_application_role_to_mst_application FOREIGN KEY (application_code) REFERENCES users.mst_application (application_code),
    CONSTRAINT fk_mst_application_role_to_mst_role FOREIGN KEY (role_code) REFERENCES users.mst_role (role_code)
);

--region insert users.mst_application_role
INSERT INTO
    users.mst_application_role (application_role_id, application_role_code, application_code, role_code, is_active,
                                usr_crt,
                                dtm_crt,
                                usr_upd, dtm_upd)
VALUES
    ('1', '906dfbfe-f1cd-4a31-bba2-2028a823305b', 'danasakti', 'sp_admin', '1', 'by system', '2024-07-17 15:00',
     'by system', '2024-07-17 15:00');
--endregion

--endregion

--region users.mst_app_role_form
DROP TABLE IF EXISTS users.mst_app_role_form CASCADE;
CREATE TABLE users.mst_app_role_form
(
    app_role_form_id      BIGSERIAL             NOT NULL,
    app_role_form_code    UUID                  NOT NULL,
    application_role_code UUID                  NOT NULL,
    form_code             VARCHAR(20),
    is_active             BOOLEAN DEFAULT FALSE NOT NULL,
    usr_crt               VARCHAR(50)           NOT NULL,
    dtm_crt               TIMESTAMP             NOT NULL,
    usr_upd               VARCHAR(50)           NULL,
    dtm_upd               TIMESTAMP             NULL,
    PRIMARY KEY (app_role_form_code),
    CONSTRAINT fk_mst_app_role_form_to_mst_application_role FOREIGN KEY (application_role_code) REFERENCES users.mst_application_role (application_role_code),
    CONSTRAINT fk_mst_app_role_form_to_mst_form FOREIGN KEY (form_code) REFERENCES users.mst_form (form_code)
);

--region insert users.mst_app_role_form
-- (select application_role_code from users.mst_application_role limit 1)
INSERT INTO
    users.mst_app_role_form (app_role_form_id, app_role_form_code, application_role_code, form_code, is_active, usr_crt,
                             dtm_crt, usr_upd, dtm_upd)
VALUES
    (1, '01c87ea4-6e7b-415d-a2a3-9403f09808e6', '906dfbfe-f1cd-4a31-bba2-2028a823305b', 'menu_mjr_account', true,
     'by system', '2024-07-17 15:00:00.000000', 'by system', '2024-07-17 15:00:00.000000');
INSERT INTO
    users.mst_app_role_form (app_role_form_id, app_role_form_code, application_role_code, form_code, is_active, usr_crt,
                             dtm_crt, usr_upd, dtm_upd)
VALUES
    (2, 'dfb6e236-b132-4dcf-9157-1d551c602d9e', '906dfbfe-f1cd-4a31-bba2-2028a823305b', 'mnu_distr_cbg', true,
     'by system', '2024-07-17 15:00:00.000000', 'by system', '2024-07-17 15:00:00.000000');
INSERT INTO
    users.mst_app_role_form (app_role_form_id, app_role_form_code, application_role_code, form_code, is_active, usr_crt,
                             dtm_crt, usr_upd, dtm_upd)
VALUES
    (3, '225cc19b-321c-4aa4-bf96-d1c7504f5927', '906dfbfe-f1cd-4a31-bba2-2028a823305b', 'mnu_mst_cbg', true,
     'by system', '2024-07-17 15:00:00.000000', 'by system', '2024-07-17 15:00:00.000000');
INSERT INTO
    users.mst_app_role_form (app_role_form_id, app_role_form_code, application_role_code, form_code, is_active, usr_crt,
                             dtm_crt, usr_upd, dtm_upd)
VALUES
    (4, '1cabb8af-0c73-4022-b7cb-08d3fdb5f045', '906dfbfe-f1cd-4a31-bba2-2028a823305b', 'mnu_mst_prd', true,
     'by system', '2024-07-17 15:00:00.000000', 'by system', '2024-07-17 15:00:00.000000');
--endregion

--endregion

--region users.mst_app_role_form_user
DROP TABLE IF EXISTS users.mst_app_role_form_user CASCADE;
CREATE TABLE users.mst_app_role_form_user
(
    app_role_form_user_id   BIGSERIAL             NOT NULL,
    app_role_form_user_code UUID                  NOT NULL,
    app_role_form_code      UUID                  NOT NULL,
    user_code               UUID                  NOT NULL,
    is_active               BOOLEAN DEFAULT FALSE NOT NULL,
    usr_crt                 VARCHAR(50)           NOT NULL,
    dtm_crt                 TIMESTAMP             NOT NULL,
    usr_upd                 VARCHAR(50)           NULL,
    dtm_upd                 TIMESTAMP             NULL,
    PRIMARY KEY (app_role_form_user_code),
    CONSTRAINT fk_mst_app_role_form_user_to_mst_app_role_form FOREIGN KEY (app_role_form_code) REFERENCES users.mst_app_role_form (app_role_form_code),
    CONSTRAINT fk_mst_app_role_form_user_to_mst_user FOREIGN KEY (user_code) REFERENCES users.mst_user (user_code)
);

--region insert users.mst_app_role_form_user
INSERT INTO
    users.mst_app_role_form_user (app_role_form_user_id, app_role_form_user_code, app_role_form_code, user_code,
                                  is_active,
                                  usr_crt,
                                  dtm_crt, usr_upd, dtm_upd)
VALUES
    ('1', gen_random_uuid(),
     '01c87ea4-6e7b-415d-a2a3-9403f09808e6',
     (
         select
             user_code
         from
             users.mst_user
         limit 1
     ), '1', 'by system', '2024-07-17 15:00', 'by system', '2024-07-17 15:00');

INSERT INTO
    users.mst_app_role_form_user (app_role_form_user_id, app_role_form_user_code, app_role_form_code, user_code,
                                  is_active,
                                  usr_crt,
                                  dtm_crt, usr_upd, dtm_upd)
VALUES
    ('2', gen_random_uuid(),
     'dfb6e236-b132-4dcf-9157-1d551c602d9e',
     (
         select
             user_code
         from
             users.mst_user
         limit 1
     ), '1', 'by system', '2024-07-17 15:00', 'by system', '2024-07-17 15:00');

INSERT INTO
    users.mst_app_role_form_user (app_role_form_user_id, app_role_form_user_code, app_role_form_code, user_code,
                                  is_active,
                                  usr_crt,
                                  dtm_crt, usr_upd, dtm_upd)
VALUES
    ('3', gen_random_uuid(),
     '225cc19b-321c-4aa4-bf96-d1c7504f5927',
     (
         select
             user_code
         from
             users.mst_user
         limit 1
     ), '1', 'by system', '2024-07-17 15:00', 'by system', '2024-07-17 15:00');

INSERT INTO
    users.mst_app_role_form_user (app_role_form_user_id, app_role_form_user_code, app_role_form_code, user_code,
                                  is_active,
                                  usr_crt,
                                  dtm_crt, usr_upd, dtm_upd)
VALUES
    ('4', gen_random_uuid(),
     '1cabb8af-0c73-4022-b7cb-08d3fdb5f045',
     (
         select
             user_code
         from
             users.mst_user
         limit 1
     ), '1', 'by system', '2024-07-17 15:00', 'by system', '2024-07-17 15:00');

--endregion

--endregion
--endregion

-- region V4
alter table public.customer
    add column if not exists agree_legal_share boolean default false not null;

alter table public.customer
    add column if not exists cust_external_code varchar(50) null;

alter table public.invoice
    add column if not exists status varchar(20) null;

alter table public.financing_hdr
    add column if not exists financing_step VARCHAR(50) NULL;

alter table public.bouwheer
    add column if not exists secret_key VARCHAR(100) NULL;

alter table public.bouwheer
    add column if not exists api_key VARCHAR(100) NULL;

alter table public.financing_hdr
    add column if not exists financing_step VARCHAR(50) NULL;

alter table public.invoice
    add column if not exists po_number VARCHAR(50) NULL;

alter table public.invoice
    add column if not exists posting_date timestamp NULL;

drop table if exists public.simulation_hist cascade;
create table public.simulation_hist
(
    simulation_hist_id   BIGSERIAL             NOT NULL,
    simulation_hist_code UUID,
    financing_hdr_code   UUID                  NOT NULL,
    total_invoice_amt    NUMERIC(17, 2)        NOT NULL,
    retention            NUMERIC(5, 2)         NOT NULL,
    admin_amt            NUMERIC(17, 2)        NOT NULL,
    financing_amt        NUMERIC(17, 2)        NOT NULL,
    is_used              BOOLEAN DEFAULT FALSE NOT NULL,
    usr_crt              VARCHAR(50)           NOT NULL,
    dtm_crt              TIMESTAMP             NOT NULL,
    usr_upd              VARCHAR(50)           NULL,
    dtm_upd              TIMESTAMP             NULL,
    PRIMARY KEY (simulation_hist_code),
    constraint fk_simulation_hist_to_financing_hdr foreign key (financing_hdr_code) references public.financing_hdr (financing_hdr_code)
);

drop table if exists public.cwr cascade;
create table public.cwr
(
    cwr_id          bigserial               not null,
    cwr_code        varchar(20),
    cust_code       uuid                    not null,
    bouwheer_code   uuid                    not null,
    branch_code     varchar(5)              not null,
    cwr_type        varchar(100)            not null,
    cwr_type_desc   varchar(300)            not null,
    facility        varchar(100)            not null,
    is_revolving    bool                    not null,
    currency        varchar(5)              not null,
    cwr_start_date  timestamp               not null,
    cwr_end_date    timestamp               not null,
    plafond_amt     numeric(17, 2)          not null,
    realisation_amt numeric(17, 2)          not null,
    status          varchar(20)             not null,
    usr_crt         varchar(50)             not null,
    dtm_crt         timestamp default now() not null,
    usr_upd         varchar(50)             null,
    dtm_upd         timestamp               null,
    primary key (cwr_code),
    constraint fk_cwr_to_customer foreign key (cust_code) references public.customer (cust_code),
    constraint fk_cwr_to_bouwheer foreign key (bouwheer_code) references public.bouwheer (bouwheer_code)
);

drop table if exists public.agreement cascade;
create table public.agreement
(
    agreement_id       bigserial               not null,
    agreement_code     varchar(20),
    cwr_code           varchar(20)             not null,
    application_code   varchar(20)             not null,
    financing_hdr_code uuid                    not null,
    facility           varchar(100)            not null,
    currency           varchar(5)              not null,
    financing_amt      numeric(17, 2)          not null,
    status             varchar(20)             not null,
    product_offering   varchar(100)            not null,
    usr_crt            varchar(50)             not null,
    dtm_crt            timestamp default now() not null,
    usr_upd            varchar(50),
    dtm_upd            timestamp,
    primary key (agreement_code),
    constraint fk_agreement_to_cwr foreign key (cwr_code) references public.cwr (cwr_code),
    constraint fk_agreement_to_financing_hdr foreign key (financing_hdr_code) references public.financing_hdr (financing_hdr_code)
);

drop table if exists public.branch_area_mapping cascade;
create table public.branch_area_mapping
(
    branch_area_mapping_id bigserial,
    branch_code            varchar(3),
    area                   varchar(50)             not null,
    province               varchar(50)             not null,
    city                   varchar(50)             not null,
    is_active              bool                    not null,
    usr_crt                varchar(50)             not null,
    dtm_crt                timestamp default now() not null,
    usr_upd                varchar(50),
    dtm_upd                timestamp,
    primary key (branch_area_mapping_id),
    constraint fk_branch_area_mapping_to_mst_branch foreign key (branch_code) references users.mst_branch
);

insert into
    public.branch_area_mapping (branch_code, area, province, city, is_active, usr_crt)
select
    bam."Kode Cabang"::text,
    bam.area,
    bam.provinsi,
    bam.kota,
    true::boolean as active,
    'system'
from
    test.branch_area_mapping bam
        join users.mst_branch mb on bam."Kode Cabang"::text = mb.branch_code;

delete
from
    public.email_template
where
    email_template_code = 'M_BRANCH_ASSIGN';

INSERT INTO
    email_template
(email_template_code, subject_mail, body_mail, is_active, usr_crt, dtm_crt)
VALUES
    ('M_BRANCH_ASSIGN', 'Prospect Factoring {bouwheerName}', '<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
    <style>
        .tbl {
            border-collapse: collapse;
        }

        .tbl th {
            background-color: #083B82;
            color: #fff;
            padding: 0.5rem 0.7rem;
            font-weight: 700;
            font-size: 0.8em;
        }

        .tbl td {
            padding: 0.5rem 0.7rem;
            font-weight: 400;
        }

        .tbl-center td {
            text-align: center;
        }

        .br {
            border: 0.5px solid #000;
        }

        .primary {
            font-weight: bold;
            background-color: #083B82;
            color: #fff;
        }
    </style>
</head>

<body>
    <p style="line-height: 5px;">
        Berikut adalah daftar penempatan utilisasi kontrak factoring Truckindo Utama pada cabang {branchArea}.
        Dengan detail pengajuan :
    </p>
    <br>
    <table>
        <tr>
            <td>
                Nama Perusahaan
            </td>
            <td>:</td>
            <td>
                {companyName}
            </td>
        </tr>
        <tr>
            <td>
                Email
            </td>
            <td>:</td>
            <td>
                {email}
            </td>
        </tr>
        <tr>
            <td>
                No. Hp
            </td>
            <td>:</td>
            <td>
                {phoneNumber}
            </td>
        </tr>
        <tr>
            <td>
                Tanggal Pengajuan
            </td>
            <td>:</td>
            <td>
                {applicationDate}
            </td>
        </tr>
    </table>
    <br>
    <table class="tbl tbl-center">
        <thead>
            <tr>
                <th>No. Invoice</th>
                <th>Deskripsi</th>
                <th>Pemberi Kerja</th>

                <th>Tanggal Invoice</th>
                <th>Tanggal Jatuh Tempo</th>
                <th>Nilai Tagihan</th>
            </tr>
        </thead>
        <tbody>
            {invoices}
        </tbody>
    </table>
    <br>
    <p>Adapun rincian dari pengajuan ini sebagai berikut:</p>
    <table class="tbl br text-left">
        <tr>
            <td>
                Nilai Transaksi
            </td>
            <td>
                {invoiceAmt}
            </td>
        </tr>
        <tr>
            <td>
                Retensi
            </td>
            <td>
                {retention}
            </td>
        </tr>
        <tr>
            <td>
                Nilai Pembiayaan
            </td>
            <td>
                {financingAmt}
            </td>
        </tr>
        <tr>
            <td>
                Nilai Layaan
            </td>
            <td>
                {totalFeeAmt}
            </td>
        </tr>
        <tr>
            <td>
                Tenor
            </td>
            <td>
                {tenor}
            </td>
        </tr>
        <tr>
            <td>
                Jatuh Tempo
            </td>
            <td>
                {financingDueDate}
            </td>
        </tr>
        <tr class="primary">
            <td>
                Total Pencairan
            </td>
            <td>
                {disburseAmt}
            </td>
        </tr>
    </table>
    <p>
        Demikian informasi ini disampaikan terima kasih atas kepercayaan Anda.
    </p>
    <br />
    <p>Hormat Kami,</p>
    <p style="color: rgb(14, 193, 14); font-weight: bold">PT. Candra Sakti Utama Leasing</p>
    <img src="https://www.csulfinance.com/cfind/source/images/logo.png" />
</body>

</html>', true, 'SYSTEM', NOW());

alter table public.mst_file_type
    add column if not exists cust_type_code varchar(50) null;

alter table public.legal_file
    add column if not exists  file_no varchar(50) null;

alter table legal_file
    ALTER COLUMN file_type_code DROP NOT NULL;

alter table public.financing_hdr
    add column if not exists branch_code varchar(3);

alter table public.financing_hdr
    drop constraint fk_financing_hdr_to_mst_branch;

alter table public.financing_hdr
    add constraint fk_financing_hdr_to_mst_branch foreign key (branch_code) references users.mst_branch (branch_code);


drop table if exists public.agreement_file cascade;
create table public.agreement_file
(
    agreement_file_id bigserial,
    agreement_code    varchar(20)  not null,
    file_type_code    varchar(20)  not null,
    file_name         varchar(500) not null,
    file_path         varchar(8000),
    content_type      varchar(500),
    usr_crt           varchar(50),
    dtm_crt           timestamp,
    usr_upd           varchar(50),
    dtm_upd           timestamp,
    primary key (agreement_file_id),
    constraint fk_agreement_file_to_agreement foreign key (agreement_code) references public.agreement (agreement_code),
    constraint fk_agreement_file_to_mst_file_type foreign key (file_type_code) references public.mst_file_type
);

delete
from
    public.mst_file_type
where
    file_type_code = 'AGGREMENT01';
insert into
    public.mst_file_type(file_type_code, file_type_name, file_type_desc, file_allocation, is_mandatory, max_size_mb,
                         usr_crt, dtm_crt)
values
    ('AGGREMENT01', 'Kontrak Persetujuan', 'Kontrak Persetujuan', 'Agreement', false, 20, 'system', now());


delete
from
    public.general_setting_hdr
where
    gs_hdr_code = 'BANKNM001';
insert into
    public.general_setting_hdr (gs_hdr_code, gs_description, usr_crt, dtm_crt, is_active)
values
    ('BANK001',
     'Csul Bank Account',
     'system',
     now(),
     true);

delete
from
    public.general_setting_dtl
where
    gs_dtl_code = 'DTLBANK001';
insert into
    public.general_setting_dtl (gs_dtl_code, gs_hdr_code, gs_dtl_value, usr_crt, dtm_crt, is_active)
values
    ('DTLBANK001',
     'BANK001',
     '"{\"accountNo\":\"2132412412421\",\"accountName\":\"CSUL Finance\",\"bankName\":\"Mandiri\",\"bankKey\":\"BMRI008\"}"',
     'system',
     now(),
     true);

delete
from
    public.email_template
where
    email_template_code = 'M_BOUWHEER_PAYMENT';
INSERT INTO
    email_template
(email_template_code, subject_mail, body_mail, is_active, usr_crt, dtm_crt)
VALUES
    ('M_BOUWHEER_PAYMENT', 'Pembiayaan Invoice Vendor {vendorCode} menggunakan CSUL Finance', '<!DOCTYPE html>
<html lang="id">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body {
            font-family: Arial, sans-serif;
        }

        .container {
            margin: 20px;
        }

        .header,
        .footer {
            margin-bottom: 20px;
        }

        .header {
            font-size: 16px;
            line-height: 1.6;
        }

        .table-container {
            margin-top: 20px;
            border-collapse: collapse;
            width: 100%;
        }

        .table-container th,
        .table-container td {
            border: 1px solid #dddddd;
            text-align: left;
            padding: 8px;
        }

        .table-container th {
            background-color: #002060;
            color: white;
        }

        .table-container tr:nth-child(even) {
            background-color: #f2f2f2;
        }

        .table-container tr:nth-child(odd) {
            background-color: #ffffff;
        }

        .footer {
            margin-top: 30px;
            font-size: 14px;
            line-height: 1.6;
        }
    </style>
</head>

<body>
    <div class="container">
        <div>
            Yth. {bouwheerName},<br>
            Terdapat transaksi yang akan dibiayai oleh CSUL Finance, sehingga perlu adanya perubahan bank account
            menggunakan bank account CSUL.
            Dengan detail vendor:
        </div>
        <br/>
        <table>
            <tr>
                <td>
                    Nama Vendor
                </td>
                <td>:</td>
                <td>
                    {vendorName}
                </td>
            </tr>
            <tr>
                <td>
                    Vendor Code
                </td>
                <td>:</td>
                <td>
                    {vendorCode}
                </td>
            </tr>
            <tr>
                <td>
                    Account No.
                </td>
                <td>:</td>
                <td>
                    {accountNo}
                </td>
            </tr>
            <tr>
                <td>
                    Bank Account
                </td>
                <td>:</td>
                <td>
                    {bankAccount}
                </td>
            </tr>
            <tr>
                <td>
                    Bank Name
                </td>
                <td>:</td>
                <td>
                    {bankName}
                </td>
            </tr>
            <tr>
                <td>
                    Bank Key
                </td>
                <td>:</td>
                <td>
                    {bankKey}
                </td>
            </tr>
            <tr>
                <td>
                    Tanggal Pengajuan
                </td>
                <td>:</td>
                <td>
                    {tglPengajuan}
                </td>
            </tr>
        </table>
        <br>
        <p>
            Dengan detail pengajuan:
        </p>
        <table class="table-container">
            <thead>
                <tr>
                    <th>No. Invoice</th>
                    <th>Deskripsi</th>
                    <th>Pemberi Kerja</th>
                    <th>Tanggal Invoice</th>
                    <th>Tanggal Jatuh Tempo</th>
                    <th>Nilai Tagihan</th>
                </tr>
            </thead>
            <tbody>
                {invoices}
            </tbody>
        </table>

        <div class="footer">
            Demikian informasi ini disampaikan, terima kasih atas kepercayaannya.<br><br>
            Hormat Kami,
        </div>
        <p style="color: rgb(14, 193, 14); font-weight: bold">PT. Candra Sakti Utama Leasing</p>
        <img src="https://www.csulfinance.com/cfind/source/images/logo.png" />
    </div>
</body>

</html>', true, 'SYSTEM', NOW());
-- endregion
