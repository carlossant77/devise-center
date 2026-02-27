package br.com.devisecenter.devise_center.modules.users.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.devisecenter.devise_center.modules.users.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    UserDetails findByUsername(String username);

    Optional<User> readByUsername(String username);

}
