package com.sps.purchase.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "sps.exchange";
    public static final String SHC_QUEUE = "shc.queue";
    public static final String SAM_QUEUE = "sam.queue";
    public static final String SHC_ROUTING_KEY = "purchase.completed.shc";
    public static final String SAM_ROUTING_KEY = "purchase.completed.sam";

    @Bean
    public DirectExchange purchaseExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue shcQueue() {
        return new Queue(SHC_QUEUE, true);
    }

    @Bean
    public Queue samQueue() {
        return new Queue(SAM_QUEUE, true);
    }

    @Bean
    public Binding shcBinding(Queue shcQueue, DirectExchange purchaseExchange) {
        return BindingBuilder.bind(shcQueue).to(purchaseExchange).with(SHC_ROUTING_KEY);
    }

    @Bean
    public Binding samBinding(Queue samQueue, DirectExchange purchaseExchange) {
        return BindingBuilder.bind(samQueue).to(purchaseExchange).with(SAM_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
