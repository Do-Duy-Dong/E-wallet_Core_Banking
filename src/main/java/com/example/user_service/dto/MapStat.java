package com.example.user_service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MapStat {
    private int total=0;
    private Long amount=0L;
}
