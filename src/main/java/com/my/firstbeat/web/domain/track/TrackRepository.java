package com.my.firstbeat.web.domain.track;

import java.util.Optional;

import com.my.firstbeat.web.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrackRepository extends JpaRepository<Track, Long> {


    @Query("select case when count(t) > 0 then true else false end " +
            "from Track t " +
            "join PlaylistTrack pt on pt.track = t " +
            "join Playlist p on pt.playlist = p " +
            "where p.user = :user and t.spotifyTrackId = :spotifyTrackId")
    boolean existsInUserPlaylist(@Param("user") User user, @Param("spotifyTrackId") String spotifyTrackId);

    Optional<Track> findBySpotifyTrackId(Long spotifyTrackId);
}
