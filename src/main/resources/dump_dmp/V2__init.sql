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
