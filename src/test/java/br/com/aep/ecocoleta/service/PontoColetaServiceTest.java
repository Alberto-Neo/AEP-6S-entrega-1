package br.com.aep.ecocoleta.service;

import br.com.aep.ecocoleta.exception.ResourceNotFoundException;
import br.com.aep.ecocoleta.mapper.PontoColetaMapper;
import br.com.aep.ecocoleta.model.PontoColeta;
import br.com.aep.ecocoleta.repository.PontoColetaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PontoColetaServiceTest {

    private PontoColetaRepository repository;
    private PontoColetaService service;
    private final PontoColeta ponto = new PontoColeta("p1", "Ecoponto Centro", "Rua A, 100", true);

    @BeforeEach
    void setUp() {
        repository = mock(PontoColetaRepository.class);
        service = new PontoColetaService(repository, new PontoColetaMapper());
    }

    @Test
    void deveListarTodosOsDadosDosPontos() {
        when(repository.findAll()).thenReturn(List.of(ponto));
        var resposta = service.listar();
        assertEquals(1, resposta.size());
        assertEquals(ponto.id(), resposta.getFirst().id());
        assertEquals(ponto.nome(), resposta.getFirst().nome());
        assertEquals(ponto.endereco(), resposta.getFirst().endereco());
        assertEquals(ponto.ativo(), resposta.getFirst().ativo());
    }

    @Test
    void deveRetornarListaVazia() {
        when(repository.findAll()).thenReturn(List.of());
        assertTrue(service.listar().isEmpty());
    }

    @Test
    void deveBuscarPorId() {
        when(repository.findById("p1")).thenReturn(Optional.of(ponto));
        assertEquals(ponto.nome(), service.buscarPorId("p1").nome());
        verify(repository).findById("p1");
    }

    @Test
    void deveInformarPontoInexistente() {
        when(repository.findById("ausente")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.buscarPorId("ausente"));
    }
}
