package io.roa.secretmanger.Mapper;

import io.roa.secretmanger.DTO.ProjectSummaryProjection;
import io.roa.secretmanger.DTO.projection.MemberProjection;
import io.roa.secretmanger.DTO.response.Project.MemberSummary;
import io.roa.secretmanger.DTO.response.Project.ProjectSummary;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectSummary toSummary(ProjectSummaryProjection projection);

    MemberSummary toMemberSummary(MemberProjection projection);
}
