package com.example.user_service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ResponsePageBase<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
}
