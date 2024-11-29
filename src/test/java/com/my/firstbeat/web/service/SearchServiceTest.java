package com.my.firstbeat.web.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchServiceTest {

    private SearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new SearchService();
    }

    @Test
    @DisplayName("검색어 기록 및 인기 검색어 조회 테스트")
    void testRecordSearchAndGetTopSearches() {
        // Given
        searchService.recordSearch("Spring Boot");
        searchService.recordSearch("Spring Boot");
        searchService.recordSearch("Microservices");
        searchService.recordSearch("Microservices");
        searchService.recordSearch("Microservices");
        searchService.recordSearch("Docker");

        // When
        List<String> topSearches = searchService.getTopSearches(3);

        // Then
        assertEquals(3, topSearches.size());
        assertEquals("Microservices", topSearches.get(0));
        assertEquals("Spring Boot", topSearches.get(1));
        assertEquals("Docker", topSearches.get(2));
    }
}
