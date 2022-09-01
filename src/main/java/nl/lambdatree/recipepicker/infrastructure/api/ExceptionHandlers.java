package nl.lambdatree.recipepicker.infrastructure.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ValidationException;
import java.sql.SQLException;
import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
public class ExceptionHandlers {

   // Validation errors
   @ExceptionHandler
   public ResponseEntity<ErrorResponse> handle(ValidationException e) {
      return badRequest(e);
   }

   @ExceptionHandler
   public ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException e) {
      return badRequest(e);
   }

   // Not found errors.
   @ExceptionHandler
   public ResponseEntity<ErrorResponse> handle(NoSuchElementException e) {
      var response = ErrorResponse.builder()
                                  .errorCode(ErrorCode.ENTITY_NOT_FOUND)
                                  .details(e.getMessage())
                                  .build();
      return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
   }

   // Will catch SQL constraint violations, e.g. passing in a non-existing ingredient.
   @ExceptionHandler
   public ResponseEntity<ErrorResponse> handle(SQLException e) {
      return badRequest(e);
   }

   private ResponseEntity<ErrorResponse> badRequest(Exception e) {
      log.info("Bad request: {}", e.getMessage());
      var response = ErrorResponse.builder()
                                  .errorCode(ErrorCode.INVALID_PARAM)
                                  .details(e.getMessage())
                                  .build();
      return ResponseEntity.badRequest()
                           .body(response);

   }
}
