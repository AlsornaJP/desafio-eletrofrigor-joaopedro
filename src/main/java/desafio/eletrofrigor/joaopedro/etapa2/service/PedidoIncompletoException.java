package desafio.eletrofrigor.joaopedro.etapa2.service;

/**
 * O pedido recebido não está completo (sem itens ou com itens que não fecham o valor total)
 * e por isso não pode ser gravado no painel.
 */
public class PedidoIncompletoException extends RuntimeException {

    public PedidoIncompletoException(String message) {
        super(message);
    }
}
