package com.my.firstbeat.web.domain.genre;

import com.my.firstbeat.web.domain.user.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;
import java.util.List;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    Optional<Genre> findByName(String name);

    @Query("SELECT g.id FROM Genre g WHERE g.name IN :names")
    Set<Long> findIdsByNames(@Param("names") Set<String> names);

    @Query(value = "select g.* from genre g " +
            "join user_genre ug on ug.genre_id = g.id " +
            "where ug.user_id = ?1 " +
            "order by RANDOM() limit ?2",
            nativeQuery = true)
    List<Genre> findRandomGenresByUser(Long userId, int limit);
}
