package org.example.assemblers;

import org.example.controllers.BookingController;
import org.example.controllers.StudioController;
import org.example.dto.response.AvailableSlotResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AvailableSlotModelAssembler implements RepresentationModelAssembler<AvailableSlotResponse, EntityModel<AvailableSlotResponse>> {
	@Override
	public EntityModel<AvailableSlotResponse> toModel(AvailableSlotResponse slot) {
		return EntityModel.of(slot,
				linkTo(methodOn(StudioController.class).getStudioById(slot.getStudio().getId())).withRel("studio"),
				linkTo(methodOn(BookingController.class).createBooking(null)).withRel("booking")
		);
	}
}
