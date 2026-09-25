ALTER TABLE public.email_template
  ALTER COLUMN email_template_code TYPE VARCHAR(50);

INSERT INTO public.email_template
  (email_template_code, subject_mail, body_mail, is_active, usr_crt, dtm_crt)
VALUES
  (
    'M_BRANCH_CONTRACT_UPLOAD',
    'Upload Kontrak Pencairan Agreement {agreementCode}',
    '<html lang="id">
<head><meta charset="UTF-8"/></head>
<body>
  <p>Yth. Branch Admin {branchName},</p>
  <p>Terdapat Agreement yang memerlukan proses upload kontrak pencairan.</p>
  <table>
    <tr><td>Agreement No.</td><td>:</td><td>{agreementCode}</td></tr>
    <tr><td>Leads ID</td><td>:</td><td>{financingCode}</td></tr>
    <tr><td>Vendor</td><td>:</td><td>{vendorCode} - {vendorName}</td></tr>
    <tr><td>Bouwheer</td><td>:</td><td>{bouwheerName}</td></tr>
    <tr><td>PIC Bouwheer</td><td>:</td><td>{bouwheerPicEmails}</td></tr>
  </table>
  <p>Silakan masuk ke aplikasi Dana Sakti dan melakukan upload kontrak pencairan.</p>
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
