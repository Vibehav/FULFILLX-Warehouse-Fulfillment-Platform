package com.fulfillx.inbound.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {


    // --------- Exchange Names -------------
    public static final String INBOUND_EXCHANGE = "inbound.exchange";
    // mail sorter/post office desk: letters don't directly goes inside a mailbox
    // you hand it to a exchange ( which is Topic exchange in this case)
    // Topic Exchange reads routing instructions like a mail sorter reads a zip code
    @Bean
    public TopicExchange inboundExchange() {
        return new TopicExchange(INBOUND_EXCHANGE);
    }


    // ----------------- Routing Keys ----------------
    public static final String STOCK_RECEIVED_ROUTING_KEY = "stock.received";

    // rulebook thata connects exchange to the queues
    /*
    * if an incoming message arrives at inbound.exchange with a stick note stock.recevied
    * (the route key), route it straight into the stock.recevied.queue mailbox
    * */
    public Binding stockReceivedBinding(){
        return BindingBuilder
                .bind(stockReceivedQueue())
                .to(inboundExchange())
                .with(STOCK_RECEIVED_ROUTING_KEY);
    }
    //"If a message arrives at INBOUND_EXCHANGE
    // with routing key 'stock.received'
    // → send it to STOCK_RECEIVED_QUEUE"


    // ----------------- Queue Names ---------------------
    public static final String STOCK_RECEIVED_QUEUE = "stock.received.queue";
    // mailbox: Stock arrives and stays inside the mailbox
    // until a worker service comes and picks it(conusmer)
    @Bean
    public Queue stockReceivedQueue() {
        return QueueBuilder
                .durable(STOCK_RECEIVED_QUEUE)
                .build();
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
