package com.freelancer.mapper;

import com.freelancer.dto.response.UserSummaryResponse;
import com.freelancer.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserSummaryResponse toSummary(User user);
}
