package com.freelancer.mapper;

import com.freelancer.dto.response.JobAttachmentResponse;
import com.freelancer.entity.JobAttachment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface JobAttachmentMapper {
    JobAttachmentResponse toResponse(JobAttachment attachment);
    List<JobAttachmentResponse> toResponseList(List<JobAttachment> attachments);
}
