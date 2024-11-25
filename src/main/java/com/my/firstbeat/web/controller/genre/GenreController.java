package com.my.firstbeat.web.controller.genre;

import com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse;
import com.my.firstbeat.web.service.TrackService;
import com.my.firstbeat.web.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class GenreController {

    private final TrackService trackService;

    @GetMapping("/auth/search/track") //임시 api
    public ResponseEntity<ApiResult<TrackSearchResponse>> searchTrackList(@RequestParam(value = "genre", required = true) String genre,
                                                                          @RequestParam(value = "page", required = false, defaultValue = "0") Long page,
                                                                          @RequestParam(value = "limit", required = false, defaultValue = "20")  Long limit){
        return ResponseEntity.ok(ApiResult.success(trackService.searchTrackList(genre)));
    }
}
