# EcoColeta - AEP 2026 - Primeira Entrega

## Problema

O descarte inadequado de resíduos pode gerar impactos ambientais e dificultar a destinação correta de materiais. A PoC EcoColeta demonstra uma forma simples de registrar e gerenciar solicitações de descarte.

## ODS

O projeto está alinhado ao **ODS 12 - Consumo e Produção Responsáveis**, por incentivar a organização do descarte de resíduos.

## Escopo desta versão

Esta é propositalmente uma versão inicial e simples da PoC:

- uma única coleção NoSQL: `solicitacoes`;
- documentos homogêneos e de estrutura simples;
- operações básicas de CRUD;
- interface web simples;
- testes automatizados;
- cobertura mínima de 70% verificada pelo JaCoCo.

### Documento armazenado

```json
{
  "titulo": "Descarte de eletrônico",
  "tipo": "ELETRONICO",
  "quantidade": 1,
  "descricao": "Monitor antigo para descarte",
  "status": "ABERTA"
}
```

## Tecnologias

- Java 21
- Spring Boot
- Spring Web
- Spring Data MongoDB
- Jakarta Validation
- MongoDB
- HTML, CSS e JavaScript
- JUnit 5, Mockito e MockMvc
- JaCoCo
- Maven

## Requisitos locais

- JDK 21
- Maven
- MongoDB em execução na porta padrão `27017`

O banco utilizado é:

```text
ecocoleta_entrega1
```

## Como executar

1. Inicie o MongoDB local.
2. Na raiz do projeto, execute:

```powershell
mvn spring-boot:run
```

3. Abra no navegador:

```text
http://localhost:8080
```

## API

| Método | Endpoint | Ação |
|---|---|---|
| POST | `/api/solicitacoes` | Criar solicitação |
| GET | `/api/solicitacoes` | Listar solicitações |
| GET | `/api/solicitacoes/{id}` | Buscar por ID |
| PUT | `/api/solicitacoes/{id}` | Atualizar |
| DELETE | `/api/solicitacoes/{id}` | Excluir |

## Testes e cobertura

Execute:

```powershell
mvn clean verify
```

O JaCoCo está configurado para exigir no mínimo 70% de cobertura de linhas no código considerado pela medição.

Após a execução, o relatório HTML fica em:

```text
target/site/jacoco/index.html
```

## Estrutura principal

```text
src/main/java/br/com/aep/ecocoleta
├── controller
├── dto
├── exception
├── model
├── repository
└── service
```

## Evolução futura

Funcionalidades como múltiplas coleções, usuários, pontos de coleta, relacionamentos e documentos aninhados ficam reservadas para a evolução posterior da PoC.
