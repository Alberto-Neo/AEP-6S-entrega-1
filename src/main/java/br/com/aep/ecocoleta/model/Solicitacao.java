package br.com.aep.ecocoleta.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "solicitacoes")
public record Solicitacao(
        @Id String id,
        String titulo,
        TipoResiduo tipo,
        Integer quantidade,
        String descricao,
        StatusSolicitacao status
) {
}
