package br.com.aep.ecocoleta.mapper;

import br.com.aep.ecocoleta.dto.PontoColetaResponse;
import br.com.aep.ecocoleta.model.PontoColeta;
import org.springframework.stereotype.Component;

@Component
public class PontoColetaMapper {

    public PontoColetaResponse toResponse(PontoColeta ponto) {
        return new PontoColetaResponse(ponto.id(), ponto.nome(), ponto.endereco(), ponto.ativo());
    }
}
