package com.ids.beca.usuarios.exception;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	private static final String SQLSTATE_CHECK_VIOLATION = "23514";
	private static final String CHECK_EMAIL_NORMALIZADO = "ck_usuarios_email_normalizado";
	private static final String MENSAJE_DATOS_INVALIDOS = "La solicitud contiene datos inválidos.";
	private static final String ERRORS_PROPERTY = "errors";
	private static final String CLAVE_CAMPO = "campo";
	private static final String CLAVE_MENSAJE = "mensaje";

	@ExceptionHandler(UsuarioNoEncontradoException.class)
	public ProblemDetail manejarNoEncontrado(UsuarioNoEncontradoException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(EmailDuplicadoException.class)
	public ProblemDetail manejarEmailDuplicado(EmailDuplicadoException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail manejarIntegridad(DataIntegrityViolationException ex) {
		if (esViolacionDeEmailNormalizado(ex)) {
			ProblemDetail problema = ProblemDetail.forStatusAndDetail(
					HttpStatus.BAD_REQUEST, MENSAJE_DATOS_INVALIDOS);
			problema.setProperty(ERRORS_PROPERTY, List.of(Map.of(
					CLAVE_CAMPO, "email",
					CLAVE_MENSAJE, "el correo electrónico no tiene un formato válido")));
			return problema;
		}
		return errorInesperado(ex);
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail manejarErrorInesperado(Exception ex) {
		return errorInesperado(ex);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<Map<String, String>> errores = ex.getBindingResult().getFieldErrors().stream()
				.sorted(Comparator.comparing(FieldError::getField)
						.thenComparing(error -> Objects.requireNonNullElse(
								error.getDefaultMessage(), "")))
				.map(error -> Map.of(
						CLAVE_CAMPO, error.getField(),
						CLAVE_MENSAJE, Objects.requireNonNullElse(
								error.getDefaultMessage(), "valor inválido")))
				.toList();
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, MENSAJE_DATOS_INVALIDOS);
		problema.setProperty(ERRORS_PROPERTY, errores);
		return handleExceptionInternal(ex, problema, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(status,
				"El cuerpo de la solicitud no es un JSON válido o contiene propiedades no permitidas.");
		return handleExceptionInternal(ex, problema, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
			HttpStatusCode status, WebRequest request) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(status,
				"Un parámetro de la solicitud tiene un formato inválido.");
		return handleExceptionInternal(ex, problema, headers, status, request);
	}

	// Solo se registran las clases de la excepción: sus mensajes pueden incluir filas con el hash.
	private ProblemDetail errorInesperado(Exception ex) {
		log.error("Error inesperado: {} (causa raíz: {})",
				ex.getClass().getName(),
				NestedExceptionUtils.getMostSpecificCause(ex).getClass().getName());
		return ProblemDetail.forStatusAndDetail(
				HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado.");
	}

	private boolean esViolacionDeEmailNormalizado(Throwable error) {
		for (Throwable causa = error; causa != null; causa = causa.getCause()) {
			if (causa instanceof ConstraintViolationException violacion) {
				return SQLSTATE_CHECK_VIOLATION.equals(violacion.getSQLState())
						&& CHECK_EMAIL_NORMALIZADO.equalsIgnoreCase(
								violacion.getConstraintName());
			}
		}
		return false;
	}

}
