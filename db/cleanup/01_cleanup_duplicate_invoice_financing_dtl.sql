-- =============================================================================
-- DATA CLEANUP: duplikat invoice & financing_dtl
-- Bug: POST /api/v1/loan-submissions/simulations/create
--   * 1 invoice bisa punya > 1 financing_dtl (relasi Invoice -> FinancingDtl @OneToOne)
--   * invoice tidak unik per (cust_code, bouwheer_inv_no, cust_inv_no)
--   * financing_hdr selalu dibuat baru walau invoice & detail sudah ada
--
-- CARA PAKAI (WAJIB berurutan):
--   1. Jalankan STEP 1-4 (read-only) untuk melihat dampak.
--   2. Backup tabel: invoice, financing_dtl, financing_hdr.
--   3. Jalankan STEP 5-7 di dalam transaksi (BEGIN ... COMMIT).
--   4. Jalankan STEP 8 untuk menambah unique constraint (opsional, rekomendasi).
-- =============================================================================


-- =============================================================================
-- STEP 1. Invoice yang punya lebih dari 1 financing_dtl
--         (pelanggaran relasi @OneToOne Invoice -> FinancingDtl)
-- =============================================================================
SELECT i.invoice_code,
       i.cust_code,
       i.bouwheer_code,
       i.bouwheer_inv_no,
       i.cust_inv_no,
       i.invoice_amt,
       COUNT(fd.financing_dtl_code)                    AS jumlah_detail,
       ARRAY_AGG(fd.financing_dtl_code ORDER BY fd.financing_dtl_id) AS detail_codes,
       ARRAY_AGG(fd.financing_hdr_code ORDER BY fd.financing_dtl_id) AS hdr_codes
FROM invoice i
JOIN financing_dtl fd ON fd.invoice_code = i.invoice_code
GROUP BY i.invoice_code, i.cust_code, i.bouwheer_code,
         i.bouwheer_inv_no, i.cust_inv_no, i.invoice_amt
HAVING COUNT(fd.financing_dtl_code) > 1
ORDER BY jumlah_detail DESC, i.bouwheer_inv_no;


-- =============================================================================
-- STEP 2. Invoice yang duplikat (kunci unik: cust_code + bouwheer_inv_no + cust_inv_no)
--         Ini penyebab utama: invoice yang sama tersimpan beberapa kali dengan
--         invoice_code berbeda, masing-masing punya financing_dtl sendiri.
-- =============================================================================
SELECT i.cust_code,
       i.bouwheer_code,
       i.bouwheer_inv_no,
       i.cust_inv_no,
       COUNT(*)                                 AS jumlah_invoice,
       ARRAY_AGG(i.invoice_code ORDER BY i.invoice_id)          AS invoice_codes,
       ARRAY_AGG(i.invoice_id ORDER BY i.invoice_id)            AS invoice_ids,
       MIN(i.invoice_id)                        AS invoice_id_tertua,
       MAX(i.invoice_id)                        AS invoice_id_termuda
FROM invoice i
GROUP BY i.cust_code, i.bouwheer_code, i.bouwheer_inv_no, i.cust_inv_no
HAVING COUNT(*) > 1
ORDER BY jumlah_invoice DESC, i.bouwheer_inv_no;


-- =============================================================================
-- STEP 3. Ringkasan dampak (dipakai untuk laporan sebelum cleanup)
-- =============================================================================
SELECT 'invoice duplikat (grup)'            AS metrik, COUNT(*)::text AS nilai
FROM (
  SELECT 1
  FROM invoice
  GROUP BY cust_code, bouwheer_code, bouwheer_inv_no, cust_inv_no
  HAVING COUNT(*) > 1
) dup
UNION ALL
SELECT 'invoice_code dengan >1 financing_dtl', COUNT(*)::text
FROM (
  SELECT invoice_code
  FROM financing_dtl
  GROUP BY invoice_code
  HAVING COUNT(*) > 1
) multi_dtl
UNION ALL
SELECT 'financing_dtl berlebih (akan dihapus)', COALESCE(SUM(cnt - 1), 0)::text
FROM (
  SELECT COUNT(*) AS cnt
  FROM financing_dtl
  GROUP BY invoice_code
  HAVING COUNT(*) > 1
) x
UNION ALL
SELECT 'financing_hdr tanpa detail (orphan)', COUNT(*)::text
FROM financing_hdr h
WHERE NOT EXISTS (SELECT 1 FROM financing_dtl fd WHERE fd.financing_hdr_code = h.financing_hdr_code);


-- =============================================================================
-- STEP 4. Detail mana yang DIPERTAHANKAN vs DIHAPUS
--         Aturan: pertahankan financing_dtl dengan financing_dtl_id TERKECIL
--         (paling awal dibuat) per invoice_code; sisanya dihapus.
--         Sesuaikan bila bisnis ingin mempertahankan yang terbaru (pakai MAX).
-- =============================================================================
SELECT fd.invoice_code,
       fd.financing_dtl_id,
       fd.financing_dtl_code,
       fd.financing_hdr_code,
       fd.dtm_crt,
       CASE WHEN fd.financing_dtl_id = first_value(fd.financing_dtl_id)
                 OVER (PARTITION BY fd.invoice_code ORDER BY fd.financing_dtl_id)
            THEN 'DIPERTAHANKAN' ELSE 'DIHAPUS' END AS aksi
FROM financing_dtl fd
WHERE fd.invoice_code IN (
  SELECT invoice_code FROM financing_dtl GROUP BY invoice_code HAVING COUNT(*) > 1
)
ORDER BY fd.invoice_code, fd.financing_dtl_id;


