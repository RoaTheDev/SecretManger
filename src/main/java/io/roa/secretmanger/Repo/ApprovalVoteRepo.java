package io.roa.secretmanger.Repo;

import io.roa.secretmanger.Model.Entity.ApprovalVote;
import io.roa.secretmanger.Model.Value.VoteChoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApprovalVoteRepo extends JpaRepository<ApprovalVote, UUID> {

    long countByRequestIdAndVote(UUID requestId, VoteChoice vote);
    boolean existsByRequestIdAndVoterId(UUID requestId, UUID voterId);
}