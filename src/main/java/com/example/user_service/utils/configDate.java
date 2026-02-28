package com.example.user_service.utils;

import com.example.user_service.dto.DateConfig;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class configDate {
    public static DateConfig configMonth(LocalDateTime dateTime){
        LocalDateTime startRange= dateTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endRange= startRange.plusMonths(1);
       return DateConfig.builder()
               .startRange(startRange)
               .endRange(endRange)
               .build();
    }
    public static DateConfig configDay(LocalDateTime dateTime){
        LocalDateTime startRange= dateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endRange= startRange.plusDays(1);
        return DateConfig.builder()
                .startRange(startRange)
                .endRange(endRange)
                .build();
    }
}
