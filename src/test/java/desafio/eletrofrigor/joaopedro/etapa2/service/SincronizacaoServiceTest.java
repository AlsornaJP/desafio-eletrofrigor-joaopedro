package desafio.eletrofrigor.joaopedro.etapa2.service;

import desafio.eletrofrigor.joaopedro.etapa2.TestcontainersConfiguration;
import desafio.eletrofrigor.joaopedro.etapa2.model.StatusPedido;
import desafio.eletrofrigor.joaopedro.etapa2.service.dto.PedidoSincronizacao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Testes de integração contra um Postgres real (Testcontainers), com o banco
 * criado pelas migrations do Flyway. Cada teste roda numa transação que sofre
 * rollback ao final, então o estado do seed é preservado entre os testes.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class SincronizacaoServiceTest {

    @Autowired
    private SincronizacaoService sincronizacaoService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void reprocessarPedido10047NaoDeveDuplicarNoPainel() {
        // Mesmo pedido da nova tentativa após o timeout de 30 s no log da sincronização
        var pedido10047 = new PedidoSincronizacao(
                10047,
                "Offshore Serviços Marítimos",
                "66.666.666/0001-66",
                StatusPedido.ABERTO,
                new BigDecimal("2740.00"),
                List.of(
                        new PedidoSincronizacao.Item("CMP-TEC-AE4440", 1, new BigDecimal("1900.00")),
                        new PedidoSincronizacao.Item("FIL-SEC-DML083", 6, new BigDecimal("90.00")),
                        new PedidoSincronizacao.Item("PRE-PRS-KP5", 1, new BigDecimal("300.00"))
                )
        );

        assertThatCode(() -> sincronizacaoService.sincronizar(pedido10047))
                .doesNotThrowAnyException();

        assertThat(contarPedidosNoPainel(10047))
                .as("o painel deve ter um único pedido com numero_erp = 10047")
                .isEqualTo(1);
    }

    private int contarPedidosNoPainel(int numeroErp) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pedidos WHERE numero_erp = ?", Integer.class, numeroErp);
    }
}
