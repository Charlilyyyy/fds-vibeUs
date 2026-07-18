package com.vibeus.music.repository;

import com.vibeus.music.entity.Track;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrackRepository extends JpaRepository<Track, UUID> {

    Optional<Track> findByIdAndActiveTrue(UUID id);

    Page<Track> findAllByActiveTrue(Pageable pageable);

    boolean existsByTitleIgnoreCaseAndActiveTrue(String title);

    Optional<Track> findByTitleIgnoreCaseAndActiveTrue(String title);

    Page<Track> findByAlbumIdAndActiveTrue(UUID albumId, Pageable pageable);

    Page<Track> findByArtistsIdAndActiveTrue(UUID artistId, Pageable pageable);

    Page<Track> findByGenresIdAndActiveTrue(UUID genreId, Pageable pageable);
}
