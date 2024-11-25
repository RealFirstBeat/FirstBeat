package com.my.firstbeat.web.domain.playlist;

import com.my.firstbeat.web.domain.track.Track;
import com.my.firstbeat.web.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    @Query("select t from Playlist p " +
            "left join PlaylistTrack pt on pt.playlist = p " +
            "left join Track t on pt.track = t " +
            "where p.user = :user")
    List<Track> findAllTrackByUser (@Param("user") User user);
}
