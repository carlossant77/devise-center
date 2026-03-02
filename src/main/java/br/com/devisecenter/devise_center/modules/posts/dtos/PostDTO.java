package br.com.devisecenter.devise_center.modules.posts.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class PostDTO {

    private UUID postId;

    private String content;

    private String imageUrl;

    private String publicId;

    private UUID authorId;

    private String author;

    private String profileImgUrl;

    private LocalDateTime creationDate;

}
