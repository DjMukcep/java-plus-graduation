package ru.practicum.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolation(Exception e) {
        log.warn("Ошибка валидации/конвертации параметров: {}", e.getMessage());

        return ApiError.builder()
                .message(e.getMessage())
                .reason("Incorrectly made request.")
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        log.warn("Не найдено: {}", e.getMessage());

        return ApiError.builder()
                .message(e.getMessage())
                .reason("Not Found!")
                .status(HttpStatus.NOT_FOUND.name())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {

        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        log.warn("Ошибка при валидации полей: {}", errors);

        return ApiError.builder()
                .errors(errors)
                .message("Validation failed for some fields")
                .reason("Incorrectly made request.")
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final ValidationException e) {
        log.warn("Ошибка валидации: {}", e.getMessage());

        return ApiError.builder()
                .message(e.getMessage())
                .reason("Incorrectly made request.")
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolation(final DataIntegrityViolationException e) {
        log.warn("Нарушение целостности данных в БД: {}", e.getMessage());

        return ApiError.builder()
                .status(HttpStatus.CONFLICT.name())
                .reason("Integrity constraint has been violated.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleBusinessConflict(final ConflictException e) {
        log.warn("Конфликт бизнес-логики: {}", e.getMessage());

        return ApiError.builder()
                .status("FORBIDDEN")
                .reason("For the requested operation the conditions are not met.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(final Exception e) {
        log.error("Внутренняя ошибка main-service: {} ", e.getMessage(), e);

        return ApiError.builder()
                .message("Internal Server Error")
                .reason("Error occurred on the server side.")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleHttpMessageNotReadableException(final HttpMessageNotReadableException e) {
        log.warn("Некорректное тело запроса: {}", e.getMessage());

        return ApiError.builder()
                .message("Malformed JSON request")
                .reason("Incorrectly made request.")
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    @ExceptionHandler(RequestServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiError handleRequestServiceUnavailable(
            final RequestServiceUnavailableException e) {
        log.error("Сервис заявок недоступен: {}", e.getMessage());

        return ApiError.builder()
                .message(e.getMessage())
                .reason("Dependent request-service is unavailable.")
                .status(HttpStatus.SERVICE_UNAVAILABLE.name())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    @ExceptionHandler(UserServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiError handleUserServiceUnavailable(
            final UserServiceUnavailableException e) {
        log.error("Сервис пользователей недоступен");

        return ApiError.builder()
                .message(e.getMessage())
                .reason("Dependent user-service is unavailable.")
                .status(HttpStatus.SERVICE_UNAVAILABLE.name())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    @ExceptionHandler(EventServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiError handleEventServiceUnavailable(
            final EventServiceUnavailableException e) {
        log.error("Сервис мероприятий недоступен");

        return ApiError.builder()
                .message(e.getMessage())
                .reason("Dependent event-service is unavailable.")
                .status(HttpStatus.SERVICE_UNAVAILABLE.name())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }
}
