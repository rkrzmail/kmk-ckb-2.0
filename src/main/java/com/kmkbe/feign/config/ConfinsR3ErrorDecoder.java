package com.kmkbe.feign.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfinsR3ErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder delegate = new ErrorDecoder.Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.info("Feign decoder http code {} response {} ",response.status(),response.body());
        return delegate.decode(methodKey, response);
    }
}
