package br.com.devisecenter.devise_center.modules.posts.controller;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.devisecenter.devise_center.modules.posts.dtos.CreatePost;
import br.com.devisecenter.devise_center.modules.posts.dtos.PostDTO;
import br.com.devisecenter.devise_center.modules.posts.service.PostService;
import br.com.devisecenter.devise_center.modules.upload.UploadFileService;
import br.com.devisecenter.devise_center.modules.users.entity.User;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/posts")
public class PostController {

    private PostService postService;

    private UploadFileService uploadService;

    public PostController(PostService postService, UploadFileService uploadService) {
        this.postService = postService;
        this.uploadService = uploadService;
    }

    @GetMapping
    public ResponseEntity<Page<PostDTO>> getAllPosts(
            @RequestParam(required = false) String author,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostDTO> posts = postService.findAll(author, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable("id") UUID postId) {
        PostDTO response = postService.findById(postId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = { "multipart/form-data", "application/octet-stream" })
    public ResponseEntity createPost(
            @Valid @RequestPart("data") CreatePost postDTO,
            @AuthenticationPrincipal User user,
            @RequestPart(value = "file", required = false) Optional<MultipartFile> file) {

        if (file.isPresent()) {
            Map<String, String> fileData = uploadService.uploadFile(file.get(), "/posts");
            postService.createPost(postDTO, user.getUserId(), fileData.get("url"), fileData.get("publicId"));
        } else {
            postService.createPost(postDTO, user.getUserId(), null, null);
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity updatePostById(
            @AuthenticationPrincipal User user,
            @PathVariable("id") UUID postId,
            @Valid @RequestPart("data") CreatePost postDTO,
            @RequestPart("file") Optional<MultipartFile> file) {

        if (file.isPresent()) {
            postService.updatePost(postDTO, file.get(), postId, user.getUserId());
        } else {
            postService.updatePost(postDTO, null, postId, user.getUserId());
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deletePost(@PathVariable("id") UUID postId, @AuthenticationPrincipal User user) {
        postService.deletePost(postId, user.getUserId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
