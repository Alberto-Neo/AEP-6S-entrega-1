package br.com.aep.ecocoleta.service;

import br.com.aep.ecocoleta.dto.SolicitacaoCreateRequest;
import br.com.aep.ecocoleta.dto.SolicitacaoResponse;
import br.com.aep.ecocoleta.dto.SolicitacaoUpdateRequest;
import br.com.aep.ecocoleta.exception.ResourceNotFoundException;
import br.com.aep.ecocoleta.exception.BusinessException;
import br.com.aep.ecocoleta.model.Solicitacao;
import br.com.aep.ecocoleta.model.StatusSolicitacao;
import br.com.aep.ecocoleta.repository.SolicitacaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SolicitacaoService {

    private final SolicitacaoRepository repository;

    private final UsuarioService usuarioService;
    private final PontoColetaService pontoColetaService;

    public SolicitacaoService(SolicitacaoRepository repository, UsuarioService usuarioService,
                              PontoColetaService pontoColetaService) {
        this.repository = repository;
        this.usuarioService = usuarioService;
        this.pontoColetaService = pontoColetaService;
    }

    public SolicitacaoResponse criar(SolicitacaoCreateRequest request) {
        validarRelacionamentos(request.usuarioId(), request.pontoColetaId());
        Solicitacao solicitacao = new Solicitacao(
                null,
                request.titulo(),
                request.tipo(),
                request.quantidade(),
                request.descricao(),
                StatusSolicitacao.ABERTA,
                request.usuarioId().trim(),
                request.pontoColetaId().trim()
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
        validarRelacionamentos(request.usuarioId(), request.pontoColetaId());
        Solicitacao atualizada = new Solicitacao(
                id,
                request.titulo(),
                request.tipo(),
                request.quantidade(),
                request.descricao(),
                request.status(),
                request.usuarioId().trim(),
                request.pontoColetaId().trim()
        );
        return toResponse(repository.save(atualizada));
    }

    public void excluir(String id) {
        Solicitacao solicitacao = buscarEntidade(id);
        repository.delete(solicitacao);
    }

    private void validarRelacionamentos(String usuarioId, String pontoColetaId) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new BusinessException("O usuario e obrigatorio");
        }
        if (pontoColetaId == null || pontoColetaId.isBlank()) {
            throw new BusinessException("O ponto de coleta e obrigatorio");
        }
        usuarioService.obter(usuarioId.trim());
        pontoColetaService.obter(pontoColetaId.trim());
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
                solicitacao.status(),
                solicitacao.usuarioId(),
                solicitacao.pontoColetaId()
        );
    }
}
