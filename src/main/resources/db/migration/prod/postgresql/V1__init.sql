DROP TABLE IF EXISTS customer CASCADE;
CREATE TABLE customer
(
    cust_id           BIGSERIAL                           NOT NULL,
    cust_code         UUID                                NOT NULL UNIQUE primary key,
    cust_no           VARCHAR(20)                         NULL,
    cust_name         VARCHAR(500)                        NOT NULL,
    cust_type_code    VARCHAR(50)                         NOT NULL,
    cust_id_type_code VARCHAR(4)                          NOT NULL,
    cust_id_no        VARCHAR(20)                         NOT NULL,
    cust_email        VARCHAR(100)                        NOT NULL,
    is_email_valid    BOOLEAN   DEFAULT FALSE             NOT NULL,
    cust_mobile_phone VARCHAR(20)                         NOT NULL,
    is_phone_valid    BOOLEAN                             NOT NULL,
    is_wa_active      BOOLEAN                             NOT NULL,
    cust_pin          VARCHAR(250)                        NOT NULL,
    agree_tc          BOOLEAN                             NOT NULL,
    is_active         BOOLEAN   DEFAULT FALSE             NOT NULL,
    usr_crt           VARCHAR(50)                         NOT NULL,
    dtm_crt           TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd           VARCHAR(50)                         NULL,
    dtm_upd           TIMESTAMP                           NULL
);

DROP TABLE IF EXISTS customer_company CASCADE;
CREATE TABLE customer_company
(
    cust_company_id       BIGSERIAL                           NOT NULL,
    cust_company_code     UUID primary key,
    cust_code             UUID                                NOT NULL,
    cust_company_type     VARCHAR(50)                         NOT NULL,
    company_model         VARCHAR(200)                        NOT NULL,
    identity_type         VARCHAR(50)                         NOT NULL,
    identity_no           VARCHAR(50)                         NOT NULL,
    identity_issued_date  TIMESTAMP                           NOT NULL,
    identity_expired_date TIMESTAMP                           NOT NULL,
    company_address       VARCHAR(1000)                       NOT NULL,
    rt                    VARCHAR(5)                          NOT NULL,
    rw                    VARCHAR(5)                          NOT NULL,
    kelurahan             VARCHAR(50)                         NOT NULL,
    kecamatan             VARCHAR(50)                         NOT NULL,
    city                  VARCHAR(50)                         NOT NULL,
    province              VARCHAR(50)                         NOT NULL,
    zipcode               VARCHAR(10)                         NOT NULL,
    area                  VARCHAR(5)                          NOT NULL,
    phone                 VARCHAR(20)                         NOT NULL,
    ownership_status      VARCHAR(50)                         NOT NULL,
    stay_since            TIMESTAMP                           NOT NULL,
    stay_length           NUMERIC(5, 2)                       NOT NULL,
    usr_crt               VARCHAR(50)                         NOT NULL,
    dtm_crt               TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd               VARCHAR(50)                         NULL,
    dtm_upd               TIMESTAMP                           NULL,
    CONSTRAINT fk_customer_personal_to_customer FOREIGN KEY (cust_code) REFERENCES customer (cust_code)
);

DROP TABLE IF EXISTS customer_personal CASCADE;
CREATE TABLE customer_personal
(
    cust_personal_id   BIGSERIAL                           NOT NULL,
    cust_personal_code UUID primary key,
    cust_code          UUID                                NOT NULL,
    birthplace         VARCHAR(50)                         NOT NULL,
    birthdate          TIMESTAMP                           NOT NULL,
    gender             VARCHAR(10)                         NOT NULL,
    identity_type      VARCHAR(50)                         NULL,
    identity_no        VARCHAR(50)                         NULL, -- need to ask
    expired_date       TIMESTAMP                           NULL,
    mother_maiden_name VARCHAR(50)                         NOT NULL,
    marital_status     VARCHAR(20)                         NOT NULL,
    cust_model         VARCHAR(50)                         NOT NULL,
    legal_address      VARCHAR(1000)                       NOT NULL,
    rt                 VARCHAR(5)                          NOT NULL,
    rw                 VARCHAR(5)                          NOT NULL,
    kelurahan          VARCHAR(50)                         NOT NULL,
    kecamatan          VARCHAR(50)                         NOT NULL,
    city               VARCHAR(50)                         NOT NULL,
    province           VARCHAR(50)                         NOT NULL,
    zipcode            VARCHAR(10)                         NOT NULL,
    area               VARCHAR(5)                          NOT NULL,
    phone              VARCHAR(20)                         NOT NULL,
    ownership_status   VARCHAR(50)                         NOT NULL,
    stay_since         TIMESTAMP                           NOT NULL,
    stay_length        NUMERIC(5, 2)                       NOT NULL,
    usr_crt            VARCHAR(50)                         NOT NULL,
    dtm_crt            TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd            VARCHAR(50)                         NULL,
    dtm_upd            TIMESTAMP                           NULL,
    CONSTRAINT fk_customer_personal_to_customer FOREIGN KEY (cust_code) REFERENCES customer (cust_code)
);

