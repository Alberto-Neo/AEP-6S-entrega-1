package br.com.aep.ecocoleta.dto;

import br.com.aep.ecocoleta.model.TipoResiduo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemResiduoRequest(
        @NotNull(message = "O tipo do residuo e obrigatorio") TipoResiduo tipo,
        @NotNull(message = "A quantidade e obrigatoria")
        @Min(value = 1, message = "A quantidade deve ser maior que zero") Integer quantidade
) {
}
