package br.com.aep.ecocoleta.exception;

import java.util.Map;

public record ApiErrorResponse(String mensagem, Map<String, String> campos) {
}
