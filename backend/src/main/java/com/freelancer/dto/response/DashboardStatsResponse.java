package com.freelancer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    private long totalUsers;
    private long activeUsers;
    private long totalJobs;
    private long openJobs;
    private long totalContracts;
    private long contractsInProgress;
    private long pendingPayouts;
    private long openDisputes;
    private BigDecimal totalRevenue;
    private BigDecimal totalEscrow;
    private List<DailyRevenue> dailyRevenue;

    @Data
    @AllArgsConstructor
    public static class DailyRevenue {
        private String date;
        private BigDecimal amount;
    }
}
