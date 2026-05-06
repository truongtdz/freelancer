package com.freelancer.service;

import java.math.BigDecimal;

public interface SystemSettingService {

    /** Lấy setting dạng String (trả defaultValue nếu không tìm thấy). */
    String getString(String key, String defaultValue);

    /** Lấy setting dạng int (trả defaultValue nếu không tìm thấy hoặc lỗi parse). */
    int getInt(String key, int defaultValue);

    /** Lấy setting dạng BigDecimal (trả defaultValue nếu không tìm thấy hoặc lỗi parse). */
    BigDecimal getDecimal(String key, BigDecimal defaultValue);

    /** Cập nhật setting (ADMIN). */
    void update(String key, String value, Long updatedBy);
}
