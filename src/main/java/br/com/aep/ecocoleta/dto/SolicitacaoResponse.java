package br.com.aep.ecocoleta.dto;

import br.com.aep.ecocoleta.model.StatusSolicitacao;
import br.com.aep.ecocoleta.model.TipoResiduo;

public record SolicitacaoResponse(
        String id,
        String titulo,
        TipoResiduo tipo,
        Integer quantidade,
        String descricao,
        StatusSolicitacao status
) {
}
