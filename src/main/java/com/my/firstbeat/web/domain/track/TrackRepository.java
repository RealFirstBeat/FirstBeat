package com.my.firstbeat.web.domain.track;

import java.util.Optional;
import com.my.firstbeat.web.domain.user.User;
import com.my.firstbeat.web.domain.playlist.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrackRepository extends JpaRepository<Track, Long> {

    @Query("select t from Track t join PlaylistTrack pt on pt.track = t where pt.playlist = :playlist order by pt.createdAt desc")
    Page<Track> findAllByPlaylist(@Param("playlist")Playlist playlist, Pageable pageable);

    @Query("select case when count(t) > 0 then true else false end " +
            "from Track t " +
            "join PlaylistTrack pt on pt.track = t " +
            "join Playlist p on pt.playlist = p " +
            "where p.user = :user and t.spotifyTrackId = :spotifyTrackId")
    boolean existsInUserPlaylist(@Param("user") User user, @Param("spotifyTrackId") String spotifyTrackId);

    Optional<Track> findBySpotifyTrackId(String spotifyTrackId);
}
