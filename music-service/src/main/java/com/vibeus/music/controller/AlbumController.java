package com.vibeus.music.controller;

import com.vibeus.music.dto.request.CreateAlbumRequest;
import com.vibeus.music.dto.request.UpdateAlbumRequest;
import com.vibeus.music.dto.response.AlbumResponse;
import com.vibeus.music.dto.response.ApiResponse;
import com.vibeus.music.dto.response.PageResponse;
import com.vibeus.music.service.AlbumService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @PostMapping
    public ResponseEntity<ApiResponse<AlbumResponse>> createAlbum(
            @Valid @RequestBody CreateAlbumRequest request) {

        AlbumResponse response = albumService.createAlbum(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(),
                        "Album created successfully", response));
    }

    @GetMapping("/{albumId}")
    public ResponseEntity<ApiResponse<AlbumResponse>> getAlbum(
            @PathVariable UUID albumId) {

        AlbumResponse response = albumService.getAlbumById(albumId);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Album fetched successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AlbumResponse>>> getAlbums(
            @RequestParam(required = false) UUID artistId,
            @PageableDefault(size = 20) Pageable pageable) {

        PageResponse<AlbumResponse> response = artistId != null
                ? albumService.getAlbumsByArtist(artistId, pageable)
                : albumService.getAllAlbums(pageable);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Albums fetched successfully", response));
    }

    @PutMapping("/{albumId}")
    public ResponseEntity<ApiResponse<AlbumResponse>> updateAlbum(
            @PathVariable UUID albumId,
            @Valid @RequestBody UpdateAlbumRequest request) {

        AlbumResponse response = albumService.updateAlbum(albumId, request);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Album updated successfully", response));
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable UUID albumId) {

        albumService.deleteAlbum(albumId);

        return ResponseEntity.noContent().build();
    }
}
