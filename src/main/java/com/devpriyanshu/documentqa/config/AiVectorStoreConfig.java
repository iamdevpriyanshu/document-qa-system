package com.devpriyanshu.documentqa.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;

@Configuration
public class AiVectorStoreConfig {

    private final DocumentQaProperties properties;
    private SimpleVectorStore simpleVectorStore;

    public AiVectorStoreConfig(DocumentQaProperties properties) {
        this.properties = properties;
    }

    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        var ingest = properties.getIngestion();
        return TokenTextSplitter.builder()
                .withChunkSize(ingest.getMaxChunkTokens())
                .withMinChunkSizeChars(100)
                .withMinChunkLengthToEmbed(10)
                .withMaxNumChunks(50_000)
                .withKeepSeparator(true)
                .build();
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) throws IOException {
        var builder = SimpleVectorStore.builder(embeddingModel);
        this.simpleVectorStore = builder.build();
        var path = properties.getVectorStore().persistPath();
        Files.createDirectories(path.getParent());
        if (Files.isRegularFile(path)) {
            try {
                this.simpleVectorStore.load(path.toFile());
            } catch (Exception e) {
                throw new IllegalStateException(
                            "Failed to load vector store from " + path + ". Delete the file to re-index.", e);
            }
        }
        return simpleVectorStore;
    }

    @PreDestroy
    public void persistVectorStore() {
        if (simpleVectorStore == null) {
            return;
        }
        try {
            var path = properties.getVectorStore().persistPath();
            Files.createDirectories(path.getParent());
            simpleVectorStore.save(path.toFile());
        } catch (IOException e) {
            // best-effort persistence on shutdown
        }
    }
}
