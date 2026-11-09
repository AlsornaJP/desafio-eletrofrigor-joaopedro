package desafio.eletrofrigor.joaopedro.etapa2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "itens_pedido")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** Definido apenas via {@link Pedido#adicionarItem(ItemPedido)}. */
    @Setter(AccessLevel.PACKAGE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(name = "sku", nullable = false, length = 30)
    private String sku;

    @Column(name = "qtd", nullable = false)
    private Integer qtd;

    @Column(name = "preco_unit", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoUnit;

    public ItemPedido(String sku, Integer qtd, BigDecimal precoUnit) {
        this.sku = sku;
        this.qtd = qtd;
        this.precoUnit = precoUnit;
    }
}
