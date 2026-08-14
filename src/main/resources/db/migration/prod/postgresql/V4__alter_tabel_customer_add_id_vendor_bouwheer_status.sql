ALTER TABLE public.customer ADD COLUMN IF NOT EXISTS  approval_status varchar(10) NULL;
ALTER TABLE public.customer ADD COLUMN IF NOT EXISTS  approval_note varchar(100) NULL;
ALTER TABLE public.customer ADD COLUMN IF NOT EXISTS  approval_by varchar(50) NULL;
ALTER TABLE public.customer ADD COLUMN IF NOT EXISTS  approval_at TIMESTAMP NULL;