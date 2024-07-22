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
    stay_length           NUMERIC(10, 2)                      NOT NULL, -- DOC NUMERIC(5, 2)
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
    identity_no        VARCHAR(50)                         NULL,     -- need to ask
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
    stay_length        NUMERIC(10, 2)                      NOT NULL, -- DOC NUMERIC(5, 2)
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
    cust_code      UUID                  NULL,
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
    otp_code       VARCHAR(10)                         NOT NULL,
    mobile_phone   VARCHAR(20)                         NOT NULL,
    email          VARCHAR(50)                         NOT NULL,
    generated_date TIMESTAMP                           NOT NULL,
    expired_date   TIMESTAMP                           NOT NULL,
    is_used        BOOLEAN   DEFAULT FALSE             NOT NULL,
    usr_crt        VARCHAR(50)                         NOT NULL,
    dtm_crt        TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd        VARCHAR(50)                         NULL,
    dtm_upd        TIMESTAMP                           NULL
    -- ,UNIQUE (otp_code, email, mobile_phone)
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
