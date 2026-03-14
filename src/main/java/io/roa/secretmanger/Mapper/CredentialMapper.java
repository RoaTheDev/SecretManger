package io.roa.secretmanger.Mapper;


import io.roa.secretmanger.DTO.projection.CredentialDetailProjection;
import io.roa.secretmanger.DTO.projection.CredentialSummaryProjection;
import io.roa.secretmanger.DTO.request.CredentialDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CredentialMapper {

    CredentialDto.CredentialSummary toSummary(CredentialSummaryProjection projection);

    @Mapping(source = "createdBy.name", target = "createdBy")
    CredentialDto.CredentialDetail toDetail(CredentialDetailProjection projection);
}