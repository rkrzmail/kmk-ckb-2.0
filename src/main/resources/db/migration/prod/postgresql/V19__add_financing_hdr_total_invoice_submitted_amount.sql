ALTER TABLE financing_hdr
ADD COLUMN IF NOT EXISTS total_invoice_submitted_amt numeric(17, 2) DEFAULT 0;