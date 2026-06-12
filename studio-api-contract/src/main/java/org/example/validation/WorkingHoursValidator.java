package org.example.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.dto.request.StudioRequest;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

public class WorkingHoursValidator implements ConstraintValidator<ValidWorkingHours, StudioRequest> {

	@Override
	public boolean isValid(StudioRequest request, ConstraintValidatorContext context) {
		if (request.workingHoursStart() == null || request.workingHoursEnd() == null) {
			return true;
		}

		try {
			LocalTime start = LocalTime.parse(request.workingHoursStart());
			LocalTime end   = LocalTime.parse(request.workingHoursEnd());

			if (!end.isAfter(start)) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate(
								"Время начала должно быть раньше времени окончания"
						)
						.addPropertyNode("workingHoursStart")
						.addConstraintViolation();
				return false;
			}
			return true;
		} catch (DateTimeParseException e) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Некорректный формат времени (ожидается HH:mm)")
					.addPropertyNode("workingHoursStart")
					.addConstraintViolation();
			return false;
		}
	}
}
