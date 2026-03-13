package io.roa.secretmanger.DTO.response;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
public class ApiRes<T> {

    private boolean success;
    private String message;
    private T data;

    private Map<String, String> errors;


    public ApiRes(boolean success, String message, T data, Map<String, String> errors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    public static <T> ApiRes<T> success(T data) {
        return new ApiRes<>(true, "Success", data, null);
    }

    public static <T> ApiRes<T> success(String message, T data) {
        return new ApiRes<>(true, message, data, null);
    }

    public static <T> ApiRes<T> success() {
        return new ApiRes<>(true, "Success", null, null);
    }

    public static <T> ApiRes<T> error(String message) {
        return new ApiRes<>(false, message, null, null);
    }

    public static <T> ApiRes<T> error(String message, Map<String, String> errors) {
        return new ApiRes<>(false, message, null, errors);
    }
}

