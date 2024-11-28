package com.my.firstbeat.web.domain.playlist;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PopularSearchCache {

    private final Map<String, Integer> searchCounts = new ConcurrentHashMap<>();

    // 검색어 추가 및 횟수 증가
    public void addSearch(String keyword) {
        searchCounts.merge(keyword, 1, Integer::sum);
    }

    // 상위 n개의 인기 검색어 조회
    public List<String> getTopSearches(int n) {
        return searchCounts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())) // 검색 횟수 기준으로 내림차순 정렬
                .limit(n)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
