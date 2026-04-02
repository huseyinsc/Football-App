package com.huseyinsacikay.repository;

import com.huseyinsacikay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // Email ile kullanıcıyı bulmak için (Login ve Register kontrollerinde kullanılacak)
    Optional<User> findByEmail(String email);

    // Kullanıcı adıyla arama yapmak için
    Optional<User> findByUsername(String username);

    // Bir email veya username'in var olup olmadığını hızlıca kontrol etmek için
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
}