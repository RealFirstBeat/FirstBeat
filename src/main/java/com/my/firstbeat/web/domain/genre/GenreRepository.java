package com.my.firstbeat.web.domain.genre;

import com.my.firstbeat.web.domain.user.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;
import java.util.List;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    @Query("SELECT g.id FROM Genre g WHERE g.name IN :names")
    Set<Long> findIdsByNames(@Param("names") Set<String> names);

    @Query("select g from Genre g join UserGenre ug on ug.genre = g where ug.user = :user " +
            "order by funcion('random')")
    List<Genre> findRandomGenresByUser(@Param("user") User user, Pageable pageable);
}
