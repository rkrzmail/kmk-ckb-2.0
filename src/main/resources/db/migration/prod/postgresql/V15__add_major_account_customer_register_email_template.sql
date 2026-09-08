INSERT INTO public.email_template
  (email_template_code, subject_mail, body_mail, is_active, usr_crt, dtm_crt)
VALUES
  (
    'M_NEW_REGISTER',
    'Registrasi User',
    '<!DOCTYPE html>
     <html lang="en">

     <head>
         <meta charset="UTF-8">
         <meta name="viewport" content="width=device-width, initial-scale=1.0">
         <title>Document</title>
     </head>

     <body>
         <p style="line-height: 5px;">
             Berikut user register baru dari {bouwheerName}, Harap segera melakukan cek verifikasi Account . Dengan detail:
         </p>
         <br>
         <table>
             <tr>
                 <td>
                     Nama
                 </td>
                 <td>:</td>
                 <td>
                     {name}
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
         </table>
         <br>

         <p>
             Demikian informasi ini disampaikan terima kasih.
         </p>
         <br />
         <p>Hormat Kami,</p>
         <p style="color: rgb(14, 193, 14); font-weight: bold">PT. Candra Sakti Utama Leasing</p>
         <img src="https://www.csulfinance.com/cfind/source/images/logo.png" />
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
