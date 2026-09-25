ALTER TABLE product
    ADD COLUMN IF NOT EXISTS bouwheer_code UUID NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uq_bouwheer_bouwheer_code'
    ) THEN
        ALTER TABLE bouwheer
            ADD CONSTRAINT uq_bouwheer_bouwheer_code
            UNIQUE (bouwheer_code);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_product_to_bouwheer'
    ) THEN
        ALTER TABLE product
            ADD CONSTRAINT fk_product_to_bouwheer
            FOREIGN KEY (bouwheer_code)
            REFERENCES bouwheer (bouwheer_code);
    END IF;
END $$;
