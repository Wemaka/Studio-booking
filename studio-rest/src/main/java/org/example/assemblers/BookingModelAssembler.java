package org.example.assemblers;

import org.example.controllers.BookingController;
import org.example.controllers.StudioController;
import org.example.dto.response.BookingResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class BookingModelAssembler implements RepresentationModelAssembler<BookingResponse, EntityModel<BookingResponse>> {

	@Override
	public EntityModel<BookingResponse> toModel(BookingResponse booking) {
		EntityModel<BookingResponse> model = EntityModel.of(booking,
				// Ссылка на само бронирование
				linkTo(methodOn(BookingController.class)
						.getBookingById(booking.getId()))
						.withSelfRel(),
				// Ссылка на студию, к которой относится бронь
				linkTo(methodOn(StudioController.class)
						.getStudioById(booking.getStudio().getId()))
						.withRel("studio"),
				// Ссылка на список всех броней (коллекция)
				linkTo(methodOn(BookingController.class)
						.getAllBookings(null, null, null, null, null, 0, 20))
						.withRel("bookings")
		);

		return model;
	}
}
