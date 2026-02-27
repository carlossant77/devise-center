package br.com.devisecenter.devise_center.modules.users.dtos;

public record UpdateUser(
                String username,
                String email,
                String password) {
}
