package org.example.graphql.types.paging;

/**
 * Метаданные страницы для пагинации.
 * Соответствует типу PageInfo в GraphQL-схеме.
 */
public record PageInfoGql(
		int pageNumber,
		int pageSize,
		int totalPages,
		boolean last
) {}
