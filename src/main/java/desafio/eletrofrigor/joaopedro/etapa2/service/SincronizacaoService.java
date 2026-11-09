package desafio.eletrofrigor.joaopedro.etapa2.service;

import desafio.eletrofrigor.joaopedro.etapa2.model.ItemPedido;
import desafio.eletrofrigor.joaopedro.etapa2.model.Pedido;
import desafio.eletrofrigor.joaopedro.etapa2.repository.PedidoRepository;
import desafio.eletrofrigor.joaopedro.etapa2.service.dto.PedidoSincronizacao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SincronizacaoService {

    private final PedidoRepository pedidoRepository;

    /**
     * Grava um pedido do ERP no painel.
     * <p>
     * Transação: o pedido e todos os seus itens são gravados juntos. Se o pedido estiver
     * incompleto ou qualquer gravação falhar, nada é persistido (rollback).
     * <p>
     * Idempotência: o pedido é identificado pelo {@code numero_erp}. Se já existir, é atualizado
     * em vez de inserido novamente, então reprocessar o mesmo pedido (por exemplo, numa nova
     * tentativa após timeout) não gera duplicata.
     */
    @Transactional
    public Pedido sincronizar(PedidoSincronizacao dados) {
        validarCompleto(dados);

        LocalDateTime agora = LocalDateTime.now();
        List<ItemPedido> itens = dados.itens().stream()
                .map(i -> new ItemPedido(i.sku(), i.qtd(), i.precoUnit()))
                .toList();

        Pedido pedido = pedidoRepository.findByNumeroErp(dados.numeroErp())
                .map(existente -> {
                    existente.setCliente(dados.cliente());
                    existente.setCnpj(dados.cnpj());
                    existente.setStatus(dados.status());
                    existente.setValorTotal(dados.valorTotal());
                    existente.setSincronizadoEm(agora);
                    existente.substituirItens(itens);
                    log.info("pedido {} já existia no painel (id={}); atualizado", dados.numeroErp(), existente.getId());
                    return existente;
                })
                .orElseGet(() -> {
                    Pedido novo = new Pedido(dados.numeroErp(), dados.cliente(), dados.cnpj(),
                            dados.status(), dados.valorTotal(), agora);
                    itens.forEach(novo::adicionarItem);
                    return novo;
                });

        Pedido salvo = pedidoRepository.saveAndFlush(pedido);
        log.info("pedido {} sincronizado (id={}, {} itens)", dados.numeroErp(), salvo.getId(), salvo.getItens().size());
        return salvo;
    }

    private void validarCompleto(PedidoSincronizacao dados) {
        if (dados.itens().isEmpty()) {
            throw new PedidoIncompletoException("pedido %d sem itens".formatted(dados.numeroErp()));
        }
        BigDecimal somaItens = dados.itens().stream()
                .map(PedidoSincronizacao.Item::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (somaItens.compareTo(dados.valorTotal()) != 0) {
            throw new PedidoIncompletoException("pedido %d: soma dos itens (%s) difere do valor total (%s)"
                    .formatted(dados.numeroErp(), somaItens, dados.valorTotal()));
        }
    }
}
