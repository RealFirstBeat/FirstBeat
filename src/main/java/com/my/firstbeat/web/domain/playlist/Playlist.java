package com.my.firstbeat.web.domain.playlist;

import com.my.firstbeat.web.domain.base.BaseEntity;
import com.my.firstbeat.web.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Playlist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 30)
    private String title;

    @Column(length = 256)
    private String description;

    @Builder
    public Playlist(String title, String description, User user) {
        this.title = title;
        this.description = description;
        this.user = user;
    }

}
