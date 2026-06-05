package com.fulfillx.inbound.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConfig {

    public static final String STOCK_RECEIVED_QUEUE = "stock.received.queue";
    public static final String INBOUND_EXCHANGE = "inbound.exchange";
    public static final String STOCK_RECEIVED_ROUTING_KEY = "stock.received";

    public static final String STOCK_DLQ = "stock.received.dlq";
    public static final String DLX_EXCHANGE = "inbound.dlx.exchange";
    public static final String STOCK_DLQ_ROUTING_KEY = "stock.received.dlq.key";


    // until a worker service comes and picks it(conusmer) // QUEUE
    // Quorum Queue tied with a DLX
    @Bean
    public Queue stockReceivedQueue() {
        return QueueBuilder
                .durable(STOCK_RECEIVED_QUEUE)
                .quorum()// This sets x-queue-type: quorum
                .withArgument("x-dead-letter-exchange",DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", STOCK_DLQ_ROUTING_KEY)
                .build();
    }

    // Messages are sent to Topic Exchanges
    // Topic Exchange reads routing instructions like a mail sorter reads a zip code and sends it
    @Bean
    public TopicExchange inboundExchange() {
        return new TopicExchange(INBOUND_EXCHANGE);
    }

    // Rulebook that connects Exchange to the queues
    // if an incoming message arrives at INBOUND_EXCHANGE with a note stock.recevied
    // (the route key), route it straight into the stock.recevied.queue mailbox
    @Bean
    public Binding stockReceivedBinding(){
        return BindingBuilder
                .bind(stockReceivedQueue())
                .to(inboundExchange())
                .with(STOCK_RECEIVED_ROUTING_KEY);
    }

    // Declare the Dead Letter Queue (also a Quorum!) and Exchange
    @Bean
    public Queue StockReceivedDLQ() {
        return QueueBuilder.durable(STOCK_DLQ).quorum().build();
    }

    @Bean
    public DirectExchange StockReceivedDLX() {
        return new DirectExchange(DLX_EXCHANGE);
    }
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(StockReceivedDLQ())
                .to(StockReceivedDLX())
                .with(STOCK_DLQ_ROUTING_KEY);
    }

    // Message Converter
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        template.setMandatory(true); //  publisher-returns: true in yml
        // without this, unroutable messages are silently dropped instead of returned.

        // Set callbacks here, not in @PostConstruct because what if messages are sent before the bean is created
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (correlationData == null) return;
            String messageId = correlationData.getId();
            if (ack) {
                log.info("RabbitMQ confirmed receipt of message ID: {}", messageId);
            } else {
                log.error("RabbitMQ Rejected message ID: {}, Reason: {}", messageId, cause);
            }

            // future work: save to outbox table in db for cron job
        });

            //Returns Callback was true
        template.setReturnsCallback(returned -> {
            log.error("Message returned: exchange={}, routingKey={}, replyText={}",
                    returned.getExchange(),
                    returned.getRoutingKey(),
                    returned.getReplyText());
        });

        return template;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }


}
