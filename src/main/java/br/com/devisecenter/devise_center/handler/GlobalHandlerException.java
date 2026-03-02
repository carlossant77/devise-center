package br.com.devisecenter.devise_center.handler;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.devisecenter.devise_center.exceptions.dto.ErrorResponse;
import br.com.devisecenter.devise_center.exceptions.exception.api.MissingProfileImage;
import br.com.devisecenter.devise_center.exceptions.exception.api.MissingToken;
import br.com.devisecenter.devise_center.exceptions.exception.api.UsernameAlreadyExist;
import br.com.devisecenter.devise_center.exceptions.exception.upload.ExternalServiceException;
import br.com.devisecenter.devise_center.exceptions.exception.upload.SizeLimitExceeded;
import br.com.devisecenter.devise_center.exceptions.exception.upload.UploadException;
import br.com.devisecenter.devise_center.exceptions.exception.validation.BusinessException;
import br.com.devisecenter.devise_center.exceptions.exception.validation.ResourceNotFound;
import br.com.devisecenter.devise_center.exceptions.exception.validation.UnauthorizedOperation;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalHandlerException {

        @ExceptionHandler(ResourceNotFound.class)
        public ResponseEntity<ErrorResponse> handlerResourceNotFound(
                        ResourceNotFound ex,
                        HttpServletRequest request) {

                ErrorResponse response = new ErrorResponse(
                                404,
                                ex.getMessage(),
                                request.getRequestURI(),
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        @ExceptionHandler(MissingProfileImage.class)
        public ResponseEntity<ErrorResponse> handlerMissingProfileImage(
                        ResourceNotFound ex,
                        HttpServletRequest request) {

                ErrorResponse response = new ErrorResponse(
                                400,
                                ex.getMessage(),
                                request.getRequestURI(),
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handlerValidateInput(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                String message = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .collect(Collectors.joining(", "));

                ErrorResponse response = new ErrorResponse(
                                400,
                                message,
                                request.getRequestURI(),
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        @ExceptionHandler(UnauthorizedOperation.class)
        public ResponseEntity<ErrorResponse> handlerUnauthorizedOperation(
                        UnauthorizedOperation ex,
                        HttpServletRequest request) {

                ErrorResponse response = new ErrorResponse(
                                401,
                                ex.getMessage(),
                                request.getRequestURI(),
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        @ExceptionHandler(UsernameAlreadyExist.class)
        public ResponseEntity<ErrorResponse> handlerUsernameAlreadyExist(
                        UsernameAlreadyExist ex,
                        HttpServletRequest request) {
                ErrorResponse response = new ErrorResponse(
                                409,
                                ex.getMessage(),
                                request.getRequestURI(),
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        @ExceptionHandler(ExternalServiceException.class)
        public ResponseEntity<ErrorResponse> handlerExternalServiceException(
                        ExternalServiceException ex,
                        HttpServletRequest request) {
                ErrorResponse response = new ErrorResponse(
                                500,
                                ex.getMessage(),
                                request.getRequestURI(),
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        @ExceptionHandler(UploadException.class)
        public ResponseEntity<ErrorResponse> handlerUploadException(
                        UploadException ex,
                        HttpServletRequest request) {
                ErrorResponse response = new ErrorResponse(
                                400,
                                ex.getMessage(),
                                request.getRequestURI(),
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        @ExceptionHandler(SizeLimitExceeded.class)
        public ResponseEntity<ErrorResponse> handlerSizeLimitExceeded(
                        SizeLimitExceeded ex,
                        HttpServletRequest request) {
                ErrorResponse response = new ErrorResponse(
                                400,
                                ex.getMessage(),
                                request.getRequestURI(),
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ErrorResponse> handlerBusinessException(
                        BusinessException ex,
                        HttpServletRequest request) {
                ErrorResponse response = new ErrorResponse(
                                400,
                                ex.getMessage(),
                                request.getRequestURI(),
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handlerIllegalArgumentException(
                        IllegalArgumentException ex,
                        HttpServletRequest request) {
                ErrorResponse response = new ErrorResponse(
                                400,
                                ex.getMessage(),
                                request.getRequestURI(),
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handlerHttpMediaTypeNotSupportedException(
                        HttpMediaTypeNotSupportedException ex,
                        HttpServletRequest request) {
                ErrorResponse response = new ErrorResponse(
                                400,
                                ex.getMessage(),
                                request.getRequestURI(),
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handlerHttpMessageNotReadableException(
                        HttpMessageNotReadableException ex,
                        HttpServletRequest request) {
                ErrorResponse response = new ErrorResponse(
                                400,
                                ex.getMessage(),
                                request.getRequestURI(),
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        @ExceptionHandler(NullPointerException.class)
        public ResponseEntity<ErrorResponse> handlerNullPointerException(
                        NullPointerException ex,
                        HttpServletRequest request) {
                ErrorResponse response = new ErrorResponse(
                                400,
                                "Verifique se não há nenhum dado que deveria ter sido inserido faltando",
                                request.getRequestURI(),
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        @ExceptionHandler(MissingToken.class)
        public ResponseEntity<ErrorResponse> handlerMissingToken(
                        MissingToken ex,
                        HttpServletRequest request) {
                ErrorResponse response = new ErrorResponse(
                                400,
                                ex.getMessage(),
                                request.getRequestURI(),
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
}
