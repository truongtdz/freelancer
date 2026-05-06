package com.freelancer.repository;

import com.freelancer.entity.User;
import com.freelancer.entity.enums.UserRole;
import com.freelancer.entity.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Page<User> findByRoleAndStatus(UserRole role, UserStatus status, Pageable pageable);

    Page<User> findByDeletedAtIsNull(Pageable pageable);

    Page<User> findByRoleAndDeletedAtIsNull(UserRole role, Pageable pageable);

    List<User> findByRole(UserRole role);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    long countByStatusAndDeletedAtIsNull(UserStatus status);

    long countByDeletedAtIsNull();

    long countByRoleNotAndDeletedAtIsNull(UserRole role);

    long countByRoleNotAndStatusAndDeletedAtIsNull(UserRole role, UserStatus status);
}
