package com.wieczorek.michal.webcompiler.webcompiler.configuration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wieczorek.michal.webcompiler.webcompiler.api.response.CompilationResponse
import com.wieczorek.michal.webcompiler.webcompiler.service.FileCompilationPerformerService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class RabbitMQConfiguration(
    @Autowired private val fileCompilationPerformerService: FileCompilationPerformerService
) {

    private val logger: Logger = LoggerFactory.getLogger(RabbitMQConfiguration::class.java)

    @Bean
    open fun rpcExchange(): DirectExchange {
        return DirectExchange("rpc_exchange")
    }

    @Bean
    open fun compileQueue(): Queue {
        return Queue("compile_queue", true)
    }

    @Bean
    open fun replyQueue(): Queue {
        return Queue("reply_queue", true)
    }

    @Bean
    open fun compileBinding(): Binding {
        return BindingBuilder.bind(compileQueue()).to(rpcExchange()).with("rpc_key")
    }

    @Bean
    open fun replyBinding(): Binding {
        return BindingBuilder.bind(replyQueue()).to(rpcExchange()).with("reply_key")
    }

    @Bean
    open fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val template = RabbitTemplate(connectionFactory)
        template.setExchange("rpc_exchange") // Default exchange for sending messages
        return template
    }

    @Bean
    open fun messageListenerContainer(
        connectionFactory: ConnectionFactory,
        rabbitTemplate: RabbitTemplate
    ): SimpleMessageListenerContainer {
        val container = SimpleMessageListenerContainer(connectionFactory)
        container.setQueueNames("compile_queue")
        container.setMessageListener { message ->
            val file = String(message.body)
            val correlationId = message.messageProperties.correlationId
            val replyTo = message.messageProperties.replyTo

            logger.info("Received message with correlationId: $correlationId")

            try {
                // Perform file compilation
                val result = fileCompilationPerformerService.performFileCompilation(file)
                val mapper = jacksonObjectMapper()
                logger.info("File: $file")
                logger.info("Output: $result")

                // Stwórz właściwości wiadomości z ustawionym Correlation ID
                val messageProperties = org.springframework.amqp.core.MessageProperties().apply {
                    this.correlationId = correlationId
                }
                val responseMessage = org.springframework.amqp.core.Message(
                    mapper.writeValueAsBytes(CompilationResponse(output = result)),
                    messageProperties
                )

                // Wysłanie odpowiedzi do kolejki replyTo
                rabbitTemplate.send(rpcExchange().name, replyBinding().routingKey, responseMessage)

                logger.info("Sent response with correlationId: $correlationId to queue: $replyTo")

            } catch (e: Exception) {
                logger.error("Error processing message with correlationId: $correlationId", e)
            }
        }
        return container
    }
}
