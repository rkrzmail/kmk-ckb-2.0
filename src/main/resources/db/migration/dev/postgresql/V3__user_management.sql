set search_path = "users";

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
