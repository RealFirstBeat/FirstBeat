package com.my.firstbeat.web.domain.playlist;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

	//특정 사용자의 디폴트 플레이리스트 조회
	Optional<Playlist> findByUserIdAndIsDefault(Long userId, boolean isDefault);
}
