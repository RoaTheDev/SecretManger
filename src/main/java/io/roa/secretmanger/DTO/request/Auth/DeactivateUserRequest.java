package io.roa.secretmanger.DTO.request.Auth;

import java.util.Set;
import java.util.UUID;

public record DeactivateUserRequest(Set<UUID> adminIds) {}
