package com.earthport.rabbitmq.support.configuration;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.support.RabbitGatewaySupport;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.util.Assert;

public class DeadLetterMessageRecoverer extends RabbitGatewaySupport implements MessageRecoverer
{
    private final String exchangeName;

    /**
     * @param rabbitTemplate
     */
    public DeadLetterMessageRecoverer(RabbitTemplate template, Exchange exchange)
    {
        Assert.notNull(template, "The rabbitTemplate cannot be null");
        Assert.notNull(exchange, "The exchange cannot be null");

        setRabbitTemplate(template);
        this.exchangeName = exchange.getName();
    }

    /**
     * Post the message to the configured dead letter exchange.
     */
    public void recover(Message message, Throwable cause)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cause.printStackTrace(new PrintStream(out));
        message.getMessageProperties().getHeaders().put("exception", out.toString());
        message.getMessageProperties().getHeaders().put("receivedExchange", message.getMessageProperties().getReceivedExchange());
            getRabbitTemplate().send(exchangeName, message.getMessageProperties().getReceivedRoutingKey(), message);
    }
}