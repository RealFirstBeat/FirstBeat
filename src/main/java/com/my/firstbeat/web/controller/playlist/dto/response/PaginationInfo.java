package com.my.firstbeat.web.controller.playlist.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class PaginationInfo {
    private int page;
    private int limit;
    private int totalPages;
    private int totalItems;
}