package br.com.aep.ecocoleta.repository;

import br.com.aep.ecocoleta.model.Solicitacao;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SolicitacaoRepository extends MongoRepository<Solicitacao, String> {
}
