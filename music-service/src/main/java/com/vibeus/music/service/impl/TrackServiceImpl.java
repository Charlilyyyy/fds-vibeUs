package com.vibeus.music.service.impl;

import com.vibeus.music.dto.request.CreateTrackRequest;
import com.vibeus.music.dto.request.UpdateTrackRequest;
import com.vibeus.music.dto.response.ArtistSummaryResponse;
import com.vibeus.music.dto.response.GenreSummaryResponse;
import com.vibeus.music.dto.response.PageResponse;
import com.vibeus.music.dto.response.TrackResponse;
import com.vibeus.music.entity.Album;
import com.vibeus.music.entity.Artist;
import com.vibeus.music.entity.Genre;
import com.vibeus.music.entity.Track;
import com.vibeus.music.exception.DuplicateResourceException;
import com.vibeus.music.exception.ResourceNotFoundException;
import com.vibeus.music.repository.AlbumRepository;
import com.vibeus.music.repository.ArtistRepository;
import com.vibeus.music.repository.GenreRepository;
import com.vibeus.music.repository.TrackRepository;
import com.vibeus.music.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TrackServiceImpl implements TrackService {

    private final TrackRepository trackRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;

    @Override
    public TrackResponse createTrack(CreateTrackRequest request) {

        String title = request.title().trim();

        if (trackRepository.existsByTitleIgnoreCaseAndActiveTrue(title)) {
            throw new DuplicateResourceException("Track already exists with title: " + title);
        }

        Set<Artist> artists = getActiveArtists(request.artistIds());
        Set<Genre> genres = getActiveGenres(request.genreIds());

        Album album = null;
        if (request.albumId() != null) {
            album = getActiveAlbum(request.albumId());
            validateAlbumArtistRelation(album, artists);
        }

        Track track = Track.builder()
                .title(title)
                .durationInSeconds(request.durationInSeconds())
                .audioUrl(request.audioUrl())
                .coverImageUrl(request.coverImageUrl())
                .album(album)
                .artists(artists)
                .genres(genres)
                .active(true)
                .build();

        return mapToResponse(trackRepository.save(track));
    }

    @Override
    public TrackResponse updateTrack(UUID trackId, UpdateTrackRequest request) {

        Track track = getActiveTrack(trackId);

        String title = request.title().trim();

        trackRepository.findByTitleIgnoreCaseAndActiveTrue(title)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(trackId)) {
                        throw new DuplicateResourceException(
                                "Track already exists with title: " + title);
                    }
                });

        Set<Artist> artists = getActiveArtists(request.artistIds());
        Set<Genre> genres = getActiveGenres(request.genreIds());

        Album album = null;
        if (request.albumId() != null) {
            album = getActiveAlbum(request.albumId());
            validateAlbumArtistRelation(album, artists);
        }

        track.setTitle(title);
        track.setDurationInSeconds(request.durationInSeconds());
        track.setAudioUrl(request.audioUrl());
        track.setCoverImageUrl(request.coverImageUrl());
        track.setAlbum(album);
        track.setArtists(artists);
        track.setGenres(genres);

        return mapToResponse(trackRepository.save(track));
    }

    @Override
    @Transactional(readOnly = true)
    public TrackResponse getTrackById(UUID trackId) {
        return mapToResponse(getActiveTrack(trackId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TrackResponse> getAllTracks(Pageable pageable) {
        return PageResponse.from(
                trackRepository.findAllByActiveTrue(pageable),
                this::mapToResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TrackResponse> getTracksByArtist(UUID artistId, Pageable pageable) {
        return PageResponse.from(
                trackRepository.findByArtistsIdAndActiveTrue(artistId, pageable),
                this::mapToResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TrackResponse> getTracksByAlbum(UUID albumId, Pageable pageable) {
        return PageResponse.from(
                trackRepository.findByAlbumIdAndActiveTrue(albumId, pageable),
                this::mapToResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TrackResponse> getTracksByGenre(UUID genreId, Pageable pageable) {
        return PageResponse.from(
                trackRepository.findByGenresIdAndActiveTrue(genreId, pageable),
                this::mapToResponse
        );
    }

    @Override
    public void deleteTrack(UUID trackId) {
        Track track = getActiveTrack(trackId);
        track.setActive(false);
        trackRepository.save(track);
    }

    private Track getActiveTrack(UUID trackId) {
        return trackRepository.findByIdAndActiveTrue(trackId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Track not found with id: " + trackId));
    }

    private Album getActiveAlbum(UUID albumId) {
        return albumRepository.findByIdAndActiveTrue(albumId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Album not found with id: " + albumId));
    }

    private Set<Artist> getActiveArtists(Set<UUID> artistIds) {
        Set<Artist> artists = new HashSet<>();
        for (UUID artistId : artistIds) {
            artists.add(artistRepository.findByIdAndActiveTrue(artistId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Artist not found with id: " + artistId)));
        }
        return artists;
    }

    private Set<Genre> getActiveGenres(Set<UUID> genreIds) {
        Set<Genre> genres = new HashSet<>();
        for (UUID genreId : genreIds) {
            genres.add(genreRepository.findByIdAndActiveTrue(genreId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Genre not found with id: " + genreId)));
        }
        return genres;
    }

    private void validateAlbumArtistRelation(Album album, Set<Artist> artists) {
        boolean containsAlbumArtist = artists.stream()
                .anyMatch(artist -> artist.getId().equals(album.getArtist().getId()));

        if (!containsAlbumArtist) {
            throw new IllegalArgumentException("Track artists must contain the album artist");
        }
    }

    private TrackResponse mapToResponse(Track track) {
        List<ArtistSummaryResponse> artists = track.getArtists().stream()
                .map(artist -> new ArtistSummaryResponse(artist.getId(), artist.getName()))
                .toList();

        List<GenreSummaryResponse> genres = track.getGenres().stream()
                .map(genre -> new GenreSummaryResponse(genre.getId(), genre.getName()))
                .toList();

        UUID albumId = null;
        String albumTitle = null;
        if (track.getAlbum() != null) {
            albumId = track.getAlbum().getId();
            albumTitle = track.getAlbum().getTitle();
        }

        return new TrackResponse(
                track.getId(),
                track.getTitle(),
                track.getDurationInSeconds(),
                track.getAudioUrl(),
                track.getCoverImageUrl(),
                track.getPlayCount(),
                albumId,
                albumTitle,
                artists,
                genres
        );
    }
}
