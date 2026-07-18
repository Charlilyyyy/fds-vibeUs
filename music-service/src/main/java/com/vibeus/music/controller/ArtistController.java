package com.vibeus.music.controller;

import com.vibeus.music.dto.request.CreateArtistRequest;
import com.vibeus.music.dto.request.UpdateArtistRequest;
import com.vibeus.music.dto.response.ApiResponse;
import com.vibeus.music.dto.response.ArtistResponse;
import com.vibeus.music.dto.response.PageResponse;
import com.vibeus.music.service.ArtistService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    @PostMapping
    public ResponseEntity<ApiResponse<ArtistResponse>> createArtist(
            @Valid @RequestBody CreateArtistRequest request) {

        ArtistResponse response = artistService.createArtist(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(),
                        "Artist created successfully", response));
    }

    @GetMapping("/{artistId}")
    public ResponseEntity<ApiResponse<ArtistResponse>> getArtist(
            @PathVariable UUID artistId) {

        ArtistResponse response = artistService.getArtistById(artistId);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Artist fetched successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ArtistResponse>>> getAllArtists(
            @PageableDefault(size = 20) Pageable pageable) {

        PageResponse<ArtistResponse> response = artistService.getAllArtists(pageable);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Artists fetched successfully", response));
    }

    @PutMapping("/{artistId}")
    public ResponseEntity<ApiResponse<ArtistResponse>> updateArtist(
            @PathVariable UUID artistId,
            @Valid @RequestBody UpdateArtistRequest request) {

        ArtistResponse response = artistService.updateArtist(artistId, request);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Artist updated successfully", response));
    }

    @DeleteMapping("/{artistId}")
    public ResponseEntity<Void> deleteArtist(@PathVariable UUID artistId) {

        artistService.deactivateArtist(artistId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{artistId}/image")
    public ResponseEntity<ApiResponse<ArtistResponse>> uploadArtistImage(
            @PathVariable UUID artistId,
            @RequestParam("file") MultipartFile file) {

        ArtistResponse response = artistService.uploadArtistImage(artistId, file);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Artist image uploaded successfully", response));
    }
}