-- =============================================================================
-- STEP 5. BACKUP (jalankan sebelum DELETE)
-- =============================================================================
DROP TABLE IF EXISTS backup_financing_dtl_dup;
CREATE TABLE backup_financing_dtl_dup AS
SELECT * FROM financing_dtl
WHERE financing_dtl_id IN (
  SELECT fd.financing_dtl_id
  FROM financing_dtl fd
  WHERE fd.invoice_code IN (
      SELECT invoice_code FROM financing_dtl GROUP BY invoice_code HAVING COUNT(*) > 1
  )
  AND fd.financing_dtl_id <> (
      SELECT MIN(fd2.financing_dtl_id) FROM financing_dtl fd2
      WHERE fd2.invoice_code = fd.invoice_code
  )
);

DROP TABLE IF EXISTS backup_invoice_dup;
CREATE TABLE backup_invoice_dup AS
SELECT * FROM invoice
WHERE invoice_id IN (
  SELECT i.invoice_id
  FROM invoice i
  WHERE EXISTS (
    SELECT 1 FROM invoice i2
    WHERE i2.cust_code = i.cust_code
      AND i2.bouwheer_code = i.bouwheer_code
      AND COALESCE(i2.bouwheer_inv_no, '') = COALESCE(i.bouwheer_inv_no, '')
      AND COALESCE(i2.cust_inv_no, '') = COALESCE(i.cust_inv_no, '')
      AND i2.invoice_id < i.invoice_id
  )
);

-- Verifikasi isi backup
SELECT 'backup_financing_dtl_dup' AS tabel, COUNT(*) AS baris FROM backup_financing_dtl_dup
UNION ALL
SELECT 'backup_invoice_dup', COUNT(*) FROM backup_invoice_dup;


-- =============================================================================
-- STEP 6. HAPUS financing_dtl duplikat (pertahankan financing_dtl_id terkecil)
-- =============================================================================
BEGIN;

DELETE FROM financing_dtl fd
WHERE fd.invoice_code IN (
    SELECT invoice_code FROM financing_dtl GROUP BY invoice_code HAVING COUNT(*) > 1
  )
  AND fd.financing_dtl_id <> (
    SELECT MIN(fd2.financing_dtl_id)
    FROM financing_dtl fd2
    WHERE fd2.invoice_code = fd.invoice_code
  );

-- 6a. (Opsional) hapus financing_hdr yang jadi yatim karena detailnya dihapus
--     Cek dulu hasilnya; header yatim biasanya hasil bug "header selalu baru".
DELETE FROM financing_hdr h
WHERE NOT EXISTS (
  SELECT 1 FROM financing_dtl fd WHERE fd.financing_hdr_code = h.financing_hdr_code
);

COMMIT;


-- =============================================================================
-- STEP 7. HAPUS invoice duplikat (setelah detail duplikat dibersihkan)
--         Pertahankan invoice_id terkecil per (cust_code, bouwheer_code,
--         bouwheer_inv_no, cust_inv_no). Hanya invoice tanpa financing_dtl
--         yang dihapus agar aman.
-- =============================================================================
BEGIN;

DELETE FROM invoice i
WHERE i.invoice_id IN (
    SELECT i2.invoice_id
    FROM invoice i2
    WHERE EXISTS (
      SELECT 1 FROM invoice i3
      WHERE i3.cust_code = i2.cust_code
        AND i3.bouwheer_code = i2.bouwheer_code
        AND COALESCE(i3.bouwheer_inv_no, '') = COALESCE(i2.bouwheer_inv_no, '')
        AND COALESCE(i3.cust_inv_no, '') = COALESCE(i2.cust_inv_no, '')
        AND i3.invoice_id < i2.invoice_id
    )
  )
  AND NOT EXISTS (
    SELECT 1 FROM financing_dtl fd WHERE fd.invoice_code = i.invoice_code
  );

COMMIT;


-- =============================================================================
-- STEP 8. (REKOMENDASI) Unique constraint agar bug tidak terulang di level DB
--         Jalankan setelah STEP 1 & 2 bersih (tidak ada duplikat).
-- =============================================================================
-- Pastikan bersih dulu:
SELECT COUNT(*) AS sisa_duplikat
FROM (
  SELECT 1 FROM invoice
  GROUP BY cust_code, bouwheer_code, bouwheer_inv_no, cust_inv_no
  HAVING COUNT(*) > 1
) d;

-- Jika 0, buat constraint:
-- ALTER TABLE invoice
--   ADD CONSTRAINT uq_invoice_cust_bouwheer_inv
--   UNIQUE (cust_code, bouwheer_code, bouwheer_inv_no, cust_inv_no);

-- Relasi @OneToOne: pastikan 1 invoice_code hanya 1 financing_dtl
-- ALTER TABLE financing_dtl
--   ADD CONSTRAINT uq_financing_dtl_invoice UNIQUE (invoice_code);


-- =============================================================================
-- STEP 9. Verifikasi akhir (semua harus 0)
-- =============================================================================
SELECT 'invoice_code dengan >1 financing_dtl' AS pemeriksaan, COUNT(*) AS jumlah
FROM (SELECT invoice_code FROM financing_dtl GROUP BY invoice_code HAVING COUNT(*) > 1) a
UNION ALL
SELECT 'invoice duplikat (cust+bouwheerInvNo+custInvNo)', COUNT(*)
FROM (
  SELECT 1 FROM invoice
  GROUP BY cust_code, bouwheer_code, bouwheer_inv_no, cust_inv_no
  HAVING COUNT(*) > 1
) b
UNION ALL
SELECT 'financing_dtl yatim (invoice tidak ada)', COUNT(*)
FROM financing_dtl fd
WHERE NOT EXISTS (SELECT 1 FROM invoice i WHERE i.invoice_code = fd.invoice_code);
