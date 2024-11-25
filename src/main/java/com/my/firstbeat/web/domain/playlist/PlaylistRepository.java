package com.my.firstbeat.web.domain.playlist;

import com.my.firstbeat.web.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    boolean existsByUserAndTitle(User user, String title);

}
