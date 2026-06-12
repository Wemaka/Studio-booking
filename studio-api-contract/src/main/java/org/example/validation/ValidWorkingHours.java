package org.example.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE}) // Применяется к КЛАССУ, так как проверяет связь двух полей
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = WorkingHoursValidator.class) // Указываем, кто будет проверять
@Documented
public @interface ValidWorkingHours {
	// Сообщение по умолчанию
	String message() default "Время начала должно быть раньше времени окончания";

	// Обязательные поля для любой аннотации валидации
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}