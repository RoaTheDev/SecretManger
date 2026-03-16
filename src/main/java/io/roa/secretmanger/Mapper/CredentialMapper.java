package io.roa.secretmanger.Mapper;


import io.roa.secretmanger.DTO.projection.CredentialDetailProjection;
import io.roa.secretmanger.DTO.projection.CredentialSummaryProjection;
import io.roa.secretmanger.DTO.response.Shamir.CredentialDetail;
import io.roa.secretmanger.DTO.response.Shamir.CredentialSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CredentialMapper {

    CredentialSummary toSummary(CredentialSummaryProjection projection);

    @Mapping(source = "createdBy.name", target = "createdBy")
    CredentialDetail toDetail(CredentialDetailProjection projection);
}