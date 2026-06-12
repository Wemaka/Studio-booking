package org.example.controllers;

import jakarta.validation.Valid;
import org.example.assemblers.AvailableSlotModelAssembler;
import org.example.assemblers.StudioModelAssembler;
import org.example.dto.request.*;
import org.example.dto.response.AvailableSlotResponse;
import org.example.dto.response.PagedResponse;
import org.example.dto.response.StudioResponse;
import org.example.endpoints.StudioApi;
import org.example.service.StudioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@RestController
public class StudioController implements StudioApi {

	private final StudioService studioService;
	private final StudioModelAssembler studioAssembler;
	private final AvailableSlotModelAssembler availableSlotAssembler;
	private final PagedResourcesAssembler<StudioResponse> pagedStudioAssembler;
	private final PagedResourcesAssembler<AvailableSlotResponse> pagedAvailableAssembler;

	public StudioController(StudioService studioService, StudioModelAssembler studioAssembler, AvailableSlotModelAssembler availableSlotAssembler, PagedResourcesAssembler<StudioResponse> pagedStudioAssembler, PagedResourcesAssembler<AvailableSlotResponse> pagedAvailableAssembler) {
		this.studioService = studioService;
		this.studioAssembler = studioAssembler;
		this.availableSlotAssembler = availableSlotAssembler;
		this.pagedStudioAssembler = pagedStudioAssembler;
		this.pagedAvailableAssembler = pagedAvailableAssembler;
	}

	@Override
	public EntityModel<StudioResponse> getStudioById(Long id) {
		StudioResponse studio = studioService.getStudioById(id);
		return studioAssembler.toModel(studio);
	}

	@Override
	public PagedModel<EntityModel<StudioResponse>> getAllStudios(
			String name,
			String location,
			Integer pricePerHour,
			Boolean isActive,
			int page,
			int size) {

		PagedResponse<StudioResponse> paged = studioService.getAllStudios(pricePerHour, name, location, isActive, page, size);

		Page<StudioResponse> springPage = new PageImpl<>(
				paged.content(),
				PageRequest.of(paged.pageNumber(), paged.pageSize()),
				paged.totalElements()
		);

		return pagedStudioAssembler.toModel(springPage, studioAssembler);
	}

	@Override
	public PagedModel<EntityModel<AvailableSlotResponse>> getAllAvailableSlots(
			Long studioId,
			LocalDate date,
			LocalTime time,
			Integer durationMinutes,
			int page,
			int size) {

		PagedResponse<AvailableSlotResponse> paged = studioService.getAllAvailableSlots(
				studioId, date, time, durationMinutes, page, size);

		Page<AvailableSlotResponse> springPage = new PageImpl<>(
				paged.content(),
				PageRequest.of(paged.pageNumber(), paged.pageSize()),
				paged.totalElements()
		);

		return pagedAvailableAssembler.toModel(springPage, availableSlotAssembler);
	}

	@Override
	public ResponseEntity<EntityModel<StudioResponse>> createStudio(StudioRequest request) {
		StudioResponse created = studioService.createStudio(request);
		EntityModel<StudioResponse> model = studioAssembler.toModel(created);
		return ResponseEntity
				.created(model.getRequiredLink("self").toUri())
				.body(model);
	}

	@Override
	public EntityModel<StudioResponse> updateStudio(Long id, UpdateStudioRequest request) {
		StudioResponse updated = studioService.updateStudio(id, request);
		return studioAssembler.toModel(updated);
	}

	@Override
	public EntityModel<StudioResponse> patchStudio(Long id, PatchStudioRequest request) {
		StudioResponse patched = studioService.patchStudio(id, request);
		return studioAssembler.toModel(patched);
	}
}