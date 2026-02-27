package br.com.devisecenter.devise_center.modules.users.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private UUID userId;

    private String username;

    private String email;

    private String pictureUrl;

    private String publicId;

    private LocalDateTime creationDate;

}
