package br.com.devisecenter.devise_center.modules.users.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.devisecenter.devise_center.exceptions.exception.api.MissingProfileImage;
import br.com.devisecenter.devise_center.exceptions.exception.api.MissingToken;
import br.com.devisecenter.devise_center.modules.users.dtos.UpdateUser;
import br.com.devisecenter.devise_center.modules.users.dtos.UserDTO;
import br.com.devisecenter.devise_center.modules.users.dtos.UserMapper;
import br.com.devisecenter.devise_center.modules.users.entity.User;
import br.com.devisecenter.devise_center.modules.users.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    private UserService userService;

    private UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ResponseEntity<Page<UserDTO>> findAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserDTO> users = userService.findAll(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> findUserById(@PathVariable("id") UUID userId) {
        UserDTO user = userService.findById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateUserById(
            @AuthenticationPrincipal User user,
            @PathVariable("id") UUID targetId,
            @Valid @RequestBody UpdateUser userDTO) {

        userService.updateUser(userDTO, targetId, user.getUserId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteUser(@PathVariable("id") UUID targetId, @AuthenticationPrincipal User user) {
        userService.deleteUser(targetId, user.getUserId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> me(@AuthenticationPrincipal Optional<User> user) {
        if (user.isEmpty()) {
            throw new MissingToken("É necessário enviar o token de acesso para acessar esta rota.");
        }
        return ResponseEntity.ok(userMapper.UsertoDTO(user.get()));
    }

    // ROTAS PARA FOTO DE PERFIL //
    // (feito de forma separada para permitir a criação simplificada de um usuário
    // sem foto de perfil)

    @PutMapping(value = "/{id}/pictures", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity updateProfilePic(
            @AuthenticationPrincipal User user,
            @RequestParam("file") Optional<MultipartFile> file,
            @PathVariable("id") UUID targetId) {
        if (file.isEmpty())
            throw new MissingProfileImage("É necessário enviar uma imagem para foto de perfil");
        userService.setProfilePicture(file.get(), targetId, user.getUserId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/{id}/pictures")
    public ResponseEntity removeProfilePic(@PathVariable UUID targetId, @AuthenticationPrincipal User user) {
        userService.removeProfilePic(targetId, user.getUserId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
