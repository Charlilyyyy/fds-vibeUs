package com.vibeus.music.controller;

import com.vibeus.music.dto.response.ApiResponse;
import com.vibeus.music.dto.response.FileUploadResponse;
import com.vibeus.music.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Standalone media upload endpoints for clients that want to upload an asset
 * before it is attached to a catalog resource.
 */
@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/artist-images")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadArtistImage(
            @RequestParam("file") MultipartFile file) {

        FileUploadResponse response = fileStorageService.uploadArtistImage(file);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Artist image uploaded successfully", response));
    }

    @PostMapping("/album-covers")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadAlbumCover(
            @RequestParam("file") MultipartFile file) {

        FileUploadResponse response = fileStorageService.uploadAlbumCover(file);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Album cover uploaded successfully", response));
    }

    @PostMapping("/track-audio")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadTrackAudio(
            @RequestParam("file") MultipartFile file) {

        FileUploadResponse response = fileStorageService.uploadTrackAudio(file);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                "Track audio uploaded successfully", response));
    }
}
