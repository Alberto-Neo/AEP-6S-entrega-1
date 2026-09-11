package br.com.aep.ecocoleta.service;

import br.com.aep.ecocoleta.dto.SolicitacaoCreateRequest;
import br.com.aep.ecocoleta.dto.SolicitacaoResponse;
import br.com.aep.ecocoleta.dto.SolicitacaoUpdateRequest;
import br.com.aep.ecocoleta.exception.ResourceNotFoundException;
import br.com.aep.ecocoleta.model.Solicitacao;
import br.com.aep.ecocoleta.model.StatusSolicitacao;
import br.com.aep.ecocoleta.repository.SolicitacaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SolicitacaoService {

    private final SolicitacaoRepository repository;

    public SolicitacaoService(SolicitacaoRepository repository) {
        this.repository = repository;
    }

    public SolicitacaoResponse criar(SolicitacaoCreateRequest request) {
        Solicitacao solicitacao = new Solicitacao(
                null,
                request.titulo(),
                request.tipo(),
                request.quantidade(),
                request.descricao(),
                StatusSolicitacao.ABERTA
        );
        return toResponse(repository.save(solicitacao));
    }

    public List<SolicitacaoResponse> listar() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public SolicitacaoResponse buscarPorId(String id) {
        return toResponse(buscarEntidade(id));
    }

    public SolicitacaoResponse atualizar(String id, SolicitacaoUpdateRequest request) {
        buscarEntidade(id);
        Solicitacao atualizada = new Solicitacao(
                id,
                request.titulo(),
                request.tipo(),
                request.quantidade(),
                request.descricao(),
                request.status()
        );
        return toResponse(repository.save(atualizada));
    }

    public void excluir(String id) {
        Solicitacao solicitacao = buscarEntidade(id);
        repository.delete(solicitacao);
    }

    private Solicitacao buscarEntidade(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitacao nao encontrada"));
    }

    private SolicitacaoResponse toResponse(Solicitacao solicitacao) {
        return new SolicitacaoResponse(
                solicitacao.id(),
                solicitacao.titulo(),
                solicitacao.tipo(),
                solicitacao.quantidade(),
                solicitacao.descricao(),
                solicitacao.status()
        );
    }
}
