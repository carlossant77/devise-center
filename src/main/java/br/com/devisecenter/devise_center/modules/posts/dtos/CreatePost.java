package br.com.devisecenter.devise_center.modules.posts.dtos;

import jakarta.validation.constraints.NotNull;

public record CreatePost(
        @NotNull(message = "Informar o conteúdo do post é obrigatório") String content) {
}
