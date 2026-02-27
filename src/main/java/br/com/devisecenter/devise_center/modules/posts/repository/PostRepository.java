package br.com.devisecenter.devise_center.modules.posts.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.devisecenter.devise_center.modules.posts.entity.Post;

public interface PostRepository extends JpaRepository<Post, UUID> {

    Page<Post> findByAuthor_UserId(UUID authorId, Pageable pageable);

}
