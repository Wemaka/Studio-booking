package org.example.graphql.exception;

import com.netflix.graphql.types.errors.TypedGraphQLError;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import org.example.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class GraphQlExceptionHandler implements DataFetcherExceptionHandler {
	@Override
	public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(
			DataFetcherExceptionHandlerParameters handlerParameters) {

		Throwable exception = handlerParameters.getException();

		// Ресурс не найден — аналог HTTP 404
		if (exception instanceof NotFoundException) {
			var error = TypedGraphQLError.newNotFoundBuilder()
					.message(exception.getMessage())
					.path(handlerParameters.getPath())
					.build();
			return CompletableFuture.completedFuture(
					DataFetcherExceptionHandlerResult.newResult()
							.error(error)
							.build());
		}

		// Все остальные исключения — внутренняя ошибка сервера.
		// Не раскрываем детали клиенту в целях безопасности.
		var error = TypedGraphQLError.newInternalErrorBuilder()
				.message("Внутренняя ошибка сервера")
				.path(handlerParameters.getPath())
				.build();

		return CompletableFuture.completedFuture(
				DataFetcherExceptionHandlerResult.newResult()
						.error(error)
						.build());
	}
}
