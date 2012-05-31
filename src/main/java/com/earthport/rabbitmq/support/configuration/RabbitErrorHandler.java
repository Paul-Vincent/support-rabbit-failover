package com.earthport.rabbitmq.support.configuration;

import org.springframework.util.ErrorHandler;

public class RabbitErrorHandler implements ErrorHandler {
	public void handleError(Throwable t) {
		t.printStackTrace();
	}
}