package com.earthport.rabbitmq.support.command;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class CloseAccountCommand {
	private final String accountId;
	
	@JsonCreator
	public CloseAccountCommand(@JsonProperty("accountId") String accountId) {
		this.accountId = accountId;
	}

	public String getAccountId() {
		return accountId;
	}
}
