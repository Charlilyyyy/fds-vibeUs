package com.vibeus.music.service.impl;

import com.vibeus.music.dto.request.CreateAlbumRequest;
import com.vibeus.music.dto.request.UpdateAlbumRequest;
import com.vibeus.music.dto.response.AlbumResponse;
import com.vibeus.music.dto.response.FileUploadResponse;
import com.vibeus.music.dto.response.PageResponse;
import com.vibeus.music.entity.Album;
import com.vibeus.music.entity.Artist;
import com.vibeus.music.exception.DuplicateResourceException;
import com.vibeus.music.exception.ResourceNotFoundException;
import com.vibeus.music.repository.AlbumRepository;
import com.vibeus.music.repository.ArtistRepository;
import com.vibeus.music.service.AlbumService;
import com.vibeus.music.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public AlbumResponse createAlbum(CreateAlbumRequest request) {

        Artist artist = getActiveArtist(request.artistId());

        String title = request.title().trim();

        if (albumRepository.existsByTitleIgnoreCaseAndArtistIdAndActiveTrue(
                title, artist.getId())) {
            throw new DuplicateResourceException(
                    "Album already exists with title: " + title);
        }

        Album album = Album.builder()
                .title(title)
                .type(request.type())
                .releaseDate(request.releaseDate())
                .coverImageUrl(request.coverImageUrl())
                .artist(artist)
                .active(true)
                .build();

        return mapToResponse(albumRepository.save(album));
    }

    @Override
    @Transactional(readOnly = true)
    public AlbumResponse getAlbumById(UUID albumId) {
        return mapToResponse(getActiveAlbum(albumId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AlbumResponse> getAllAlbums(Pageable pageable) {
        return PageResponse.from(
                albumRepository.findAllByActiveTrue(pageable),
                this::mapToResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AlbumResponse> getAlbumsByArtist(UUID artistId, Pageable pageable) {
        getActiveArtist(artistId);
        return PageResponse.from(
                albumRepository.findByArtistIdAndActiveTrue(artistId, pageable),
                this::mapToResponse
        );
    }

    @Override
    @Transactional
    public AlbumResponse updateAlbum(UUID albumId, UpdateAlbumRequest request) {

        Album album = getActiveAlbum(albumId);

        UUID targetArtistId = request.artistId() != null
                ? request.artistId()
                : album.getArtist().getId();
        Artist artist = getActiveArtist(targetArtistId);

        String targetTitle = request.title() != null
                ? request.title().trim()
                : album.getTitle();

        boolean titleOrArtistChanged =
                !targetTitle.equalsIgnoreCase(album.getTitle())
                        || !targetArtistId.equals(album.getArtist().getId());

        if (titleOrArtistChanged
                && albumRepository.existsByTitleIgnoreCaseAndArtistIdAndActiveTrue(
                targetTitle, targetArtistId)) {
            throw new DuplicateResourceException(
                    "Album already exists with title: " + targetTitle);
        }

        album.setTitle(targetTitle);
        album.setArtist(artist);

        if (request.type() != null) {
            album.setType(request.type());
        }

        if (request.releaseDate() != null) {
            album.setReleaseDate(request.releaseDate());
        }

        if (request.coverImageUrl() != null) {
            album.setCoverImageUrl(request.coverImageUrl());
        }

        return mapToResponse(albumRepository.save(album));
    }

    @Override
    @Transactional
    public void deleteAlbum(UUID albumId) {
        Album album = getActiveAlbum(albumId);
        album.setActive(false);
        albumRepository.save(album);
    }

    @Override
    @Transactional
    public AlbumResponse uploadAlbumCover(UUID albumId, MultipartFile file) {
        Album album = getActiveAlbum(albumId);

        String oldCoverUrl = album.getCoverImageUrl();
        FileUploadResponse upload = fileStorageService.uploadAlbumCover(file);
        album.setCoverImageUrl(upload.fileUrl());

        Album saved = albumRepository.save(album);
        fileStorageService.deleteFile(oldCoverUrl);

        return mapToResponse(saved);
    }

    private Artist getActiveArtist(UUID artistId) {
        return artistRepository.findByIdAndActiveTrue(artistId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Artist not found with id: " + artistId));
    }

    private Album getActiveAlbum(UUID albumId) {
        return albumRepository.findByIdAndActiveTrue(albumId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Album not found with id: " + albumId));
    }

    private AlbumResponse mapToResponse(Album album) {
        return new AlbumResponse(
                album.getId(),
                album.getTitle(),
                album.getType(),
                album.getReleaseDate(),
                album.getCoverImageUrl(),
                album.isActive(),
                album.getArtist().getId(),
                album.getArtist().getName(),
                album.getCreatedAt(),
                album.getUpdatedAt()
        );
    }
}
