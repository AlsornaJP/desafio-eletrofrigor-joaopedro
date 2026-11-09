package desafio.eletrofrigor.joaopedro.etapa2.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pedido no banco do painel de vendas, alimentado pela integração com o ERP.
 */
@Entity
@Table(name = "pedidos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "numero_erp", nullable = false)
    private Integer numeroErp;

    @Setter
    @Column(name = "cliente", nullable = false, length = 100)
    private String cliente;

    @Setter
    @Column(name = "cnpj", nullable = false, length = 18)
    private String cnpj;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusPedido status;

    @Setter
    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Setter
    @Column(name = "sincronizado_em", nullable = false)
    private LocalDateTime sincronizadoEm;

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    public Pedido(Integer numeroErp, String cliente, String cnpj, StatusPedido status,
                  BigDecimal valorTotal, LocalDateTime sincronizadoEm) {
        this.numeroErp = numeroErp;
        this.cliente = cliente;
        this.cnpj = cnpj;
        this.status = status;
        this.valorTotal = valorTotal;
        this.sincronizadoEm = sincronizadoEm;
    }

    /** Mantém os dois lados do relacionamento consistentes. */
    public void adicionarItem(ItemPedido item) {
        item.setPedido(this);
        itens.add(item);
    }

    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }
}
