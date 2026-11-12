package br.com.aep.ecocoleta.controller;

import br.com.aep.ecocoleta.exception.GlobalExceptionHandler;
import br.com.aep.ecocoleta.mapper.UsuarioMapper;
import br.com.aep.ecocoleta.model.Usuario;
import br.com.aep.ecocoleta.repository.UsuarioRepository;
import br.com.aep.ecocoleta.repository.SolicitacaoRepository;
import br.com.aep.ecocoleta.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UsuarioControllerTest {

    private UsuarioRepository repository;
    private MockMvc mockMvc;
    private final Usuario existente = new Usuario("u1", "Ana", "ana@exemplo.com", Instant.parse("2026-01-01T00:00:00Z"));
    private static final String CADASTRO = """
            {"nome":" Ana ","email":"ANA@EXEMPLO.COM"}
            """;

    @BeforeEach
    void setUp() {
        repository = mock(UsuarioRepository.class);
        var service = new UsuarioService(repository, new UsuarioMapper(), mock(SolicitacaoRepository.class));
        mockMvc = MockMvcBuilders.standaloneSetup(new UsuarioController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void deveCadastrarComStatus201EDadosNormalizados() throws Exception {
        when(repository.save(any())).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            return new Usuario("u1", usuario.nome(), usuario.email(), usuario.criadoEm());
        });
        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(CADASTRO))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value("u1"))
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana@exemplo.com"))
                .andExpect(jsonPath("$.criadoEm").exists());
    }

    @Test
    void deveListarEBuscarUsuarios() throws Exception {
        when(repository.findAll()).thenReturn(List.of(existente));
        when(repository.findById("u1")).thenReturn(Optional.of(existente));
        mockMvc.perform(get("/api/usuarios")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("u1"));
        mockMvc.perform(get("/api/usuarios/u1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@exemplo.com"));
    }

    @Test
    void deveAtualizarUsuario() throws Exception {
        when(repository.findById("u1")).thenReturn(Optional.of(existente));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        mockMvc.perform(put("/api/usuarios/u1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana Souza\",\"email\":\"NOVO@EXEMPLO.COM\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value("u1"))
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.email").value("novo@exemplo.com"));
    }

    @Test
    void deveExcluirComStatus204() throws Exception {
        when(repository.findById("u1")).thenReturn(Optional.of(existente));
        mockMvc.perform(delete("/api/usuarios/u1")).andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(repository).delete(existente);
    }

    @Test
    void deveRetornar404NoFormatoAtualDaApi() throws Exception {
        mockMvc.perform(get("/api/usuarios/ausente")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuario nao encontrado"))
                .andExpect(jsonPath("$.campos").isMap());
        mockMvc.perform(put("/api/usuarios/ausente").contentType(MediaType.APPLICATION_JSON).content(CADASTRO))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/usuarios/ausente")).andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400ParaEmailDuplicadoNaCriacaoEEdicao() throws Exception {
        when(repository.existsByEmailIgnoreCase("ana@exemplo.com")).thenReturn(true);
        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(CADASTRO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Ja existe um usuario cadastrado com este e-mail"))
                .andExpect(jsonPath("$.campos").isMap());
        when(repository.findById("u1")).thenReturn(Optional.of(existente));
        when(repository.existsByEmailIgnoreCaseAndIdNot("ana@exemplo.com", "u1")).thenReturn(true);
        mockMvc.perform(put("/api/usuarios/u1").contentType(MediaType.APPLICATION_JSON).content(CADASTRO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Ja existe outro usuario cadastrado com este e-mail"));
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"nome\":null,\"email\":null}",
            "{\"nome\":\"\",\"email\":\"\"}",
            "{\"nome\":\"   \",\"email\":\"ana@exemplo.com\"}",
            "{\"nome\":\"Ana\",\"email\":\"   \"}",
            "{\"nome\":\"Ana\",\"email\":\"invalido\"}"
    })
    void deveValidarCadastroEAtualizacaoAntesDeAcessarBanco(String body) throws Exception {
        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem").value("Dados invalidos"))
                .andExpect(jsonPath("$.campos").isNotEmpty());
        mockMvc.perform(put("/api/usuarios/u1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos").isNotEmpty());
        verifyNoInteractions(repository);
    }
}
