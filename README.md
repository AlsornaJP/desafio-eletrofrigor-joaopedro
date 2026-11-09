# Desafio Eletrofrigor — "O pedido que desapareceu"

Solução da etapa 2 do desafio técnico. O cenário simula uma integração que copia, de hora em hora, os pedidos do ERP (`erp_pedidos`) para o banco do painel de vendas (`pedidos` e `itens_pedido`). A análise do log da sincronização mostrou que alguns pedidos chegavam duplicados, incompletos ou nem chegavam.

## Problemas encontrados

| Pedido | O que aconteceu | Causa |
|---|---|---|
| 10047 | Gravado duas vezes (ids 6 e 7) | Timeout de 30 s seguido de nova tentativa, sem verificação de duplicidade por `numero_erp` |
| 10051 | Gravado com 0 itens | O item com SKU não cadastrado foi descartado e o pedido foi gravado mesmo assim |
| 10045 | Nunca chegou ao painel | `pedidos.cnpj` era `NOT NULL`, e pedidos de consumidor final não têm CNPJ |

## Solução

**`SincronizacaoService`**
- **Transação única:** o pedido e todos os seus itens são gravados juntos (`@Transactional`). Se qualquer parte falhar, nada é gravado.
- **Pedido incompleto:** é recusado com `PedidoIncompletoException` quando não tem itens ou quando a soma `qtd × preco_unit` difere do `valor_total`.
- **Idempotência por `numero_erp`:** se o pedido já existe no painel, é atualizado e seus itens são substituídos. Reprocessar o mesmo pedido não gera duplicata.

**Migrations (Flyway)**
- **`V1__estrutura_inicial_e_dados_seed.sql`:** estrutura e dados originais do desafio, reproduzindo o estado do incidente.
- **`V2__remove_duplicados_unique_numero_erp_cnpj_opcional.sql`:**
  - remove os pedidos duplicados, mantendo o de menor id;
  - cria `UNIQUE (numero_erp)`, que impede duplicatas no banco mesmo com tentativas simultâneas;
  - torna `cnpj` opcional.

**Por que o `cnpj` deixou de ser obrigatório.** O ERP é a fonte da verdade, e o painel é um espelho dele. No ERP, `erp_pedidos.cnpj` aceita nulo, mas no painel a coluna era `NOT NULL`. Com isso, todo pedido válido no ERP sem CNPJ era recusado pela integração, como aconteceu com o 10045. A melhor escolha é igualar as restrições do banco do painel às do ERP, e não o contrário. Assim nenhum pedido legítimo se perde no caminho, inclusive os de consumidores finais, que não têm CNPJ e são potenciais clientes.

## Stack

- Java 25
- Spring Boot 4.1 (Web MVC, Data JPA)
- PostgreSQL e Flyway
- Lombok
- JUnit 5, AssertJ e Testcontainers

## Como rodar

### Pré-requisitos

- JDK 25
- Docker, para os testes e para rodar a aplicação sem um Postgres local

### Testes

```bash
./mvnw test
```

O Testcontainers sobe um PostgreSQL temporário e o Flyway aplica as migrations antes dos testes. Não é preciso configurar nenhum banco.

### Aplicação com Postgres via Docker

```bash
./mvnw spring-boot:test-run
```

### Aplicação com Postgres local

1. Crie o banco `desafio`.
2. Copie o arquivo de exemplo de credenciais:

   ```bash
   cp .env.example .env
   ```

3. Preencha `DB_USERNAME` e `DB_PASSWORD` no `.env`. Esse arquivo não é versionado. As mesmas chaves também podem ser definidas como variáveis de ambiente.
4. Suba a aplicação:

   ```bash
   ./mvnw spring-boot:run
   ```

## Testes de integração

O teste de integração está em `SincronizacaoServiceTest` e roda contra um Postgres real:

| Teste | O que verifica |
|---|---|
| `reprocessarPedido10047NaoDeveDuplicarNoPainel` | Reprocessar o 10047 mantém um único registro no painel |

O desenvolvimento seguiu o ciclo red → green: o teste de idempotência foi escrito primeiro e falhou contra a V1 (`5b2b675`). Ele passou a ficar verde depois da V2 (`a3cef85`).

## Estrutura

```
src/main/java/.../etapa2
├── model/          # entidades JPA: ErpPedido, Pedido, ItemPedido, StatusPedido
├── repository/     # PedidoRepository
└── service/        # SincronizacaoService, PedidoIncompletoException e dto/
src/main/resources
├── application.yaml
└── db/migration/   # V1 e V2
```
