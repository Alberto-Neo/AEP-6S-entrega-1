package br.com.aep.ecocoleta.service;

import br.com.aep.ecocoleta.dto.UsuarioCreateRequest;
import br.com.aep.ecocoleta.dto.UsuarioUpdateRequest;
import br.com.aep.ecocoleta.exception.BusinessException;
import br.com.aep.ecocoleta.exception.ResourceNotFoundException;
import br.com.aep.ecocoleta.mapper.UsuarioMapper;
import br.com.aep.ecocoleta.model.Usuario;
import br.com.aep.ecocoleta.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;
    private UsuarioService service;
    private final Usuario existente = new Usuario("u1", "Ana", "ana@exemplo.com", Instant.parse("2026-01-01T00:00:00Z"));

    @BeforeEach
    void setUp() {
        service = new UsuarioService(repository, new UsuarioMapper());
    }

    @Test
    void deveCriarUsuarioComDadosNormalizados() {
        when(repository.save(any())).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            return new Usuario("u1", usuario.nome(), usuario.email(), usuario.criadoEm());
        });
        var response = service.criar(new UsuarioCreateRequest(" Ana ", " ANA@EXEMPLO.COM "));
        assertEquals("u1", response.id());
        assertEquals("Ana", response.nome());
        assertEquals("ana@exemplo.com", response.email());
        assertNotNull(response.criadoEm());
        verify(repository).existsByEmailIgnoreCase("ana@exemplo.com");
    }

    @Test
    void deveRejeitarCadastroComEmailDuplicado() {
        when(repository.existsByEmailIgnoreCase("ana@exemplo.com")).thenReturn(true);
        assertThrows(BusinessException.class,
                () -> service.criar(new UsuarioCreateRequest("Outra", "ANA@EXEMPLO.COM")));
        verify(repository, never()).save(any());
    }

    @Test
    void deveListarUsuarios() {
        when(repository.findAll()).thenReturn(List.of(existente));
        var resultado = service.listar();
        assertEquals(1, resultado.size());
        assertEquals(existente.id(), resultado.getFirst().id());
    }

    @Test
    void deveRetornarListaVaziaSemUsuarios() {
        when(repository.findAll()).thenReturn(List.of());
        assertTrue(service.listar().isEmpty());
    }

    @Test
    void deveBuscarUsuarioPorId() {
        when(repository.findById("u1")).thenReturn(Optional.of(existente));
        assertEquals(existente.email(), service.buscarPorId("u1").email());
    }

    @Test
    void deveAtualizarPreservandoIdEData() {
        when(repository.findById("u1")).thenReturn(Optional.of(existente));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var resultado = service.atualizar("u1", new UsuarioUpdateRequest(" Nome Novo ", " NOVO@EXEMPLO.COM "));
        assertEquals("u1", resultado.id());
        assertEquals(existente.criadoEm(), resultado.criadoEm());
        assertEquals("Nome Novo", resultado.nome());
        assertEquals("novo@exemplo.com", resultado.email());
        verify(repository).existsByEmailIgnoreCaseAndIdNot("novo@exemplo.com", "u1");
    }

    @Test
    void devePermitirManterOProprioEmail() {
        when(repository.findById("u1")).thenReturn(Optional.of(existente));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals(existente.email(), service.atualizar("u1", new UsuarioUpdateRequest("Ana", "ANA@EXEMPLO.COM")).email());
        verify(repository).existsByEmailIgnoreCaseAndIdNot("ana@exemplo.com", "u1");
    }

    @Test
    void deveRejeitarEmailDeOutroUsuarioNaAtualizacao() {
        when(repository.findById("u1")).thenReturn(Optional.of(existente));
        when(repository.existsByEmailIgnoreCaseAndIdNot("outro@exemplo.com", "u1")).thenReturn(true);
        assertThrows(BusinessException.class,
                () -> service.atualizar("u1", new UsuarioUpdateRequest("Ana", "OUTRO@EXEMPLO.COM")));
        verify(repository, never()).save(any());
    }

    @Test
    void deveExcluirUsuarioExistente() {
        when(repository.findById("u1")).thenReturn(Optional.of(existente));
        service.excluir("u1");
        verify(repository).delete(existente);
    }

    @ParameterizedTest
    @ValueSource(strings = {"buscar", "atualizar", "excluir"})
    void deveRejeitarOperacoesComUsuarioInexistente(String operacao) {
        when(repository.findById("ausente")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            switch (operacao) {
                case "buscar" -> service.buscarPorId("ausente");
                case "atualizar" -> service.atualizar("ausente", new UsuarioUpdateRequest("Ana", "ana@exemplo.com"));
                case "excluir" -> service.excluir("ausente");
                default -> throw new IllegalArgumentException(operacao);
            }
        });
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any());
    }
}
