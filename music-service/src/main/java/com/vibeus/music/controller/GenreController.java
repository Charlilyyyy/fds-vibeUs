package com.vibeus.music.controller;

import com.vibeus.music.dto.request.CreateGenreRequest;
import com.vibeus.music.dto.request.UpdateGenreRequest;
import com.vibeus.music.dto.response.ApiResponse;
import com.vibeus.music.dto.response.GenreResponse;
import com.vibeus.music.dto.response.PageResponse;
import com.vibeus.music.service.GenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @PostMapping
    public ResponseEntity<ApiResponse<GenreResponse>> createGenre(
            @Valid @RequestBody CreateGenreRequest request) {

        GenreResponse response = genreService.createGenre(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(),
                        "Genre created successfully", response));
    }

    @GetMapping("/{genreId}")
    public ResponseEntity<ApiResponse<GenreResponse>> getGenre(
            @PathVariable UUID genreId) {

        GenreResponse response = genreService.getGenreById(genreId);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Genre fetched successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GenreResponse>>> getAllGenres(
            @PageableDefault(size = 20) Pageable pageable) {

        PageResponse<GenreResponse> response = genreService.getAllGenres(pageable);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Genres fetched successfully", response));
    }

    @PutMapping("/{genreId}")
    public ResponseEntity<ApiResponse<GenreResponse>> updateGenre(
            @PathVariable UUID genreId,
            @Valid @RequestBody UpdateGenreRequest request) {

        GenreResponse response = genreService.updateGenre(genreId, request);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Genre updated successfully", response));
    }

    @DeleteMapping("/{genreId}")
    public ResponseEntity<Void> deleteGenre(@PathVariable UUID genreId) {

        genreService.deleteGenre(genreId);

        return ResponseEntity.noContent().build();
    }
}
