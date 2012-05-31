package com.earthport.rabbitmq.support.event;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class AccountClosedEvent {
	private final String accountId;

	@JsonCreator
	public AccountClosedEvent(@JsonProperty("accountId") String accountId) {
		this.accountId = accountId;
	}

	public String getAccountId() {
		return accountId;
	}
}
