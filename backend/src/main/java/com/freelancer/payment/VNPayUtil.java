package com.freelancer.payment;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@UtilityClass
public class VNPayUtil {

    private static final DateTimeFormatter VNP_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    /** HMAC-SHA512 ký chuỗi hashData bằng secretKey (UTF-8). */
    public static String hmacSHA512(String secretKey, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(raw.length * 2);
            for (byte b : raw) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot compute HMAC-SHA512", e);
        }
    }

    /**
     * Build hashData từ params (sort alphabet, exclude vnp_SecureHash / vnp_SecureHashType),
     * URL-encode value theo chuẩn VNPay 2.1.0.
     */
    public static String buildHashData(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            if ("vnp_SecureHash".equals(key) || "vnp_SecureHashType".equals(key)) continue;
            String value = params.get(key);
            if (value != null && !value.isEmpty()) {
                if (!sb.isEmpty()) sb.append('&');
                sb.append(URLEncoder.encode(key, StandardCharsets.US_ASCII))
                  .append('=')
                  .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
            }
        }
        return sb.toString();
    }

    /** Build query string từ params (sort alphabet, append signature). */
    public static String buildQueryString(Map<String, String> params, String hashSecret) {
        String hashData = buildHashData(params);
        String signature = hmacSHA512(hashSecret, hashData);
        return hashData + "&vnp_SecureHash=" + signature;
    }

    /** Lấy IP address của client từ request (hỗ trợ reverse proxy). */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Multiple IPs → take first
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "127.0.0.1";
    }

    /** Tạo vnp_TxnRef duy nhất: timestamp-contractId */
    public static String generateTxnRef(Long contractId) {
        return System.currentTimeMillis() + "-" + contractId;
    }

    /** Format LocalDateTime → VNPay yyyyMMddHHmmss (giờ Việt Nam GMT+7) */
    public static String formatDate(LocalDateTime dt) {
        return ZonedDateTime.of(dt, ZoneId.systemDefault())
                .withZoneSameInstant(VN_ZONE)
                .format(VNP_DATE_FMT);
    }

    /**
     * Verify chữ ký từ VNPay callback params.
     * @return true nếu chữ ký hợp lệ
     */
    public static boolean verifySignature(Map<String, String> params, String hashSecret) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isEmpty()) return false;
        String computed = hmacSHA512(hashSecret, buildHashData(params));
        return computed.equalsIgnoreCase(receivedHash);
    }

    /** Chuyển Map<String, String[]> từ HttpServletRequest.getParameterMap() → Map<String, String>. */
    public static Map<String, String> extractParams(Map<String, String[]> parameterMap) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                result.put(entry.getKey(), values[0]);
            }
        }
        return result;
    }
}
