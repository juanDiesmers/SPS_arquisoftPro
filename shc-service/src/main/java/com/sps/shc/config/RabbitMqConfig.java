package com.sps.shc.config;

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
    public static final String SHC_ROUTING_KEY = "purchase.completed.shc";

    @Bean
    public DirectExchange purchaseExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue shcQueue() {
        return new Queue(SHC_QUEUE, true);
    }

    @Bean
    public Binding shcBinding(Queue shcQueue, DirectExchange purchaseExchange) {
        return BindingBuilder.bind(shcQueue).to(purchaseExchange).with(SHC_ROUTING_KEY);
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
