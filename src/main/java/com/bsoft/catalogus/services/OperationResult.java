package com.bsoft.catalogus.services;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor()
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class OperationResult <T>{

    private final T successResult;

    private final T failureResult;

    public static <T> OperationResult<T> success(T succesResult) {
        return new OperationResult(succesResult, null);
    }

    public static <T> OperationResult<T> failure(T failureResult) {
        return new OperationResult<>(null, failureResult);
    }

    public boolean isSuccess() {
        return failureResult == null && successResult != null;
    }

}
