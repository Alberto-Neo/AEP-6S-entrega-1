package br.com.aep.ecocoleta.repository;

import br.com.aep.ecocoleta.model.PontoColeta;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PontoColetaRepository extends MongoRepository<PontoColeta, String> {
}
