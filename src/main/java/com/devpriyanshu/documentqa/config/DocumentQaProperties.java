package com.devpriyanshu.documentqa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.nio.file.Path;

@Validated
@ConfigurationProperties(prefix = "app")
public class DocumentQaProperties {

    @NotBlank
    private String uploadDir = "./uploads";

    private final Rag rag = new Rag();
    private final Ingestion ingestion = new Ingestion();
    private final VectorStore vectorStore = new VectorStore();

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public Path uploadPath() {
        return Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public Rag getRag() {
        return rag;
    }

    public Ingestion getIngestion() {
        return ingestion;
    }

    public VectorStore getVectorStore() {
        return vectorStore;
    }

    public static class Rag {
        @Min(1)
        @Max(32)
        private int topK = 6;

        private double minSimilarity = 0.0;

        @Min(1024)
        @Max(100_000)
        private int maxContextChars = 12000;

        public int getTopK() {
            return topK;
        }

        public void setTopK(int topK) {
            this.topK = topK;
        }

        public double getMinSimilarity() {
            return minSimilarity;
        }

        public void setMinSimilarity(double minSimilarity) {
            this.minSimilarity = minSimilarity;
        }

        public int getMaxContextChars() {
            return maxContextChars;
        }

        public void setMaxContextChars(int maxContextChars) {
            this.maxContextChars = maxContextChars;
        }
    }

    public static class Ingestion {
        @Min(50)
        @Max(2000)
        private int maxChunkTokens = 380;

        public int getMaxChunkTokens() {
            return maxChunkTokens;
        }

        public void setMaxChunkTokens(int maxChunkTokens) {
            this.maxChunkTokens = maxChunkTokens;
        }
    }

    public static class VectorStore {
        private String persistFile = "./data/vector-store.json";

        public String getPersistFile() {
            return persistFile;
        }

        public void setPersistFile(String persistFile) {
            this.persistFile = persistFile;
        }

        public Path persistPath() {
            return Path.of(persistFile).toAbsolutePath().normalize();
        }
    }
}
