INSERT INTO public.email_template
  (email_template_code, subject_mail, body_mail, is_active, usr_crt, dtm_crt)
VALUES
  (
    'M_CUST_VERIFY_MJR',
    'Verifikasi Akun Debitur Baru Dana Sakti',
    '<html lang="id">
<head><meta charset="UTF-8"/></head>
<body>
  <p>Yth. Major Account,</p>
  <p>Terdapat akun debitur baru yang telah menyelesaikan verifikasi email dan menunggu proses verifikasi akun.</p>
  <p>Nama Debitur: <strong>{name}</strong></p>
  <p>Email: <strong>{email}</strong></p>
  <p>No. Identitas: <strong>{id_no}</strong></p>
  <p>Vendor Code: <strong>{vendor_code}</strong></p>
  <p>Tipe Debitur: <strong>{customer_type}</strong></p>
  <p>Silakan melakukan pemeriksaan melalui menu List Verifikasi Akun pada aplikasi Dana Sakti.</p>
  <br/>
  <p>Hormat Kami,</p>
  <p><strong>PT. Candra Sakti Utama Leasing</strong></p>
</body>
</html>',
    true,
    'SYSTEM',
    NOW()
  )
ON CONFLICT (email_template_code) DO UPDATE SET
  subject_mail = EXCLUDED.subject_mail,
  body_mail = EXCLUDED.body_mail,
  is_active = EXCLUDED.is_active,
  usr_upd = 'SYSTEM',
  dtm_upd = NOW();
