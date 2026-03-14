package io.roa.secretmanger.Mapper;

import io.roa.secretmanger.DTO.ProjectSummaryProjection;
import io.roa.secretmanger.DTO.projection.MemberProjection;
import io.roa.secretmanger.DTO.request.ProjectDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectDto.ProjectSummary toSummary(ProjectSummaryProjection projection);

    ProjectDto.MemberSummary toMemberSummary(MemberProjection projection);
}
