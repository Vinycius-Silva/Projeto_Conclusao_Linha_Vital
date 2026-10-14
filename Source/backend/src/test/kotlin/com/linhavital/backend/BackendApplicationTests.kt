package com.linhavital.backend

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles(
	"test"
)
class BackendApplicationTests {

	@Test
	fun contextLoads() {

		/*
         * Se o contexto Spring subir completamente,
         * o teste é considerado aprovado.
         *
         * O profile "test" usa H2 e SMTP local fictício,
         * portanto não depende do PostgreSQL nem do
         * servidor SMTP real.
         */
	}
}