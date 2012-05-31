package com.earthport.rabbitmq.support.publisher;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.support.RabbitGatewaySupport;
import org.springframework.util.Assert;

import com.earthport.rabbitmq.support.command.CloseAccountCommand;
import com.earthport.rabbitmq.support.command.CreateAccountCommand;

public class AccountCommandPublisher extends RabbitGatewaySupport {
	private final String exchangeName;

	public AccountCommandPublisher(RabbitTemplate rabbitTemplate,
			Exchange exchange) {
		Assert.notNull(rabbitTemplate);
		Assert.notNull(exchange);

		setRabbitTemplate(rabbitTemplate);
		this.exchangeName = exchange.getName();
	}

	public void publish(CreateAccountCommand command) {
		final String routingKey = "command.createAccount";
		getRabbitTemplate().convertAndSend(exchangeName, routingKey, command);
	}

	public void publish(CloseAccountCommand command) {
		final String routingKey = "command.closeAccount";
		getRabbitTemplate().convertAndSend(exchangeName, routingKey, command);
	}
}
