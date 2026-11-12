package br.com.aep.ecocoleta.service;

import br.com.aep.ecocoleta.dto.EnderecoRequest;
import br.com.aep.ecocoleta.dto.ItemResiduoRequest;
import br.com.aep.ecocoleta.dto.SolicitacaoCreateRequest;
import br.com.aep.ecocoleta.dto.SolicitacaoUpdateRequest;
import br.com.aep.ecocoleta.exception.BusinessException;
import br.com.aep.ecocoleta.mapper.SolicitacaoMapper;
import br.com.aep.ecocoleta.model.Solicitacao;
import br.com.aep.ecocoleta.model.StatusSolicitacao;
import br.com.aep.ecocoleta.model.TipoResiduo;
import br.com.aep.ecocoleta.repository.SolicitacaoRepository;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
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
        var conversions = MongoCustomConversions.create(config -> {});
        context.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
        context.afterPropertiesSet();
        var converter = new MappingMongoConverter(NoOpDbRefResolver.INSTANCE, context);
        converter.setCustomConversions(conversions);
        converter.afterPropertiesSet();
        var documento = new Document("_id", "s1").append("titulo", "Antigo").append("tipo", "VIDRO")
                .append("quantidade", 2).append("status", "ABERTA");
        var antigo = converter.read(Solicitacao.class, documento);
        assertNull(antigo.endereco());
        assertNull(antigo.itens());
        assertNull(antigo.criadoEm());
        assertNull(antigo.usuarioId());
        assertNull(antigo.pontoColetaId());
        var repository = mock(SolicitacaoRepository.class);
        var service = new SolicitacaoService(repository, mock(UsuarioService.class),
                mock(PontoColetaService.class), new SolicitacaoMapper());
        when(repository.findById("s1")).thenReturn(Optional.of(antigo));
        when(repository.findAll()).thenReturn(List.of(antigo));
        assertEquals("Antigo", service.buscarPorId("s1").titulo());
        assertNull(service.listar().getFirst().usuarioId());
        assertTrue(service.buscarPorId("s1").itens().isEmpty());
        assertEquals(TipoResiduo.VIDRO, service.buscarPorId("s1").tipo());
        assertEquals(2, service.buscarPorId("s1").quantidade());
        verify(repository, never()).save(any());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        var atualizado = service.atualizar("s1", new SolicitacaoUpdateRequest("Antigo",
                null, StatusSolicitacao.ABERTA, "u1", "p1", new EnderecoRequest("Rua A", "10",
                "Centro", "Maringa"), List.of(new ItemResiduoRequest(TipoResiduo.VIDRO, 2))));
        assertEquals("s1", atualizado.id());
        assertNull(atualizado.criadoEm());
        assertEquals("Rua A", atualizado.endereco().rua());
        assertEquals(2, atualizado.itens().getFirst().quantidade());
        assertEquals("u1", atualizado.usuarioId());
        assertEquals("p1", atualizado.pontoColetaId());
        var persistido = new Document();
        converter.write(new Solicitacao("s1", "Antigo", TipoResiduo.VIDRO, 2,
                null, StatusSolicitacao.ABERTA, "u1", "p1", null, null, null), persistido);
        assertEquals("u1", persistido.getString("usuarioId"));
        assertEquals("p1", persistido.getString("pontoColetaId"));
    }

    @Test
    void deveGravarEnderecoEItensComoDocumentosBsonAninhados() throws Exception {
        var context = new MongoMappingContext();
        var conversions = MongoCustomConversions.create(config -> {});
        context.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
        context.afterPropertiesSet();
        var converter = new MappingMongoConverter(NoOpDbRefResolver.INSTANCE, context);
        converter.setCustomConversions(conversions);
        converter.afterPropertiesSet();
        var mapper = new SolicitacaoMapper();
        var nova = mapper.toModel(new SolicitacaoCreateRequest("Titulo", null, "u1", "p1",
                new EnderecoRequest("Rua A", "10", "Centro", "Maringa"),
                List.of(new ItemResiduoRequest(TipoResiduo.VIDRO, 2),
                        new ItemResiduoRequest(TipoResiduo.PLASTICO, 3))));
        var documento = new Document();
        converter.write(nova, documento);
        assertEquals("Rua A", documento.get("endereco", Document.class).getString("rua"));
        var itens = documento.getList("itens", Document.class);
        assertEquals(2, itens.size());
        assertEquals("VIDRO", itens.getFirst().getString("tipo"));
        assertEquals(3, itens.get(1).getInteger("quantidade"));
        assertEquals("u1", documento.getString("usuarioId"));
        assertEquals("p1", documento.getString("pontoColetaId"));
        var relida = converter.read(Solicitacao.class, documento);
        assertEquals(nova.endereco(), relida.endereco());
        assertEquals(nova.itens(), relida.itens());
        assertEquals(nova.criadoEm().toEpochMilli(), relida.criadoEm().toEpochMilli());
    }

    @Test
    void deveExigirIdsTambemAoChamarServiceDiretamente() {
        var repository = mock(SolicitacaoRepository.class);
        var usuarios = mock(UsuarioService.class);
        var pontos = mock(PontoColetaService.class);
        var service = new SolicitacaoService(repository, usuarios, pontos, new SolicitacaoMapper());
        when(repository.findById("s1")).thenReturn(Optional.of(new Solicitacao("s1",
                "Titulo", TipoResiduo.VIDRO, 1, null, StatusSolicitacao.ABERTA, null, null, null,
                null, null)));
        for (String valor : new String[]{null, "", "   "}) {
            assertThrows(BusinessException.class, () -> service.criar(new SolicitacaoCreateRequest("Titulo",
                    null, valor, "p1", new EnderecoRequest("Rua A", "10", "Centro", "Maringa"), List.of(new ItemResiduoRequest(TipoResiduo.VIDRO,
                    1)))));
            assertThrows(BusinessException.class, () -> service.criar(new SolicitacaoCreateRequest("Titulo",
                    null, "u1", valor, new EnderecoRequest("Rua A", "10", "Centro", "Maringa"), List.of(new ItemResiduoRequest(TipoResiduo.VIDRO,
                    1)))));
            assertThrows(BusinessException.class, () -> service.atualizar("s1",
                    new SolicitacaoUpdateRequest("Titulo", null, StatusSolicitacao.ABERTA, valor,
                    "p1", new EnderecoRequest("Rua A", "10", "Centro", "Maringa"), List.of(new ItemResiduoRequest(TipoResiduo.VIDRO,
                    1)))));
            assertThrows(BusinessException.class, () -> service.atualizar("s1",
                    new SolicitacaoUpdateRequest("Titulo", null, StatusSolicitacao.ABERTA, "u1",
                    valor, new EnderecoRequest("Rua A", "10", "Centro", "Maringa"), List.of(new ItemResiduoRequest(TipoResiduo.VIDRO,
                    1)))));
        }
        verify(repository, never()).save(any());
        verifyNoInteractions(usuarios, pontos);
    }
}
