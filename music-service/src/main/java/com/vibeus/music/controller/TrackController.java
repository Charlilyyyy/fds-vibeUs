package com.vibeus.music.controller;

import com.vibeus.music.dto.request.CreateTrackRequest;
import com.vibeus.music.dto.request.UpdateTrackRequest;
import com.vibeus.music.dto.response.ApiResponse;
import com.vibeus.music.dto.response.PageResponse;
import com.vibeus.music.dto.response.TrackResponse;
import com.vibeus.music.service.TrackService;
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
@RequestMapping("/api/v1/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackService trackService;

    @PostMapping
    public ResponseEntity<ApiResponse<TrackResponse>> createTrack(
            @Valid @RequestBody CreateTrackRequest request) {

        TrackResponse response = trackService.createTrack(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(),
                        "Track created successfully", response));
    }

    @GetMapping("/{trackId}")
    public ResponseEntity<ApiResponse<TrackResponse>> getTrack(
            @PathVariable UUID trackId) {

        TrackResponse response = trackService.getTrackById(trackId);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Track fetched successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TrackResponse>>> getTracks(
            @RequestParam(required = false) UUID artistId,
            @RequestParam(required = false) UUID albumId,
            @RequestParam(required = false) UUID genreId,
            @PageableDefault(size = 20) Pageable pageable) {

        PageResponse<TrackResponse> response;
        if (artistId != null) {
            response = trackService.getTracksByArtist(artistId, pageable);
        } else if (albumId != null) {
            response = trackService.getTracksByAlbum(albumId, pageable);
        } else if (genreId != null) {
            response = trackService.getTracksByGenre(genreId, pageable);
        } else {
            response = trackService.getAllTracks(pageable);
        }

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Tracks fetched successfully", response));
    }

    @PutMapping("/{trackId}")
    public ResponseEntity<ApiResponse<TrackResponse>> updateTrack(
            @PathVariable UUID trackId,
            @Valid @RequestBody UpdateTrackRequest request) {

        TrackResponse response = trackService.updateTrack(trackId, request);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Track updated successfully", response));
    }

    @DeleteMapping("/{trackId}")
    public ResponseEntity<Void> deleteTrack(@PathVariable UUID trackId) {

        trackService.deleteTrack(trackId);

        return ResponseEntity.noContent().build();
    }
}
