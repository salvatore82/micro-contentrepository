package it.sdeluca.microcontentrepository.config;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;

@Configuration
@EnableJms
public class JmsConfig {

	@Value("${spring.activemq.broker-url}")
	private String BROKER_URL;
	@Value("${spring.activemq.user}")
	private String BROKER_USERNAME;
	@Value("${spring.activemq.password}")
	private String BROKER_PASSWORD;

	// Initial ConnectionFactory
	@Bean
	public ConnectionFactory connectionFactory() {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		connectionFactory.setBrokerURL(BROKER_URL);
		connectionFactory.setPassword(BROKER_USERNAME);
		connectionFactory.setUserName(BROKER_PASSWORD);
		return connectionFactory;
	}

	// Serialize message content
	@Bean
	public MessageConverter jacksonJmsMessageConverter() {
		return new MappingJackson2MessageConverter();
	}

	// Used for Sending Messages.
	@Bean
	public JmsTemplate jmsTemplate() {
		JmsTemplate template = new JmsTemplate();
		template.setMessageConverter(jacksonJmsMessageConverter());
		template.setConnectionFactory(connectionFactory());
		template.setPubSubDomain(Boolean.TRUE);
		return template;
	}

	// Used for JMSListener annotation
	@Bean
	public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory());
		factory.setConcurrency("1-1");
		factory.setPubSubDomain(true);
		return factory;
	}

}