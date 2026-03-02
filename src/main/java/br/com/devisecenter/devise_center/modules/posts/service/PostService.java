package br.com.devisecenter.devise_center.modules.posts.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.devisecenter.devise_center.exceptions.exception.validation.ResourceNotFound;
import br.com.devisecenter.devise_center.exceptions.exception.validation.UnauthorizedOperation;
import br.com.devisecenter.devise_center.modules.comments.repository.CommentRepository;
import br.com.devisecenter.devise_center.modules.posts.dtos.CreatePost;
import br.com.devisecenter.devise_center.modules.posts.dtos.PostDTO;
import br.com.devisecenter.devise_center.modules.posts.dtos.PostMapper;
import br.com.devisecenter.devise_center.modules.posts.entity.Post;
import br.com.devisecenter.devise_center.modules.posts.repository.PostRepository;
import br.com.devisecenter.devise_center.modules.upload.UploadFileService;
import br.com.devisecenter.devise_center.modules.users.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class PostService {

    private PostRepository repository;

    private UserRepository userRepository;

    private CommentRepository commentRepository;

    private PostMapper postMapper;

    private UploadFileService uploadService;

    public PostService(
            PostRepository postRepository,
            UserRepository userRepository,
            CommentRepository commentRepository,
            PostMapper postMapper,
            UploadFileService uploadFileService) {
        this.repository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.postMapper = postMapper;
        this.uploadService = uploadFileService;
    }

    public Page<PostDTO> findAll(String author, Pageable pageable) {
        if (author != null) {
            if (userRepository.readByUsername(author).isEmpty()) {
                throw new ResourceNotFound("Nenhum usuário encontrado com o username informado");
            }
            return repository.findByAuthor_UserId(
                    userRepository.readByUsername(author).get().getUserId(),
                    pageable).map(postMapper::PostToDTO);
        }
        return repository.findAll(pageable).map(postMapper::PostToDTO);
    }

    public PostDTO findById(UUID postid) {
        return postMapper.PostToDTO(
                repository.findById(postid)
                        .orElseThrow(() -> new ResourceNotFound("Post com o ID informado não encontrado")));
    }

    public void createPost(CreatePost dto, UUID authorId, String imageUrl, String publicId) {
        Post newPost = postMapper.DTOToPost(dto);
        newPost.setAuthor(userRepository.findById(authorId).orElseThrow(
                () -> new ResourceNotFound("Nenhum usuário encontrado com o ID informado.")));
        newPost.setImageUrl(imageUrl);
        newPost.setPublicId(publicId);
        newPost.setCreatedAt(LocalDateTime.now());
        repository.save(newPost);
    }

    @Transactional
    public void updatePost(CreatePost dto, MultipartFile file, UUID postId, UUID requesterId) {
        Post post = repository.findById(postId)
                .orElseThrow(() -> new ResourceNotFound("Post com o ID informado não encontrado"));
        if (!post.getAuthor().getUserId().equals(requesterId)) {
            throw new UnauthorizedOperation("Você não tem permissão para alterar este post");
        }
        if (file != null) {
            Map<String, String> fileData = uploadService.updateFile(file, "/posts", post.getPublicId());
            post.setImageUrl(fileData.get("url"));
            post.setPublicId(fileData.get("publicId"));
        }
        post.setContent(dto.content());
    }

    @Transactional
    public void deletePost(UUID postId, UUID requesterId) {
        Post post = repository.findById(postId)
                .orElseThrow(() -> new ResourceNotFound("Post com o ID informado não encontrado"));
        if (!post.getAuthor().getUserId().equals(requesterId)) {
            throw new UnauthorizedOperation("Você não tem permissão para alterar este post");
        }

        commentRepository.deleteByPost_PostId(postId);

        if (post.getImageUrl() != null) {
            uploadService.deleteFile(post.getPublicId());
            repository.delete(post);
        } else {
            repository.delete(post);
        }
    }

}
