package com.earthport.rabbitmq.support.command;

import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;

import com.earthport.rabbitmq.support.publisher.AccountCommandPublisher;

public class SampleDriver {

	private static final int NUM_MESSAGES = 1000;
	private final AccountCommandPublisher accountCommandPublisher;

	public SampleDriver(AccountCommandPublisher accountCommandPublisher) {
		this.accountCommandPublisher = accountCommandPublisher;
	}

	@Scheduled(fixedDelay = 5000L)
	public void createAccounts() {
		System.out.println("Starting batch");
		for (int i = 0; i < NUM_MESSAGES; i++) {
			String accountId = UUID.randomUUID().toString();
			accountCommandPublisher
					.publish(new CreateAccountCommand(
							accountId));
		}
		System.out.println("Done");
	}
}