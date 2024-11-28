package com.my.firstbeat.web.service;

import com.my.firstbeat.web.domain.playlist.PopularSearchCache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final PopularSearchCache popularSearchCache = new PopularSearchCache();

    public void recordSearch(String keyword) {
        popularSearchCache.addSearch(keyword);
    }

    public List<String> getTopSearches(int limit) {
        return popularSearchCache.getTopSearches(limit);
    }
}
