package br.com.aep.ecocoleta.controller;

import br.com.aep.ecocoleta.exception.GlobalExceptionHandler;
import br.com.aep.ecocoleta.mapper.PontoColetaMapper;
import br.com.aep.ecocoleta.mapper.SolicitacaoMapper;
import br.com.aep.ecocoleta.mapper.UsuarioMapper;
import br.com.aep.ecocoleta.model.PontoColeta;
import br.com.aep.ecocoleta.model.Solicitacao;
import br.com.aep.ecocoleta.model.StatusSolicitacao;
import br.com.aep.ecocoleta.model.TipoResiduo;
import br.com.aep.ecocoleta.model.Usuario;
import br.com.aep.ecocoleta.repository.PontoColetaRepository;
import br.com.aep.ecocoleta.repository.SolicitacaoRepository;
import br.com.aep.ecocoleta.repository.UsuarioRepository;
import br.com.aep.ecocoleta.service.PontoColetaService;
import br.com.aep.ecocoleta.service.SolicitacaoService;
import br.com.aep.ecocoleta.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SolicitacaoRelacionamentoTest {

    private SolicitacaoRepository solicitacoes;
    private UsuarioRepository usuarios;
    private PontoColetaRepository pontos;
    private MockMvc mvc;
    private final Usuario usuario = new Usuario("u1", "Ana", "ana@exemplo.com", Instant.now());
    private final Solicitacao existente = new Solicitacao("s1", "Titulo", TipoResiduo.VIDRO,
            1, null, StatusSolicitacao.ABERTA, "u1", "p1", null, null, null);

    @BeforeEach
    void setUp() {
        solicitacoes = mock(SolicitacaoRepository.class);
        usuarios = mock(UsuarioRepository.class);
        pontos = mock(PontoColetaRepository.class);
        var usuarioService = new UsuarioService(usuarios, new UsuarioMapper(), solicitacoes);
        var pontoService = new PontoColetaService(pontos, new PontoColetaMapper());
        var service = new SolicitacaoService(solicitacoes, usuarioService, pontoService, new SolicitacaoMapper());
        mvc = MockMvcBuilders.standaloneSetup(new SolicitacaoController(service), new UsuarioController(usuarioService))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    private ObjectNode dados() {
        ObjectNode dados = new ObjectMapper().createObjectNode().put("titulo", "Titulo").put("tipo", "VIDRO")
                .put("quantidade", 1).put("status", "ABERTA").put("usuarioId", "u1").put("pontoColetaId", "p1");
        dados.putObject("endereco").put("rua", "Rua A").put("numero", "10")
                .put("bairro", "Centro").put("cidade", "Maringa");
        dados.putArray("itens").addObject().put("tipo", "VIDRO").put("quantidade", 1);
        dados.remove(List.of("tipo", "quantidade"));
        return dados;
    }

    private void relacionamentosValidos() {
        when(usuarios.findById("u1")).thenReturn(Optional.of(usuario));
        when(pontos.findById("p1")).thenReturn(Optional.of(new PontoColeta("p1", "Centro", "Rua A", true)));
    }

    @Test
    void devePersistirERetornarVinculosNormalizadosNaCriacao() throws Exception {
        relacionamentosValidos();
        when(solicitacoes.save(any())).thenAnswer(i -> {
            Solicitacao s = i.getArgument(0);
            return new Solicitacao("s1", s.titulo(), s.tipo(), s.quantidade(),
                    s.descricao(), s.status(), s.usuarioId(), s.pontoColetaId(), s.endereco(), s.itens(),
                    s.criadoEm());
        });
        var body = dados().put("usuarioId", " u1 ").put("pontoColetaId", " p1 ");
        mvc.perform(post("/api/solicitacoes").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value("s1"))
                .andExpect(jsonPath("$.usuarioId").value("u1")).andExpect(jsonPath("$.pontoColetaId").value("p1"));
        verify(solicitacoes).save(argThat(s -> s.usuarioId().equals("u1") && s.pontoColetaId().equals("p1")));
        verify(usuarios).findById("u1");
        verify(pontos).findById("p1");
    }

    @Test
    void deveAtualizarVinculosSemTrocarIdDaSolicitacao() throws Exception {
        when(solicitacoes.findById("s1")).thenReturn(Optional.of(existente));
        when(usuarios.findById("u2")).thenReturn(Optional.of(new Usuario("u2", "Bia", "bia@exemplo.com", Instant.now())));
        when(pontos.findById("p2")).thenReturn(Optional.of(new PontoColeta("p2", "Outro", "Rua B", true)));
        when(solicitacoes.save(any())).thenAnswer(i -> i.getArgument(0));
        var body = dados().put("usuarioId", "u2").put("pontoColetaId", "p2");
        mvc.perform(put("/api/solicitacoes/s1").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value("s1"))
                .andExpect(jsonPath("$.usuarioId").value("u2")).andExpect(jsonPath("$.pontoColetaId").value("p2"));
    }

    @Test
    void deveExporVinculosNaListagemENaConsulta() throws Exception {
        when(solicitacoes.findAll()).thenReturn(List.of(existente));
        when(solicitacoes.findById("s1")).thenReturn(Optional.of(existente));
        mvc.perform(get("/api/solicitacoes")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarioId").value("u1"));
        mvc.perform(get("/api/solicitacoes/s1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.pontoColetaId").value("p1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"usuarioId", "pontoColetaId"})
    void deveRejeitarVinculosAusentesNulosOuEmBranco(String campo) throws Exception {
        for (String valor : new String[]{null, "", "   ", "ausente"}) {
            var body = dados();
            if ("ausente".equals(valor)) body.remove(campo);
            else if (valor == null) body.putNull(campo);
            else body.put(campo, valor);
            mvc.perform(post("/api/solicitacoes").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos." + campo).exists());
            mvc.perform(put("/api/solicitacoes/s1").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos." + campo).exists());
        }
        verifyNoInteractions(solicitacoes, usuarios, pontos);
    }

    @ParameterizedTest
    @ValueSource(strings = {"usuarioId", "pontoColetaId"})
    void deveRejeitarVinculoInexistenteSemSalvar(String campo) throws Exception {
        relacionamentosValidos();
        when(solicitacoes.findById("s1")).thenReturn(Optional.of(existente));
        var body = dados().put(campo, "inexistente");
        mvc.perform(post("/api/solicitacoes").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.mensagem").exists());
        mvc.perform(put("/api/solicitacoes/s1").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                .andExpect(status().isNotFound());
        verify(solicitacoes, never()).save(any());
    }

    @Test
    void deveBloquearExclusaoDoUsuarioAteRemoverSolicitacoesVinculadas() throws Exception {
        when(usuarios.findById("u1")).thenReturn(Optional.of(usuario));
        when(solicitacoes.existsByUsuarioId("u1")).thenReturn(true, false);
        mvc.perform(delete("/api/usuarios/u1")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Usuario possui solicitacoes vinculadas e nao pode ser excluido"));
        verify(usuarios, never()).delete(any());
        when(solicitacoes.findById("s1")).thenReturn(Optional.of(existente));
        mvc.perform(delete("/api/solicitacoes/s1")).andExpect(status().isNoContent());
        verify(solicitacoes).delete(existente);
        mvc.perform(delete("/api/usuarios/u1")).andExpect(status().isNoContent());
        verify(usuarios).delete(usuario);
    }
}
