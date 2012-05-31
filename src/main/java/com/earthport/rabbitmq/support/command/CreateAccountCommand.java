package com.earthport.rabbitmq.support.command;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class CreateAccountCommand {
	private final String accountId;

	@JsonCreator
	public CreateAccountCommand(@JsonProperty("accountId") String accountId) {
		this.accountId = accountId;
	}

	public String getAccountId() {
		return accountId;
	}
}
