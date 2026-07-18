package com.vibeus.music.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibeus.music.dto.request.CreateArtistRequest;
import com.vibeus.music.dto.response.ArtistResponse;
import com.vibeus.music.service.ArtistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ArtistController.class)
@AutoConfigureMockMvc(addFilters = false)
class ArtistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ArtistService artistService;

    @Test
    void createArtist_shouldReturnCreated() throws Exception {
        UUID id = UUID.randomUUID();
        CreateArtistRequest request = new CreateArtistRequest("Miles Davis", "Jazz", null);
        when(artistService.createArtist(any(CreateArtistRequest.class)))
                .thenReturn(new ArtistResponse(id, "Miles Davis", "Jazz", null, false, true, Instant.now()));

        mockMvc.perform(post("/api/v1/artists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.name").value("Miles Davis"));
    }

    @Test
    void createArtist_shouldRejectBlankName() throws Exception {
        CreateArtistRequest request = new CreateArtistRequest("", null, null);

        mockMvc.perform(post("/api/v1/artists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getArtist_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(artistService.getArtistById(id))
                .thenReturn(new ArtistResponse(id, "Miles Davis", "Jazz", null, true, true, Instant.now()));

        mockMvc.perform(get("/api/v1/artists/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verified").value(true));
    }
}
