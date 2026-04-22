package ru.rentplatform.catalogservice.api.exception;

public class InvalidItemStatusException extends RuntimeException {
    public InvalidItemStatusException(String message) {
        super(message);
    }
}
