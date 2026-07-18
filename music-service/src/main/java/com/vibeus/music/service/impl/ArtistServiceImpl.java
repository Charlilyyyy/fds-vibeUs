package com.vibeus.music.service.impl;

import com.vibeus.music.dto.request.CreateArtistRequest;
import com.vibeus.music.dto.request.UpdateArtistRequest;
import com.vibeus.music.dto.response.ArtistResponse;
import com.vibeus.music.dto.response.FileUploadResponse;
import com.vibeus.music.dto.response.PageResponse;
import com.vibeus.music.entity.Artist;
import com.vibeus.music.exception.DuplicateResourceException;
import com.vibeus.music.exception.ResourceNotFoundException;
import com.vibeus.music.repository.ArtistRepository;
import com.vibeus.music.service.ArtistService;
import com.vibeus.music.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public ArtistResponse createArtist(CreateArtistRequest request) {

        String name = request.name().trim();

        if (artistRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Artist already exists with name: " + name);
        }

        Artist artist = Artist.builder()
                .name(name)
                .bio(request.bio())
                .imageUrl(request.imageUrl())
                .active(true)
                .build();

        return mapToResponse(artistRepository.save(artist));
    }

    @Override
    @Transactional(readOnly = true)
    public ArtistResponse getArtistById(UUID artistId) {
        return mapToResponse(getActiveArtist(artistId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArtistResponse> getAllArtists(Pageable pageable) {
        return PageResponse.from(
                artistRepository.findAllByActiveTrue(pageable),
                this::mapToResponse
        );
    }

    @Override
    @Transactional
    public ArtistResponse updateArtist(UUID artistId, UpdateArtistRequest request) {

        Artist artist = getActiveArtist(artistId);

        if (request.name() != null) {
            String name = request.name().trim();
            if (!name.equalsIgnoreCase(artist.getName())
                    && artistRepository.existsByNameIgnoreCase(name)) {
                throw new DuplicateResourceException("Artist already exists with name: " + name);
            }
            artist.setName(name);
        }

        if (request.bio() != null) {
            artist.setBio(request.bio());
        }

        if (request.imageUrl() != null) {
            artist.setImageUrl(request.imageUrl());
        }

        if (request.verified() != null) {
            artist.setVerified(request.verified());
        }

        return mapToResponse(artistRepository.save(artist));
    }

    @Override
    @Transactional
    public void deactivateArtist(UUID artistId) {
        Artist artist = getActiveArtist(artistId);
        artist.setActive(false);
        artistRepository.save(artist);
    }

    @Override
    @Transactional
    public ArtistResponse uploadArtistImage(UUID artistId, MultipartFile file) {
        Artist artist = getActiveArtist(artistId);

        String oldImageUrl = artist.getImageUrl();
        FileUploadResponse upload = fileStorageService.uploadArtistImage(file);
        artist.setImageUrl(upload.fileUrl());

        Artist saved = artistRepository.save(artist);
        fileStorageService.deleteFile(oldImageUrl);

        return mapToResponse(saved);
    }

    private Artist getActiveArtist(UUID artistId) {
        return artistRepository.findByIdAndActiveTrue(artistId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Artist not found with id: " + artistId));
    }

    private ArtistResponse mapToResponse(Artist artist) {
        return new ArtistResponse(
                artist.getId(),
                artist.getName(),
                artist.getBio(),
                artist.getImageUrl(),
                artist.isVerified(),
                artist.isActive(),
                artist.getCreatedAt()
        );
    }
}
