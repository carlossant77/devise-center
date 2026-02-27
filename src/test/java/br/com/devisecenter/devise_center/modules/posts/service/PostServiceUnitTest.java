package br.com.devisecenter.devise_center.modules.posts.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;

import br.com.devisecenter.devise_center.exceptions.exception.validation.ResourceNotFound;
import br.com.devisecenter.devise_center.exceptions.exception.validation.UnauthorizedOperation;
import br.com.devisecenter.devise_center.modules.posts.dtos.CreatePost;
import br.com.devisecenter.devise_center.modules.posts.dtos.PostDTO;
import br.com.devisecenter.devise_center.modules.posts.dtos.PostMapper;
import br.com.devisecenter.devise_center.modules.posts.entity.Post;
import br.com.devisecenter.devise_center.modules.posts.repository.PostRepository;
import br.com.devisecenter.devise_center.modules.upload.UploadFileService;
import br.com.devisecenter.devise_center.modules.users.entity.User;
import br.com.devisecenter.devise_center.modules.users.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PostServiceUnitTest {

        @Mock
        private PostRepository repository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private PostMapper postMapper;

        @Mock
        private UploadFileService uploadService;

        @InjectMocks
        private PostService service;

        private UUID postId;
        private UUID userId;
        private Post post;
        private User user;

        @BeforeEach
        void setup() {
                postId = UUID.randomUUID();
                userId = UUID.randomUUID();

                user = new User();
                user.setUserId(userId);

                post = new Post();
                post.setPostId(postId);
                post.setAuthor(user);
                post.setCreatedAt(LocalDateTime.now());
        }

        // =========================
        // FIND ALL
        // =========================

        @Test
        @DisplayName("Should return all posts - everything is ok")
        void shouldReturnAllPostsWhenAuthorIsNull() {

                Pageable pageable = PageRequest.of(0, 10);
                Page<Post> page = new PageImpl<>(List.of(post));
                PostDTO dto = mock(PostDTO.class);

                when(repository.findAll(pageable)).thenReturn(page);
                when(postMapper.PostToDTO(post)).thenReturn(dto);

                Page<PostDTO> result = service.findAll(null, pageable);

                assertEquals(1, result.getContent().size());
                verify(repository).findAll(pageable);
        }

        @Test
        @DisplayName("Should return all posts that meet the author name - everything is ok")
        void shouldReturnPostsByAuthor() {

                Pageable pageable = PageRequest.of(0, 10);
                Page<Post> page = new PageImpl<>(List.of(post));
                PostDTO dto = mock(PostDTO.class);

                when(userRepository.readByUsername("john"))
                                .thenReturn(Optional.of(user));
                when(repository.findByAuthor_UserId(userId, pageable))
                                .thenReturn(page);
                when(postMapper.PostToDTO(post)).thenReturn(dto);

                Page<PostDTO> result = service.findAll("john", pageable);

                assertEquals(1, result.getContent().size());
        }

        @Test
        @DisplayName("Should throw exception: ResourceNotFound")
        void shouldThrowWhenAuthorDoesNotExist() {

                when(userRepository.readByUsername("ghost"))
                                .thenReturn(Optional.empty());

                assertThrows(ResourceNotFound.class,
                                () -> service.findAll("ghost", PageRequest.of(0, 10)));
        }

        // =========================
        // FIND BY ID
        // =========================

        @Test
        @DisplayName("Should return post - everything is ok")
        void shouldReturnPostById() {

                PostDTO dto = mock(PostDTO.class);

                when(repository.findById(postId))
                                .thenReturn(Optional.of(post));
                when(postMapper.PostToDTO(post))
                                .thenReturn(dto);

                PostDTO result = service.findById(postId);

                assertNotNull(result);
        }

        @Test
        @DisplayName("Should throw exception: ResourceNotFound")
        void shouldThrowWhenPostNotFound() {

                when(repository.findById(postId))
                                .thenReturn(Optional.empty());

                assertThrows(ResourceNotFound.class,
                                () -> service.findById(postId));
        }

        // =========================
        // CREATE POST
        // =========================

        @Test
        @DisplayName("Should create post - everything is ok")
        void shouldCreatePostSuccessfully() {

                CreatePost dto = mock(CreatePost.class);

                when(postMapper.DTOToPost(dto)).thenReturn(post);

                service.createPost(dto, UUID.randomUUID(), "imageUrl", "publicId");

                verify(repository).save(post);
                assertNotNull(post.getCreatedAt());
        }

        // =========================
        // UPDATE POST
        // =========================

        @Test
        @DisplayName("Should update post - everything is ok")
        void shouldUpdatePostSuccessfully() {

                CreatePost dto = mock(CreatePost.class);

                when(dto.content()).thenReturn("updated");
                when(repository.findById(postId))
                                .thenReturn(Optional.of(post));

                service.updatePost(dto, mock(MultipartFile.class), postId, userId);

                assertEquals("updated", post.getContent());
        }

        @Test
        @DisplayName("Should throw Exception: Unauthorized Operation (trying to update an post that isn't yours.)")
        void shouldThrowWhenUpdatingAnotherUserPost() {

                when(repository.findById(postId))
                                .thenReturn(Optional.of(post));

                assertThrows(UnauthorizedOperation.class,
                                () -> service.updatePost(
                                                mock(CreatePost.class),
                                                mock(MultipartFile.class),
                                                postId,
                                                UUID.randomUUID()));
        }

        // =========================
        // DELETE POST
        // =========================

        @Test
        @DisplayName("Should delete a post that doesn't have an image - everthing is ok")
        void shouldDeletePostWithoutImage() {

                post.setImageUrl(null);

                when(repository.findById(postId))
                                .thenReturn(Optional.of(post));

                service.deletePost(postId, userId);

                verify(repository).delete(post);
                verify(uploadService, never()).deleteFile(any());
        }

        @Test
        @DisplayName("Should delete a post that have an image - everthing is ok")
        void shouldDeletePostWithImage() {

                post.setImageUrl("image");
                post.setPublicId("abc123");

                when(repository.findById(postId))
                                .thenReturn(Optional.of(post));

                service.deletePost(postId, userId);

                verify(uploadService).deleteFile("abc123");
                verify(repository).delete(post);
        }

        @Test
        @DisplayName("Should throw Exception: Unauthorized Operation (trying to delete an post that isn't yours.)")
        void shouldThrowWhenDeletingAnotherUsersPost() {

                when(repository.findById(postId))
                                .thenReturn(Optional.of(post));

                assertThrows(UnauthorizedOperation.class,
                                () -> service.deletePost(postId, UUID.randomUUID()));
        }

}