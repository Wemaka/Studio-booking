package org.example.graphql.scalar;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsRuntimeWiring;
import graphql.scalars.ExtendedScalars;
import graphql.schema.idl.RuntimeWiring;

@DgsComponent
public class DateTimeScalarRegistration {

	/**
	 * Регистрируем скаляры DateTime и Date в runtime wiring.
	 *
	 * ExtendedScalars — это коллекция готовых скаляров от graphql-java.
	 * Они уже содержат логику сериализации/десериализации, нам достаточно
	 * лишь зарегистрировать их.
	 */
	@DgsRuntimeWiring
	public RuntimeWiring.Builder addScalars(RuntimeWiring.Builder builder) {
		return builder
				.scalar(ExtendedScalars.DateTime)
				.scalar(ExtendedScalars.Date)
				.scalar(ExtendedScalars.LocalTime);
	}
}
