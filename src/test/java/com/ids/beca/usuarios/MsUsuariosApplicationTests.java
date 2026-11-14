package com.ids.beca.usuarios;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.ids.beca.usuarios.support.ConClaveJwtPruebas;

@SpringBootTest
@ActiveProfiles("test")
@ConClaveJwtPruebas
class MsUsuariosApplicationTests {

	@Test
	void contextLoads() {
	}

}
