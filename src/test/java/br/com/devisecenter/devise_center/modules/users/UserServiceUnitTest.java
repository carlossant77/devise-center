package br.com.devisecenter.devise_center.modules.users;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import br.com.devisecenter.devise_center.exceptions.exception.api.UsernameAlreadyExist;
import br.com.devisecenter.devise_center.exceptions.exception.validation.ResourceNotFound;
import br.com.devisecenter.devise_center.exceptions.exception.validation.UnauthorizedOperation;
import br.com.devisecenter.devise_center.modules.auth.dtos.RegisterDTO;
import br.com.devisecenter.devise_center.modules.upload.UploadFileService;
import br.com.devisecenter.devise_center.modules.users.dtos.*;
import br.com.devisecenter.devise_center.modules.users.entity.User;
import br.com.devisecenter.devise_center.modules.users.repository.UserRepository;
import br.com.devisecenter.devise_center.modules.users.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository repository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UploadFileService uploadService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService service;

    private UUID userId;
    private UUID requesterId;
    private User user;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        requesterId = userId;

        user = new User();
        user.setUserId(userId);
        user.setUsername("test");
        user.setEmail("test@email.com");
        user.setPassword("encoded");
        user.setCreatedAt(LocalDateTime.now());
    }

    // =========================
    // FIND ALL
    // =========================

    @Test
    @DisplayName("Should return users - everthing is ok")
    void shouldReturnPagedUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(user));
        UserDTO dto = mock(UserDTO.class);

        when(repository.findAll(pageable)).thenReturn(page);
        when(userMapper.UsertoDTO(user)).thenReturn(dto);

        Page<UserDTO> result = service.findAll(pageable);

        assertEquals(1, result.getContent().size());
        verify(repository).findAll(pageable);
    }

    // =========================
    // FIND BY ID
    // =========================

    @Test
    @DisplayName("Should return user - everthing is ok")
    void shouldReturnUserById() {
        UserDTO dto = mock(UserDTO.class);

        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.UsertoDTO(user)).thenReturn(dto);

        UserDTO result = service.findById(userId);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should throw exception: Resource not found")
    void shouldThrowWhenUserNotFound() {
        when(repository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class,
                () -> service.findById(userId));
    }

    // =========================
    // CREATE USER
    // =========================

    @Test
    @DisplayName("Should create user - everthing is ok")
    void shouldCreateUserSuccessfully() {
        RegisterDTO dto = mock(RegisterDTO.class);

        when(dto.username()).thenReturn("test");
        when(dto.password()).thenReturn("123");
        when(repository.readByUsername("test")).thenReturn(Optional.empty());
        when(userMapper.DTOToUser(dto)).thenReturn(user);
        when(passwordEncoder.encode("123")).thenReturn("encoded");

        service.createUser(dto);

        verify(repository).save(user);
        assertNotNull(user.getCreatedAt());
        assertEquals("encoded", user.getPassword());
    }

    @Test
    @DisplayName("Should throw Exception: Username Already Exists")
    void shouldThrowWhenUsernameAlreadyExists() {
        RegisterDTO dto = mock(RegisterDTO.class);

        when(dto.username()).thenReturn("test");
        when(repository.readByUsername("test")).thenReturn(Optional.of(user));

        assertThrows(UsernameAlreadyExist.class,
                () -> service.createUser(dto));

        verify(repository, never()).save(any());
    }

    // =========================
    // UPDATE USER
    // =========================

    @Test
    @DisplayName("Should update user - everthing is ok")
    void shouldUpdateUserSuccessfully() {
        UpdateUser dto = mock(UpdateUser.class);

        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(dto.username()).thenReturn("newName");
        when(dto.email()).thenReturn("new@email.com");
        when(dto.password()).thenReturn("123");
        when(passwordEncoder.encode("123")).thenReturn("encoded");

        service.updateUser(dto, userId, requesterId);

        assertEquals("newName", user.getUsername());
        assertEquals("new@email.com", user.getEmail());
        assertEquals("encoded", user.getPassword());
    }

    @Test
    @DisplayName("Should throw Exception: Unauthorized Operation (trying to update an account that isn't yours.)")
    void shouldThrowWhenUpdatingAnotherUser() {
        when(repository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(UnauthorizedOperation.class,
                () -> service.updateUser(mock(UpdateUser.class),
                        userId,
                        UUID.randomUUID()));
    }

    // =========================
    // DELETE USER
    // =========================

    @Test
    @DisplayName("Should delete user - everthing is ok")
    void shouldDeleteUserSuccessfully() {
        when(repository.findById(userId)).thenReturn(Optional.of(user));

        service.deleteUser(userId, requesterId);

        verify(repository).delete(user);
    }

    @Test
    @DisplayName("Should throw Exception: Unauthorized Operation (trying to delete an account that isn't yours.)")
    void shouldThrowWhenDeletingAnotherUser() {
        assertThrows(UnauthorizedOperation.class,
                () -> service.deleteUser(userId, UUID.randomUUID()));
    }

    // =========================
    // SET PROFILE PICTURE
    // =========================

    @Test
    @DisplayName("Should set new profile pic - everthing is ok")
    void shouldUploadNewProfilePicture() {
        MultipartFile file = mock(MultipartFile.class);
        Map<String, String> response = Map.of(
                "url", "http://image",
                "publicId", "abc123");

        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(uploadService.uploadFile(file, "/pictures")).thenReturn(response);

        service.setProfilePicture(file, userId, requesterId);

        assertEquals("http://image", user.getPictureUrl());
        assertEquals("abc123", user.getPublidId());
    }

    @Test
    @DisplayName("Should update a existent profile pic - everthing is ok")
    void shouldUpdateExistingProfilePicture() {
        MultipartFile file = mock(MultipartFile.class);
        user.setPublidId("oldId");

        Map<String, String> response = Map.of(
                "url", "http://newimage",
                "publicId", "newId");

        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(uploadService.updateFile(file, "/pictures", "publicId"))
                .thenReturn(response);

        service.setProfilePicture(file, userId, requesterId);

        assertEquals("http://newimage", user.getPictureUrl());
        assertEquals("newId", user.getPublidId());
    }

    @Test
    @DisplayName("Should throw Exception: Unauthorized Operation (trying to delete an profile pic that isn't yours.)")
    void shouldThrowWhenSettingPictureForAnotherUser() {
        MultipartFile file = mock(MultipartFile.class);
        when(repository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(UnauthorizedOperation.class,
                () -> service.setProfilePicture(file,
                        userId,
                        UUID.randomUUID()));
    }

    // =========================
    // REMOVE PROFILE PICTURE
    // =========================

    @Test
    @DisplayName("Should delete a existent profile pic - everthing is ok")
    void shouldRemoveProfilePicture() {
        user.setPublidId("abc123");
        when(repository.findById(userId)).thenReturn(Optional.of(user));

        service.removeProfilePic(userId, requesterId);

        verify(uploadService).deleteFile("abc123");
    }

    @Test
    @DisplayName("Should throw Exception: Resource Not Found")
    void shouldThrowWhenNoProfilePictureExists() {
        user.setPublidId("");

        when(repository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ResourceNotFound.class,
                () -> service.removeProfilePic(userId, requesterId));
    }

    @Test
    void testCreateUser() {

    }

    @Test
    void testDeleteUser() {

    }

    @Test
    void testFindAll() {

    }

    @Test
    void testFindById() {

    }

    @Test
    void testRemoveProfilePic() {

    }

    @Test
    void testSetProfilePicture() {

    }

    @Test
    void testUpdateUser() {

    }

}
