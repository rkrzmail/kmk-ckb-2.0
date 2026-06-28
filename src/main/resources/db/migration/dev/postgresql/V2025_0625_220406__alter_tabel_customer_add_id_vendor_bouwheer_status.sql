ALTER TABLE IF NOT EXISTS public.customer ADD COLUMN IF NOT EXISTS  vendor_id varchar(60) NULL;
ALTER TABLE public.customer ADD COLUMN IF NOT EXISTS  bouwheer varchar(36) NULL;
ALTER TABLE public.customer ADD COLUMN IF NOT EXISTS  status varchar(10) NULL;
ALTER TABLE public.financing_hdr ADD COLUMN IF NOT EXISTS vendor_id varchar(60) NULL;