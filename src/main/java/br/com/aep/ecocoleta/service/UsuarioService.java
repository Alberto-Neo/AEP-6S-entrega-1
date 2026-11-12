package br.com.aep.ecocoleta.service;

import br.com.aep.ecocoleta.dto.UsuarioCreateRequest;
import br.com.aep.ecocoleta.dto.UsuarioResponse;
import br.com.aep.ecocoleta.dto.UsuarioUpdateRequest;
import br.com.aep.ecocoleta.exception.BusinessException;
import br.com.aep.ecocoleta.exception.ResourceNotFoundException;
import br.com.aep.ecocoleta.mapper.UsuarioMapper;
import br.com.aep.ecocoleta.model.Usuario;
import br.com.aep.ecocoleta.repository.UsuarioRepository;
import br.com.aep.ecocoleta.repository.SolicitacaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final UsuarioMapper mapper;

    private final SolicitacaoRepository solicitacaoRepository;

    public UsuarioService(UsuarioRepository repository, UsuarioMapper mapper, SolicitacaoRepository solicitacaoRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.solicitacaoRepository = solicitacaoRepository;
    }

    public UsuarioResponse criar(UsuarioCreateRequest request) {
        Usuario usuario = mapper.toModel(request);
        if (repository.existsByEmailIgnoreCase(usuario.email())) {
            throw new BusinessException("Ja existe um usuario cadastrado com este e-mail");
        }
        return mapper.toResponse(repository.save(usuario));
    }

    public List<UsuarioResponse> listar() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    public UsuarioResponse buscarPorId(String id) {
        return mapper.toResponse(obter(id));
    }

    public UsuarioResponse atualizar(String id, UsuarioUpdateRequest request) {
        Usuario atual = obter(id);
        Usuario atualizado = mapper.update(atual, request);
        if (repository.existsByEmailIgnoreCaseAndIdNot(atualizado.email(), id)) {
            throw new BusinessException("Ja existe outro usuario cadastrado com este e-mail");
        }
        return mapper.toResponse(repository.save(atualizado));
    }

    public void excluir(String id) {
        Usuario usuario = obter(id);
        if (solicitacaoRepository.existsByUsuarioId(usuario.id())) {
            throw new BusinessException("Usuario possui solicitacoes vinculadas e nao pode ser excluido");
        }
        repository.delete(usuario);
    }

    public Usuario obter(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }
}
