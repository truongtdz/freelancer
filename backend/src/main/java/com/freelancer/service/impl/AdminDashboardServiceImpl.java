package com.freelancer.service.impl;

import com.freelancer.dto.response.DashboardStatsResponse;
import com.freelancer.entity.enums.ContractStatus;
import com.freelancer.entity.enums.DisputeStatus;
import com.freelancer.entity.enums.JobStatus;
import com.freelancer.entity.enums.TransactionType;
import com.freelancer.entity.enums.UserRole;
import com.freelancer.entity.enums.UserStatus;
import com.freelancer.repository.*;
import com.freelancer.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ContractRepository contractRepository;
    private final DisputeRepository disputeRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        long totalUsers       = userRepository.countByRoleNotAndDeletedAtIsNull(UserRole.ADMIN);
        long activeUsers      = userRepository.countByRoleNotAndStatusAndDeletedAtIsNull(UserRole.ADMIN, UserStatus.ACTIVE);
        long totalJobs        = jobRepository.countByDeletedAtIsNull();
        long openJobs         = jobRepository.countByStatusAndDeletedAtIsNull(JobStatus.OPEN);
        long totalContracts   = contractRepository.count();
        long inProgress       = contractRepository.countByStatus(ContractStatus.IN_PROGRESS);
        long pendingPayouts   = contractRepository.countByStatus(ContractStatus.CLIENT_CONFIRMED);
        long openDisputes     = disputeRepository.countByStatus(DisputeStatus.OPEN);

        BigDecimal totalRevenue = transactionRepository.sumByTypeSuccess(TransactionType.COMMISSION);
        BigDecimal totalEscrow  = transactionRepository.sumByTypeSuccess(TransactionType.ESCROW);

        List<DashboardStatsResponse.DailyRevenue> dailyRevenue = buildDailyRevenue();

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalJobs(totalJobs)
                .openJobs(openJobs)
                .totalContracts(totalContracts)
                .contractsInProgress(inProgress)
                .pendingPayouts(pendingPayouts)
                .openDisputes(openDisputes)
                .totalRevenue(totalRevenue)
                .totalEscrow(totalEscrow)
                .dailyRevenue(dailyRevenue)
                .build();
    }

    private List<DashboardStatsResponse.DailyRevenue> buildDailyRevenue() {
        LocalDate today = LocalDate.now();
        LocalDate from  = today.minusDays(6);

        List<Object[]> rows = transactionRepository.dailyDepositLast7Days(from);
        Map<String, BigDecimal> byDate = rows.stream()
                .collect(Collectors.toMap(
                        r -> r[0].toString(),
                        r -> new BigDecimal(r[1].toString())
                ));

        List<DashboardStatsResponse.DailyRevenue> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            String day = from.plusDays(i).toString();
            result.add(new DashboardStatsResponse.DailyRevenue(
                    day,
                    byDate.getOrDefault(day, BigDecimal.ZERO)
            ));
        }
        return result;
    }
}
