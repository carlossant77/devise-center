package br.com.devisecenter.devise_center.modules.posts.dtos;

import org.springframework.stereotype.Component;

import br.com.devisecenter.devise_center.modules.posts.entity.Post;

@Component
public class PostMapper {

    public Post DTOToPost(CreatePost dto) {
        Post newPost = new Post();
        newPost.setContent(dto.content());
        return newPost;
    }

    public PostDTO PostToDTO(Post post) {
        PostDTO dto = new PostDTO();
        dto.setAuthorId(post.getAuthor().getUserId());
        dto.setAuthor(post.getAuthor().getUsername());
        dto.setProfileImgUrl(post.getAuthor().getPictureUrl());
        dto.setContent(post.getContent());
        dto.setCreationDate(post.getCreatedAt());
        dto.setImageUrl(post.getImageUrl());
        dto.setPostId(post.getPostId());
        dto.setPublicId(post.getPublicId());
        return dto;
    }

}
