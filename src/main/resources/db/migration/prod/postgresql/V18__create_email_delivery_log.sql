CREATE TABLE public.email_delivery_log (
  email_delivery_id BIGSERIAL PRIMARY KEY,
  customer_code UUID NOT NULL,
  template_code VARCHAR(50) NOT NULL,
  recipient VARCHAR(1000) NOT NULL,
  status VARCHAR(20) NOT NULL,
  attempt_count INTEGER NOT NULL DEFAULT 0,
  error_message VARCHAR(2000),
  requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  sent_at TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT ck_email_delivery_status CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);

CREATE INDEX idx_email_delivery_customer ON public.email_delivery_log (customer_code, requested_at DESC);
CREATE INDEX idx_email_delivery_status ON public.email_delivery_log (status);
