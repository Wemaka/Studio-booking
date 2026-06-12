package org.example.graphql.security;

import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация защиты GraphQL API от вредоносных запросов.
 * Для защиты используются Instrumentation — механизм graphql-java,
 * позволяющий перехватывать запрос на этапе валидации и отклонять его
 * до начала выполнения.
 */
@Configuration
public class GraphQLSecurityConfig {

	/**
	 * Максимальная глубина вложенности запроса (20 уровней).
	 *
	 * Важно: стандартный introspection-запрос (используется GraphiQL/Apollo Studio)
	 * имеет глубину ~15 уровней из-за рекурсивного фрагмента TypeRef.
	 * Поэтому лимит не может быть ниже 15, иначе IDE не сможет загрузить схему.
	 *
	 * Значение 20 достаточно для любых легитимных запросов и защищает от рекурсии.
	 * При превышении graphql-java вернёт ошибку валидации ещё до выполнения запроса.
	 */
	@Bean
	public Instrumentation maxQueryDepthInstrumentation() {
		return new MaxQueryDepthInstrumentation(20);
	}

	/**
	 * Максимальная сложность запроса (200 единиц).
	 *
	 * Каждое запрошенное поле добавляет 1 к сложности. Запрос из 200+ полей
	 * будет отклонён. Это защищает от широких запросов с большим количеством
	 * полей и связей.
	 *
	 * В промышленном приложении можно назначать разные веса разным полям
	 * (например, список книг «стоит» дороже, чем скалярное поле title).
	 */
	@Bean
	public Instrumentation maxQueryComplexityInstrumentation() {
		return new MaxQueryComplexityInstrumentation(200);
	}
}

