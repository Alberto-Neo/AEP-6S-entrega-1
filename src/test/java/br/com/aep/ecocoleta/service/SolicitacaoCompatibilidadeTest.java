package br.com.aep.ecocoleta.service;

import br.com.aep.ecocoleta.dto.SolicitacaoCreateRequest;
import br.com.aep.ecocoleta.dto.SolicitacaoUpdateRequest;
import br.com.aep.ecocoleta.exception.BusinessException;
import br.com.aep.ecocoleta.model.Solicitacao;
import br.com.aep.ecocoleta.model.StatusSolicitacao;
import br.com.aep.ecocoleta.model.TipoResiduo;
import br.com.aep.ecocoleta.repository.SolicitacaoRepository;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SolicitacaoCompatibilidadeTest {

    @Test
    void deveLerDocumentoAntigoSemInventarVinculosEPermitirVincularNaEdicao() throws Exception {
        var context = new MongoMappingContext();
        context.afterPropertiesSet();
        var converter = new MappingMongoConverter(NoOpDbRefResolver.INSTANCE, context);
        converter.afterPropertiesSet();
        var documento = new Document("_id", "s1").append("titulo", "Antigo").append("tipo", "VIDRO")
                .append("quantidade", 2).append("status", "ABERTA");
        var antigo = converter.read(Solicitacao.class, documento);
        assertNull(antigo.usuarioId());
        assertNull(antigo.pontoColetaId());
        var repository = mock(SolicitacaoRepository.class);
        var service = new SolicitacaoService(repository, mock(UsuarioService.class), mock(PontoColetaService.class));
        when(repository.findById("s1")).thenReturn(Optional.of(antigo));
        when(repository.findAll()).thenReturn(List.of(antigo));
        assertEquals("Antigo", service.buscarPorId("s1").titulo());
        assertNull(service.listar().getFirst().usuarioId());
        verify(repository, never()).save(any());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        var atualizado = service.atualizar("s1", new SolicitacaoUpdateRequest("Antigo", TipoResiduo.VIDRO, 2,
                null, StatusSolicitacao.ABERTA, "u1", "p1"));
        assertEquals("s1", atualizado.id());
        assertEquals("u1", atualizado.usuarioId());
        assertEquals("p1", atualizado.pontoColetaId());
        var persistido = new Document();
        converter.write(new Solicitacao("s1", "Antigo", TipoResiduo.VIDRO, 2, null,
                StatusSolicitacao.ABERTA, "u1", "p1"), persistido);
        assertEquals("u1", persistido.getString("usuarioId"));
        assertEquals("p1", persistido.getString("pontoColetaId"));
    }

    @Test
    void deveExigirIdsTambemAoChamarServiceDiretamente() {
        var repository = mock(SolicitacaoRepository.class);
        var usuarios = mock(UsuarioService.class);
        var pontos = mock(PontoColetaService.class);
        var service = new SolicitacaoService(repository, usuarios, pontos);
        when(repository.findById("s1")).thenReturn(Optional.of(new Solicitacao("s1", "Titulo", TipoResiduo.VIDRO,
                1, null, StatusSolicitacao.ABERTA, null, null)));
        for (String valor : new String[]{null, "", "   "}) {
            assertThrows(BusinessException.class, () -> service.criar(new SolicitacaoCreateRequest("Titulo", TipoResiduo.VIDRO,
                    1, null, valor, "p1")));
            assertThrows(BusinessException.class, () -> service.criar(new SolicitacaoCreateRequest("Titulo", TipoResiduo.VIDRO,
                    1, null, "u1", valor)));
            assertThrows(BusinessException.class, () -> service.atualizar("s1", new SolicitacaoUpdateRequest("Titulo", TipoResiduo.VIDRO,
                    1, null, StatusSolicitacao.ABERTA, valor, "p1")));
            assertThrows(BusinessException.class, () -> service.atualizar("s1", new SolicitacaoUpdateRequest("Titulo", TipoResiduo.VIDRO,
                    1, null, StatusSolicitacao.ABERTA, "u1", valor)));
        }
        verify(repository, never()).save(any());
        verifyNoInteractions(usuarios, pontos);
    }
}
