package br.com.devisecenter.devise_center.exceptions.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String message,
        String path,
        LocalDateTime timestamp) {
}
