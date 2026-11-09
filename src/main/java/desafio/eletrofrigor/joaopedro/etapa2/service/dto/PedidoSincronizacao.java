package desafio.eletrofrigor.joaopedro.etapa2.service.dto;

import desafio.eletrofrigor.joaopedro.etapa2.model.StatusPedido;

import java.math.BigDecimal;
import java.util.List;

/**
 * Pedido vindo do ERP, com seus itens, a ser gravado no painel.
 */
public record PedidoSincronizacao(
        Integer numeroErp,
        String cliente,
        String cnpj,
        StatusPedido status,
        BigDecimal valorTotal,
        List<Item> itens
) {

    public PedidoSincronizacao {
        itens = itens == null ? List.of() : List.copyOf(itens);
    }

    public record Item(String sku, Integer qtd, BigDecimal precoUnit) {

        public BigDecimal subtotal() {
            return precoUnit.multiply(BigDecimal.valueOf(qtd));
        }
    }
}
