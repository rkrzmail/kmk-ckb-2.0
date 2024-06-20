package com.kmkbe.core.service;

import com.kmkbe.core.model.CommonResult;
import com.kmkbe.core.model.SingleResult;
import org.springframework.stereotype.Service;

@Service
public class ResponseService {

    public <T> CommonResult getResult(T data) {
        return getSingleResult(data);
    }

    public <T> SingleResult<T> getSingleResult(T data) {
        SingleResult<T> response = new SingleResult<>();
        response.setData(data);
        return response;
    }
}
