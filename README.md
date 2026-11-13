# EcoColeta — AEP Engenharia de Software

O EcoColeta tem como objetivo auxiliar no registro e organização de solicitações para descarte de resíduos, relacionando usuários, pontos de coleta e os materiais que serão descartados.

O projeto está relacionado ao **ODS 12 — Consumo e Produção Responsáveis**, incentivando práticas de descarte e consumo mais conscientes.

## Evolução do projeto

A aplicação foi desenvolvida de forma incremental.

### Primeira entrega

A primeira versão possuía:

- uma única coleção MongoDB: `solicitacoes`;
- documentos simples e homogêneos;
- operações básicas de CRUD;
- interface web simples;
- testes automatizados;
- cobertura de testes com JaCoCo.

### Segunda entrega

Na segunda etapa, a aplicação foi evoluída para possuir:

- múltiplas coleções MongoDB;
- relacionamento entre coleções;
- documentos aninhados;
- lista de subdocumentos;
- gerenciamento de usuários;
- pontos de coleta pré-definidos;
- interface adaptada à nova modelagem;
- testes automatizados atualizados;
- documentação técnica da solução.

## Tecnologias utilizadas

- Java 21
- Spring Boot
- Spring Data MongoDB
- MongoDB
- Maven
- JUnit
- JaCoCo
- HTML
- CSS
- JavaScript
- Git e GitHub

## Estrutura do banco de dados

A versão atual utiliza três coleções principais.

### `usuarios`

Representa os usuários relacionados às solicitações.

Exemplo:

```json
{
  "id": "usuario123",
  "nome": "Teste",
  "email": "Teste@email.com",
  "criadoEm": "2026-11-12T10:00:00"
}
```

### `pontos_coleta`

Contém os pontos de coleta disponíveis na aplicação.

Os pontos são inicializados automaticamente e podem ser consultados pela API.

Exemplo:

```json
{
  "id": "ponto123",
  "nome": "Ponto de Coleta Central",
  "endereco": "Av. Exemplo, 100"
}
```

### `solicitacoes`

Representa as solicitações de descarte.

Na segunda entrega essa coleção passou a possuir relacionamentos com usuários e pontos de coleta, além de estruturas mais complexas.

Exemplo simplificado:

```json
{
  "id": "solicitacao123",
  "usuarioId": "usuario123",
  "pontoColetaId": "ponto123",
  "titulo": "Descarte de eletrônicos",
  "status": "ABERTA",
  "endereco": {
    "rua": "Rua A",
    "numero": "100",
    "bairro": "Centro",
    "cidade": "Maringá"
  },
  "itens": [
    {
      "tipo": "ELETRONICO",
      "quantidade": 2
    }
  ]
}
```

Dessa forma, a aplicação demonstra:

- múltiplas coleções;
- relacionamento entre coleções;
- objeto aninhado (`Endereco`);
- lista de subdocumentos (`ItemResiduo`).

## Organização da aplicação

O backend utiliza separação de responsabilidades por camadas:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
MongoDB
```

Também são utilizados:

- Models para representar os documentos;
- DTOs para entrada e saída de dados;
- Mappers para conversão entre objetos;
- tratamento centralizado de exceções;
- testes automatizados das principais funcionalidades.

## Principais endpoints

### Usuários

```text
POST   /api/usuarios
GET    /api/usuarios
GET    /api/usuarios/{id}
PUT    /api/usuarios/{id}
DELETE /api/usuarios/{id}
```

### Pontos de coleta

```text
GET /api/pontos
GET /api/pontos/{id}
```

Os pontos de coleta são pré-definidos pela aplicação e não possuem cadastro manual pela API.

### Solicitações

```text
POST   /api/solicitacoes
GET    /api/solicitacoes
GET    /api/solicitacoes/{id}
PUT    /api/solicitacoes/{id}
DELETE /api/solicitacoes/{id}
```

## Como executar

### Pré-requisitos

É necessário possuir:

- Java 21;
- Maven;
- MongoDB instalado e em execução.

### 1. Iniciar o MongoDB

No Windows, caso o MongoDB esteja instalado como serviço:

```powershell
Start-Service MongoDB
```

Para verificar:

```powershell
Get-Service MongoDB
```

O serviço deve aparecer como `Running`.

### 2. Executar a aplicação

A aplicação pode ser iniciada pelo método `main` da classe:

```text
EcoColetaApplication.java
```

ou pelo Maven:

```powershell
mvn spring-boot:run
```

### 3. Abrir a aplicação

Com o backend iniciado:

```text
http://localhost:8080
```

## Testes automatizados

Os testes podem ser executados com:

```powershell
mvn clean verify
```

Esse comando:

- compila o projeto;
- executa os testes automatizados;
- valida a aplicação;
- gera o relatório de cobertura do JaCoCo.

Na última validação da segunda entrega foram executados **100 testes sem falhas**.

## Cobertura de testes

O projeto utiliza JaCoCo para medir a cobertura.

Após executar:

```powershell
mvn clean verify
```

o relatório pode ser encontrado em:

```text
target/site/jacoco/index.html
```

No Windows:

```powershell
start target\site\jacoco\index.html
```

A última validação apresentou **100% das linhas consideradas pelo JaCoCo**, mantendo as exclusões configuradas no projeto.

A AEP exige cobertura mínima de 70%.

## Versionamento

O desenvolvimento foi realizado de forma incremental no GitHub.

A primeira entrega possuía apenas a estrutura simples de solicitações.

Na segunda entrega foram adicionados gradualmente:

1. gerenciamento de usuários;
2. pontos de coleta pré-definidos;
3. relacionamento entre coleções;
4. estruturas complexas nas solicitações;
5. atualização da interface;
6. atualização dos testes;
7. documentação final.

O histórico de commits permite acompanhar essa evolução.

A versão final da segunda entrega será identificada pela tag:

```text
entrega-2
```

## Estrutura resumida

```text
src/
├── main/
│   ├── java/
│   │   └── ...
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── exception/
│   │       ├── mapper/
│   │       ├── model/
│   │       ├── repository/
│   │       └── service/
│   └── resources/
│       ├── static/
│       └── application.properties
└── test/
    └── java/
```

## ODS

**ODS 12 — Consumo e Produção Responsáveis**
O EcoColeta busca contribuir com a organização do descarte de resíduos e com a conscientização sobre práticas de consumo e descarte mais responsáveis.

## Vídeo da segunda entrega

Link: 

```text
https://youtu.be/0HFjapEBZOo
```
