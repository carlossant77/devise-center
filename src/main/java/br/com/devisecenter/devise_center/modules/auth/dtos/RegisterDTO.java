package br.com.devisecenter.devise_center.modules.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record RegisterDTO(
                @NotNull(message = "Informar o nome de usuário é obrigatório") String username,
                @Email @NotNull(message = "Informar o email do usuário é obrigatório") String email,
                @NotNull(message = "Informar a senha do usuário é obrigatório") String password) {
}
