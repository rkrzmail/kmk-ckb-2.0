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

-- DELETE FROM email_template WHERE email_template_code = 'M_CUST_LOAN';
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
