package com.my.firstbeat.web.domain.track;

import com.my.firstbeat.web.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Columns;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Track extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String spotifyTrackId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String artistName;

    @Column(nullable = false)
    private String albumCoverUrl;

    @Column(nullable = false)
    private String previewUrl;

}
