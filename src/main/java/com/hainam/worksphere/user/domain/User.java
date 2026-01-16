package com.hainam.worksphere.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "given_name", nullable = false, length = 50)
    private String givenName;

    @Column(name = "family_name", nullable = false, length = 50)
    private String familyName;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "is_enabled")
    @Builder.Default
    private Boolean isEnabled = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
