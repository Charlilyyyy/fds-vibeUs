package com.vibeus.music.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "artists")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Artist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 3000)
    private String bio;

    private String imageUrl;

    @Builder.Default
    private boolean verified = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
