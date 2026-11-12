package br.com.aep.ecocoleta.controller;

import br.com.aep.ecocoleta.exception.GlobalExceptionHandler;
import br.com.aep.ecocoleta.mapper.SolicitacaoMapper;
import br.com.aep.ecocoleta.model.Solicitacao;
import br.com.aep.ecocoleta.repository.SolicitacaoRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SolicitacaoEstruturaComplexaTest {

    private MockMvc mvc;
    private SolicitacaoRepository repository;
    private final AtomicReference<Solicitacao> salva = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        repository = mock(SolicitacaoRepository.class);
        var service = new SolicitacaoService(repository, mock(UsuarioService.class),
                mock(PontoColetaService.class), new SolicitacaoMapper());
        mvc = MockMvcBuilders.standaloneSetup(new SolicitacaoController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        when(repository.save(any())).thenAnswer(invocation -> {
            Solicitacao s = invocation.getArgument(0);
            var persistida = new Solicitacao("s1", s.titulo(), s.tipo(), s.quantidade(), s.descricao(),
                    s.status(), s.usuarioId(), s.pontoColetaId(), s.endereco(), s.itens(), s.criadoEm());
            salva.set(persistida);
            return persistida;
        });
    }

    private ObjectNode dados() throws Exception {
        return (ObjectNode) new ObjectMapper().readTree("""
                {"titulo":"Coleta", "usuarioId":"u1", "pontoColetaId":"p1", "status":"ABERTA",
                 "endereco":{"rua":" Rua A ","numero":" 10 ","bairro":" Centro ","cidade":" Maringa "},
                 "itens":[{"tipo":"VIDRO","quantidade":2},{"tipo":"PLASTICO","quantidade":3}]}
                """);
    }

    @Test
    void deveCriarConsultarListarEAtualizarEstruturaPreservandoIdentidadeEData() throws Exception {
        mvc.perform(post("/api/solicitacoes").contentType(MediaType.APPLICATION_JSON).content(dados().toString()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value("s1"))
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.endereco.rua").value("Rua A"))
                .andExpect(jsonPath("$.endereco.numero").value("10"))
                .andExpect(jsonPath("$.endereco.bairro").value("Centro"))
                .andExpect(jsonPath("$.endereco.cidade").value("Maringa"))
                .andExpect(jsonPath("$.itens.length()").value(2))
                .andExpect(jsonPath("$.itens[0].tipo").value("VIDRO"))
                .andExpect(jsonPath("$.itens[1].quantidade").value(3))
                .andExpect(jsonPath("$.criadoEm").exists());
        var original = salva.get();
        assertNotNull(original.criadoEm());
        assertNull(original.tipo());
        assertNull(original.quantidade());
        when(repository.findById("s1")).thenReturn(Optional.of(original));
        when(repository.findAll()).thenReturn(List.of(original));
        mvc.perform(get("/api/solicitacoes/s1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[1].tipo").value("PLASTICO"));
        mvc.perform(get("/api/solicitacoes")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].endereco.cidade").value("Maringa"))
                .andExpect(jsonPath("$[0].itens.length()").value(2));
        var atualizado = dados().put("status", "CONCLUIDA");
        ((ObjectNode) atualizado.get("endereco")).put("cidade", "Londrina");
        atualizado.putArray("itens").addObject().put("tipo", "ELETRONICO").put("quantidade", 7);
        mvc.perform(put("/api/solicitacoes/s1").contentType(MediaType.APPLICATION_JSON).content(atualizado.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value("s1"))
                .andExpect(jsonPath("$.usuarioId").value("u1"))
                .andExpect(jsonPath("$.pontoColetaId").value("p1"))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.endereco.cidade").value("Londrina"))
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.itens[0].tipo").value("ELETRONICO"))
                .andExpect(jsonPath("$.itens[0].quantidade").value(7));
        assertEquals(original.criadoEm(), salva.get().criadoEm());
    }

    @ParameterizedTest
    @ValueSource(strings = {"enderecoAusente", "enderecoNulo", "itensAusentes", "itensNulos", "itensVazios", "itemNulo"})
    void deveRejeitarEstruturaAusenteOuVazia(String caso) throws Exception {
        var body = dados();
        String campo;
        switch (caso) {
            case "enderecoAusente" -> { body.remove("endereco"); campo = "endereco"; }
            case "enderecoNulo" -> { body.putNull("endereco"); campo = "endereco"; }
            case "itensAusentes" -> { body.remove("itens"); campo = "itens"; }
            case "itensNulos" -> { body.putNull("itens"); campo = "itens"; }
            case "itensVazios" -> { body.putArray("itens"); campo = "itens"; }
            default -> { body.putArray("itens").addNull(); campo = "itens[0]"; }
        }
        rejeitar(body, campo);
    }

    @ParameterizedTest
    @ValueSource(strings = {"rua", "numero", "bairro", "cidade"})
    void deveValidarCadaCampoDoEndereco(String campo) throws Exception {
        for (String valor : new String[]{null, "", "   ", "ausente"}) {
            var body = dados();
            var endereco = (ObjectNode) body.get("endereco");
            if ("ausente".equals(valor)) endereco.remove(campo);
            else if (valor == null) endereco.putNull(campo);
            else endereco.put(campo, valor);
            rejeitar(body, "endereco." + campo);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"tipoAusente", "tipoNulo", "quantidadeAusente", "quantidadeNula", "zero", "negativa"})
    void deveValidarTambemItensAposOPrimeiro(String caso) throws Exception {
        var body = dados();
        var item = (ObjectNode) body.get("itens").get(1);
        String campo = caso.startsWith("tipo") ? "tipo" : "quantidade";
        switch (caso) {
            case "tipoAusente", "quantidadeAusente" -> item.remove(campo);
            case "tipoNulo", "quantidadeNula" -> item.putNull(campo);
            case "zero" -> item.put(campo, 0);
            default -> item.put(campo, -1);
        }
        rejeitar(body, "itens[1]." + campo);
    }

    private void rejeitar(ObjectNode body, String campo) throws Exception {
        for (var request : List.of(post("/api/solicitacoes"), put("/api/solicitacoes/s1"))) {
            mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(body.toString()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campos['" + campo + "']").exists());
        }
        verify(repository, never()).save(any());
        verify(repository, never()).findById(any());
    }
}
