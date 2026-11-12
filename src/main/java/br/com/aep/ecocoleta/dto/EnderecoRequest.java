package br.com.aep.ecocoleta.dto;

import jakarta.validation.constraints.NotBlank;

public record EnderecoRequest(
        @NotBlank(message = "Rua e obrigatoria") String rua,
        @NotBlank(message = "Numero e obrigatorio") String numero,
        @NotBlank(message = "Bairro e obrigatorio") String bairro,
        @NotBlank(message = "Cidade e obrigatoria") String cidade
) {
}
