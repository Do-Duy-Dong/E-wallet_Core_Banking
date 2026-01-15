package com.example.user_service.utils;

public class GenerateOtp {
    public static String generateOtp(){
        int otpLength = 6;
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            int digit = (int) (Math.random() * 10);
            otp.append(digit);
        }
        return otp.toString();
    }
}
