package com.my.firstbeat.web.controller.playlist;

import com.my.firstbeat.web.service.SearchService;
import com.my.firstbeat.web.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/v2/searches/trending-searches")
    public ResponseEntity<ApiResult<List<String>>> getPopularSearches(
            @RequestParam(defaultValue = "5") int topN) {
        List<String> popularSearches = searchService.getTopSearches(topN);
        return ResponseEntity.ok(ApiResult.success(popularSearches));
    }
}
