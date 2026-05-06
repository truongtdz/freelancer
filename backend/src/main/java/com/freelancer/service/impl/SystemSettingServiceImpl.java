package com.freelancer.service.impl;

import com.freelancer.entity.SystemSetting;
import com.freelancer.repository.SystemSettingRepository;
import com.freelancer.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSettingServiceImpl implements SystemSettingService {

    private final SystemSettingRepository systemSettingRepository;

    @Override
    public String getString(String key, String defaultValue) {
        return systemSettingRepository.findBySettingKey(key)
                .map(SystemSetting::getSettingValue)
                .orElse(defaultValue);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        String value = getString(key, null);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("SystemSetting '{}' cannot be parsed as int, using default {}", key, defaultValue);
            return defaultValue;
        }
    }

    @Override
    public BigDecimal getDecimal(String key, BigDecimal defaultValue) {
        String value = getString(key, null);
        if (value == null) return defaultValue;
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            log.warn("SystemSetting '{}' cannot be parsed as BigDecimal, using default {}", key, defaultValue);
            return defaultValue;
        }
    }

    @Override
    @Transactional
    public void update(String key, String value, Long updatedBy) {
        Optional<SystemSetting> existing = systemSettingRepository.findBySettingKey(key);
        SystemSetting setting = existing.orElseGet(() -> SystemSetting.builder().settingKey(key).build());
        setting.setSettingValue(value);
        setting.setUpdatedBy(updatedBy);
        setting.setUpdatedAt(LocalDateTime.now());
        systemSettingRepository.save(setting);
    }
}
