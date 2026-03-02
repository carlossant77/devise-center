package br.com.devisecenter.devise_center.modules.users.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.devisecenter.devise_center.exceptions.exception.api.UsernameAlreadyExist;
import br.com.devisecenter.devise_center.exceptions.exception.validation.ResourceNotFound;
import br.com.devisecenter.devise_center.exceptions.exception.validation.UnauthorizedOperation;
import br.com.devisecenter.devise_center.modules.auth.dtos.RegisterDTO;
import br.com.devisecenter.devise_center.modules.upload.UploadFileService;
import br.com.devisecenter.devise_center.modules.users.dtos.UpdateUser;
import br.com.devisecenter.devise_center.modules.users.dtos.UserDTO;
import br.com.devisecenter.devise_center.modules.users.dtos.UserMapper;
import br.com.devisecenter.devise_center.modules.users.entity.User;
import br.com.devisecenter.devise_center.modules.users.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class UserService {

    private UserRepository repository;

    private UserMapper userMapper;

    private UploadFileService uploadService;

    private BCryptPasswordEncoder passwordEncoder;

    public UserService(
            UserRepository repository,
            UserMapper userMapper,
            UploadFileService uploadFileService,
            BCryptPasswordEncoder encoder) {
        this.repository = repository;
        this.userMapper = userMapper;
        this.uploadService = uploadFileService;
        this.passwordEncoder = encoder;
    }

    public Page<UserDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(userMapper::UsertoDTO);
    }

    public UserDTO findById(UUID userId) {
        return userMapper.UsertoDTO(
                repository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFound("Usuário com o ID informado não encontrado")));
    }

    public void createUser(RegisterDTO dto) {
        Optional<User> user = repository.readByUsername(dto.username());
        if (user.isPresent()) {
            throw new UsernameAlreadyExist("O nome de usuário escolhido já está em uso");
        }
        User newUser = userMapper.DTOToUser(dto);
        newUser.setPassword(passwordEncoder.encode(dto.password()));
        newUser.setCreatedAt(LocalDateTime.now());
        repository.save(newUser);
    }

    @Transactional
    public void updateUser(UpdateUser dto, UUID userId, UUID requesterId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFound("Usuário com o ID informado não encontrado"));
        if (!userId.equals(requesterId)) {
            throw new UnauthorizedOperation("Você não tem permissão para alterar este usuário");
        }
        if (dto.username() != null) {
            user.setUsername(dto.username());
        }
        if (dto.email() != null) {
            user.setEmail(dto.email());
        }
        if (dto.password() != null) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }
    }

    public void deleteUser(UUID userId, UUID requesterId) {
        if (!userId.equals(requesterId)) {
            throw new UnauthorizedOperation("Você não tem permissão para alterar este usuário");
        }

        User user = repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFound("Usuário com o ID informado não encontrado"));

        if (user.getPublicId() == null || user.getPublicId().isBlank()) {
            repository.delete(user);
        } else {
            removeProfilePic(userId, requesterId);
            repository.delete(user);
        }
    }

    // tratativas para foto de perfil //

    @Transactional
    public void setProfilePicture(MultipartFile file, UUID userId, UUID requesterId) {

        User user = repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFound("Usuário com o ID informado não encontrado"));

        if (!user.getUserId().equals(requesterId)) {
            throw new UnauthorizedOperation("Você não tem permissão para alterar este usuário");
        }

        if (user.getPublicId() != null) {
            Map<String, String> fileData = uploadService.updateFile(file, "/pictures", user.getPublicId());
            user.setPictureUrl(fileData.get("url"));
            user.setPublicId(fileData.get("publicId"));
        } else {
            Map<String, String> fileData = uploadService.uploadFile(file, "/pictures");
            user.setPictureUrl(fileData.get("url"));
            user.setPublicId(fileData.get("publicId"));
        }

    }

    public void removeProfilePic(UUID userId, UUID requesterId) {
        if (!userId.equals(requesterId)) {
            throw new UnauthorizedOperation("Você não tem permissão para alterar este usuário");
        }
        User user = repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFound("Usuário com o ID informado não encontrado"));
        if (user.getPublicId().isBlank()) {
            throw new ResourceNotFound("O usuário informado não possui foto de perfil");
        }
        uploadService.deleteFile(user.getPublicId());
    }

}
