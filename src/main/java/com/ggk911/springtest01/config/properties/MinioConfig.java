package com.ggk911.springtest01.config.properties;

import io.minio.MinioClient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author TangHaoKai
 * @version V1.0 2023-11-23 16:32
 **/
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    /**
     * minio地址
     */
    private String endpoint;

    /**
     * minio用户名
     */
    private String accessKey;

    /**
     * minio密码
     */
    private String secretKey;

    /**
     * minio的桶名称
     */
    private String bucketName;

    @Bean
    public MinioClient getMinioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}