ALTER TABLE public.financing_hdr
  ADD COLUMN IF NOT EXISTS financeable_invoice_amt numeric(17, 2);
