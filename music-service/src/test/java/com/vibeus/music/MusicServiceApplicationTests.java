package com.vibeus.music;

import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class MusicServiceApplicationTests {

    @MockitoBean
    private MinioClient minioClient;

    @Test
    void contextLoads() {
    }
}
