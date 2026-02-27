package br.com.devisecenter.devise_center.modules.auth.dtos;

import jakarta.validation.constraints.NotNull;

public record LoginDTO(@NotNull String username, @NotNull String password) {

}
