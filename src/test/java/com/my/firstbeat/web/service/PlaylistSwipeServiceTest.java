package com.my.firstbeat.web.service;

import com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse;
import com.my.firstbeat.web.domain.playlist.Playlist;
import com.my.firstbeat.web.domain.playlistTrack.PlaylistTrackRepository;
import com.my.firstbeat.web.domain.track.Track;
import com.my.firstbeat.web.domain.track.TrackRepository;
import com.my.firstbeat.web.dummy.DummyObject;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.controller.track.dto.response.TrackRecommendationResponse;
import com.my.firstbeat.web.ex.BusinessException;
import com.my.firstbeat.web.ex.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlaylistSwipeServiceTest extends DummyObject {

	@InjectMocks
	private PlaylistSwipeService playlistSwipeService;

	@Mock
	private RecommendationService recommendationService;

	@Mock
	private PlaylistService playlistService;

	@Mock
	private TrackRepository trackRepository;

	@Mock
	private PlaylistTrackRepository playlistTrackRepository;

	public PlaylistSwipeServiceTest() {
		MockitoAnnotations.openMocks(this);
	}

	// LikeTrack 성공 테스트
	@Test
	@DisplayName("LikeTrack 성공 시 트랙을 플레이리스트에 추가한다")
	void testLikeTrack_Success() {
		// Given: 더미 데이터 생성
		User mockUser = mockUserWithId(1L);
		String spotifyTrackId = "12345";
		Track mockTrack = mockTrack(1L, spotifyTrackId);
		Playlist mockPlaylist = mockPlaylist(mockUser, true);

		// Mock 설정
		when(playlistService.getDefaultPlaylist(mockUser.getId())).thenReturn(mockPlaylist);
		when(trackRepository.findBySpotifyTrackId(spotifyTrackId)).thenReturn(java.util.Optional.of(mockTrack));
		when(playlistTrackRepository.existsByPlaylistAndTrack(mockPlaylist, mockTrack)).thenReturn(false);

		// When: likeTrack 호출
		String result = playlistSwipeService.likeTrack(mockUser, spotifyTrackId);

		// Then: 결과 검증
		assertEquals("곡이 성공적으로 플레이리스트에 추가되었습니다", result);
		verify(playlistTrackRepository, times(1)).save(any());
	}

	// LikeTrack 실패 - 이미 플레이리스트에 존재
	@Test
	@DisplayName("LikeTrack 실패 - 트랙이 이미 플레이리스트에 존재")
	void testLikeTrack_TrackAlreadyExists() {
		// Given: 더미 데이터 생성
		User mockUser = mockUserWithId(1L);
		String spotifyTrackId = "12345";
		Track mockTrack = mockTrack(1L, spotifyTrackId);
		Playlist mockPlaylist = mockPlaylist(mockUser, true);

		// Mock 설정
		when(playlistService.getDefaultPlaylist(mockUser.getId())).thenReturn(mockPlaylist);
		when(trackRepository.findBySpotifyTrackId(String.valueOf(Long.valueOf(spotifyTrackId)))).thenReturn(java.util.Optional.of(mockTrack));
		when(playlistTrackRepository.existsByPlaylistAndTrack(mockPlaylist, mockTrack)).thenReturn(true);

		// When & Then: 예외 검증
		BusinessException exception = assertThrows(BusinessException.class, () ->
			playlistSwipeService.likeTrack(mockUser, spotifyTrackId)
		);
		assertEquals(ErrorCode.DUPLICATE_PLAYLIST_TITLE, exception.getErrorCode());
	}

	// SkipTrack 성공 테스트
	@Test
	@DisplayName("SkipTrack 성공 시 트랙을 스킵한다")
	void testSkipTrack_Success() {
		// Given: 테스트를 위한 Mock 데이터와 설정 준비
		TrackSearchResponse.TrackResponse.ArtistResponse mockArtist =
			new TrackSearchResponse.TrackResponse.ArtistResponse("Test Artist");

		TrackSearchResponse.TrackResponse mockTrackResponse = TrackSearchResponse.TrackResponse.builder()
			.id("12345")
			.trackName("Test Track")
			.isPlayable(true)
			.previewUrl("http://preview.url")
			.name("Test Album")
			.albumCoverUrl("http://album.cover.url")
			.artists(mockArtist)
			.build();

		TrackRecommendationResponse mockRecommendation = new TrackRecommendationResponse(mockTrackResponse);

		// Mock 설정
		when(recommendationService.getRecommendations(mockUser().getId())).thenReturn(mockRecommendation);
		when(recommendationService.removeTrackFromRecommendations(mockUser().getId(),"12345")).thenReturn(true);

		// When: skipTrack 메서드 호출
		String result = playlistSwipeService.skipTrack(mockUser(), "12345");

		// Then: 결과 검증 및 Mock 호출 확인
		assertEquals("추천 트랙이 성공적으로 스킵되었습니다.", result);
		verify(recommendationService, times(1)).removeTrackFromRecommendations(mockUser().getId(), "12345");
	}

	@Test
	@DisplayName("추천 트랙과 스킵하려는 트랙 ID 불일치 - 예외 발생")
	void testSkipTrack_TrackNotInRecommendations() {
		// Given
		User mockUser = mockUserWithId(1L);
		TrackSearchResponse.TrackResponse.ArtistResponse mockArtist =
			new TrackSearchResponse.TrackResponse.ArtistResponse("Test Artist");

		TrackSearchResponse.TrackResponse mockTrackResponse = TrackSearchResponse.TrackResponse.builder()
			.id("12345")
			.trackName("Test Track")
			.isPlayable(true)
			.previewUrl("http://preview.url")
			.name("Test Album")
			.albumCoverUrl("http://album.cover.url")
			.artists(mockArtist)
			.build();

		TrackRecommendationResponse mockRecommendation = new TrackRecommendationResponse(mockTrackResponse);

		// Mock 설정
		when(recommendationService.getRecommendations(mockUser.getId())).thenReturn(mockRecommendation);

		// When & Then
		BusinessException exception = assertThrows(BusinessException.class, () ->
			playlistSwipeService.skipTrack(mockUser,"67890") // 다른 트랙 ID
		);

		assertEquals(ErrorCode.NO_NEW_RECOMMENDATIONS_AVAILABLE, exception.getErrorCode());
		verify(recommendationService, times(1)).getRecommendations(mockUser.getId());
		verify(recommendationService, never()).removeTrackFromRecommendations(anyLong(), anyString());
	}
}