DROP TABLE IF EXISTS login_log CASCADE;
CREATE TABLE login_log
(
    login_log_id   BIGSERIAL             NOT NULL,
    login_log_code UUID                  NOT NULL PRIMARY KEY,
    login_role     VARCHAR(50)           NOT NULL,
    cust_code      UUID                  NOT NULL,
    login_date     TIMESTAMP             NOT NULL,
    is_logout      BOOLEAN DEFAULT FALSE NOT NULL,
    logout_date    TIMESTAMP             NULL,
    usr_logout     TIMESTAMP             NULL,
    CONSTRAINT fk_login_log_to_customer FOREIGN KEY (cust_code) REFERENCES customer (cust_code)
);

DROP TABLE IF EXISTS change_password_log CASCADE;
CREATE TABLE change_password_log
(
    change_password_id BIGSERIAL                           NOT NULL PRIMARY KEY,
    cust_code          UUID                                NOT NULL,
    old_pin            VARCHAR(250)                        NOT NULL,
    new_pin            VARCHAR(250)                        NOT NULL,
    usr_crt            VARCHAR(50)                         NOT NULL,
    dtm_crt            TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_change_password_log_to_customer FOREIGN KEY (cust_code) REFERENCES customer (cust_code)
);

DROP TABLE IF EXISTS otp_log CASCADE;
CREATE TABLE otp_log
(
    otp_log_id     BIGSERIAL PRIMARY KEY,
    otp_code       VARCHAR(10) UNIQUE                  NOT NULL,
    mobile_phone   VARCHAR(20)                         NOT NULL,
    email          VARCHAR(50)                         NOT NULL,
    generated_date TIMESTAMP                           NOT NULL,
    expired_date   TIMESTAMP                           NOT NULL,
    is_used        BOOLEAN   DEFAULT FALSE             NOT NULL,
    usr_crt        VARCHAR(50)                         NOT NULL,
    dtm_crt        TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd        VARCHAR(50)                         NULL,
    dtm_upd        TIMESTAMP                           NULL
);

DROP TABLE IF EXISTS email_template CASCADE;
CREATE TABLE email_template
(
    email_template_id   BIGSERIAL                           NOT NULL,
    email_template_code VARCHAR(20) UNIQUE PRIMARY KEY,
    subject_mail        VARCHAR(1000)                       NULL,
    body_mail           VARCHAR(8000)                       NULL,
    mail_to             VARCHAR(1000)                       NULL,
    mail_cc             VARCHAR(1000)                       NULL,
    mail_bcc            VARCHAR(1000)                       NULL,
    is_active           BOOLEAN   DEFAULT FALSE             NOT NULL,
    usr_crt             VARCHAR(50)                         NOT NULL,
    dtm_crt             TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd             VARCHAR(50)                         NULL,
    dtm_upd             TIMESTAMP                           NULL
);

DROP TABLE IF EXISTS general_setting_hdr CASCADE;
CREATE TABLE general_setting_hdr
(
    gs_hdr_id      BIGSERIAL                           NOT NULL,
    gs_hdr_code    VARCHAR(20) UNIQUE                  NOT NULL PRIMARY KEY,
    gs_description VARCHAR(100)                        NOT NULL,
    is_active      BOOLEAN   DEFAULT FALSE             NOT NULL,
    usr_crt        VARCHAR(50)                         NOT NULL,
    dtm_crt        TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd        VARCHAR(50)                         NULL,
    dtm_upd        TIMESTAMP                           NULL
);

DROP TABLE IF EXISTS general_setting_dtl CASCADE;
CREATE TABLE general_setting_dtl
(
    gs_dtl_id    BIGSERIAL                           NOT NULL,
    gs_dtl_code  UUID PRIMARY KEY,
    gs_hdr_code  VARCHAR(20)                         NOT NULL,
    gs_dtl_value VARCHAR(250)                        NOT NULL,
    is_active    BOOLEAN   DEFAULT FALSE             NOT NULL,
    usr_crt      VARCHAR(50)                         NOT NULL,
    dtm_crt      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd      VARCHAR(50)                         NOT NULL,
    dtm_upd      TIMESTAMP                           NULL,
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

--DROP TABLE IF EXISTS fly_way;
--CREATE TABLE fly_way
--(
--    fly_id         BIGSERIAL                           NOT NULL,
--    server_name    VARCHAR(20)                         NOT NULL,
--    db_name        VARCHAR(100)                        NOT NULL,
--    scheme         VARCHAR(20)                         NOT NULL,
--    table_name     VARCHAR(100)                        NOT NULL,
--    script_create  VARCHAR(8000)                       NOT NULL,
--    execution_time INT8                                NOT NULL,
--    is_production  BOOL                                NOT NULL,
--    usr_crt        VARCHAR(50)                         NOT NULL,
--    dtm_crt        TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
--    usr_upd        VARCHAR(50)                         NULL,
--    dtm_upd        TIMESTAMP                           NULL
--);
