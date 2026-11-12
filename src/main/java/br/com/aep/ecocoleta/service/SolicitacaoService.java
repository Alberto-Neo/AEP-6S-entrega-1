package br.com.aep.ecocoleta.service;

import br.com.aep.ecocoleta.dto.SolicitacaoCreateRequest;
import br.com.aep.ecocoleta.dto.SolicitacaoResponse;
import br.com.aep.ecocoleta.dto.SolicitacaoUpdateRequest;
import br.com.aep.ecocoleta.exception.BusinessException;
import br.com.aep.ecocoleta.exception.ResourceNotFoundException;
import br.com.aep.ecocoleta.mapper.SolicitacaoMapper;
import br.com.aep.ecocoleta.model.Solicitacao;
import br.com.aep.ecocoleta.repository.SolicitacaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SolicitacaoService {

    private final SolicitacaoRepository repository;
    private final SolicitacaoMapper mapper;
    private final UsuarioService usuarioService;
    private final PontoColetaService pontoColetaService;

    public SolicitacaoService(SolicitacaoRepository repository, UsuarioService usuarioService,
                              PontoColetaService pontoColetaService, SolicitacaoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.usuarioService = usuarioService;
        this.pontoColetaService = pontoColetaService;
    }

    public SolicitacaoResponse criar(SolicitacaoCreateRequest request) {
        validarRelacionamentos(request.usuarioId(), request.pontoColetaId());
        return mapper.toResponse(repository.save(mapper.toModel(request)));
    }

    public List<SolicitacaoResponse> listar() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    public SolicitacaoResponse buscarPorId(String id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    public SolicitacaoResponse atualizar(String id, SolicitacaoUpdateRequest request) {
        Solicitacao atual = buscarEntidade(id);
        validarRelacionamentos(request.usuarioId(), request.pontoColetaId());
        return mapper.toResponse(repository.save(mapper.update(atual, request)));
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
}
