package br.com.aep.ecocoleta.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SolicitacaoCreateRequest(
        @NotBlank(message = "O titulo e obrigatorio")
        @Size(max = 80, message = "O titulo deve ter no maximo 80 caracteres")
        String titulo,

        @Size(max = 300, message = "A descricao deve ter no maximo 300 caracteres")
        String descricao,

        @NotBlank(message = "O usuario e obrigatorio")
        String usuarioId,

        @NotBlank(message = "O ponto de coleta e obrigatorio")
        String pontoColetaId,

        @NotNull(message = "O endereco e obrigatorio")
        @Valid EnderecoRequest endereco,

        @NotEmpty(message = "Informe pelo menos um item")
        List<@NotNull(message = "O item nao pode ser nulo") @Valid ItemResiduoRequest> itens
) {
}
