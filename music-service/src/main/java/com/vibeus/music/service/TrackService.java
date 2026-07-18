package com.vibeus.music.service;

import com.vibeus.music.dto.request.CreateTrackRequest;
import com.vibeus.music.dto.request.UpdateTrackRequest;
import com.vibeus.music.dto.response.PageResponse;
import com.vibeus.music.dto.response.TrackResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface TrackService {

    TrackResponse createTrack(CreateTrackRequest request);

    TrackResponse getTrackById(UUID trackId);

    PageResponse<TrackResponse> getAllTracks(Pageable pageable);

    PageResponse<TrackResponse> getTracksByArtist(UUID artistId, Pageable pageable);

    PageResponse<TrackResponse> getTracksByAlbum(UUID albumId, Pageable pageable);

    PageResponse<TrackResponse> getTracksByGenre(UUID genreId, Pageable pageable);

    TrackResponse updateTrack(UUID trackId, UpdateTrackRequest request);

    void deleteTrack(UUID trackId);

    TrackResponse uploadTrackAudio(UUID trackId, MultipartFile file);

    TrackResponse uploadTrackCover(UUID trackId, MultipartFile file);
}
