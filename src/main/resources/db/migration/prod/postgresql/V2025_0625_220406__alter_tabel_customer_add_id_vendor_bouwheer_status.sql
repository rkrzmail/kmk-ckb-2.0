ALTER TABLE public.customer ADD COLUMN IF NOT EXISTS  npwp varchar(20) NULL;
ALTER TABLE public.customer ADD COLUMN IF NOT EXISTS  vendor_id varchar(60) NULL;
ALTER TABLE public.customer ADD COLUMN IF NOT EXISTS  bouwheer_code  UUID NULL;
ALTER TABLE public.customer ADD COLUMN IF NOT EXISTS  approval_status varchar(10) NULL;
ALTER TABLE public.customer ADD COLUMN IF NOT EXISTS  approval_note varchar(100) NULL;
ALTER TABLE public.customer ADD COLUMN IF NOT EXISTS  approval_by varchar(50) NULL;
ALTER TABLE public.customer ADD COLUMN IF NOT EXISTS  approval_at TIMESTAMP NULL;
ALTER TABLE public.financing_hdr ADD COLUMN IF NOT EXISTS vendor_id varchar(60) NULL;