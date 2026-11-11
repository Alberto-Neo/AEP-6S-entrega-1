package br.com.aep.ecocoleta.dto;

import java.time.Instant;

public record UsuarioResponse(String id, String nome, String email, Instant criadoEm) {
}
