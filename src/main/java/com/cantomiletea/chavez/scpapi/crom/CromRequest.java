package com.cantomiletea.chavez.scpapi.crom;

import java.util.Map;

public record CromRequest(String query, Map<String, Object> variables) {

}
