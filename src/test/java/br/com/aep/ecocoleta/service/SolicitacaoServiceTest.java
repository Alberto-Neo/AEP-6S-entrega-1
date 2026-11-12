package br.com.aep.ecocoleta.service;

import br.com.aep.ecocoleta.dto.EnderecoRequest;
import br.com.aep.ecocoleta.dto.ItemResiduoRequest;
import br.com.aep.ecocoleta.dto.SolicitacaoCreateRequest;
import br.com.aep.ecocoleta.dto.SolicitacaoUpdateRequest;
import br.com.aep.ecocoleta.exception.ResourceNotFoundException;
import br.com.aep.ecocoleta.mapper.SolicitacaoMapper;
import br.com.aep.ecocoleta.model.Solicitacao;
import br.com.aep.ecocoleta.model.StatusSolicitacao;
import br.com.aep.ecocoleta.model.TipoResiduo;
import br.com.aep.ecocoleta.repository.SolicitacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitacaoServiceTest {

    @Mock
    private SolicitacaoRepository repository;

    private SolicitacaoService service;

    @BeforeEach
    void setUp() {
        service = new SolicitacaoService(repository, mock(UsuarioService.class),
                mock(PontoColetaService.class), new SolicitacaoMapper());
    }

    @Test
    void deveCriarSolicitacaoComStatusAberta() {
        var request = new SolicitacaoCreateRequest("Descarte de eletrônico", "Monitor antigo",
                "u1", "p1", new EnderecoRequest("Rua A", "10", "Centro", "Maringa"), List.of(new ItemResiduoRequest(TipoResiduo.ELETRONICO,
                1)));

        when(repository.save(any(Solicitacao.class))).thenAnswer(invocacao -> {
            Solicitacao s = invocacao.getArgument(0);
            return new Solicitacao("1", s.titulo(), s.tipo(), s.quantidade(),
                    s.descricao(), s.status(), s.usuarioId(), s.pontoColetaId(), s.endereco(), s.itens(),
                    s.criadoEm());
        });

        var response = service.criar(request);

        assertEquals("1", response.id());
        assertEquals(StatusSolicitacao.ABERTA, response.status());
        verify(repository).save(any(Solicitacao.class));
    }

    @Test
    void deveListarSolicitacoes() {
        when(repository.findAll()).thenReturn(List.of(
                new Solicitacao("1", "Descarte de vidro", TipoResiduo.VIDRO, 2,
                        "Garrafas", StatusSolicitacao.ABERTA, "u1", "p1", null, null, null)
        ));

        var resultado = service.listar();

        assertEquals(1, resultado.size());
        assertEquals(TipoResiduo.VIDRO, resultado.getFirst().tipo());
    }

    @Test
    void deveBuscarSolicitacaoPorId() {
        when(repository.findById("1")).thenReturn(Optional.of(
                new Solicitacao("1", "Descarte de vidro", TipoResiduo.VIDRO, 3,
                        "Garrafas", StatusSolicitacao.ABERTA, "u1", "p1", null, null, null)
        ));

        var resultado = service.buscarPorId("1");

        assertEquals("1", resultado.id());
        assertEquals(3, resultado.quantidade());
    }

    @Test
    void deveLancarErroQuandoSolicitacaoNaoExiste() {
        when(repository.findById("404")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.buscarPorId("404"));
    }

    @Test
    void deveAtualizarSolicitacao() {
        when(repository.findById("1")).thenReturn(Optional.of(
                new Solicitacao("1", "Antiga", TipoResiduo.PAPEL_PAPELAO, 1, "Antiga",
                        StatusSolicitacao.ABERTA, "u1", "p1", null, null, null)
        ));
        when(repository.save(any(Solicitacao.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        var request = new SolicitacaoUpdateRequest("Descarte de plástico", "Garrafas PET",
                StatusSolicitacao.CONCLUIDA, "u1", "p1", new EnderecoRequest("Rua A", "10", "Centro",
                "Maringa"), List.of(new ItemResiduoRequest(TipoResiduo.PLASTICO, 5)));

        var resultado = service.atualizar("1", request);

        assertEquals("Descarte de plástico", resultado.titulo());
        assertEquals(StatusSolicitacao.CONCLUIDA, resultado.status());
    }

    @Test
    void deveExcluirSolicitacao() {
        Solicitacao solicitacao = new Solicitacao("1", "Descarte", TipoResiduo.PAPEL_PAPELAO,
                1, "Item", StatusSolicitacao.ABERTA, "u1", "p1", null, null, null);
        when(repository.findById("1")).thenReturn(Optional.of(solicitacao));

        service.excluir("1");

        verify(repository).delete(solicitacao);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void devePersistirSemDescricaoComStatusAberta(String descricao) {
        when(repository.save(any(Solicitacao.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        var resposta = service.criar(new SolicitacaoCreateRequest("Descarte de vidro",
                descricao, "u1", "p1", new EnderecoRequest("Rua A", "10", "Centro", "Maringa"),
                List.of(new ItemResiduoRequest(TipoResiduo.VIDRO, 1))));
        assertEquals(descricao, resposta.descricao());
        assertEquals(StatusSolicitacao.ABERTA, resposta.status());
        verify(repository).save(argThat(s -> Objects.equals(descricao, s.descricao())
                && s.status() == StatusSolicitacao.ABERTA));
    }
}
