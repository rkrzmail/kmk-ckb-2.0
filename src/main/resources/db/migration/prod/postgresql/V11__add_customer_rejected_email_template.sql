CREATE UNIQUE INDEX IF NOT EXISTS ux_email_template_email_template_code
  ON public.email_template (email_template_code);

INSERT INTO public.email_template
  (email_template_code, subject_mail, body_mail, is_active, usr_crt, dtm_crt)
VALUES
  (
    'M_CUST_REJECTED',
    'Verifikasi Akun Dana Sakti Ditolak',
    '<html lang="id">
<head><meta charset="UTF-8"/></head>
<body>
  <p>Hi <span>{name}</span>,</p>
  <p>Verifikasi akun Dana Sakti Anda belum dapat disetujui.</p>
  <p>Alasan penolakan: <strong>{approval_note}</strong></p>
  <p>Silakan perbaiki data yang diperlukan dan lakukan pendaftaran ulang.</p>
  <p>Jika Anda membutuhkan bantuan, hubungi <a href="mailto:help.danasakti@csul.com">help.danasakti@csul.com</a>.</p>
  <br/>
  <p>Hormat Kami,</p>
  <p style="color: rgb(14, 193, 14); font-weight: bold">PT. Chandra Sakti Utama Leasing</p>
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
