package com.vibeus.music.service;

import com.vibeus.music.dto.request.CreateArtistRequest;
import com.vibeus.music.dto.request.UpdateArtistRequest;
import com.vibeus.music.dto.response.ArtistResponse;
import com.vibeus.music.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ArtistService {

    ArtistResponse createArtist(CreateArtistRequest request);

    ArtistResponse getArtistById(UUID artistId);

    PageResponse<ArtistResponse> getAllArtists(Pageable pageable);

    ArtistResponse updateArtist(UUID artistId, UpdateArtistRequest request);

    void deactivateArtist(UUID artistId);

    ArtistResponse uploadArtistImage(UUID artistId, MultipartFile file);
}
