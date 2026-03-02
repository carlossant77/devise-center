package br.com.devisecenter.devise_center.modules.comments.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class CommentDTO {

    private UUID commentId;

    private String content;

    private UUID authorId;

    private String author;

    private String profileImgUrl;

    private UUID postId;

    private UUID parentId;

    private LocalDateTime creationDate;

}
