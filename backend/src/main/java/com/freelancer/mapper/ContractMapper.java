package com.freelancer.mapper;

import com.freelancer.dto.response.ContractResponse;
import com.freelancer.entity.Contract;
import com.freelancer.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {UserMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ContractMapper {

    @Mapping(source = "contract.id",               target = "id")
    @Mapping(source = "contract.contractCode",     target = "contractCode")
    @Mapping(source = "contract.jobId",            target = "jobId")
    @Mapping(source = "jobTitle",                  target = "jobTitle")
    @Mapping(source = "client",                    target = "client")
    @Mapping(source = "freelancer",                target = "freelancer")
    @Mapping(source = "contract.agreedPrice",      target = "agreedPrice")
    @Mapping(source = "contract.commissionRate",   target = "commissionRate")
    @Mapping(source = "contract.commissionAmount", target = "commissionAmount")
    @Mapping(source = "contract.netAmount",        target = "netAmount")
    @Mapping(source = "contract.startDate",        target = "startDate")
    @Mapping(source = "contract.endDate",          target = "endDate")
    @Mapping(source = "contract.status",           target = "status")
    @Mapping(source = "contract.createdAt",        target = "createdAt")
    public abstract ContractResponse toResponse(Contract contract,
                                                String jobTitle,
                                                User client,
                                                User freelancer);
}
