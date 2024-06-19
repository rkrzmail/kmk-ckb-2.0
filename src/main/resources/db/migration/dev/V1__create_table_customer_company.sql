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
    usr_crt               VARCHAR(50)                         NULL,
    dtm_crt               TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd               VARCHAR(50)                         NULL,
    dtm_upd               TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
