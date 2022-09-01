package nl.lambdatree.recipepicker.infrastructure.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
   private ErrorCode errorCode;
   private String details;
}
