package br.com.aep.ecocoleta.dto;

import br.com.aep.ecocoleta.model.TipoResiduo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SolicitacaoCreateRequest(
        @NotBlank(message = "O titulo e obrigatorio")
        @Size(max = 80, message = "O titulo deve ter no maximo 80 caracteres")
        String titulo,

        @NotNull(message = "O tipo do residuo e obrigatorio")
        TipoResiduo tipo,

        @NotNull(message = "A quantidade e obrigatoria")
        @Min(value = 1, message = "A quantidade deve ser maior que zero")
        Integer quantidade,

        @Size(max = 300, message = "A descricao deve ter no maximo 300 caracteres")
        String descricao
) {
}
