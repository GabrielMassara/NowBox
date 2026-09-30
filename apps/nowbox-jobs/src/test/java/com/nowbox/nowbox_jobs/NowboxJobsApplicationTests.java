package com.nowbox.nowbox_jobs;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:jobs",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.rabbitmq.password=test",
		"spring.rabbitmq.listener.simple.auto-startup=false",
		"spring.mail.username=teste@nowbox.com",
		"spring.mail.password=test",
		"app.storage.minio.access-key=test",
		"app.storage.minio.secret-key=test-secret"
})
class NowboxJobsApplicationTests {

	@Test
	void contextLoads() {
	}

}
