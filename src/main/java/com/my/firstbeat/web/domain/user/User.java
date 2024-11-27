package com.my.firstbeat.web.domain.user;

import com.my.firstbeat.web.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 20, nullable = false)
    private String name;

    @Column(length = 60, nullable = false)
    private String password;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    public User(Long id, String email, Role role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

}
