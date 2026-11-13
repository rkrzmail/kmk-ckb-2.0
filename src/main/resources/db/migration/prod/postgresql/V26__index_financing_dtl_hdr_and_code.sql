CREATE INDEX IF NOT EXISTS idx_financing_dtl_hdr_code_dtl_code
  ON public.financing_dtl (financing_hdr_code, financing_dtl_code);
