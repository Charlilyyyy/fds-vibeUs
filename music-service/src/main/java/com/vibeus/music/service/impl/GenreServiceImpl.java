package com.vibeus.music.service.impl;

import com.vibeus.music.dto.request.CreateGenreRequest;
import com.vibeus.music.dto.request.UpdateGenreRequest;
import com.vibeus.music.dto.response.GenreResponse;
import com.vibeus.music.dto.response.PageResponse;
import com.vibeus.music.entity.Genre;
import com.vibeus.music.exception.DuplicateResourceException;
import com.vibeus.music.exception.ResourceNotFoundException;
import com.vibeus.music.repository.GenreRepository;
import com.vibeus.music.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;

    @Override
    @Transactional
    public GenreResponse createGenre(CreateGenreRequest request) {

        String name = request.name().trim();

        if (genreRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Genre already exists with name: " + name);
        }

        Genre genre = Genre.builder()
                .name(name)
                .description(request.description())
                .active(true)
                .build();

        return mapToResponse(genreRepository.save(genre));
    }

    @Override
    @Transactional(readOnly = true)
    public GenreResponse getGenreById(UUID genreId) {
        return mapToResponse(getActiveGenre(genreId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GenreResponse> getAllGenres(Pageable pageable) {
        return PageResponse.from(
                genreRepository.findAllByActiveTrue(pageable),
                this::mapToResponse
        );
    }

    @Override
    @Transactional
    public GenreResponse updateGenre(UUID genreId, UpdateGenreRequest request) {

        Genre genre = getActiveGenre(genreId);

        if (request.name() != null) {
            String name = request.name().trim();
            if (!name.equalsIgnoreCase(genre.getName())
                    && genreRepository.existsByNameIgnoreCase(name)) {
                throw new DuplicateResourceException("Genre already exists with name: " + name);
            }
            genre.setName(name);
        }

        if (request.description() != null) {
            genre.setDescription(request.description());
        }

        return mapToResponse(genreRepository.save(genre));
    }

    @Override
    @Transactional
    public void deleteGenre(UUID genreId) {
        Genre genre = getActiveGenre(genreId);
        genre.setActive(false);
        genreRepository.save(genre);
    }

    private Genre getActiveGenre(UUID genreId) {
        return genreRepository.findByIdAndActiveTrue(genreId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Genre not found with id: " + genreId));
    }

    private GenreResponse mapToResponse(Genre genre) {
        return new GenreResponse(
                genre.getId(),
                genre.getName(),
                genre.getDescription(),
                genre.isActive(),
                genre.getCreatedAt(),
                genre.getUpdatedAt()
        );
    }
}
