ALTER TABLE public.financing_hdr
    ADD COLUMN assigned_ao_employee_code varchar(50),
    ADD COLUMN assigned_ao_name varchar(100),
    ADD COLUMN assigned_ao_email varchar(254);
