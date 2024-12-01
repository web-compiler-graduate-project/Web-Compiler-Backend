package com.wieczorek.michal.webcompiler.webcompiler.integration_test

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wieczorek.michal.webcompiler.webcompiler.configuration.response.CompilationResponse
import com.wieczorek.michal.webcompiler.webcompiler.integration_test.config.TestContainersConfig
import org.junit.jupiter.api.Test
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageListener
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.Assert

class CompilationProcessTest : TestContainersConfig() {

    @Autowired
    lateinit var rabbitTemplate: RabbitTemplate

    @Autowired
    lateinit var connectionFactory: ConnectionFactory

    @Test
    fun `test file compilation process with success`() {
        val cppCode = """
            #include <iostream>
            int main() {
                std::cout << "Hello, World!" << std::endl;
                return 0;
            }
        """.trimIndent()

        val correlationId = "test-correlation-id"
        val replyQueue = "reply_queue"

        val message = Message(cppCode.toByteArray()).apply {
            messageProperties.correlationId = correlationId
            messageProperties.replyTo = replyQueue
        }

        rabbitTemplate.send("rpc_exchange", "rpc_key", message)

        val listener = MessageListener { message ->
            val receivedMessage = String(message.body)
            val response = jacksonObjectMapper().readValue(receivedMessage, CompilationResponse::class.java)
            println("Received response: $response")

            Assert.isTrue(response.output.contains("Hello, World!"), "Compilation failed or output is incorrect.")
        }

        val listenerContainer = SimpleMessageListenerContainer(connectionFactory)
        listenerContainer.setQueueNames(replyQueue)
        listenerContainer.setMessageListener(listener)
        listenerContainer.start()

        Thread.sleep(5000)

        listenerContainer.stop()
    }

    @Test
    fun `test compilation process with error`() {
        val cppCode = """
            #include <iostream>
            int main() {
                std::cout << "Hello, World!" << std::endl
                return 0;
            }
        """.trimIndent()

        val correlationId = "test-correlation-id-error"
        val replyQueue = "reply_queue"

        val message = Message(cppCode.toByteArray()).apply {
            messageProperties.correlationId = correlationId
            messageProperties.replyTo = replyQueue
        }

        rabbitTemplate.send("rpc_exchange", "rpc_key", message)

        val listener = MessageListener { message ->
            val receivedMessage = String(message.body)
            val response = jacksonObjectMapper().readValue(receivedMessage, CompilationResponse::class.java)
            println("Received response: $response")

            Assert.isTrue(response.output.contains("error"), "Expected compilation error but got: ${response.output}")
        }

        val listenerContainer = SimpleMessageListenerContainer(connectionFactory)
        listenerContainer.setQueueNames(replyQueue)
        listenerContainer.setMessageListener(listener)
        listenerContainer.start()

        Thread.sleep(5000)

        listenerContainer.stop()
    }
}
