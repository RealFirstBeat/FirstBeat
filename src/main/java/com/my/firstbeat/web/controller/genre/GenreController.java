package com.my.firstbeat.web.controller.genre;

import com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse;
import com.my.firstbeat.web.service.GenreService;
import com.my.firstbeat.web.service.TrackService;
import com.my.firstbeat.web.util.api.ApiResult;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class GenreController {

    private final TrackService trackService;
    private final GenreService genreService;

    @GetMapping("/api/v1/search/tracks")
    public ResponseEntity<ApiResult<TrackSearchResponse>> searchTrackList(@RequestParam(value = "genre", required = true) String genre,
                                                                          @RequestParam(value = "page", required = false, defaultValue = "0") @PositiveOrZero Long page,
                                                                          @RequestParam(value = "limit", required = false, defaultValue = "20") @Positive Long limit){
        return ResponseEntity.ok(ApiResult.success(trackService.searchTrackList(genre)));
    }

    @PostMapping("/api/v1/genres")
    public ResponseEntity<ApiResult<String>> getAndUpdateGenreList(){
        genreService.updateGenreList();
        return ResponseEntity.ok(ApiResult.success("Spotify에서 제공하는 장르 목록 저장 완료"));
    }

}
