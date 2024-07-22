INSERT INTO
    email_template
(email_template_code, subject_mail, body_mail, is_active, usr_crt, dtm_crt)
VALUES
    ('M_CUST_NEW_OTP', 'Aktifasi Akun Dana Sakit', '<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta
            name="viewport"
            content="width=device-width, initial-scale=1.0"/>
</head>

<body>
    <p>Hi <span>{name}</span></p>
    <p>Selamat akun anda telah terdaftar dengan detail informasi:</p>
    <br/>
    <p>Email: <span>{email}</span></p>
    <p>No. KTP/NPWP: <span>{id_no}</span></p>
    <br/>
    <p>Jika Informasi di atas telah sesuai, harap memasukkan 4 digit kode OTP di bawah ini di website Dana Sakti untuk
        memverifikasi akun anda</p>
    <br/>
    <b style="font-size: 20px">{otp_code}</b>
    <br/>
    <br/>
    <p>Pada saat akun anda telah aktif, silahkan melanjutkan proses transaksi anda.</p>
    <p>
        Jika anda membutuhkan bantuan, harap hubungi customer service kamu melalui email
        <a href="mailto:help.danasakti@csul.com">help.danasakti@csul.com</a>
    </p>
    <br/>
    <br/>
    <p>Hormat Kami,</p>
    <br/>
    <p style="color: rgb(14, 193, 14); font-weight: bold">PT. Candra Sakti Utama Leasing</p>
    <img src="https://www.csulfinance.com/cfind/source/images/logo.png"/>
</body>
</html>', true, 'SYSTEM', NOW());

INSERT INTO
    email_template
(email_template_code, subject_mail, body_mail, is_active, usr_crt, dtm_crt)
VALUES
    ('M_CUST_ACTIVE', 'Konfirmasi Akun Dana Sakti Behasil di Aktifasi', '<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta
            name="viewport"
            content="width=device-width, initial-scale=1.0"/>
</head>
<body>
    <p>Hi <span>{name}</span></p>
    <p>
        Selamat akun anda telah berhasil di aktifasi,
        kini anda sudah dapat melanjutkan proses pembiayaan Dana Sakti anda.
    </p>
    <br/>
    <p>
        Jika anda membutuhkan bantuan, harap hubungi customer service kamu melalui email
        <a href="mailto:help.danasakti@csul.com">help.danasakti@csul.com</a>
    </p>
    <br/>
    <br/>
    <p>Hormat Kami,</p>
    <br/>
    <p style="color: rgb(14, 193, 14); font-weight: bold">PT. Candra Sakti Utama Leasing</p>
    <img src="https://www.csulfinance.com/cfind/source/images/logo.png"/>
</body>
</html>', true, 'SYSTEM', NOW());

INSERT INTO
    email_template
(email_template_code, subject_mail, body_mail, is_active, usr_crt, dtm_crt)
VALUES
    ('M_CUST_CHANGE_OTP', 'Pengajuan Pergantian Kode Akses (PIN) Dana Sakti', '<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <meta
            name="viewport"
            content="width=device-width, initial-scale=1.0"/>
</head>
<body>
    <p>Hi <span>{name}</span></p>
    <p>
        Terdapat permintaan pergantian Kode Akses
        untuk akun "{email}" di Website Dana Sakti
    </p>
    <p>
        Untuk mengkonfirmasi perubahan kode akses (PIN),
        harap memasukkan 4 digit kode OTP ini di Website Dana Sakti anda
    </p>
    <br/>
    <b style="font-size: 20px">{otp_code}</b>
    <br/>
    <br/>
    <p>
        Jika permintaan perubahan kode akses (PIN) tidak dilakukan oleh anda,
        maka harap abaikan pesan ini.
    </p>
    <p>
        Jika anda membutuhkan bantuan, harap hubungi customer service kamu melalui email
        <a href="mailto:help.danasakti@csul.com">help.danasakti@csul.com</a>
    </p>
    <br/>
    <br/>
    <p>Hormat Kami,</p>
    <br/>
    <p style="color: rgb(14, 193, 14); font-weight: bold">PT. Candra Sakti Utama Leasing</p>
    <img src="https://www.csulfinance.com/cfind/source/images/logo.png"/>
</body>
</html>', true, 'SYSTEM', NOW());
