package com.vibeus.music.service;

import com.vibeus.music.dto.request.CreateAlbumRequest;
import com.vibeus.music.dto.request.UpdateAlbumRequest;
import com.vibeus.music.dto.response.AlbumResponse;
import com.vibeus.music.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface AlbumService {

    AlbumResponse createAlbum(CreateAlbumRequest request);

    AlbumResponse getAlbumById(UUID albumId);

    PageResponse<AlbumResponse> getAllAlbums(Pageable pageable);

    PageResponse<AlbumResponse> getAlbumsByArtist(UUID artistId, Pageable pageable);

    AlbumResponse updateAlbum(UUID albumId, UpdateAlbumRequest request);

    void deleteAlbum(UUID albumId);

    AlbumResponse uploadAlbumCover(UUID albumId, MultipartFile file);
}
