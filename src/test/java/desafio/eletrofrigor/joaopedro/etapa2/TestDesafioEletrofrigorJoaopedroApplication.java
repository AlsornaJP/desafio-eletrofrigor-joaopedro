package desafio.eletrofrigor.joaopedro.etapa2;

import org.springframework.boot.SpringApplication;

public class TestDesafioEletrofrigorJoaopedroApplication {

	public static void main(String[] args) {
		SpringApplication.from(DesafioEletrofrigorJoaopedroApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
