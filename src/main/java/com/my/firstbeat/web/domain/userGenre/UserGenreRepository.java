package com.my.firstbeat.web.domain.userGenre;

import java.util.List;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserGenreRepository extends JpaRepository<UserGenre, Long> {
    @Query("SELECT ug FROM UserGenre ug JOIN FETCH ug.genre WHERE ug.user.id = :userId")
    List<UserGenre> findByUserIdWithGenre(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM UserGenre ug WHERE ug.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
