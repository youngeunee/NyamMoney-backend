package com.ssafy.project.config;

import java.time.Duration;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore.MetadataField;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPooled;

@Configuration
public class AiConfig {

        @Value("${spring.data.redis.host}")
        private String redisHost;

        @Value("${spring.data.redis.port}")
        private int redisPort;

        @Value("${spring.data.redis.password}")
        private String redisPassword;

        @Bean(name = "customVectorStore")
        public VectorStore customVectorStore(EmbeddingModel embeddingModel) {

                DefaultJedisClientConfig config = DefaultJedisClientConfig.builder()
                                .password(redisPassword)
                                .connectionTimeoutMillis((int) Duration.ofSeconds(120).toMillis())
                                .socketTimeoutMillis((int) Duration.ofSeconds(120).toMillis())
                                .build();

                JedisPooled jedis = new JedisPooled(
                                new HostAndPort(redisHost, redisPort),
                                config);

                return RedisVectorStore.builder(jedis, embeddingModel)
                                .initializeSchema(true)
                                .metadataFields(
                                                MetadataField.tag("category"),
                                                MetadataField.numeric("meta_num"),
                                                MetadataField.text("meta_txt"))
                                .build();
        }
}