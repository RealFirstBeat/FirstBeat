package com.my.firstbeat.web.domain.playlistTrack;

import org.springframework.data.jpa.repository.JpaRepository;

import com.my.firstbeat.web.domain.playlist.Playlist;
import com.my.firstbeat.web.domain.track.Track;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Long> {

	void deleteByPlaylistAndTrack(Playlist playlist, Track track);
}
