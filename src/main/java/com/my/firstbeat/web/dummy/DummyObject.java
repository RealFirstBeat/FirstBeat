package com.my.firstbeat.web.dummy;

import java.util.ArrayList;
import java.util.List;

import com.my.firstbeat.client.spotify.dto.response.TrackSearchResponse;
import com.my.firstbeat.web.controller.track.dto.response.TrackRecommendationResponse;
import com.my.firstbeat.web.domain.genre.Genre;
import com.my.firstbeat.web.domain.playlist.Playlist;
import com.my.firstbeat.web.domain.track.Track;
import com.my.firstbeat.web.domain.playlist.Playlist;
import com.my.firstbeat.web.domain.playlistTrack.PlaylistTrack;
import com.my.firstbeat.web.domain.track.Track;
import com.my.firstbeat.web.domain.user.Role;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.domain.userGenre.UserGenre;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.ArrayList;
import java.util.List;

public class DummyObject {

    protected String mockUserPassword = "test1234";

	protected User mockUser(){
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		return User.builder()
			.name("test name")
			.email("test1234@naver.com")
			.password(encoder.encode(mockUserPassword))
			.role(Role.USER)
			.build();
	}

    protected User mockUserWithId(){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        return User.builder()
                .id(1L)
                .name("test name")
                .email("test1234@naver.com")
                .password(encoder.encode(mockUserPassword))
                .role(Role.USER)
                .build();
    }

    protected User mockUserWithId(Long id) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        return User.builder()
                .id(id)
                .name("test name")
                .email("test1234@naver.com")
                .password(encoder.encode(mockUserPassword))
                .role(Role.USER)
                .build();
    }

    protected List<UserGenre> mockUserGenres(User user) {
        List<UserGenre> userGenres = new ArrayList<>();

        Genre genre1 = Genre.builder().name("Rock").build();
        Genre genre2 = Genre.builder().name("Pop").build();

        UserGenre userGenre1 = UserGenre.builder().user(user).genre(genre1).build();
        UserGenre userGenre2 = UserGenre.builder().user(user).genre(genre2).build();

        userGenres.add(userGenre1);
        userGenres.add(userGenre2);

        return userGenres;
    }

	protected Playlist mockPlaylist(User user, boolean isDefault) {
		return Playlist.builder()
			.user(user)
			.title(isDefault ? "Default Playlist" : "Custom Playlist")
			.description(isDefault ? "This is the default playlist" : "This is a custom playlist")
			.isDefault(isDefault)
			.build();
	}

	protected Playlist mockPlaylist(Long id, User user) {
		return Playlist.builder()
			.id(id)
			.user(user)
			.title("Test Playlist " + id)
			.description("Test Description")
			.build();
	}

	protected Track mockTrack(Long id, String spotifyId) {
		return Track.builder()
			.id(id)
			.name("Test Track")
			.artistName("Test Artist")
			.spotifyTrackId(spotifyId)
			.build();
	}

	protected Track mockTrack(Long id) {
		return Track.builder()
			.id(id)
			.name("Test Track " + id)
			.albumCoverUrl("http://test.com/album" + id)
			.artistName("Artist " + id)
			.build();
	}
}

