package com.earthport.rabbitmq.support.consumer;

import com.earthport.rabbitmq.support.command.CloseAccountCommand;
import com.earthport.rabbitmq.support.command.CreateAccountCommand;
import com.earthport.rabbitmq.support.event.AccountClosedEvent;
import com.earthport.rabbitmq.support.event.AccountCreatedEvent;
import com.earthport.rabbitmq.support.publisher.AccountCommandPublisher;
import com.earthport.rabbitmq.support.publisher.AccountEventPublisher;

public class AccountGateway {
	private final AccountEventPublisher accountEventPublisher;
	private final AccountCommandPublisher accountCommandPublisher;

	public AccountGateway(AccountEventPublisher accountEventPublisher,
			AccountCommandPublisher accountCommandPublisher) {
		this.accountEventPublisher = accountEventPublisher;
		this.accountCommandPublisher = accountCommandPublisher;
	}

	public void handleMessage(CreateAccountCommand createAccountCommand) {
		accountEventPublisher.publish(new AccountCreatedEvent(
				createAccountCommand.getAccountId()));
	}
	
	public void handleMessage(AccountCreatedEvent accountCreatedEvent) {
		accountCommandPublisher.publish(new CloseAccountCommand(
				accountCreatedEvent.getAccountId()));
	}
	
	public void handleMessage(CloseAccountCommand closeAccountCommand) {
		accountEventPublisher.publish(new AccountClosedEvent(
				closeAccountCommand.getAccountId()));
	}
	
	public void handleMessage(AccountClosedEvent accountClosedEvent) {
	}
}
