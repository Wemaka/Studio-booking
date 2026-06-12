package org.example.config;

import org.example.events.RoutingKeys;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitMQConfig {

	/**
	 * JSON-конвертер для сериализации доменных событий в JSON.
	 *
	 * JacksonJsonMessageConverter — актуальная реализация для Spring AMQP 4.0+
	 * (замена устаревшему Jackson2JsonMessageConverter).
	 *
	 * Принимаем настроенный ObjectMapper из Spring Boot — он уже поддерживает
	 * java.time (Instant/LocalDate) через автоконфигурацию Jackson 3.
	 */
	@Bean
	public MessageConverter jsonMessageConverter(JsonMapper jsonMapper) {
		return new JacksonJsonMessageConverter(jsonMapper);
	}

	/**
	 * RabbitTemplate — фасад для отправки сообщений в RabbitMQ.
	 *
	 * Подключаем JSON-конвертер, чтобы вызовы convertAndSend() автоматически
	 * сериализовали Java-объекты в JSON.
	 */
	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
	                                     MessageConverter jsonMessageConverter) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(jsonMessageConverter);
		return template;
	}

	/**
	 * Topic exchange — точка обмена для всех доменных событий.
	 *
	 * Имя exchange берём из контракта (RoutingKeys.EXCHANGE) — publisher и consumer
	 * используют одно и то же имя. Рассогласование имени exchange —
	 * распространённая ошибка, которую трудно отладить.
	 */
	@Bean
	public TopicExchange eventsExchange() {
		return ExchangeBuilder
				.topicExchange(RoutingKeys.EXCHANGE)
				.durable(true)
				.build();
	}
}

