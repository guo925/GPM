package com.gjx.gpms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 队列、交换机、死信与重试配置。
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    public static final String SELECTION_EXCHANGE = "selection.exchange";
    public static final String SELECTION_QUEUE = "selection.queue";
    public static final String SELECTION_ROUTING_KEY = "selection.routing.key";

    public static final String NOTICE_EXCHANGE = "notice.exchange";
    public static final String NOTICE_QUEUE = "notice.queue";
    public static final String NOTICE_ROUTING_KEY = "notice.routing.key";

    public static final String LOG_EXCHANGE = "log.exchange";
    public static final String LOG_QUEUE = "log.queue";
    public static final String LOG_ROUTING_KEY = "log.routing.key";

    public static final String DEAD_LETTER_EXCHANGE = "dead.letter.exchange";
    public static final String DEAD_LETTER_QUEUE = "dead.letter.queue";
    public static final String DEAD_LETTER_ROUTING_KEY = "dead.letter.routing.key";

    /**
     * 处理messageConverter相关逻辑。
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 处理rabbitTemplate相关逻辑。
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    /**
     * 处理rabbitListenerContainerFactory相关逻辑。
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(6);
        return factory;
    }

    /**
     * 查询exchange相关逻辑。
     */
    @Bean
    public DirectExchange selectionExchange() {
        return new DirectExchange(SELECTION_EXCHANGE, true, false);
    }

    /**
     * 处理noticeExchange相关逻辑。
     */
    @Bean
    public DirectExchange noticeExchange() {
        return new DirectExchange(NOTICE_EXCHANGE, true, false);
    }

    /**
     * 处理logExchange相关逻辑。
     */
    @Bean
    public DirectExchange logExchange() {
        return new DirectExchange(LOG_EXCHANGE, true, false);
    }

    /**
     * 处理deadLetterExchange相关逻辑。
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    /**
     * 查询queue相关逻辑。
     */
    @Bean
    public Queue selectionQueue() {
        return queueWithDeadLetter(SELECTION_QUEUE);
    }

    /**
     * 处理noticeQueue相关逻辑。
     */
    @Bean
    public Queue noticeQueue() {
        return queueWithDeadLetter(NOTICE_QUEUE);
    }

    /**
     * 处理logQueue相关逻辑。
     */
    @Bean
    public Queue logQueue() {
        return queueWithDeadLetter(LOG_QUEUE);
    }

    /**
     * 处理deadLetterQueue相关逻辑。
     */
    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DEAD_LETTER_QUEUE, true);
    }

    /**
     * 查询binding相关逻辑。
     */
    @Bean
    public Binding selectionBinding(Queue selectionQueue, DirectExchange selectionExchange) {
        return BindingBuilder.bind(selectionQueue).to(selectionExchange).with(SELECTION_ROUTING_KEY);
    }

    /**
     * 处理noticeBinding相关逻辑。
     */
    @Bean
    public Binding noticeBinding(Queue noticeQueue, DirectExchange noticeExchange) {
        return BindingBuilder.bind(noticeQueue).to(noticeExchange).with(NOTICE_ROUTING_KEY);
    }

    /**
     * 处理logBinding相关逻辑。
     */
    @Bean
    public Binding logBinding(Queue logQueue, DirectExchange logExchange) {
        return BindingBuilder.bind(logQueue).to(logExchange).with(LOG_ROUTING_KEY);
    }

    /**
     * 处理deadLetterBinding相关逻辑。
     */
    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DEAD_LETTER_ROUTING_KEY);
    }

    /**
     * 处理queueWithDeadLetter相关逻辑。
     */
    private Queue queueWithDeadLetter(String queueName) {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        args.put("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY);
        log.info("初始化 RabbitMQ 队列：{}", queueName);
        return new Queue(queueName, true, false, false, args);
    }
}
