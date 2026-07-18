package com.vibeus.music.service.impl;

import com.vibeus.music.dto.request.CreateGenreRequest;
import com.vibeus.music.dto.response.GenreResponse;
import com.vibeus.music.entity.Genre;
import com.vibeus.music.exception.DuplicateResourceException;
import com.vibeus.music.exception.ResourceNotFoundException;
import com.vibeus.music.repository.GenreRepository;
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
class GenreServiceImplTest {

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private GenreServiceImpl genreService;

    @Test
    void createGenre_shouldPersistWhenNameUnique() {
        CreateGenreRequest request = new CreateGenreRequest("Jazz", "Improvised music");
        when(genreRepository.existsByNameIgnoreCase("Jazz")).thenReturn(false);
        when(genreRepository.save(any(Genre.class))).thenAnswer(inv -> {
            Genre g = inv.getArgument(0);
            g.setId(UUID.randomUUID());
            return g;
        });

        GenreResponse response = genreService.createGenre(request);

        assertThat(response.name()).isEqualTo("Jazz");
        assertThat(response.active()).isTrue();
    }

    @Test
    void createGenre_shouldRejectDuplicate() {
        CreateGenreRequest request = new CreateGenreRequest("Jazz", null);
        when(genreRepository.existsByNameIgnoreCase("Jazz")).thenReturn(true);

        assertThatThrownBy(() -> genreService.createGenre(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(genreRepository, never()).save(any());
    }

    @Test
    void deleteGenre_shouldThrowWhenMissing() {
        UUID id = UUID.randomUUID();
        when(genreRepository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> genreService.deleteGenre(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
