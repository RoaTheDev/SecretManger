package io.roa.secretmanger.Repo;

import io.roa.secretmanger.Model.Entity.ShamirShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShamirShareRepo extends JpaRepository<ShamirShare, UUID> {
    void deleteByAdminId(UUID adminId);
    Optional<ShamirShare> findByAdminId(UUID adminId);

    boolean existsByAdminId(UUID adminId);

    List<ShamirShare> findAllByOrderByShareIndexAsc();
}