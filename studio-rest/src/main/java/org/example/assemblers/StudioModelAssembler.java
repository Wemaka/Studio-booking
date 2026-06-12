package org.example.assemblers;

import org.example.controllers.BookingController;
import org.example.controllers.StudioController;
import org.example.dto.response.StudioResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class StudioModelAssembler implements RepresentationModelAssembler<StudioResponse, EntityModel<StudioResponse>> {

	@Override
	public EntityModel<StudioResponse> toModel(StudioResponse studio) {
		EntityModel<StudioResponse> model = EntityModel.of(studio,
				// Ссылка на саму студию
				linkTo(methodOn(StudioController.class)
						.getStudioById(studio.getId()))
						.withSelfRel(),
				// Ссылка на список броней этой студии
				linkTo(methodOn(BookingController.class)
						.getAllBookings(null, studio.getId(), null, null, null, 0, 20))
						.withRel("bookings"),
				// Ссылка на коллекцию всех студий
				linkTo(methodOn(StudioController.class)
						.getAllStudios(null, null, null, null, 0, 20))
						.withRel("studios")
		);

		return model;
	}
}
