package com.wieczorek.michal.webcompiler.webcompiler.integration_test.config

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.utility.DockerImageName

@SpringBootTest
@ExtendWith(SpringExtension::class)
open class TestContainersConfig {

    companion object {
        private lateinit var rabbitMqContainer: RabbitMQContainer
        private lateinit var backendContainer: GenericContainer<*>

        @BeforeAll
        @JvmStatic
        fun setup() {
            rabbitMqContainer = RabbitMQContainer(DockerImageName.parse("rabbitmq:3-management"))
                .apply {
                    withExposedPorts(5672, 15672)
                    start()
                }

            System.setProperty("RABBITMQ_USER", rabbitMqContainer.adminUsername)
            System.setProperty("RABBITMQ_PASSWORD", rabbitMqContainer.adminPassword)
            System.setProperty("RABBITMQ_HOST", rabbitMqContainer.host)
            System.setProperty("RABBITMQ_PORT", rabbitMqContainer.getMappedPort(5672).toString())

            backendContainer = GenericContainer<Nothing>("michaelwieczorek/web-compiler:web-compiler-backend")
                .apply {
                    withExposedPorts(8080)
                    withEnv("RABBITMQ_USER", rabbitMqContainer.adminUsername)
                    withEnv("RABBITMQ_PASSWORD", rabbitMqContainer.adminPassword)
                    withEnv("RABBITMQ_HOST", rabbitMqContainer.host)
                    withEnv("RABBITMQ_PORT", rabbitMqContainer.getMappedPort(5672).toString())
                    start()
                }
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            rabbitMqContainer.stop()
            backendContainer.stop()
        }
    }
}
