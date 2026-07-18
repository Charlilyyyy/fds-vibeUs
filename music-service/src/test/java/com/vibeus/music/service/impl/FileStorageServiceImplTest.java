package com.vibeus.music.service.impl;

import com.vibeus.music.config.MinioProperties;
import com.vibeus.music.dto.response.FileUploadResponse;
import com.vibeus.music.exception.FileStorageException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceImplTest {

    @Mock
    private MinioClient minioClient;

    private FileStorageServiceImpl fileStorageService;

    @BeforeEach
    void setUp() {
        MinioProperties properties = new MinioProperties();
        properties.setEndpoint("http://localhost:9000");
        properties.setPublicUrl("http://localhost:9000");
        properties.setAccessKey("access");
        properties.setSecretKey("secret");
        properties.getBuckets().setArtists("vibeus-artists");
        properties.getBuckets().setAlbums("vibeus-albums");
        properties.getBuckets().setTracks("vibeus-tracks");

        fileStorageService = new FileStorageServiceImpl(minioClient, properties);
    }

    @Test
    void uploadArtistImage_shouldRejectEmptyFile() {
        MultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> fileStorageService.uploadArtistImage(file))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void uploadArtistImage_shouldRejectUnsupportedType() {
        MultipartFile file = new MockMultipartFile(
                "file", "a.gif", "image/gif", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> fileStorageService.uploadArtistImage(file))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Unsupported file type");
    }

    @Test
    void uploadTrackAudio_shouldRejectImageContentType() {
        MultipartFile file = new MockMultipartFile(
                "file", "a.png", "image/png", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> fileStorageService.uploadTrackAudio(file))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Unsupported file type");
    }

    @Test
    void uploadArtistImage_shouldStoreAndReturnUrl() throws Exception {
        MultipartFile file = new MockMultipartFile(
                "file", "cover.png", "image/png", new byte[]{1, 2, 3});

        FileUploadResponse response = fileStorageService.uploadArtistImage(file);

        assertThat(response.fileUrl())
                .startsWith("http://localhost:9000/vibeus-artists/");
        assertThat(response.fileName()).endsWith(".png");
        verify(minioClient).putObject(org.mockito.ArgumentMatchers.any(PutObjectArgs.class));
    }
}
