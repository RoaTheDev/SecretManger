package io.roa.secretmanger.Repo;


import io.roa.secretmanger.DTO.projection.UserSummaryProjection;
import io.roa.secretmanger.Model.Entity.User;
import io.roa.secretmanger.Model.Value.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(UserRole role);

    Page<UserSummaryProjection> findAllProjectedBy(Pageable pageable);

    @Query("""
            SELECT u FROM User u
            WHERE u.role = :role
            AND u.isActive = true
            """)
    List<UserSummaryProjection> findActiveByRole(@Param("role") UserRole role);

    @Query("""
            SELECT u FROM User u
            WHERE u.role IN ('TEAM_LEAD', 'PROJECT_MANAGER', 'ADMIN')
            AND u.isActive = true
            """)
    List<UserSummaryProjection> findAllProjectApprovers();

    List<User> findAllByRole(UserRole role);
}