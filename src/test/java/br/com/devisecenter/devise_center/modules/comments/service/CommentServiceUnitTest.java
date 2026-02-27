package br.com.devisecenter.devise_center.modules.comments.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import br.com.devisecenter.devise_center.exceptions.exception.validation.BusinessException;
import br.com.devisecenter.devise_center.exceptions.exception.validation.ResourceNotFound;
import br.com.devisecenter.devise_center.exceptions.exception.validation.UnauthorizedOperation;
import br.com.devisecenter.devise_center.modules.comments.dtos.CommentDTO;
import br.com.devisecenter.devise_center.modules.comments.dtos.CommentMapper;
import br.com.devisecenter.devise_center.modules.comments.dtos.CreateComment;
import br.com.devisecenter.devise_center.modules.comments.entity.Comment;
import br.com.devisecenter.devise_center.modules.comments.repository.CommentRepository;
import br.com.devisecenter.devise_center.modules.posts.entity.Post;
import br.com.devisecenter.devise_center.modules.posts.repository.PostRepository;
import br.com.devisecenter.devise_center.modules.users.entity.User;
import br.com.devisecenter.devise_center.modules.users.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CommentServiceUnitTest {

    @Mock
    private CommentRepository repository;

    @Mock
    private CommentMapper mapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CommentService service;

    private UUID userId;
    private UUID postId;
    private UUID commentId;

    private User user;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        postId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        user = new User();
        user.setUserId(userId);

        post = new Post();
        post.setPostId(postId);

        comment = new Comment();
        comment.setCommentId(commentId);
        comment.setAuthor(user);
        comment.setPost(post);
        comment.setCreatedAt(LocalDateTime.now());
    }

    // ============================
    // FIND ALL
    // ============================

    @Test
    @DisplayName("Should return comment - everything is ok")
    void shouldFindAllWithoutFilters() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Comment> page = new PageImpl<>(java.util.List.of(comment));

        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.CommentToDTO(comment)).thenReturn(mock(CommentDTO.class));

        Page<CommentDTO> result = service.findAll(null, null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(repository).findAll(pageable);
        verify(mapper).CommentToDTO(comment);
    }

    @Test
    @DisplayName("Should throw Exception: ResourceNotFound")
    void shouldThrowWhenAuthorNotFound() {
        when(userRepository.readByUsername("john")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class,
                () -> service.findAll("john", null, PageRequest.of(0, 10)));
    }

    @Test
    @DisplayName("Should throw Exception: ResourceNotFound")
    void shouldThrowWhenPostNotFound() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class,
                () -> service.findAll(null, postId, PageRequest.of(0, 10)));
    }

    @Test
    @DisplayName("Should return comments (filtering by author and post) - everything is ok")
    void shouldFindByAuthorAndPost() {
        when(userRepository.readByUsername("john")).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        Page<Comment> page = new PageImpl<>(java.util.List.of(comment));
        when(repository.findByPost_PostIdAndAuthor_UserId(postId, userId, PageRequest.of(0, 10)))
                .thenReturn(page);
        when(mapper.CommentToDTO(comment)).thenReturn(mock(CommentDTO.class));

        Page<CommentDTO> result = service.findAll("john", postId, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        verify(repository).findByPost_PostIdAndAuthor_UserId(postId, userId, PageRequest.of(0, 10));
    }

    // ============================
    // FIND BY ID
    // ============================

    @Test
    @DisplayName("Should return comment - everything is ok")
    void shouldFindById() {
        when(repository.findById(commentId)).thenReturn(Optional.of(comment));
        when(mapper.CommentToDTO(comment)).thenReturn(mock(CommentDTO.class));

        CommentDTO result = service.findById(commentId);

        assertNotNull(result);
        verify(repository).findById(commentId);
    }

    @Test
    @DisplayName("Should throw Exception: ResourceNotFound")
    void shouldThrowWhenCommentNotFound() {
        when(repository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class,
                () -> service.findById(commentId));
    }

    // ============================
    // CREATE
    // ============================

    @Test
    @DisplayName("Should create comment - everything is ok")
    void shouldCreateComment() {
        CreateComment dto = mock(CreateComment.class);

        when(mapper.DTOToComment(dto)).thenReturn(comment);

        service.createComment(dto, UUID.randomUUID());

        verify(repository).save(comment);
        assertNotNull(comment.getCreatedAt());
    }

    @Test
    @DisplayName("Should create reply - everthing is ok")
    void shouldCreateReply() {

        UUID parentId = UUID.randomUUID();

        CreateComment dto = mock(CreateComment.class);
        when(dto.parentId()).thenReturn(parentId);

        Comment parent = new Comment();
        parent.setParent(null);

        when(repository.findById(parentId)).thenReturn(Optional.of(parent));

        Comment newComment = new Comment();
        when(mapper.DTOToComment(dto)).thenReturn(newComment);

        service.createComment(dto, UUID.randomUUID());

        verify(repository).save(newComment);
    }

    @Test
    @DisplayName("Should throw Exception: BusinessException")
    void shouldThrowWhenReplyingAReply() {

        UUID parentId = UUID.randomUUID();

        CreateComment dto = mock(CreateComment.class);
        when(dto.parentId()).thenReturn(parentId);

        Comment reply = new Comment();
        reply.setParent(new Comment());

        when(repository.findById(parentId)).thenReturn(Optional.of(reply));

        assertThrows(BusinessException.class, () -> {
            service.createComment(dto, UUID.randomUUID());
        });

        verify(repository, never()).save(any());
    }

    // ============================
    // UPDATE
    // ============================

    @Test
    @DisplayName("Should update comment - everything is ok")
    void shouldUpdateComment() {
        CreateComment dto = mock(CreateComment.class);
        when(dto.content()).thenReturn("novo conteúdo");

        when(repository.findById(commentId)).thenReturn(Optional.of(comment));

        service.updateComment(dto, commentId, userId);

        assertEquals("novo conteúdo", comment.getContent());
    }

    @Test
    @DisplayName("Should throw Exception: Unauthorized Operation (trying to delete an comment that isn't yours.)")
    void shouldThrowWhenUpdatingNotOwner() {
        when(repository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(UnauthorizedOperation.class,
                () -> service.updateComment(mock(CreateComment.class), commentId, UUID.randomUUID()));
    }

    // ============================
    // DELETE
    // ============================

    @Test
    @DisplayName("Should delete comment - everything is ok")
    void shouldDeleteComment() {
        when(repository.findById(commentId)).thenReturn(Optional.of(comment));

        service.deleteComment(commentId, userId);

        verify(repository).delete(comment);
    }

    @Test
    @DisplayName("Should throw Exception: Unauthorized Operation (trying to delete an comment that isn't yours.)")
    void shouldThrowWhenDeletingNotOwner() {
        when(repository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(UnauthorizedOperation.class,
                () -> service.deleteComment(commentId, UUID.randomUUID()));
    }
}