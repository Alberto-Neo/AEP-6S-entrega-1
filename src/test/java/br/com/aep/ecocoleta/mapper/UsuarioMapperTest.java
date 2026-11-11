package br.com.aep.ecocoleta.mapper;

import br.com.aep.ecocoleta.dto.UsuarioCreateRequest;
import br.com.aep.ecocoleta.dto.UsuarioUpdateRequest;
import br.com.aep.ecocoleta.model.Usuario;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioMapperTest {

    private final UsuarioMapper mapper = new UsuarioMapper();

    @Test
    void deveNormalizarCadastroEDefinirDataDeCriacao() {
        Instant antes = Instant.now();
        Usuario usuario = mapper.toModel(new UsuarioCreateRequest(" Ana Silva ", " ANA@EXEMPLO.COM "));
        assertNull(usuario.id());
        assertEquals("Ana Silva", usuario.nome());
        assertEquals("ana@exemplo.com", usuario.email());
        assertFalse(usuario.criadoEm().isBefore(antes));
        assertFalse(usuario.criadoEm().isAfter(Instant.now()));
    }

    @Test
    void devePreservarIdEDataNaAtualizacao() {
        Usuario atual = new Usuario("u1", "Ana", "ana@exemplo.com", Instant.parse("2026-01-01T00:00:00Z"));
        Usuario atualizado = mapper.update(atual, new UsuarioUpdateRequest(" Ana Souza ", " NOVO@EXEMPLO.COM "));
        assertEquals(atual.id(), atualizado.id());
        assertEquals(atual.criadoEm(), atualizado.criadoEm());
        assertEquals("Ana Souza", atualizado.nome());
        assertEquals("novo@exemplo.com", atualizado.email());
        assertEquals("Ana", atual.nome());
    }

    @Test
    void deveRetornarTodosOsDadosPublicos() {
        Usuario usuario = new Usuario("u1", "Ana", "ana@exemplo.com", Instant.parse("2026-01-01T00:00:00Z"));
        var response = mapper.toResponse(usuario);
        assertEquals(usuario.id(), response.id());
        assertEquals(usuario.nome(), response.nome());
        assertEquals(usuario.email(), response.email());
        assertEquals(usuario.criadoEm(), response.criadoEm());
    }
}
