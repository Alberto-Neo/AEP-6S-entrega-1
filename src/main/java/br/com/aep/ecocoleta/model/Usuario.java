package br.com.aep.ecocoleta.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "usuarios")
public record Usuario(@Id String id, String nome, String email, Instant criadoEm) {
}
