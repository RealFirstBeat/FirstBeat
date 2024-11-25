package com.my.firstbeat.web.domain.playlist;

import com.my.firstbeat.web.domain.base.BaseEntity;
import com.my.firstbeat.web.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.security.ProtectionDomain;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Playlist extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 30)
    private String title;

    @Column(length = 256)
    private String description;

    @Column(nullable = false)
    private boolean isDefault; //디폴트 여부 추가

    // 커스텀 생성자
    public Playlist(User user, String title, String description, boolean isDefault) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.isDefault = isDefault;
    }

    public void updateDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
