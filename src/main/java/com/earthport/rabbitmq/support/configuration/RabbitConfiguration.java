package com.earthport.rabbitmq.support.configuration;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.earthport.rabbitmq.support.command.CreateAccountCommand;
import com.earthport.rabbitmq.support.command.SampleDriver;
import com.earthport.rabbitmq.support.consumer.AccountGateway;
import com.earthport.rabbitmq.support.event.AccountCreatedEvent;
import com.earthport.rabbitmq.support.publisher.AccountCommandPublisher;
import com.earthport.rabbitmq.support.publisher.AccountEventPublisher;

@Configuration
public class RabbitConfiguration {

	@Value("${rabbit.addresses}")
	private String rabbitAddresses;

	@Value("${rabbit.username}")
	private String rabbitUsername;

	@Value("${rabbit.password}")
	private String rabbitPassword;

	@Bean
	ConnectionFactory rabbitConnectionFactory() {
		com.rabbitmq.client.ConnectionFactory rabbitConnectionFactory = new com.rabbitmq.client.ConnectionFactory() {
			@Override
			protected void configureSocket(Socket socket) throws IOException {
				super.configureSocket(socket);
				System.out.println("getSoTimeout: " + socket.getSoTimeout());
				socket.setSoTimeout(5000);
				System.out.println("getSoTimeout: " + socket.getSoTimeout());
			}
		};
		rabbitConnectionFactory.setConnectionTimeout(5000);
		rabbitConnectionFactory.setRequestedHeartbeat(5);

		CachingConnectionFactory connectionFactory = new CachingConnectionFactory(
				rabbitConnectionFactory);
		connectionFactory.setAddresses(rabbitAddresses);
		connectionFactory.setUsername(rabbitUsername);
		connectionFactory.setPassword(rabbitPassword);
		return connectionFactory;
	}

	@Bean
	RabbitAdmin amqpAdmin() {
		return new RabbitAdmin(rabbitConnectionFactory());
	}

	@Bean
	TopicExchange exchange() {
		TopicExchange exchange = new TopicExchange("exchange.rabbit");
		amqpAdmin().declareExchange(exchange);
		return exchange;
	}

	@Bean
	TopicExchange deadLetterExchange() {
		TopicExchange exchange = new TopicExchange("exchange.rabbit.deadLetter");
		amqpAdmin().declareExchange(exchange);
		return exchange;
	}

	@Bean
	Queue deadLetterQueue() {
		Queue queue = mirroredQueue("queue.rabbit.deadLetter");
		amqpAdmin().declareQueue(queue);
		amqpAdmin().declareBinding(
				BindingBuilder.bind(queue).to(deadLetterExchange()).with("#"));
		return queue;
	}

	@Bean
	Queue createAccountCommandQueue() {
		Queue queue = mirroredQueue("queue.rabbit.createAccount");
		amqpAdmin().declareQueue(queue);
		amqpAdmin().declareBinding(
				BindingBuilder.bind(queue).to(exchange())
						.with("command.createAccount"));
		return queue;
	}

	@Bean
	Queue accountCreatedQueue() {
		Queue queue = mirroredQueue("queue.rabbit.accountCreated");
		amqpAdmin().declareQueue(queue);
		amqpAdmin().declareBinding(
				BindingBuilder.bind(queue).to(exchange())
						.with("event.accountCreated"));
		return queue;
	}
	
	@Bean
	Queue closeAccountCommandQueue() {
		Queue queue = mirroredQueue("queue.rabbit.closeAccount");
		amqpAdmin().declareQueue(queue);
		amqpAdmin().declareBinding(
				BindingBuilder.bind(queue).to(exchange())
						.with("command.closeAccount"));
		return queue;
	}

	@Bean
	Queue accountClosedQueue() {
		Queue queue = mirroredQueue("queue.rabbit.accountClosed");
		amqpAdmin().declareQueue(queue);
		amqpAdmin().declareBinding(
				BindingBuilder.bind(queue).to(exchange())
						.with("event.accountClosed"));
		return queue;
	}
	
	Queue mirroredQueue(String queueName)
    {
        boolean durable = true;
        boolean exclusive = false;
        boolean autoDelete = false;
        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("x-ha-policy", "all");
        return new Queue(queueName, durable, exclusive, autoDelete, arguments);
    }

	@Bean
	RabbitTemplate transactionalRabbitTemplate() {
		RabbitTemplate template = new RabbitTemplate(rabbitConnectionFactory());
		template.setChannelTransacted(true);
		template.setMessageConverter(jsonMessageConverter());
		return template;
	}

	@Bean
	AccountEventPublisher accountEventPublisher() {
		return new AccountEventPublisher(transactionalRabbitTemplate(),
				exchange());
	}

	@Bean
	AccountCommandPublisher accountCommandPublisher() {
		return new AccountCommandPublisher(transactionalRabbitTemplate(),
				exchange());
	}

	@Bean
	AccountGateway accountGateway() {
		return new AccountGateway(accountEventPublisher(),
				accountCommandPublisher());
	}

	@Bean
	SampleDriver sampleDriver() {
		return new SampleDriver(accountCommandPublisher());
	}

	@Bean
	MessageConverter jsonMessageConverter() {
		JsonMessageConverter converter = new JsonMessageConverter();
		converter.setClassMapper(customClassMapper());
		converter.setCreateMessageIds(true);
		return converter;
	}

	@Bean
	ClassMapper customClassMapper() {
		DefaultClassMapper mapper = new DefaultClassMapper();
		HashMap<String, Class<?>> classMapping = new HashMap<String, Class<?>>();
		classMapping.put("type.command.createAccount",
				CreateAccountCommand.class);
		classMapping
				.put("type.event.accountCreated", AccountCreatedEvent.class);
		mapper.setIdClassMapping(classMapping);
		return mapper;
	}

	@Bean
	SimpleMessageListenerContainer accountCommandListener() {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(
				rabbitConnectionFactory());
		container.setQueues(createAccountCommandQueue(), closeAccountCommandQueue());
		container.setErrorHandler(new RabbitErrorHandler());
		container.setMessageListener(new MessageListenerAdapter(
				accountGateway(), jsonMessageConverter()));
		container.setAdviceChain(retryAdviceChainFactory()
				.createDefaultRetryChain());
		container.setChannelTransacted(true);
		container.setTransactionManager(transactionManager);
		container.setTaskExecutor(taskExecutor);
		return container;
	}

	@Bean
	SimpleMessageListenerContainer accountEventListener() {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(
				rabbitConnectionFactory());
		container.setQueues(accountCreatedQueue(), accountClosedQueue());
		container.setErrorHandler(new RabbitErrorHandler());
		container.setMessageListener(new MessageListenerAdapter(
				accountGateway(), jsonMessageConverter()));
		container.setAdviceChain(retryAdviceChainFactory()
				.createDefaultRetryChain());
		container.setChannelTransacted(true);
		container.setTransactionManager(transactionManager);
		container.setTaskExecutor(taskExecutor);
		return container;
	}

	@Autowired
	TaskExecutor taskExecutor;

	@Autowired
	PlatformTransactionManager transactionManager;

	@Bean
	RetryAdviceChainFactory retryAdviceChainFactory() {
		return new RetryAdviceChainFactory(messageRecoverer());
	}

	@Bean
	DeadLetterMessageRecoverer messageRecoverer() {
		return new DeadLetterMessageRecoverer(transactionalRabbitTemplate(),
				deadLetterExchange());
	}
}