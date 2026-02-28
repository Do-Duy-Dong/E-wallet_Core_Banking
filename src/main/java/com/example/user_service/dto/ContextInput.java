package com.example.user_service.dto;

import com.example.user_service.model.Account;
import com.example.user_service.model.RuleEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ContextInput {
    private RequestChangeFund requestChangeFund;
    private Account account;
    private RuleEnum ruleEnum;
}
