package io.roa.secretmanger.Mapper;

import io.roa.secretmanger.DTO.projection.AuditLogProjection;
import io.roa.secretmanger.DTO.response.AuditLogResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuditMapper {

    @Mapping(source = "actor.name",  target = "actorName")
    @Mapping(source = "actor.email", target = "actorEmail")
    AuditLogResponse toDto(AuditLogProjection projection);
}
