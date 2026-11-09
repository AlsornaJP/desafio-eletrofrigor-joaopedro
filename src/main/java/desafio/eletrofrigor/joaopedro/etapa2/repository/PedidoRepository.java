package desafio.eletrofrigor.joaopedro.etapa2.repository;

import desafio.eletrofrigor.joaopedro.etapa2.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    Optional<Pedido> findByNumeroErp(Integer numeroErp);
}
