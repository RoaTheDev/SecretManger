package io.roa.secretmanger.Mapper;

import io.roa.secretmanger.DTO.projection.UserSummaryProjection;
import io.roa.secretmanger.DTO.response.UserSummary;
import io.roa.secretmanger.Model.Entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "id",    target = "id")
    @Mapping(source = "name",  target = "name")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "role",  target = "role")
    UserSummary toDto(UserSummaryProjection projection);

    UserSummary toDto(User user);
}
