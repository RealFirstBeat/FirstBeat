package com.my.firstbeat.web.domain.playlistTrack;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.my.firstbeat.web.domain.playlist.Playlist;
import com.my.firstbeat.web.domain.track.Track;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Long> {

	Optional<Object> findByPlaylistAndTrack(Playlist defaultPlaylist, Track track);
	boolean existsByPlaylistAndTrack(Playlist defaultPlaylist, Track track);

	void deleteByPlaylistAndTrack(Playlist playlist, Track track);
}
