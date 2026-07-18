package com.vibeus.music.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String publicUrl;

    private Buckets buckets = new Buckets();

    @Getter
    @Setter
    public static class Buckets {

        private String artists;

        private String albums;

        private String tracks;
    }
}
