package com.my.firstbeat.web.domain.track;

import com.my.firstbeat.web.domain.playlist.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

public interface TrackRepository extends JpaRepository<Track, Long> {

    @Query("select t from Track t join PlaylistTrack pt on pt.playlist = :playlist order by pt.createdAt desc")
    Page<Track> findAllByPlaylist(@Param("playlist")Playlist playlist, Pageable pageable);
}
