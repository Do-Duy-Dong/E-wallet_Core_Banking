package com.example.user_service.controller;

import com.example.user_service.dto.RequestChangeFund;
import com.example.user_service.dto.VerifyOtp;
import com.example.user_service.model.TypeEnum;
import com.example.user_service.service.DepositService;
import com.example.user_service.service.LinkedBankService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LinkBankController.class)
public class LinkBankControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LinkedBankService linkedBankService;

    @MockBean
    private DepositService depositService;

    @MockBean
    private com.example.user_service.config.JWT_config.JWTService jwtService;

    @MockBean
    private com.example.user_service.service.UserDetailService userDetailService;

    @MockBean
    private org.springframework.data.redis.core.RedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "testUser")
    public void testGetOtpDeposit() throws Exception {
        RequestChangeFund request = RequestChangeFund.builder()
                .requestId("req-123")
                .linkBankNo("bank-001")
                .accountNumber("acc-123")
                .type(TypeEnum.DEPOSIT)
                .amount(1000L)
                .signature("sig-123")
                .build();

        String expectedRequestId = "req-123";

        when(depositService.depositFundRequest(any(RequestChangeFund.class), anyString()))
                .thenReturn(expectedRequestId);

        mockMvc.perform(post("/api/linked-bank/getOtpDeposit")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP sent to your email"))
                .andExpect(jsonPath("$.requestId").value(expectedRequestId));
    }

    @Test
    @WithMockUser(username = "testUser")
    public void testConfirmDeposit() throws Exception {
        VerifyOtp verifyOtp = new VerifyOtp();
        verifyOtp.setRequestId("req-123");
        verifyOtp.setOtp("123456");

        doNothing().when(depositService).sendDepositFundToBank(any(VerifyOtp.class), anyString());

        mockMvc.perform(post("/api/linked-bank/confirmDeposit")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyOtp)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Deposit successful"));
    }
}
