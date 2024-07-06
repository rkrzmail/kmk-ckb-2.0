DROP TABLE IF EXISTS bouwheer;
CREATE TABLE bouwheer
(
    bouwheer_id      BIGSERIAL     NOT NULL,
    bouwheer_code    UUID          NOT NULL PRIMARY KEY,
    bouwheer_name    VARCHAR(100)  NOT NULL,
    legal_address    VARCHAR(1000) NOT NULL,
    rt               VARCHAR(5)    NOT NULL,
    rw               VARCHAR(5)    NOT NULL,
    kelurahan        VARCHAR(50)   NOT NULL,
    kecamatan        VARCHAR(50)   NOT NULL,
    city             VARCHAR(50)   NOT NULL,
    province         VARCHAR(50)   NOT NULL,
    zipcode          VARCHAR(10)   NOT NULL,
    area             VARCHAR(5)    NULL,
    phone            VARCHAR(20)   NOT NULL,
    is_sbu           BOOLEAN       NOT NULL,
    pic_name         VARCHAR(50)   NOT NULL,
    pic_email        VARCHAR(50)   NOT NULL,
    pic_mobile_phone VARCHAR(20)   NOT NULL,
    is_wa_active     BOOLEAN       NOT NULL,
    term_of_payment  INT8          NOT NULL,
    grace_period     INT8          NOT NULL,
    aes_key          VARCHAR(16)   NOT NULL,
    is_active        BOOLEAN       NOT NULL,
    usr_crt          VARCHAR(50)   NOT NULL,
    dtm_crt          TIMESTAMP     NOT NULL,
    usr_upd          VARCHAR(50)   NULL,
    dtm_upd          TIMESTAMP     NULL
);

DROP TABLE IF EXISTS mst_file_type;
CREATE TABLE mst_file_type
(
    file_type_id    BIGSERIAL    NOT NULL,
    file_type_code  VARCHAR(20)  NOT NULL PRIMARY KEY,
    file_type_name  VARCHAR(100) NOT NULL,
    file_type_desc  VARCHAR(500) NOT NULL,
    file_allocation VARCHAR(50)  NOT NULL,
    is_mandatory    BOOLEAN      NOT NULL,
    max_size_mb     INT8         NOT NULL,
    usr_crt         VARCHAR(50)  NOT NULL,
    dtm_crt         TIMESTAMP    NOT NULL,
    usr_upd         VARCHAR(50)  NULL,
    dtm_upd         TIMESTAMP    NULL
);

DROP TABLE IF EXISTS job_log;
CREATE TABLE job_log
(
    job_log_id      BIGSERIAL     NOT NULL PRIMARY KEY,
    server_name     VARCHAR(100)  NOT NULL,
    job_name        VARCHAR(100)  NOT NULL,
    job_type        VARCHAR(50)   NOT NULL,
    job_description VARCHAR(200)  NULL,
    frequency       VARCHAR(20)   NOT NULL, -- OneTime, Daily, Weekly, Monthly
    custom_config   VARCHAR(1000) NULL,
    start_date      TIMESTAMP     NOT NULL,
    end_date        TIMESTAMP     NULL,
    job_script      VARCHAR(8000) NOT NULL,
    is_enabled      BOOLEAN       NOT NULL,
    usr_crt         VARCHAR(50)   NOT NULL,
    dtm_crt         TIMESTAMP     NOT NULL,
    usr_upd         VARCHAR(50)   NULL,
    dtm_upd         TIMESTAMP     NULL
);

DROP TABLE IF EXISTS form_visit_log;
CREATE TABLE form_visit_log
(
    form_visit_id  BIGSERIAL     NOT NULL PRIMARY KEY ,
    login_log_code UUID          NOT NULL,
    module_code    VARCHAR(20)   NOT NULL,
    form_code      VARCHAR(20)   NOT NULL,
    path_access    VARCHAR(1000) NOT NULL,
    access_date    TIMESTAMP     NOT NULL
);

DROP TABLE IF EXISTS rabbitmq_log;
CREATE TABLE rabbitmq_log
(
    rabbitmq_log_id BIGSERIAL             NOT NULL PRIMARY KEY,
    exchange_name   VARCHAR(100)          NOT NULL,
    exchange_type   VARCHAR(100)          NOT NULL,
    last_exchange   VARCHAR(100)          NOT NULL,
    queue_name      VARCHAR(100)          NOT NULL,
    route_key       VARCHAR(100)          NOT NULL,
    payload         VARCHAR(1000)         NOT NULL,
    is_ack          BOOLEAN DEFAULT FALSE NOT NULL,
    dtm_crt         VARCHAR(50)           NOT NULL,
    dtm_upd         TIMESTAMP             NULL
);
