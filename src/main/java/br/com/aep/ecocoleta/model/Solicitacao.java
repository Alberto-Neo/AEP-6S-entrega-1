package br.com.aep.ecocoleta.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "solicitacoes")
public record Solicitacao(
        @Id String id,
        String titulo,
        // Campos legados: mantidos para consultar documentos anteriores a lista de itens.
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
