package com.devpriyanshu.documentqa.service;

import com.devpriyanshu.documentqa.config.DocumentQaProperties;
import com.devpriyanshu.documentqa.domain.IngestionStatus;
import com.devpriyanshu.documentqa.domain.StoredDocument;
import com.devpriyanshu.documentqa.repository.StoredDocumentRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class DocumentIngestionService {

    private static final String META_DOCUMENT_ID = "documentId";
    private static final String META_FILENAME = "filename";

    private final StoredDocumentRepository repository;
    private final DocumentQaProperties properties;
    private final TokenTextSplitter tokenTextSplitter;
    private final VectorStore vectorStore;

    public DocumentIngestionService(
            StoredDocumentRepository repository,
            DocumentQaProperties properties,
            TokenTextSplitter tokenTextSplitter,
            VectorStore vectorStore) {
        this.repository = repository;
        this.properties = properties;
        this.tokenTextSplitter = tokenTextSplitter;
        this.vectorStore = vectorStore;
    }

    @Transactional
    public StoredDocument ingest(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        Path uploadRoot = properties.uploadPath();
        Files.createDirectories(uploadRoot);

        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload";
        String safeName = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        String unique = UUID.randomUUID() + "_" + safeName;
        Path target = uploadRoot.resolve(unique);
        file.transferTo(target.toFile());

        StoredDocument entity = new StoredDocument();
        entity.setOriginalFilename(original);
        entity.setContentType(file.getContentType());
        entity.setSizeBytes(file.getSize());
        entity.setStoragePath(target.toAbsolutePath().toString());
        entity.setStatus(IngestionStatus.PENDING);
        entity = repository.save(entity);

        try {
            List<Document> pages = loadDocuments(target, file.getContentType(), original);
            List<Document> chunks = tokenTextSplitter.apply(pages);
            if (chunks.isEmpty()) {
                throw new IllegalStateException("No text could be extracted from the file");
            }
            String docId = String.valueOf(entity.getId());
            for (Document chunk : chunks) {
                chunk.getMetadata().put(META_DOCUMENT_ID, docId);
                chunk.getMetadata().put(META_FILENAME, original);
            }
            vectorStore.add(chunks);
            entity.setStatus(IngestionStatus.INDEXED);
        } catch (Exception ex) {
            entity.setStatus(IngestionStatus.FAILED);
            entity.setFailureReason(truncate(ex.getMessage(), 3900));
            Files.deleteIfExists(target);
            entity.setStoragePath("(deleted)");
        }
        return repository.save(entity);
    }

    private List<Document> loadDocuments(Path path, String contentType, String originalFilename)
            throws IOException {
        var resource = new FileSystemResource(path.toFile());
        String lowerName = originalFilename.toLowerCase(Locale.ROOT);
        boolean pdf = (contentType != null && contentType.toLowerCase(Locale.ROOT).contains("pdf"))
                || lowerName.endsWith(".pdf");
        if (pdf) {
            var reader = new PagePdfDocumentReader(resource, PdfDocumentReaderConfig.defaultConfig());
            return reader.get();
        }
        var tika = new TikaDocumentReader(resource);
        return tika.get();
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
