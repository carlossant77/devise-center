package br.com.devisecenter.devise_center.modules.comments.dtos;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateComment(
        @NotNull(message = "Conteúdo do comentário é obrigatório") String content,
        @NotNull(message = "Informar o post do comentário é obrigatório") UUID postId,
        UUID parentId) {
}
