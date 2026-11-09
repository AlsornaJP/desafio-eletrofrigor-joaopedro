package desafio.eletrofrigor.joaopedro.etapa2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Espelho dos pedidos do ERP (fonte da verdade). Somente leitura para a aplicação.
 */
@Entity
@Table(name = "erp_pedidos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ErpPedido {

    @Id
    @Column(name = "nunota")
    private Integer nunota;

    @Column(name = "cliente", nullable = false, length = 100)
    private String cliente;

    @Column(name = "cnpj", length = 18)
    private String cnpj;

    @Column(name = "dh_inclusao", nullable = false)
    private LocalDateTime dhInclusao;

    @Column(name = "dh_alteracao", nullable = false)
    private LocalDateTime dhAlteracao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusPedido status;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;
}
