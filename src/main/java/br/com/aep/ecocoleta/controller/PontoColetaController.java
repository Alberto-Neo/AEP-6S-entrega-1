package br.com.aep.ecocoleta.controller;

import br.com.aep.ecocoleta.dto.PontoColetaResponse;
import br.com.aep.ecocoleta.service.PontoColetaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pontos")
public class PontoColetaController {

    private final PontoColetaService service;

    public PontoColetaController(PontoColetaService service) {
        this.service = service;
    }

    @GetMapping
    public List<PontoColetaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public PontoColetaResponse buscarPorId(@PathVariable String id) {
        return service.buscarPorId(id);
    }
}
