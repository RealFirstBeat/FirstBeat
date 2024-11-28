package com.my.firstbeat.web.domain.playlist;


import com.my.firstbeat.web.domain.track.Track;
import com.my.firstbeat.web.domain.user.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;


public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    boolean existsByUserAndTitle(User user, String title);


    @Query("select t from Playlist p " +
            "join PlaylistTrack pt on pt.playlist = p " +
            "join Track t on pt.track = t " +
            "where p.user = :user " +
            "order by function('random') ")
    List<Track> findAllTrackByUser (@Param("user") User user, Pageable pageable);

	//특정 사용자의 디폴트 플레이리스트 조회
	Optional<Playlist> findByUserIdAndIsDefault(Long userId, boolean isDefault);


    Page<Playlist> findByUserId(Long userId, Pageable pageable);

	Optional<Playlist> findByIdAndUserId(Long playlistId, Long userId);

}
