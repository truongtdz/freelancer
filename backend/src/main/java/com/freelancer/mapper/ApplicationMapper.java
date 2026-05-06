package com.freelancer.mapper;

import com.freelancer.dto.response.ApplicationListItemResponse;
import com.freelancer.dto.response.ApplicationResponse;
import com.freelancer.entity.JobApplication;
import com.freelancer.entity.User;
import com.freelancer.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {UserMapper.class},
        unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public abstract class ApplicationMapper {

    // ---------- Full response ----------

    @Mapping(source = "app.id",                 target = "id")
    @Mapping(source = "app.jobId",              target = "jobId")
    @Mapping(source = "jobTitle",               target = "jobTitle")
    @Mapping(source = "freelancer",             target = "freelancer")
    @Mapping(source = "profile.ratingAvg",      target = "freelancerRating")
    @Mapping(source = "profile.totalJobsDone",  target = "freelancerCompletedJobs")
    @Mapping(source = "app.coverLetter",        target = "coverLetter")
    @Mapping(source = "app.proposedBudget",     target = "proposedPrice")
    @Mapping(source = "app.proposedDurationDays", target = "estimatedDays")
    @Mapping(source = "app.attachmentUrl",      target = "attachmentUrl")
    @Mapping(source = "app.status",             target = "status")
    @Mapping(source = "app.appliedAt",          target = "createdAt")
    @Mapping(source = "app.respondedAt",        target = "updatedAt")
    public abstract ApplicationResponse toResponse(JobApplication app,
                                                   User freelancer,
                                                   UserProfile profile,
                                                   String jobTitle);

    // ---------- List item response ----------

    @Mapping(source = "app.id",                 target = "id")
    @Mapping(source = "app.jobId",              target = "jobId")
    @Mapping(source = "jobTitle",               target = "jobTitle")
    @Mapping(source = "freelancer",             target = "freelancer")
    @Mapping(source = "profile.ratingAvg",      target = "freelancerRating")
    @Mapping(source = "profile.totalJobsDone",  target = "freelancerCompletedJobs")
    @Mapping(target = "coverLetterPreview",
             expression = "java(truncate(app.getCoverLetter(), 150))")
    @Mapping(source = "app.proposedBudget",     target = "proposedPrice")
    @Mapping(source = "app.proposedDurationDays", target = "estimatedDays")
    @Mapping(source = "app.attachmentUrl",      target = "attachmentUrl")
    @Mapping(source = "app.status",             target = "status")
    @Mapping(source = "app.appliedAt",          target = "createdAt")
    public abstract ApplicationListItemResponse toListItem(JobApplication app,
                                                           User freelancer,
                                                           UserProfile profile,
                                                           String jobTitle);

    protected String truncate(String text, int maxLen) {
        if (text == null) return null;
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "…";
    }
}
