package br.com.aep.ecocoleta.config;

import br.com.aep.ecocoleta.model.PontoColeta;
import br.com.aep.ecocoleta.repository.PontoColetaRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PontosColetaInicializador implements ApplicationRunner {

    private final PontoColetaRepository repository;

    public PontosColetaInicializador(PontoColetaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<PontoColeta> existentes = repository.findAll();
        // Pontos e enderecos ficticios para demonstracao academica.
        List<PontoColeta> predefinidos = List.of(
                new PontoColeta("exemplo-centro", "Ecoponto Centro",
                        "Rua Exemplo A, 100 - Centro, Maringa - PR", true),
                new PontoColeta("exemplo-eletronicos", "Ecoponto Eletrônicos",
                        "Rua Exemplo B, 200 - Zona 02, Maringa - PR", true),
                new PontoColeta("exemplo-reciclagem", "Ecoponto Reciclagem",
                        "Rua Exemplo C, 300 - Zona 03, Maringa - PR", true)
        );
        for (PontoColeta ponto : predefinidos) {
            boolean cadastrado = existentes.stream().anyMatch(existente ->
                    ponto.id().equals(existente.id())
                            || (existente.nome() != null && ponto.nome().equalsIgnoreCase(existente.nome().trim())));
            if (!cadastrado) {
                repository.insert(ponto);
            }
        }
    }
}
