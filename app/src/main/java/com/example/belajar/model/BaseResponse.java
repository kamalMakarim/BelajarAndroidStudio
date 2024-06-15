package com.example.belajar.model;

public class BaseResponse<T> {
    public String message;
    public T payload = null;

    public BaseResponse(String message, T payload) {
        this.message = message;
        this.payload = payload;
    }

    public BaseResponse(String message) {
        this.message = message;
    }
}
