package br.com.aep.ecocoleta.config;

import br.com.aep.ecocoleta.model.PontoColeta;
import br.com.aep.ecocoleta.repository.PontoColetaRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PontosColetaInicializadorTest {

    private PontoColetaRepository simularBanco(List<PontoColeta> pontos) {
        var repository = mock(PontoColetaRepository.class);
        when(repository.findAll()).thenAnswer(invocation -> List.copyOf(pontos));
        when(repository.insert(any(PontoColeta.class))).thenAnswer(invocation -> {
            PontoColeta ponto = invocation.getArgument(0);
            pontos.add(ponto);
            return ponto;
        });
        return repository;
    }

    @Test
    void deveInicializarTresPontosSemDuplicarNoReinicio() {
        var pontos = new ArrayList<PontoColeta>();
        var repository = simularBanco(pontos);
        var inicializador = new PontosColetaInicializador(repository);
        inicializador.run(null);
        inicializador.run(null);
        assertEquals(3, pontos.size());
        assertEquals(List.of("exemplo-centro", "exemplo-eletronicos", "exemplo-reciclagem"),
                pontos.stream().map(PontoColeta::id).toList());
        assertTrue(pontos.stream().allMatch(p -> p.ativo() && !p.nome().isBlank() && !p.endereco().isBlank()));
        verify(repository, times(3)).insert(any(PontoColeta.class));
        verify(repository, never()).save(any(PontoColeta.class));
    }

    @Test
    void deveCompletarApenasAusentesPreservandoPontoComMesmoNome() {
        var existente = new PontoColeta("id-anterior", " ecoponto centro ", "Endereco anterior", false);
        var pontos = new ArrayList<>(List.of(existente));
        var repository = simularBanco(pontos);
        new PontosColetaInicializador(repository).run(null);
        assertEquals(3, pontos.size());
        assertSame(existente, pontos.getFirst());
        verify(repository, times(2)).insert(any(PontoColeta.class));
        verify(repository, never()).save(any(PontoColeta.class));
    }

    @Test
    void devePreservarPontoRenomeadoComMesmoIdEOutrosPontos() {
        var renomeado = new PontoColeta("exemplo-centro", "Nome alterado", "Endereco alterado", false);
        var outro = new PontoColeta("outro", "Outro ponto", "Outro endereco", true);
        var pontos = new ArrayList<>(List.of(renomeado, outro));
        var repository = simularBanco(pontos);
        new PontosColetaInicializador(repository).run(null);
        assertEquals(4, pontos.size());
        assertSame(renomeado, pontos.get(0));
        assertSame(outro, pontos.get(1));
        verify(repository, times(2)).insert(any(PontoColeta.class));
        verify(repository, never()).save(any(PontoColeta.class));
    }

    @Test
    void devePreservarRegistroSemNomeSemInterromperInicializacao() {
        var legado = new PontoColeta("legado", null, "Endereco", true);
        var pontos = new ArrayList<>(List.of(legado));
        new PontosColetaInicializador(simularBanco(pontos)).run(null);
        assertEquals(4, pontos.size());
        assertSame(legado, pontos.getFirst());
    }
}
