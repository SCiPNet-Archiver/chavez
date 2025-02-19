package com.cantomiletea.chavez.util;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;

import java.util.List;

public class ControllerUtil {
    public static List<String> getBindingResultErrors(BindingResult bindingResult) {

        return bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
    }
}
