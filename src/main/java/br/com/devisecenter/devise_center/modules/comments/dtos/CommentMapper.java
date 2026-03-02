package br.com.devisecenter.devise_center.modules.comments.dtos;

import org.springframework.stereotype.Component;

import br.com.devisecenter.devise_center.exceptions.exception.validation.ResourceNotFound;
import br.com.devisecenter.devise_center.modules.comments.entity.Comment;
import br.com.devisecenter.devise_center.modules.comments.repository.CommentRepository;
import br.com.devisecenter.devise_center.modules.posts.repository.PostRepository;

@Component
public class CommentMapper {

    private CommentRepository commentRepository;

    private PostRepository postRepository;

    public CommentMapper(
            PostRepository postRepository,
            CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    public CommentDTO CommentToDTO(Comment comment) {

        CommentDTO commentResponse = new CommentDTO();
        commentResponse.setCommentId(comment.getCommentId());
        commentResponse.setContent(comment.getContent());
        commentResponse.setAuthorId(comment.getAuthor().getUserId());
        commentResponse.setPostId(comment.getPost().getPostId());
        commentResponse.setAuthor(comment.getAuthor().getUsername());
        commentResponse.setProfileImgUrl(comment.getAuthor().getPictureUrl());
        if (comment.getParent() != null) {
            commentResponse.setParentId(comment.getParent().getCommentId());
        }
        commentResponse.setCreationDate(comment.getCreatedAt());
        return commentResponse;

    }

    public Comment DTOToComment(CreateComment dto) {
        Comment newComment = new Comment();
        newComment.setContent(dto.content());
        newComment.setPost(postRepository.findById(dto.postId())
                .orElseThrow(() -> new ResourceNotFound("Post com o ID informado não encontrado")));
        if (dto.parentId() != null) {
            newComment.setParent(commentRepository.findById(dto.parentId())
                    .orElseThrow(() -> new ResourceNotFound("Comentário com o ID informado não encontrado")));
        } else {
            newComment.setParent(null);
        }
        return newComment;
    }

}
