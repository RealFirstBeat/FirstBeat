package com.my.firstbeat.web.domain.userGenre;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGenreRepository extends JpaRepository<UserGenre, Long> {
    List<UserGenre> findByUserId(Long userId);
}
