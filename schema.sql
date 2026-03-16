
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
                       id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       name          VARCHAR(100)  NOT NULL,
                       email         VARCHAR(150)  UNIQUE NOT NULL,
                       password_hash VARCHAR(255)  NOT NULL,
                       role          VARCHAR(30)   NOT NULL, -- ADMIN, TEAM_LEAD, PROJECT_MANAGER, DEVELOPER
                       is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
                       created_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
                       updated_at    TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email  ON users (email);
CREATE INDEX idx_users_role   ON users (role);

CREATE TABLE projects (
                          id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          name        VARCHAR(255) NOT NULL,
                          description TEXT,
                          created_by  UUID REFERENCES users (id),
                          created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                          updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE project_members (
                                 project_id UUID REFERENCES projects (id) ON DELETE CASCADE,
                                 user_id    UUID REFERENCES users (id)    ON DELETE CASCADE,
                                 PRIMARY KEY (project_id, user_id)
);

CREATE TABLE credentials (
                             id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             project_id      UUID REFERENCES projects (id) ON DELETE CASCADE,
                             name            VARCHAR(255) NOT NULL,
                             type            VARCHAR(100) NOT NULL, -- ENV_VAR, API_KEY, NGINX_CONFIG
                             encrypted_value TEXT         NOT NULL,
                             access_tier     VARCHAR(20)  NOT NULL, -- PROJECT, ADMIN
                             created_by      UUID REFERENCES users (id),
                             created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                             updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_credentials_project ON credentials (project_id);
CREATE INDEX idx_credentials_tier    ON credentials (access_tier);

CREATE TABLE shamir_shares (
                               id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               admin_id        UUID REFERENCES users (id) UNIQUE,
                               share_index     INT  NOT NULL,
                               encrypted_share TEXT NOT NULL,
                               created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                               updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE approval_requests (
                                   id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   credential_id   UUID REFERENCES credentials (id) ON DELETE CASCADE,
                                   requested_by    UUID REFERENCES users (id),
                                   access_tier     VARCHAR(20)  NOT NULL,
                                   status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, EXPIRED
                                   quorum_required INT          NOT NULL,
                                   quorum_reached  BOOLEAN      NOT NULL DEFAULT FALSE,
                                   created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                                   updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                                   resolved_at     TIMESTAMP
);

CREATE INDEX idx_approval_requests_credential ON approval_requests (credential_id);
CREATE INDEX idx_approval_requests_status     ON approval_requests (status);
CREATE INDEX idx_approval_requests_requester  ON approval_requests (requested_by);

CREATE TABLE approval_votes (
                                id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                request_id UUID REFERENCES approval_requests (id) ON DELETE CASCADE,
                                voter_id   UUID REFERENCES users (id),
                                vote       VARCHAR(10) NOT NULL, -- APPROVE, REJECT
                                voted_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
                                UNIQUE (request_id, voter_id)
);

CREATE INDEX idx_approval_votes_request ON approval_votes (request_id);

CREATE TABLE audit_logs (
                            id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            actor_id     UUID REFERENCES users (id),
                            action       VARCHAR(100) NOT NULL,
                            target_type  VARCHAR(50),
                            target_id    UUID,
                            metadata     JSONB,
                            performed_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_actor       ON audit_logs (actor_id);
CREATE INDEX idx_audit_logs_action      ON audit_logs (action);
CREATE INDEX idx_audit_logs_target      ON audit_logs (target_id);
CREATE INDEX idx_audit_logs_performed_at ON audit_logs (performed_at DESC);
ALTER TABLE approval_requests
    ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP;

ALTER TABLE approval_requests
    ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP;
CREATE INDEX IF NOT EXISTS idx_approval_requests_expires_at
    ON approval_requests (expires_at)
    WHERE expires_at IS NOT NULL;

ALTER TABLE credentials
    ADD COLUMN approval_policy VARCHAR(20) NOT NULL DEFAULT 'STANDARD';