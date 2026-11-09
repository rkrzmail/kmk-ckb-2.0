INSERT INTO public.email_template
  (email_template_code, subject_mail, body_mail, is_active, usr_crt, dtm_crt)
VALUES
  (
    'M_CUST_PENCAIRAN',
    'Dana Sakti Telah Ditransfer',
    '<html lang="id">
<head><meta charset="UTF-8"></head>
<body style="font-family:Arial,sans-serif;font-size:13px;color:#222;">
  <p>Yth. {companyName},<br>
  Diinformasikan bahwa pengajuan Dana Sakti dengan nomor perjanjian {agreementCode} di bawah telah ditransfer.</p>
  <table style="border-collapse:collapse;margin-bottom:16px;">
    <tr><td>Nama Perusahaan</td><td style="padding:0 8px;">:</td><td>{companyName}</td></tr>
    <tr><td>Email</td><td style="padding:0 8px;">:</td><td>{email}</td></tr>
    <tr><td>No. Hp</td><td style="padding:0 8px;">:</td><td>{phoneNumber}</td></tr>
    <tr><td>Tanggal Pengajuan</td><td style="padding:0 8px;">:</td><td>{applicationDate}</td></tr>
  </table>
  <table style="border-collapse:collapse;margin-bottom:16px;font-size:12px;">
    <thead><tr style="background:#083b82;color:#fff;">
      <th style="padding:8px;">No. Invoice</th><th style="padding:8px;">Deskripsi</th>
      <th style="padding:8px;">Pemberi Kerja</th><th style="padding:8px;">Tanggal Invoice</th>
      <th style="padding:8px;">Tanggal Jatuh Tempo</th><th style="padding:8px;">Nilai Tagihan</th>
    </tr></thead>
    <tbody>{invoices}</tbody>
  </table>
  <p>Adapun rincian dari pengajuan ini sebagai berikut:</p>
  <table style="border-collapse:collapse;font-size:12px;">
    <tr><td style="border:1px solid #999;padding:6px;">Nama Bank</td><td style="border:1px solid #999;padding:6px;">{bankName}</td></tr>
    <tr><td style="border:1px solid #999;padding:6px;">No. Rekening</td><td style="border:1px solid #999;padding:6px;">{accountNo}</td></tr>
    <tr><td style="border:1px solid #999;padding:6px;">Tanggal Pencairan</td><td style="border:1px solid #999;padding:6px;">{disbursementDate}</td></tr>
    <tr style="background:#083b82;color:#fff;font-weight:bold;"><td style="padding:6px;">Total Pencairan</td><td style="padding:6px;">Rp. {disburseAmt}</td></tr>
  </table>
  <p>Demikian informasi ini disampaikan terima kasih atas kepercayaan Anda.</p>
  <br><p>Hormat Kami,</p>
  <p style="color:#0ec10e;font-weight:bold;">PT. Candra Sakti Utama Leasing</p>
</body>
</html>',
    true,
    'SYSTEM',
    NOW()
  )
ON CONFLICT (email_template_code) DO UPDATE SET
  subject_mail = EXCLUDED.subject_mail,
  body_mail = EXCLUDED.body_mail,
  usr_upd = 'SYSTEM',
  dtm_upd = NOW();
