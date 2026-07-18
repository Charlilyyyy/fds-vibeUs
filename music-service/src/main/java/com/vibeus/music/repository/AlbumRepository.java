package com.vibeus.music.repository;

import com.vibeus.music.entity.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlbumRepository extends JpaRepository<Album, UUID> {

    Optional<Album> findByIdAndActiveTrue(UUID id);

    Page<Album> findAllByActiveTrue(Pageable pageable);

    Page<Album> findByArtistIdAndActiveTrue(UUID artistId, Pageable pageable);

    boolean existsByTitleIgnoreCaseAndArtistIdAndActiveTrue(String title, UUID artistId);
}
