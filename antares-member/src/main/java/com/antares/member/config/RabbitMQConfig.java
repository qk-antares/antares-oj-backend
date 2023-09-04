package com.antares.member.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMQConfig {
    /**
     * 手动注入RabbitMQ
     * @Parimay注解用于告诉Spring容器RabbitTemplate的首选Bean
     * @param connectionFactory
     * @return
     */
    @Primary
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());


        //设置confirmCallback和ReturnsCallback
        /**
         * 1、只要消息【抵达Broker】就ack=true
         * correlationData：当前消息的唯一关联数据(这个是消息的唯一id)
         * ack：消息是否成功收到
         * cause：失败的原因
         *
         * 在发送消息的同时，将消息的Id和消息的内容保存到数据库中，如果消息收到了，就在数据库里再记录一下消息已经被收到了
         * 定时任务遍历数据库搜索哪些消息没有收到
         *
         * 本地事务表
         */
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            System.out.println("confirm...correlationData["+correlationData+"]==>ack:["+ack+"]==>cause:["+cause+"]");
            if (!ack){
                System.err.println("异常处理。。。。");
            }
        });

        /**
         * 只要消息没有投递给指定的队列，就触发这个失败回调
         */
        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            // 处理返回的消息
            // 返回的消息可能是无法路由到目标队列的消息
            System.out.println("Returned message: " + returnedMessage);
        });

        return rabbitTemplate;
    }

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("exchange.direct");
    }

    @Bean
    public Queue followQueue() {
        return new Queue("antares.member.follow");
    }

    @Bean
    public Binding bindingDirectExchangeFollowQueue(DirectExchange directExchange, Queue followQueue) {
        return BindingBuilder.bind(followQueue).to(directExchange).with("follow");
    }

    //验证码
    @Bean
    public Queue codeQueue() {
        return new Queue("antares.member.code");
    }

    @Bean
    public Binding bindingDirectExchangeCodeQueue(DirectExchange directExchange, Queue codeQueue) {
        return BindingBuilder.bind(codeQueue).to(directExchange).with("code");
    }
}