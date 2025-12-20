package com.ssafy.project.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore.MetadataField;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

import redis.clients.jedis.JedisPooled;

import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.DefaultJedisClientConfig;

@Configuration
public class AiConfig {

    @Bean(name = "customVectorStore")
    @Primary
//    @Lazy
    public VectorStore customVectorStore(EmbeddingModel embeddingModel) {

        DefaultJedisClientConfig config =
                DefaultJedisClientConfig.builder()
                        .password("kyuzizi0428")
                        .connectionTimeoutMillis(120_000)
                        .socketTimeoutMillis(120_000)
                        .build();

        JedisPooled jedis = new JedisPooled(
                new HostAndPort("localhost", 16379),
                config
        );

        return RedisVectorStore.builder(jedis, embeddingModel)
                .initializeSchema(true)
                .metadataFields(
                        MetadataField.tag("category"),
                        MetadataField.numeric("meta_num"),
                        MetadataField.text("meta_txt")
                )
                .build();
    }
}
