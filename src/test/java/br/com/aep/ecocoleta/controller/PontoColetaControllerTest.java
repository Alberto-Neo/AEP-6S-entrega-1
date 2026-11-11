package br.com.aep.ecocoleta.controller;

import br.com.aep.ecocoleta.exception.GlobalExceptionHandler;
import br.com.aep.ecocoleta.mapper.PontoColetaMapper;
import br.com.aep.ecocoleta.model.PontoColeta;
import br.com.aep.ecocoleta.repository.PontoColetaRepository;
import br.com.aep.ecocoleta.service.PontoColetaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PontoColetaControllerTest {

    private PontoColetaRepository repository;
    private MockMvc mockMvc;
    private final PontoColeta ponto = new PontoColeta("p1", "Ecoponto Centro", "Rua A, 100", true);

    @BeforeEach
    void setUp() {
        repository = mock(PontoColetaRepository.class);
        var service = new PontoColetaService(repository, new PontoColetaMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(new PontoColetaController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void deveListarPontosComStatus200() throws Exception {
        when(repository.findAll()).thenReturn(List.of(ponto));
        mockMvc.perform(get("/api/pontos")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("p1"))
                .andExpect(jsonPath("$[0].nome").value("Ecoponto Centro"))
                .andExpect(jsonPath("$[0].endereco").value("Rua A, 100"))
                .andExpect(jsonPath("$[0].ativo").value(true));
    }

    @Test
    void deveBuscarPorIdComStatus200() throws Exception {
        when(repository.findById("p1")).thenReturn(Optional.of(ponto));
        mockMvc.perform(get("/api/pontos/p1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p1"));
    }

    @Test
    void deveRetornar404NoFormatoExistente() throws Exception {
        mockMvc.perform(get("/api/pontos/ausente")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Ponto de coleta nao encontrado"))
                .andExpect(jsonPath("$.campos").isMap());
    }

    @Test
    void naoDeveExporOperacoesDeEscrita() throws Exception {
        String body = "{\"nome\":\"Novo ponto\",\"endereco\":\"Rua A\",\"ativo\":true}";
        mockMvc.perform(post("/api/pontos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(put("/api/pontos/p1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(delete("/api/pontos/p1")).andExpect(status().isMethodNotAllowed());
        verifyNoInteractions(repository);
    }
}
