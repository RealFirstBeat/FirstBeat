package com.my.firstbeat.web.dummy;

import com.my.firstbeat.web.domain.genre.Genre;
import com.my.firstbeat.web.domain.playlist.Playlist;
import com.my.firstbeat.web.domain.track.Track;
import com.my.firstbeat.web.domain.user.Role;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.domain.userGenre.UserGenre;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.ArrayList;
import java.util.List;

public class DummyObject {

    protected String mockUserPassword = "test1234";

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

    protected User mockUser(){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        return User.builder()
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

    protected Playlist mockPlaylist(Long id, User user) {
        return Playlist.builder()
            .id(id)
            .user(user)
            .title("Test Playlist " + id)
            .description("Test Description")
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
