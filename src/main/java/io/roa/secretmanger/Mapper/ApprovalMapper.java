package io.roa.secretmanger.Mapper;

import io.roa.secretmanger.DTO.projection.ApprovalRequestSummaryProjection;
import io.roa.secretmanger.DTO.response.ApprovalRequest.ApprovalRequestSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface ApprovalMapper {

    @Mapping(source = "credential.id", target = "credentialId")
    @Mapping(source = "credential.name", target = "credentialName")
    @Mapping(source = "requestedBy.name", target = "requestedBy")
    @Mapping(target = "approveCount", ignore = true)
    @Mapping(target = "rejectCount", ignore = true)
    ApprovalRequestSummary toSummary(ApprovalRequestSummaryProjection projection);
}