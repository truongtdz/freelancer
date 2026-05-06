package com.freelancer.mapper;

import com.freelancer.dto.request.JobCreateRequest;
import com.freelancer.dto.response.JobDetailResponse;
import com.freelancer.dto.response.JobListItemResponse;
import com.freelancer.entity.*;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring",
        uses = {CategoryMapper.class, SkillMapper.class, UserMapper.class, JobAttachmentMapper.class})
public abstract class JobMapper {

    @Mapping(source = "job.id",          target = "id")
    @Mapping(source = "job.title",       target = "title")
    @Mapping(source = "job.budgetMin",   target = "budgetMin")
    @Mapping(source = "job.budgetMax",   target = "budgetMax")
    @Mapping(source = "job.budgetType",  target = "budgetType")
    @Mapping(source = "job.workMode",    target = "workMode")
    @Mapping(source = "job.status",      target = "status")
    @Mapping(source = "job.location",    target = "location")
    @Mapping(source = "job.deadline",    target = "deadline")
    @Mapping(source = "job.createdAt",   target = "createdAt")
    @Mapping(source = "client",          target = "client")
    @Mapping(source = "category",        target = "category")
    @Mapping(source = "skills",          target = "skills")
    @Mapping(source = "applicationCount", target = "applicationCount")
    @Mapping(target = "shortDescription",
             expression = "java(truncate(job.getDescription(), 200))")
    public abstract JobListItemResponse toListItem(Job job, User client, Category category,
                                                   Set<Skill> skills, long applicationCount);

    @Mapping(source = "job.id",           target = "id")
    @Mapping(source = "job.title",        target = "title")
    @Mapping(source = "job.description",  target = "description")
    @Mapping(source = "job.budgetMin",    target = "budgetMin")
    @Mapping(source = "job.budgetMax",    target = "budgetMax")
    @Mapping(source = "job.budgetType",   target = "budgetType")
    @Mapping(source = "job.workMode",     target = "workMode")
    @Mapping(source = "job.status",       target = "status")
    @Mapping(source = "job.location",     target = "location")
    @Mapping(source = "job.deadline",     target = "deadline")
    @Mapping(source = "job.createdAt",    target = "createdAt")
    @Mapping(source = "job.updatedAt",    target = "updatedAt")
    @Mapping(source = "client",           target = "client")
    @Mapping(source = "category",         target = "category")
    @Mapping(source = "skills",           target = "skills")
    @Mapping(source = "attachments",      target = "attachments")
    @Mapping(source = "applicationCount", target = "applicationCount")
    @Mapping(source = "canApply",         target = "canApply")
    @Mapping(source = "isOwner",          target = "owner")
    @Mapping(target = "shortDescription",
             expression = "java(truncate(job.getDescription(), 200))")
    public abstract JobDetailResponse toDetail(Job job, User client, Category category,
                                               Set<Skill> skills, List<JobAttachment> attachments,
                                               long applicationCount, boolean canApply, boolean isOwner);

    @Mapping(source = "clientId",        target = "clientId")
    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "status",          constant = "OPEN")
    @Mapping(target = "viewCount",       ignore = true)
    @Mapping(target = "applicationCount", ignore = true)
    @Mapping(target = "deletedAt",       ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "updatedAt",       ignore = true)
    @Mapping(target = "requirements",    ignore = true)
    @Mapping(target = "durationDays",    ignore = true)
    public abstract Job toEntity(JobCreateRequest req, Long clientId);

    protected String truncate(String text, int maxLen) {
        if (text == null) return null;
        return text.length() > maxLen ? text.substring(0, maxLen) : text;
    }
}
