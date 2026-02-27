package br.com.devisecenter.devise_center.modules.users.dtos;

import org.springframework.stereotype.Component;

import br.com.devisecenter.devise_center.modules.auth.dtos.RegisterDTO;
import br.com.devisecenter.devise_center.modules.users.entity.User;

@Component
public class UserMapper {

    public User DTOToUser(RegisterDTO dto) {
        User user = new User();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPassword(dto.password());
        return user;
    }

    public UserDTO UsertoDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setCreationDate(user.getCreatedAt());
        dto.setEmail(user.getEmail());
        dto.setPictureUrl(user.getPictureUrl());
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        return dto;
    }

}
