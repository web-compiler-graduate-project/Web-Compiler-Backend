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

class RabbitMqCommunicationTest: TestContainersConfig() {

    @Autowired
    lateinit var rabbitTemplate: RabbitTemplate

    @Autowired
    lateinit var connectionFactory: ConnectionFactory

    @Test
    fun `test RPC message sending and receiving`() {
        val messageBody = "Test file content"
        val correlationId = "test-correlation-id"
        val replyQueue = "reply_queue"

        val message = Message(messageBody.toByteArray()).apply {
            messageProperties.correlationId = correlationId
            messageProperties.replyTo = replyQueue
        }

        rabbitTemplate.send("rpc_exchange", "rpc_key", message)

        val listener = MessageListener { message ->
            val receivedMessage = String(message.body)
            val response = jacksonObjectMapper().readValue(receivedMessage, CompilationResponse::class.java)
            println("Received response: $response")
            assert(response.output.contains("expected output"))
        }

        val listenerContainer = SimpleMessageListenerContainer(connectionFactory)
        listenerContainer.setQueueNames(replyQueue)
        listenerContainer.setMessageListener(listener)
        listenerContainer.start()
        Thread.sleep(5000)
        listenerContainer.stop()
    }
}
