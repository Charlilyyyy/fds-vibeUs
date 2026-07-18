package com.vibeus.music.repository;

import com.vibeus.music.entity.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GenreRepository extends JpaRepository<Genre, UUID> {

    Optional<Genre> findByIdAndActiveTrue(UUID id);

    Page<Genre> findAllByActiveTrue(Pageable pageable);

    boolean existsByNameIgnoreCase(String name);
}
