package com.my.firstbeat.web.domain.genre;

import com.my.firstbeat.web.domain.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    @Query("select g from Genre g join UserGenre ug on ug.genre = g where ug.user = :user")
    List<Genre> findTop5GenresByUser(@Param("user") User user, Pageable pageable);
}
