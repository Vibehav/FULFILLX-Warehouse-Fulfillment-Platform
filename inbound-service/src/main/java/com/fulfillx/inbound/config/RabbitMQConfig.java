package com.fulfillx.inbound.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String STOCK_RECEIVED_QUEUE = "stock.received.queue";
    public static final String INBOUND_EXCHANGE = "inbound.exchange";
    public static final String STOCK_RECEIVED_ROUTING_KEY = "stock.received";

    public static final String DLX_EXCHANGE = "inbound.dlx.exchange";
    public static final String STOCK_DLQ = "stock.received.dlq";
    public static final String STOCK_DLQ_ROUTING_KEY = "stock.received.dlq.routing.key";


    // until a worker service comes and picks it(conusmer) // QUEUE
    // Quorum Queue tied with a DLX
    @Bean
    public Queue stockReceivedQueue() {
        return QueueBuilder
                .durable(STOCK_RECEIVED_QUEUE)
                .quorum() // This sets x-queue-type: quorum
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
    public Binding stockReceivedBinding(){
        return BindingBuilder
                .bind(stockReceivedQueue())
                .to(inboundExchange())
                .with(STOCK_RECEIVED_ROUTING_KEY);
    }

    // Declare the Dead Letter Queue (also a Quorum!) and Exchange
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(STOCK_DLQ).quorum().build();
    }
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }
    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(STOCK_DLQ_ROUTING_KEY);
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
        return template;
    }


}
