package com.vibeus.music.service;

import com.vibeus.music.dto.request.CreateGenreRequest;
import com.vibeus.music.dto.request.UpdateGenreRequest;
import com.vibeus.music.dto.response.GenreResponse;
import com.vibeus.music.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GenreService {

    GenreResponse createGenre(CreateGenreRequest request);

    GenreResponse getGenreById(UUID genreId);

    PageResponse<GenreResponse> getAllGenres(Pageable pageable);

    GenreResponse updateGenre(UUID genreId, UpdateGenreRequest request);

    void deleteGenre(UUID genreId);
}
