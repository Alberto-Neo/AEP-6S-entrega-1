package br.com.aep.ecocoleta.controller;

import br.com.aep.ecocoleta.dto.SolicitacaoResponse;
import br.com.aep.ecocoleta.exception.GlobalExceptionHandler;
import br.com.aep.ecocoleta.exception.ResourceNotFoundException;
import br.com.aep.ecocoleta.model.StatusSolicitacao;
import br.com.aep.ecocoleta.model.TipoResiduo;
import br.com.aep.ecocoleta.service.SolicitacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SolicitacaoControllerTest {

    private SolicitacaoService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(SolicitacaoService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new SolicitacaoController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deveCriarSolicitacao() throws Exception {
        when(service.criar(any())).thenReturn(new SolicitacaoResponse(
                "1", "Descarte de eletrônico", TipoResiduo.ELETRONICO, 1,
                "Monitor antigo", StatusSolicitacao.ABERTA));

        mockMvc.perform(post("/api/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo":"Descarte de eletrônico",
                                  "tipo":"ELETRONICO",
                                  "quantidade":1,
                                  "descricao":"Monitor antigo"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.status").value("ABERTA"));
    }

    @Test
    void deveRejeitarCadastroInvalido() throws Exception {
        mockMvc.perform(post("/api/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo":"",
                                  "tipo":"ELETRONICO",
                                  "quantidade":0,
                                  "descricao":""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Dados invalidos"));

        verifyNoInteractions(service);
    }

    @Test
    void deveListarSolicitacoes() throws Exception {
        when(service.listar()).thenReturn(List.of(new SolicitacaoResponse(
                "1", "Descarte de vidro", TipoResiduo.VIDRO, 2,
                "Garrafas", StatusSolicitacao.ABERTA)));

        mockMvc.perform(get("/api/solicitacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"));
    }

    @Test
    void deveBuscarPorId() throws Exception {
        when(service.buscarPorId("1")).thenReturn(new SolicitacaoResponse(
                "1", "Descarte de vidro", TipoResiduo.VIDRO, 3,
                "Garrafas", StatusSolicitacao.ABERTA));

        mockMvc.perform(get("/api/solicitacoes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("VIDRO"));
    }

    @Test
    void deveRetornar404QuandoNaoEncontrar() throws Exception {
        when(service.buscarPorId("404")).thenThrow(new ResourceNotFoundException("Solicitacao nao encontrada"));

        mockMvc.perform(get("/api/solicitacoes/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Solicitacao nao encontrada"));
    }

    @Test
    void deveAtualizarSolicitacao() throws Exception {
        when(service.atualizar(eq("1"), any())).thenReturn(new SolicitacaoResponse(
                "1", "Descarte de plástico", TipoResiduo.PLASTICO, 4,
                "Garrafas PET", StatusSolicitacao.CONCLUIDA));

        mockMvc.perform(put("/api/solicitacoes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo":"Descarte de plástico",
                                  "tipo":"PLASTICO",
                                  "quantidade":4,
                                  "descricao":"Garrafas PET",
                                  "status":"CONCLUIDA"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));
    }

    @Test
    void deveExcluirSolicitacao() throws Exception {
        doNothing().when(service).excluir("1");

        mockMvc.perform(delete("/api/solicitacoes/1"))
                .andExpect(status().isNoContent());

        verify(service).excluir("1");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ausente", "null", "", "   "})
    void deveAceitarDescricaoOpcionalNaCriacaoEEdicao(String descricao) throws Exception {
        ObjectNode dados = new ObjectMapper().createObjectNode()
                .put("titulo", "Descarte de vidro")
                .put("tipo", "VIDRO")
                .put("quantidade", 1);
        if ("null".equals(descricao)) {
            dados.putNull("descricao");
        } else if (!"ausente".equals(descricao)) {
            dados.put("descricao", descricao);
        }

        String descricaoEsperada = "ausente".equals(descricao) || "null".equals(descricao) ? null : descricao;
        when(service.criar(any())).thenReturn(new SolicitacaoResponse(
                "1", "Descarte de vidro", TipoResiduo.VIDRO, 1, descricaoEsperada, StatusSolicitacao.ABERTA));
        mockMvc.perform(post("/api/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dados.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTA"));
        verify(service).criar(argThat(request -> Objects.equals(descricaoEsperada, request.descricao())));

        dados.put("status", "ABERTA");
        mockMvc.perform(put("/api/solicitacoes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dados.toString()))
                .andExpect(status().isOk());
        verify(service).atualizar(eq("1"),
                argThat(request -> Objects.equals(descricaoEsperada, request.descricao())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"tituloAusente", "tituloNulo", "tituloVazio", "tituloBranco",
            "tipoAusente", "tipoNulo", "quantidadeAusente", "quantidadeNula", "zero", "negativa"})
    void deveValidarCamposObrigatoriosNaCriacaoEEdicao(String caso) throws Exception {
        ObjectNode dados = new ObjectMapper().createObjectNode()
                .put("titulo", "Descarte")
                .put("tipo", "VIDRO")
                .put("quantidade", 1)
                .put("status", "ABERTA");
        switch (caso) {
            case "tituloAusente" -> dados.remove("titulo");
            case "tituloNulo" -> dados.putNull("titulo");
            case "tituloVazio" -> dados.put("titulo", "");
            case "tituloBranco" -> dados.put("titulo", "   ");
            case "tipoAusente" -> dados.remove("tipo");
            case "tipoNulo" -> dados.putNull("tipo");
            case "quantidadeAusente" -> dados.remove("quantidade");
            case "quantidadeNula" -> dados.putNull("quantidade");
            case "zero" -> dados.put("quantidade", 0);
            case "negativa" -> dados.put("quantidade", -1);
        }
        mockMvc.perform(post("/api/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dados.toString()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/solicitacoes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dados.toString()))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"PILHA_BATERIA", "METAL", "MOVEL", "OUTRO"})
    void deveRejeitarTiposRemovidos(String tipo) throws Exception {
        String dados = new ObjectMapper().createObjectNode()
                .put("titulo", "Descarte")
                .put("tipo", tipo)
                .put("quantidade", 1)
                .put("status", "ABERTA").toString();
        mockMvc.perform(post("/api/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON).content(dados))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/solicitacoes/1")
                        .contentType(MediaType.APPLICATION_JSON).content(dados))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
