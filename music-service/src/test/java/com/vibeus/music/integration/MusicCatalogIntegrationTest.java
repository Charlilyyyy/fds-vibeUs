package com.vibeus.music.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibeus.music.dto.request.CreateAlbumRequest;
import com.vibeus.music.dto.request.CreateArtistRequest;
import com.vibeus.music.enums.AlbumType;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the full catalog stack (controller -> service -> repository -> H2)
 * to verify artist and album flows work end-to-end.
 */
@SpringBootTest
@AutoConfigureMockMvc
class MusicCatalogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MinioClient minioClient;

    @Test
    void artistThenAlbum_shouldPersistAndBeRetrievable() throws Exception {
        CreateArtistRequest artistRequest =
                new CreateArtistRequest("Miles Davis " + UUID.randomUUID(), "Jazz legend", null);

        MvcResult artistResult = mockMvc.perform(post("/api/v1/artists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(artistRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode artistJson = objectMapper.readTree(artistResult.getResponse().getContentAsString());
        String artistId = artistJson.path("data").path("id").asText();
        assertThat(artistId).isNotBlank();

        CreateAlbumRequest albumRequest = new CreateAlbumRequest(
                "Kind of Blue",
                AlbumType.ALBUM,
                LocalDate.of(1959, 8, 17),
                null,
                UUID.fromString(artistId));

        MvcResult albumResult = mockMvc.perform(post("/api/v1/albums")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(albumRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Kind of Blue"))
                .andExpect(jsonPath("$.data.artistId").value(artistId))
                .andReturn();

        JsonNode albumJson = objectMapper.readTree(albumResult.getResponse().getContentAsString());
        String albumId = albumJson.path("data").path("id").asText();

        mockMvc.perform(get("/api/v1/albums/" + albumId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Kind of Blue"))
                .andExpect(jsonPath("$.data.type").value("ALBUM"));
    }

    @Test
    void createAlbum_shouldReturnNotFoundForUnknownArtist() throws Exception {
        CreateAlbumRequest albumRequest = new CreateAlbumRequest(
                "Orphan Album",
                AlbumType.SINGLE,
                LocalDate.now(),
                null,
                UUID.randomUUID());

        mockMvc.perform(post("/api/v1/albums")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(albumRequest)))
                .andExpect(status().isNotFound());
    }
}
