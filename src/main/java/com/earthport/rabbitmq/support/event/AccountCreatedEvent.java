package com.earthport.rabbitmq.support.event;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class AccountCreatedEvent {
	private final String accountId;

	@JsonCreator
	public AccountCreatedEvent(@JsonProperty("accountId") String accountId) {
		this.accountId = accountId;
	}

	public String getAccountId() {
		return accountId;
	}
}
