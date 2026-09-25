
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


DROP TABLE IF EXISTS disbursement_log CASCADE;
CREATE TABLE disbursement_log
(
    disbursement_id     BIGSERIAL       NOT NULL,
    disbursement_code   UUID            PRIMARY KEY,
    agreement_code      varchar(20),
    ap_no               varchar(20),
    ap_desc             varchar(500),
    currency            varchar(20),
    ap_amt              NUMERIC(17,2),
    ap_paid_amt         NUMERIC(17,2),
    ap_amt_inprocess    NUMERIC(17,2),
    ap_unpaid_amt       NUMERIC(17,2),
    ap_type_code        varchar(10),
    ap_type_name        varchar(150),
    ap_due_date         TIMESTAMP,
    branch_code         varchar(3),
    ap_paid_location    varchar(3),
    usr_crt             varchar(50),
    dtm_crt             TIMESTAMP,
    usr_upd             varchar(50),
    dtm_upd             TIMESTAMP
        CONSTRAINT fk_disbursement_log_to_agreement FOREIGN KEY (agreement_code) REFERENCES public.agreement (agreement_code)
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
    drop constraint if exists fk_financing_hdr_to_mst_branch;

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

alter table public.agreement
    add column if not exists approval_flag varchar(150) null;
-- endregion
