package com.vibeus.music.repository;

import com.vibeus.music.entity.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, UUID> {

    Optional<Artist> findByIdAndActiveTrue(UUID id);

    Page<Artist> findAllByActiveTrue(Pageable pageable);

    boolean existsByNameIgnoreCase(String name);
}
