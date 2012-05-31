package com.earthport.rabbitmq.support.publisher;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.support.RabbitGatewaySupport;
import org.springframework.util.Assert;

import com.earthport.rabbitmq.support.event.AccountClosedEvent;
import com.earthport.rabbitmq.support.event.AccountCreatedEvent;

public class AccountEventPublisher extends RabbitGatewaySupport {
	private final String exchangeName;

	public AccountEventPublisher(RabbitTemplate rabbitTemplate,
			Exchange exchange) {
		Assert.notNull(rabbitTemplate);
		Assert.notNull(exchange);

		setRabbitTemplate(rabbitTemplate);
		this.exchangeName = exchange.getName();
	}

	public void publish(AccountCreatedEvent event) {

		final String routingKey = "event.accountCreated";
		getRabbitTemplate().convertAndSend(exchangeName, routingKey, event);
	}

	public void publish(AccountClosedEvent event) {

		final String routingKey = "event.accountClosed";
		getRabbitTemplate().convertAndSend(exchangeName, routingKey, event);
	}
}
