package br.com.aep.ecocoleta.dto;

import br.com.aep.ecocoleta.model.Endereco;
import br.com.aep.ecocoleta.model.ItemResiduo;
import br.com.aep.ecocoleta.model.StatusSolicitacao;
import br.com.aep.ecocoleta.model.TipoResiduo;

import java.time.Instant;
import java.util.List;

public record SolicitacaoResponse(
        String id,
        String titulo,
        TipoResiduo tipo,
        Integer quantidade,
        String descricao,
        StatusSolicitacao status,
        String usuarioId,
        String pontoColetaId,
        Endereco endereco,
        List<ItemResiduo> itens,
        Instant criadoEm
) {
}
