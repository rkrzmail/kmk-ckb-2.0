package com.kmkbe.feign.config;

import feign.Response;
import feign.codec.ErrorDecoder;

public class ConfinsR3ErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder delegate = new ErrorDecoder.Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        return delegate.decode(methodKey, response);
    }
}
