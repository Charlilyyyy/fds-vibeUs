package com.vibeus.music.service;

import com.vibeus.music.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    FileUploadResponse uploadArtistImage(MultipartFile file);

    FileUploadResponse uploadAlbumCover(MultipartFile file);

    FileUploadResponse uploadTrackAudio(MultipartFile file);

    FileUploadResponse uploadTrackCover(MultipartFile file);

    void deleteFile(String fileUrl);
}
