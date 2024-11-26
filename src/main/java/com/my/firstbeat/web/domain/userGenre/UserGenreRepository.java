package com.my.firstbeat.web.domain.userGenre;

import com.my.firstbeat.web.domain.genre.Genre;
import com.my.firstbeat.web.domain.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserGenreRepository extends JpaRepository<UserGenre, Long> {

}
