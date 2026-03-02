package br.com.devisecenter.devise_center.modules.comments.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.devisecenter.devise_center.exceptions.exception.validation.BusinessException;
import br.com.devisecenter.devise_center.exceptions.exception.validation.ResourceNotFound;
import br.com.devisecenter.devise_center.exceptions.exception.validation.UnauthorizedOperation;
import br.com.devisecenter.devise_center.modules.comments.dtos.CommentDTO;
import br.com.devisecenter.devise_center.modules.comments.dtos.CommentMapper;
import br.com.devisecenter.devise_center.modules.comments.dtos.CreateComment;
import br.com.devisecenter.devise_center.modules.comments.entity.Comment;
import br.com.devisecenter.devise_center.modules.comments.repository.CommentRepository;
import br.com.devisecenter.devise_center.modules.posts.repository.PostRepository;
import br.com.devisecenter.devise_center.modules.users.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class CommentService {

    private CommentRepository repository;

    private UserRepository userRepository;

    private PostRepository postRepository;

    private CommentMapper commentMapper;

    public CommentService(
            CommentRepository repository,
            CommentMapper commentMapper,
            UserRepository userRepository,
            PostRepository postRepository) {
        this.repository = repository;
        this.commentMapper = commentMapper;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    public Page<CommentDTO> findAll(String authorUsername, UUID post, Pageable pageable) {

        if (authorUsername != null && post != null) {
            if (userRepository.readByUsername(authorUsername).isEmpty()) {
                throw new ResourceNotFound("Nenhum usuário encontrado com o username informado");
            }
            if (postRepository.findById(post).isEmpty()) {
                throw new ResourceNotFound("Nenhum post encontrado com o ID informado");
            }
            return repository.findByPost_PostIdAndAuthor_UserId(
                    post,
                    userRepository.readByUsername(authorUsername).get().getUserId(),
                    pageable).map(commentMapper::CommentToDTO);
        }

        if (authorUsername != null) {
            if (userRepository.readByUsername(authorUsername).isEmpty()) {
                throw new ResourceNotFound("Nenhum usuário encontrado com o username informado");
            }
            return repository.findByAuthor_UserId(
                    userRepository.readByUsername(authorUsername).get().getUserId(),
                    pageable).map(commentMapper::CommentToDTO);
        }

        if (post != null) {
            if (postRepository.findById(post).isEmpty()) {
                throw new ResourceNotFound("Nenhum post encontrado com o ID informado");
            }
            return repository.findByPost_PostId(post, pageable).map(commentMapper::CommentToDTO);
        }

        return repository.findAll(pageable).map(commentMapper::CommentToDTO);
    }

    public CommentDTO findById(UUID commentId) {
        return commentMapper.CommentToDTO(repository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFound("Comentário com o ID informado não encontrado")));
    }

    public Page<CommentDTO> findReplies(UUID parentId, Pageable pageable) {
        return repository.findByParent_CommentId(parentId, pageable).map(commentMapper::CommentToDTO);
    }

    @Transactional
    public void createComment(CreateComment commentDTO, UUID authorId) {
        if (commentDTO.parentId() != null) {
            Comment parent = repository.findById(commentDTO.parentId()).orElseThrow(
                    () -> new ResourceNotFound("Comentário com o ID informado não encontrado"));
            if (parent.getParent() != null) {
                throw new BusinessException("Não é possível responder uma resposta de um comentário");
            }
        }

        Comment newComment = commentMapper.DTOToComment(commentDTO);
        newComment.setAuthor(userRepository.findById(authorId).orElseThrow(
                () -> new ResourceNotFound("Nenhum usuário com o ID informado foi encontrado")));
        newComment.setCreatedAt(LocalDateTime.now());
        repository.save(newComment);
    }

    @Transactional
    public void updateComment(CreateComment commentDTO, UUID commentId, UUID requesterId) {
        Comment comment = repository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFound("Comentário com o ID informado não encontrado"));
        if (!comment.getAuthor().getUserId().equals(requesterId)) {
            throw new UnauthorizedOperation("Você não tem permissão para alterar esse comentário");
        }
        comment.setContent(commentDTO.content());
    }

    @Transactional
    public void deleteComment(UUID commentId, UUID requesterId) {
        Comment comment = repository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFound("Comentário com o ID informado não encontrado"));
        if (!comment.getAuthor().getUserId().equals(requesterId)) {
            throw new UnauthorizedOperation("Você não tem permissão para alterar esse comentário");
        }

        repository.deleteByParent_CommentId(commentId);

        repository.delete(comment);
    }

}
