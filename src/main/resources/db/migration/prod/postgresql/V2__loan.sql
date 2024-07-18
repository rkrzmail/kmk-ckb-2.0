DROP TABLE IF EXISTS bouwheer CASCADE;
CREATE TABLE bouwheer
(
    bouwheer_id      BIGSERIAL     NOT NULL,
    bouwheer_code    UUID PRIMARY KEY,
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

DROP TABLE IF EXISTS mst_file_type CASCADE;
CREATE TABLE mst_file_type
(
    file_type_id    BIGSERIAL    NOT NULL,
    file_type_code  VARCHAR(20) PRIMARY KEY,
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

DROP TABLE IF EXISTS job_log CASCADE;
CREATE TABLE job_log
(
    job_log_id      BIGSERIAL PRIMARY KEY,
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

DROP TABLE IF EXISTS form_visit_log CASCADE;
CREATE TABLE form_visit_log
(
    form_visit_id  BIGSERIAL PRIMARY KEY,
    login_log_code UUID          NOT NULL,
    module_code    VARCHAR(20)   NOT NULL,
    form_code      VARCHAR(20)   NOT NULL,
    path_access    VARCHAR(1000) NOT NULL,
    access_date    TIMESTAMP     NOT NULL,
    CONSTRAINT fk_form_visit_log_to_login_log FOREIGN KEY (login_log_code) REFERENCES login_log (login_log_code)
    -- ,CONSTRAINT fk_form_visit_log_to_mst_module FOREIGN KEY (login_log_code) REFERENCES login_log (login_log_code)
    -- ,CONSTRAINT fk_form_visit_log_to_mst_form FOREIGN KEY (login_log_code) REFERENCES login_log (login_log_code)
);

DROP TABLE IF EXISTS rabbitmq_log CASCADE;
CREATE TABLE rabbitmq_log
(
    rabbitmq_log_id BIGSERIAL PRIMARY KEY,
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

DROP TABLE IF EXISTS fly_way CASCADE;
CREATE TABLE fly_way
(
    fly_id         BIGSERIAL                           NOT NULL,
    server_name    VARCHAR(20)                         NOT NULL,
    db_name        VARCHAR(100)                        NOT NULL,
    scheme         VARCHAR(20)                         NOT NULL,
    table_name     VARCHAR(100)                        NOT NULL,
    script_create  VARCHAR(8000)                       NOT NULL,
    execution_time INT8                                NOT NULL,
    is_production  BOOL                                NOT NULL,
    usr_crt        VARCHAR(50)                         NOT NULL,
    dtm_crt        TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    usr_upd        VARCHAR(50)                         NULL,
    dtm_upd        TIMESTAMP                           NULL
);

DROP TABLE IF EXISTS api_integration_log CASCADE;
CREATE TABLE api_integration_log
(
    api_log_id      BIGSERIAL PRIMARY KEY,
    endpoint_url    VARCHAR(500)  NOT NULL,
    content_type    VARCHAR(50)   NOT NULL,
    request_payload VARCHAR(8000) NOT NULL,
    response_json   VARCHAR(8000) NOT NULL,
    response_status VARCHAR(30)   NOT NULL,
    usr_crt         VARCHAR(50)   NOT NULL,
    dtm_crt         TIMESTAMP     NOT NULL,
    dtm_upd         TIMESTAMP     NULL
);

DROP TABLE IF EXISTS error_log CASCADE;
CREATE TABLE error_log
(
    error_log_id   BIGSERIAL PRIMARY KEY,
    login_log_code UUID          NOT NULL,
    error_type     VARCHAR(100)  NULL,
    error_line     VARCHAR(10)   NULL,
    error_msg      VARCHAR(500)  NULL,
    page_url       VARCHAR(500)  NULL,
    method_name    VARCHAR(100)  NULL,
    request_param  VARCHAR(1000) NULL,
    usr_crt        VARCHAR(50)   NOT NULL,
    dtm_crt        TIMESTAMP     NOT NULL,
    usr_upd        VARCHAR(50)   NULL,
    dtm_upd        TIMESTAMP     NULL,
    CONSTRAINT fk_error_log_to_login_log FOREIGN KEY (login_log_code) REFERENCES login_log (login_log_code)
);

DROP TABLE IF EXISTS legal_file CASCADE;
CREATE TABLE legal_file
(
    file_id        BIGSERIAL PRIMARY KEY,
    cust_code      UUID          NOT NULL,
    file_type_code VARCHAR(20)   NOT NULL,
    file_name      VARCHAR(500)  NOT NULL,
    file_path      VARCHAR(8000) NOT NULL,
    content_type   VARCHAR(500)  NOT NULL,
    usr_crt        VARCHAR(50)   NOT NULL,
    dtm_crt        TIMESTAMP     NOT NULL,
    usr_upd        VARCHAR(50)   NULL,
    dtm_upd        TIMESTAMP     NULL,
    CONSTRAINT fk_legal_file_to_customer FOREIGN KEY (cust_code) REFERENCES customer (cust_code),
    CONSTRAINT fk_legal_file_to_mst_file_type FOREIGN KEY (file_type_code) REFERENCES mst_file_type (file_type_code)
);

DROP TABLE IF EXISTS invoice CASCADE;
CREATE TABLE invoice
(
    invoice_id          BIGSERIAL      NOT NULL,
    invoice_code        UUID PRIMARY KEY,
    cust_code           UUID           NOT NULL,
    bouwheer_code       UUID           NOT NULL,
    bouwheer_inv_no     VARCHAR(50)    NOT NULL,
    cust_inv_no         VARCHAR(50)    NOT NULL,
    invoice_description VARCHAR(250)   NULL,
    invoice_date        TIMESTAMP      NOT NULL,
    invoice_due_date    TIMESTAMP      NOT NULL,
    invoice_amt         NUMERIC(17, 2) NOT NULL,
    usr_crt             VARCHAR(50)    NOT NULL,
    dtm_crt             TIMESTAMP      NOT NULL,
    usr_upd             VARCHAR(50)    NULL,
    dtm_upd             TIMESTAMP      NULL,
    CONSTRAINT fk_invoice_to_customer FOREIGN KEY (cust_code) REFERENCES customer (cust_code),
    CONSTRAINT fk_invoice_to_bouwheer FOREIGN KEY (bouwheer_code) REFERENCES bouwheer (bouwheer_code)
);

DROP TABLE IF EXISTS product CASCADE;
CREATE TABLE product
(
    product_id      BIGSERIAL PRIMARY KEY,
    branch_code     VARCHAR(3)     NOT NULL,
    product_name    VARCHAR(100)   NOT NULL,
    effective_date  TIMESTAMP      NOT NULL,
    ntf_from        NUMERIC(17, 2) NOT NULL,
    ntf_to          NUMERIC(17, 2) NOT NULL,
    effective_rate  NUMERIC(5, 2)  NOT NULL,
    provision_rate  NUMERIC(5, 2)  NOT NULL,
    survey_fee      NUMERIC(17, 2) NOT NULL,
    legal_fee       NUMERIC(17, 2) NOT NULL,
    admin_limit_fee NUMERIC(17, 2) NOT NULL,
    admin_rate      NUMERIC(5, 2)  NOT NULL,
    insurance_rate  NUMERIC(5, 2)  NOT NULL,
    others_fee      NUMERIC(17, 2) NOT NULL,
    is_active       BOOL           NOT NULL,
    usr_crt         VARCHAR(50)    NOT NULL,
    dtm_crt         TIMESTAMP      NOT NULL,
    usr_upd         VARCHAR(50)    NULL,
    dtm_upd         TIMESTAMP      NULL
    --,CONSTRAINT fk_product_to_branch FOREIGN KEY (branch_code) REFERENCES ()
);

DROP TABLE IF EXISTS financing_hdr CASCADE;
CREATE TABLE financing_hdr
(
    financing_hdr_id         BIGSERIAL      NOT NULL,
    financing_hdr_code       UUID PRIMARY KEY,
    cust_code                UUID           NOT NULL,
    bouwheer_code            UUID           NOT NULL,
    financing_date           TIMESTAMP      NOT NULL,
    currency_code            VARCHAR(5)     NOT NULL,
    invoice_qty              INT8           NOT NULL,
    interest_type            VARCHAR(20)    NOT NULL,
    tenor                    INT8           NOT NULL,
    effective_rate           NUMERIC(5, 2)  NOT NULL,
    interest_amt             NUMERIC(17, 2) NOT NULL,
    term_of_payment          INT8           NOT NULL,
    grace_period             INT8           NOT NULL,
    retention                NUMERIC(5, 2)  NOT NULL,
    total_invoice_amt        NUMERIC(17, 2) NOT NULL,
    provision_fee_percentage NUMERIC(5, 2)  NOT NULL,
    provision_fee_amt        NUMERIC(17, 2) NOT NULL,
    survey_fee_amt           NUMERIC(17, 2) NOT NULL,
    survey_fee_amt_nett      NUMERIC(17, 2) NOT NULL,
    legal_fee_amt            NUMERIC(17, 2) NOT NULL,
    legal_fee_amt_nett       NUMERIC(17, 2) NOT NULL,
    admin_limit_amt          NUMERIC(17, 2) NOT NULL,
    admin_fee_percentage     NUMERIC(5, 2)  NOT NULL,
    admin_fee_amt            NUMERIC(17, 2) NOT NULL,
    insurance_fee_percentage NUMERIC(5, 2)  NOT NULL,
    insurance_fee_amt        NUMERIC(17, 2) NOT NULL,
    others_fee_amt           NUMERIC(17, 2) NOT NULL,
    financing_amt            NUMERIC(17, 2) NOT NULL,
    disburse_amt             NUMERIC(17, 2) NOT NULL,
    disburse_date            NUMERIC(17, 2) NOT NULL,
    financing_due_date       TIMESTAMP      NOT NULL,
    financing_status         VARCHAR(50)    NOT NULL,
    usr_crt                  VARCHAR(50)    NOT NULL,
    dtm_crt                  TIMESTAMP      NOT NULL,
    usr_upd                  VARCHAR(50)    NULL,
    dtm_upd                  TIMESTAMP      NULL,
    CONSTRAINT fk_financing_hdr_to_customer FOREIGN KEY (cust_code) REFERENCES customer (cust_code),
    CONSTRAINT fk_financing_hdr_to_bouwheer FOREIGN KEY (bouwheer_code) REFERENCES bouwheer (bouwheer_code)
);

DROP TABLE IF EXISTS financing_dtl CASCADE;
CREATE TABLE financing_dtl
(
    financing_dtl_id   BIGSERIAL   NOT NULL,
    financing_dtl_code UUID PRIMARY KEY,
    financing_hdr_code UUID        NOT NULL,
    bouwheer_inv_no    VARCHAR(50) NOT NULL,
    invoice_seqno      INT8        NOT NULL,
    paid_to_cust_date  TIMESTAMP   NULL,
    bouwheer_paid_date TIMESTAMP   NULL,
    usr_crt            VARCHAR(50) NOT NULL,
    dtm_crt            TIMESTAMP   NOT NULL,
    usr_upd            VARCHAR(50) NULL,
    dtm_upd            TIMESTAMP   NULL,
    CONSTRAINT fk_financing_dtl_to_financing_hdr FOREIGN KEY (financing_hdr_code) REFERENCES financing_hdr (financing_hdr_code)
    -- ,CONSTRAINT fk_invoice_to_bouwheer FOREIGN KEY (bouwheer_inv_no) REFERENCES invoice (bouwheer_inv_no)
);

insert into
    mst_file_type (file_type_code, file_type_name, file_type_desc, file_allocation, is_mandatory, max_size_mb, usr_crt,
                   dtm_crt)
values
    ('DOC001', 'Akta Penyesuaian Anggaran Dasar.PDF', 'Akta Penyesuaian Anggaran Dasar terhadap UU 40/2007', 'Financing', false,
     20, 'SYSTEM', current_timestamp);


insert into
    mst_file_type (file_type_code, file_type_name, file_type_desc, file_allocation, is_mandatory, max_size_mb, usr_crt,
                   dtm_crt)
values
    ('DOC002', 'Akta Perubahan Maksud dan Tujuan Persero.PDF', 'Akta Perubahan Maksud dan Tujuan Persero', 'Financing', false,
     20, 'SYSTEM', current_timestamp);

insert into
    mst_file_type (file_type_code, file_type_name, file_type_desc, file_allocation, is_mandatory, max_size_mb, usr_crt,
                   dtm_crt)
values
    ('DOC003', 'Akta Perubahan Susunan Pengurus Perseroan.PDF', 'Akta Perubahan Terakhir mengenai Perubahan Susunan Pengurus Perseroan', 'Financing', false,
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

        .tbl tr:nth-child(even) {
            background-color: #E7ECFF;
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
            <!-- <td>{invoices}</td> -->
            <tr>
                <td>1</td>
                <td>Beli</td>
                <td>PT. Suka</td>
                <td>20/05/2024</td>
                <td>25/05/2024</td>
                <td>Rp. 100.000.000</td>
            </tr>
            <tr>
                <td>1</td>
                <td>Beli</td>
                <td>PT. Suka</td>
                <td>20/05/2024</td>
                <td>25/05/2024</td>
                <td>Rp. 100.000.000</td>
            </tr>
            <tr>
                <td>1</td>
                <td>Beli</td>
                <td>PT. Suka</td>
                <td>20/05/2024</td>
                <td>25/05/2024</td>
                <td>Rp. 100.000.000</td>
            </tr>
            <tr>
                <td>1</td>
                <td>Beli</td>
                <td>PT. Suka</td>
                <td>20/05/2024</td>
                <td>25/05/2024</td>
                <td>Rp. 100.000.000</td>
            </tr>
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
