CREATE TABLE customer
(
    cust_id           BIGSERIAL primary key, -- should serial or identity, serial is old way which is not recommended. used identity instead
    cust_code         UUID                                NOT NULL,
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
    dtm_upd           TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
