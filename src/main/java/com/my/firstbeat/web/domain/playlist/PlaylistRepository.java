package com.my.firstbeat.web.domain.playlist;


import com.my.firstbeat.web.domain.track.Track;
import com.my.firstbeat.web.domain.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
  
    boolean existsByUserAndTitle(User user, String title);


    @Query("select t from Playlist p " +
            "left join PlaylistTrack pt on pt.playlist = p " +
            "left join Track t on pt.track = t " +
            "where p.user = :user")
    List<Track> findAllTrackByUser (@Param("user") User user);

	//특정 사용자의 디폴트 플레이리스트 조회
	Optional<Playlist> findByUserIdAndIsDefault(Long userId, boolean isDefault);

}
