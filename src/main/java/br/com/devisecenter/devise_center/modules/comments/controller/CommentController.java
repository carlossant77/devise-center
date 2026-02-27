package br.com.devisecenter.devise_center.modules.comments.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.devisecenter.devise_center.modules.comments.dtos.CommentDTO;
import br.com.devisecenter.devise_center.modules.comments.dtos.CreateComment;
import br.com.devisecenter.devise_center.modules.comments.service.CommentService;
import br.com.devisecenter.devise_center.modules.users.entity.User;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<Page<CommentDTO>> getAllComments(
            @RequestParam(required = false) String author,
            @RequestParam(required = false) UUID post,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CommentDTO> comments = commentService.findAll(author, post, pageable);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentDTO> getCommentById(@PathVariable("id") UUID commentId) {
        CommentDTO comment = commentService.findById(commentId);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/{id}/replies")
    public ResponseEntity<Page<CommentDTO>> getRepliesByParentId(
            @PathVariable("id") UUID parentId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CommentDTO> replies = commentService.findReplies(parentId, pageable);
        return ResponseEntity.ok(replies);
    }

    @PostMapping
    public ResponseEntity createComment(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateComment commentDTO) {
        commentService.createComment(commentDTO, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity updateCommentById(
            @PathVariable("id") UUID commentId,
            @AuthenticationPrincipal User userDetails,
            @Valid @RequestBody CreateComment commentDTO) {

        commentService.updateComment(commentDTO, commentId, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteComment(
            @PathVariable("id") UUID commentId,
            @AuthenticationPrincipal User userDetails) {
        commentService.deleteComment(commentId, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
