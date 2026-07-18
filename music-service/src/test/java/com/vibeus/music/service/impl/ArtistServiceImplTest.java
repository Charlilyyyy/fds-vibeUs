package com.vibeus.music.service.impl;

import com.vibeus.music.dto.request.CreateArtistRequest;
import com.vibeus.music.dto.request.UpdateArtistRequest;
import com.vibeus.music.dto.response.ArtistResponse;
import com.vibeus.music.entity.Artist;
import com.vibeus.music.exception.DuplicateResourceException;
import com.vibeus.music.exception.ResourceNotFoundException;
import com.vibeus.music.repository.ArtistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtistServiceImplTest {

    @Mock
    private ArtistRepository artistRepository;

    @InjectMocks
    private ArtistServiceImpl artistService;

    @Test
    void createArtist_shouldPersistWhenNameUnique() {
        CreateArtistRequest request = new CreateArtistRequest("Miles Davis", "Jazz legend", null);
        when(artistRepository.existsByNameIgnoreCase("Miles Davis")).thenReturn(false);
        when(artistRepository.save(any(Artist.class))).thenAnswer(inv -> {
            Artist a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        ArtistResponse response = artistService.createArtist(request);

        assertThat(response.name()).isEqualTo("Miles Davis");
        assertThat(response.active()).isTrue();
    }

    @Test
    void createArtist_shouldRejectDuplicateName() {
        CreateArtistRequest request = new CreateArtistRequest("Miles Davis", null, null);
        when(artistRepository.existsByNameIgnoreCase("Miles Davis")).thenReturn(true);

        assertThatThrownBy(() -> artistService.createArtist(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(artistRepository, never()).save(any());
    }

    @Test
    void getArtistById_shouldThrowWhenMissing() {
        UUID id = UUID.randomUUID();
        when(artistRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistService.getArtistById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateArtist_shouldApplyProvidedFields() {
        UUID id = UUID.randomUUID();
        Artist artist = Artist.builder().id(id).name("Old").active(true).build();
        when(artistRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(artist));
        when(artistRepository.existsByNameIgnoreCase("New")).thenReturn(false);
        when(artistRepository.save(any(Artist.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateArtistRequest request = new UpdateArtistRequest("New", null, null, true);
        ArtistResponse response = artistService.updateArtist(id, request);

        assertThat(response.name()).isEqualTo("New");
        assertThat(response.verified()).isTrue();
    }

    @Test
    void deactivateArtist_shouldSetInactive() {
        UUID id = UUID.randomUUID();
        Artist artist = Artist.builder().id(id).name("Miles").active(true).build();
        when(artistRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(artist));

        artistService.deactivateArtist(id);

        assertThat(artist.isActive()).isFalse();
        verify(artistRepository).save(artist);
    }
}
