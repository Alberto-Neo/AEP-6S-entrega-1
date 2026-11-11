package br.com.aep.ecocoleta.mapper;

import br.com.aep.ecocoleta.dto.UsuarioCreateRequest;
import br.com.aep.ecocoleta.dto.UsuarioResponse;
import br.com.aep.ecocoleta.dto.UsuarioUpdateRequest;
import br.com.aep.ecocoleta.model.Usuario;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Locale;

@Component
public class UsuarioMapper {

    public Usuario toModel(UsuarioCreateRequest request) {
        return new Usuario(null, request.nome().trim(), normalizarEmail(request.email()), Instant.now());
    }

    public Usuario update(Usuario atual, UsuarioUpdateRequest request) {
        return new Usuario(atual.id(), request.nome().trim(), normalizarEmail(request.email()), atual.criadoEm());
    }

    public UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.id(), usuario.nome(), usuario.email(), usuario.criadoEm());
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
