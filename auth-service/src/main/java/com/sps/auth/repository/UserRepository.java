package com.sps.auth.repository;

import com.sps.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByCedula(String cedula);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}