package org.example.exception;

public class NotFoundException extends RuntimeException {
	public NotFoundException(String resourceName, String resourceId) {
		super(resourceName + " with id=" + resourceId + " not found");
	}
}