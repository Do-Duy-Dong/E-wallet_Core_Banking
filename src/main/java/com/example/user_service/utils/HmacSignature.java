package com.example.user_service.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


public class HmacSignature {

    public static String calculateHMAC(
            String requestId,
            String fromAccount,
            String toAccount,
            long amount,
            String secretKey) {
        try {
            // Tạo instance thuật toán HmacSHA256
            Mac mac = Mac.getInstance("HmacSHA256");

            // khóa bí mật
            String data= requestId  + fromAccount + toAccount  + String.valueOf(amount);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKeySpec);

            // Băm dữ liệu
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Chuyển đổi sang chuỗi Hex
            return bytesToHex(rawHmac);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Lỗi tạo chữ ký: " + e.getMessage());
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
