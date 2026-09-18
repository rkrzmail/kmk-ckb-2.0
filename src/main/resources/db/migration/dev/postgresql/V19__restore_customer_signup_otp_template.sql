UPDATE public.email_template
SET body_mail = '<html lang="en">
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
</html>',
    mail_to = NULL,
    usr_upd = 'MIGRATION',
    dtm_upd = NOW()
WHERE email_template_code = 'M_CUST_NEW_OTP'
  AND body_mail NOT LIKE '%{otp_code}%';

UPDATE public.email_template
SET mail_to = NULL,
    usr_upd = 'MIGRATION',
    dtm_upd = NOW()
WHERE email_template_code = 'M_CUST_NEW_OTP'
  AND mail_to IS NOT NULL;
