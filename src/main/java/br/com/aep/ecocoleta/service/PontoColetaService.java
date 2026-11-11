package br.com.aep.ecocoleta.service;

import br.com.aep.ecocoleta.dto.PontoColetaResponse;
import br.com.aep.ecocoleta.exception.ResourceNotFoundException;
import br.com.aep.ecocoleta.mapper.PontoColetaMapper;
import br.com.aep.ecocoleta.model.PontoColeta;
import br.com.aep.ecocoleta.repository.PontoColetaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PontoColetaService {

    private final PontoColetaRepository repository;
    private final PontoColetaMapper mapper;

    public PontoColetaService(PontoColetaRepository repository, PontoColetaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<PontoColetaResponse> listar() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    public PontoColetaResponse buscarPorId(String id) {
        return mapper.toResponse(obter(id));
    }

    public PontoColeta obter(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ponto de coleta nao encontrado"));
    }
}
