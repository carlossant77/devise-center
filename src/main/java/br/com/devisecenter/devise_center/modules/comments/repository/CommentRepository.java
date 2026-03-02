package br.com.devisecenter.devise_center.modules.comments.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.devisecenter.devise_center.modules.comments.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    Page<Comment> findByPost_PostId(UUID postId, Pageable pageable);

    Page<Comment> findByAuthor_UserId(UUID authorId, Pageable pageable);

    Page<Comment> findByPost_PostIdAndAuthor_UserId(UUID postId, UUID authorId, Pageable pageable);

    Page<Comment> findByParent_CommentId(UUID parentId, Pageable pageable);

    void deleteByParent_CommentId(UUID parentId);

    void deleteByPost_PostId(UUID postId);
}
