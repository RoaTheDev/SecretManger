package io.roa.secretmanger.Repo;


import io.roa.secretmanger.DTO.ProjectSummaryProjection;
import io.roa.secretmanger.DTO.projection.MemberProjection;
import io.roa.secretmanger.Model.Entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepo extends JpaRepository<Project, UUID> {

    @Query(
            value = """
            SELECT p.id            AS id,
                   p.name          AS name,
                   p.description   AS description,
                   p.created_at    AS createdAt,
                   COUNT(m.user_id) AS memberCount
            FROM projects p
            LEFT JOIN project_members m ON m.project_id = p.id
            WHERE p.created_by = :userId
               OR p.id IN (
                   SELECT pm.project_id
                   FROM project_members pm
                   WHERE pm.user_id = :userId
               )
            GROUP BY p.id, p.name, p.description, p.created_at
            ORDER BY p.created_at DESC
            """,
            countQuery = """
                    SELECT COUNT(DISTINCT p.id)
                    FROM projects p
                    LEFT JOIN project_members m ON m.project_id = p.id
                    WHERE p.created_by = :userId
                       OR p.id IN (
                           SELECT pm.project_id
                           FROM project_members pm
                           WHERE pm.user_id = :userId
                       )
                    """,
            nativeQuery = true
    )
    Page<ProjectSummaryProjection> findSummariesForUser(@Param("userId") UUID userId,
                                                        Pageable pageable);
    @Query("""
            SELECT u.id    AS id,
                   u.name  AS name,
                   u.email AS email,
                   u.role  AS role
            FROM Project p
            JOIN p.members u
            WHERE p.id = :projectId
            """)
    List<MemberProjection> findMembersByProjectId(@Param("projectId") UUID projectId);

    @Query("""
            SELECT COUNT(p) > 0 FROM Project p
            LEFT JOIN p.members m
            WHERE p.id = :projectId
            AND (p.createdBy.id = :userId OR m.id = :userId)
            """)
    boolean isMember(@Param("projectId") UUID projectId, @Param("userId") UUID userId);
}
