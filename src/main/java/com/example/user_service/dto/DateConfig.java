package com.example.user_service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class DateConfig {
    private LocalDateTime startRange;
    private LocalDateTime endRange;
}
