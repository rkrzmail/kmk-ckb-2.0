DROP TABLE IF EXISTS customer CASCADE;
CREATE TABLE customer
(
    cust_id           BIGSERIAL primary key, -- should serial or identity, serial is old way which is not recommended. used identity instead
    cust_code         UUID                                NOT NULL UNIQUE,
    cust_no           VARCHAR(20)                         NULL,
    cust_name         VARCHAR(500)                        NULL,
    cust_type_code    VARCHAR(50)                         NULL,
    cust_id_type_code VARCHAR(4)                          NULL,
    cust_id_no        VARCHAR(20)                         NULL,
    cust_email        VARCHAR(100)                        NULL,
    is_email_valid    BOOLEAN   DEFAULT FALSE             NOT NULL,
    cust_mobile_phone VARCHAR(20)                         NULL,
    is_phone_valid    BOOLEAN                             NULL,
    is_wa_active      BOOLEAN                             NULL,
    cust_pin          VARCHAR(250)                        NULL,
    agree_tc          BOOLEAN                             NULL,
    is_active         BOOLEAN   DEFAULT FALSE             NOT NULL,
    usr_crt           VARCHAR(50)                         NULL,
    dtm_crt           TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd           VARCHAR(50)                         NULL,
    dtm_upd           TIMESTAMP                           NULL
);

DROP TABLE IF EXISTS customer_company CASCADE;
CREATE TABLE customer_company
(
    cust_company_code     BIGSERIAL primary key,                        -- should serial or identity, serial is old way which is not recommended. used identity instead
    cust_code             UUID                                NOT NULL, -- why can i made relation if the reference column isn't PK?
    cust_company_type     VARCHAR(50)                         NULL,
    company_model         VARCHAR(200)                        NULL,
    identity_type         VARCHAR(50)                         NULL,
    identity_no           VARCHAR(50)                         NULL,
    identity_issued_date  TIMESTAMP                           NULL,
    identity_expired_date TIMESTAMP                           NULL,
    company_address       VARCHAR(1000)                       NULL,
    rt                    VARCHAR(5)                          NULL,
    rw                    VARCHAR(5)                          NULL,
    kelurahan             VARCHAR(50)                         NULL,
    kecamatan             VARCHAR(50)                         NULL,
    city                  VARCHAR(50)                         NULL,
    province              VARCHAR(50)                         NULL,
    zipcode               VARCHAR(10)                         NULL,
    area                  VARCHAR(5)                          NULL,
    phone                 VARCHAR(20)                         NULL,
    ownership_status      VARCHAR(50)                         NULL,
    stay_since            TIMESTAMP                           NULL,
    stay_length           NUMERIC(5, 2)                       NULL,
    usr_crt               VARCHAR(50)                         NULL,
    dtm_crt               TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd               VARCHAR(50)                         NULL,
    dtm_upd               TIMESTAMP                           NULL,
    CONSTRAINT fk_customer_personal_to_cust FOREIGN KEY (cust_code) REFERENCES customer (cust_code)
);

DROP TABLE IF EXISTS customer_personal CASCADE;
CREATE TABLE customer_personal
(
    cust_personal_code BIGSERIAL primary key,                        -- should serial or identity, serial is old way which is not recommended. used identity instead
    cust_code          UUID                                NOT NULL, -- why can i made relation if the reference column isn't PK?
    birthplace         VARCHAR(50)                         NULL,
    birthdate          TIMESTAMP                           NULL,
    gender             VARCHAR(10)                         NULL,
    identity_type      VARCHAR(50)                         NULL,
    identity_no        VARCHAR(50)                         NULL,     -- need to ask
    expired_date       TIMESTAMP                           NULL,
    mother_maiden_name VARCHAR(50)                         NULL,
    marital_status     VARCHAR(20)                         NULL,
    cust_model         VARCHAR(50)                         NULL,
    legal_address      VARCHAR(1000)                       NULL,
    rt                 VARCHAR(5)                          NULL,
    rw                 VARCHAR(5)                          NULL,
    kelurahan          VARCHAR(50)                         NULL,
    kecamatan          VARCHAR(50)                         NULL,
    city               VARCHAR(50)                         NULL,
    province           VARCHAR(50)                         NULL,
    zipcode            VARCHAR(10)                         NULL,
    area               VARCHAR(5)                          NULL,
    phone              VARCHAR(20)                         NULL,
    ownership_status   VARCHAR(50)                         NULL,
    stay_since         TIMESTAMP                           NULL,
    stay_length        NUMERIC(5, 2)                       NULL,
    usr_crt            VARCHAR(50)                         NULL,
    dtm_crt            TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd            VARCHAR(50)                         NULL,
    dtm_upd            TIMESTAMP                           NULL,
    CONSTRAINT fk_customer_personal_to_cust FOREIGN KEY (cust_code) REFERENCES customer (cust_code)
);

DROP TABLE IF EXISTS login_log CASCADE;
CREATE TABLE login_log
(
    login_log_id   BIGSERIAL PRIMARY KEY,
    login_log_code UUID        NOT NULL,
    login_role     VARCHAR(50) NOT NULL,
    cust_code      UUID        NOT NULL,
    login_date     TIMESTAMP   NOT NULL,
    is_logout      BOOLEAN     NOT NULL,
    logout_date    TIMESTAMP   NULL,
    usr_logout     TIMESTAMP   NULL,
    CONSTRAINT fk_login_log_to_cust FOREIGN KEY (cust_code) REFERENCES customer (cust_code)
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
    email_template_id   BIGSERIAL PRIMARY KEY,
    email_template_code VARCHAR(20) UNIQUE                  NOT NULL,
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
    gs_hdr_id      BIGSERIAL PRIMARY KEY,
    gs_hdr_code    VARCHAR(20) UNIQUE                  NOT NULL,
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
    gs_dtl_id    BIGSERIAL PRIMARY KEY,
    gs_hdr_code  VARCHAR(20)                         NOT NULL,
    gs_dtl_code  VARCHAR(100) UNIQUE,
    gs_dtl_value VARCHAR(250),
    is_active    BOOLEAN   DEFAULT FALSE             NOT NULL,
    usr_crt      VARCHAR(50)                         NOT NULL,
    dtm_crt      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd      VARCHAR(50)                         NOT NULL,
    dtm_upd      TIMESTAMP                           NULL,
    CONSTRAINT fk_general_setting_dtl_to_hdr FOREIGN KEY (gs_hdr_code) REFERENCES general_setting_hdr (gs_hdr_code)
);
