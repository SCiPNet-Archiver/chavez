package com.cantomiletea.chavez.scpapi.crom;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class CromResponse<T> {
    @Setter
    private T data;
    private Map<String, Object> errors = new HashMap<>();

    public T getData() { return data; }

    @JsonAnySetter
    public void setErrors(String key, Object value) {
        this.errors.put(key, value);
    }

    public Map<String, Object> getErrors() { return errors; }

}
