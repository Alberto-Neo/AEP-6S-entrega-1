package br.com.aep.ecocoleta.mapper;

import br.com.aep.ecocoleta.dto.EnderecoRequest;
import br.com.aep.ecocoleta.dto.ItemResiduoRequest;
import br.com.aep.ecocoleta.dto.SolicitacaoCreateRequest;
import br.com.aep.ecocoleta.dto.SolicitacaoResponse;
import br.com.aep.ecocoleta.dto.SolicitacaoUpdateRequest;
import br.com.aep.ecocoleta.model.Endereco;
import br.com.aep.ecocoleta.model.ItemResiduo;
import br.com.aep.ecocoleta.model.Solicitacao;
import br.com.aep.ecocoleta.model.StatusSolicitacao;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class SolicitacaoMapper {

    public Solicitacao toModel(SolicitacaoCreateRequest request) {
        return new Solicitacao(null, request.titulo(), null, null, request.descricao(),
                StatusSolicitacao.ABERTA, request.usuarioId().trim(), request.pontoColetaId().trim(),
                endereco(request.endereco()), itens(request.itens()), Instant.now());
    }

    public Solicitacao update(Solicitacao atual, SolicitacaoUpdateRequest request) {
        return new Solicitacao(atual.id(), request.titulo(), null, null, request.descricao(),
                request.status(), request.usuarioId().trim(), request.pontoColetaId().trim(),
                endereco(request.endereco()), itens(request.itens()), atual.criadoEm());
    }

    public SolicitacaoResponse toResponse(Solicitacao solicitacao) {
        return new SolicitacaoResponse(solicitacao.id(), solicitacao.titulo(), solicitacao.tipo(),
                solicitacao.quantidade(), solicitacao.descricao(), solicitacao.status(),
                solicitacao.usuarioId(), solicitacao.pontoColetaId(), solicitacao.endereco(),
                solicitacao.itens() == null ? List.of() : solicitacao.itens(), solicitacao.criadoEm());
    }

    private Endereco endereco(EnderecoRequest request) {
        return new Endereco(request.rua().trim(), request.numero().trim(), request.bairro().trim(),
                request.cidade().trim());
    }

    private List<ItemResiduo> itens(List<ItemResiduoRequest> requests) {
        return requests.stream().map(item -> new ItemResiduo(item.tipo(), item.quantidade())).toList();
    }
}
