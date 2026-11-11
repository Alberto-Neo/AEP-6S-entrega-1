package br.com.aep.ecocoleta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioCreateRequest(
        @NotBlank(message = "O nome e obrigatorio") String nome,
        @NotBlank(message = "O e-mail e obrigatorio")
        @Email(message = "E-mail invalido") String email
) {
}
