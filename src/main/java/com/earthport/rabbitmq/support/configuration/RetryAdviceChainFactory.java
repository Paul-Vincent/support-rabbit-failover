package com.earthport.rabbitmq.support.configuration;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.rabbit.config.StatefulRetryOperationsInterceptorFactoryBean;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.interceptor.StatefulRetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * A factory for retry advice chains to be used around message listeners.
  */
public class RetryAdviceChainFactory {
	private final MessageRecoverer messageRecoverer;

	public RetryAdviceChainFactory(MessageRecoverer messageRecoverer) {
		this.messageRecoverer = messageRecoverer;
	}

	/**
	 * A factory method to create an advice chain of one.
	 * 
	 * @param backOffPolicy
	 * @param retryPolicy
	 * @return
	 */
	public Advice[] createRetryChain(final BackOffPolicy backOffPolicy,
			final RetryPolicy retryPolicy) {
		return new Advice[] { retryHandler(backOffPolicy, retryPolicy) };
	}

	/**
	 * @return Returns a default advice chain, that will retry 3 times, with an
	 *         exponential back off policy.
	 */
	public Advice[] createDefaultRetryChain() {
		return createRetryChain(exponentialBackOffPolicy(),
				new SimpleRetryPolicy());
	}

	private StatefulRetryOperationsInterceptor retryHandler(
			final BackOffPolicy backOffPolicy, final RetryPolicy retryPolicy) {
		final StatefulRetryOperationsInterceptorFactoryBean retryOperationsInterceptor = new StatefulRetryOperationsInterceptorFactoryBean();

		final RetryTemplate retryTemplate = new RetryTemplate();
		retryTemplate.setRetryPolicy(retryPolicy);
		retryTemplate.setBackOffPolicy(backOffPolicy);

		retryOperationsInterceptor.setRetryOperations(retryTemplate);
		retryOperationsInterceptor.setMessageRecoverer(messageRecoverer);
		return retryOperationsInterceptor.getObject();
	}

	private BackOffPolicy exponentialBackOffPolicy() {
		return new ExponentialBackOffPolicy();
	}
}