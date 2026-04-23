package com.stotracker.service;

public record Result<T>(boolean success, T data, String error) {

    public static <T> Result<T> ok(T data) {
        return new Result<>(true, data, null);
    }

    public static <T> Result<T> err(String error) {
        return new Result<>(false, null, error);
    }
}