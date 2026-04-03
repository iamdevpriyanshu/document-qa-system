package com.devpriyanshu.documentqa.service;

import com.devpriyanshu.documentqa.domain.StoredDocument;
import com.devpriyanshu.documentqa.exception.DocumentNotFoundException;
import com.devpriyanshu.documentqa.repository.StoredDocumentRepository;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class DocumentCatalogService {

    private final StoredDocumentRepository repository;
    private final VectorStore vectorStore;

    public DocumentCatalogService(StoredDocumentRepository repository, VectorStore vectorStore) {
        this.repository = repository;
        this.vectorStore = vectorStore;
    }

    @Transactional(readOnly = true)
    public List<StoredDocument> listAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public StoredDocument require(Long id) {
        return repository.findById(id).orElseThrow(() -> new DocumentNotFoundException(id));
    }

    @Transactional
    public void delete(Long id) throws IOException {
        StoredDocument doc = require(id);
        Filter.Expression filter = new FilterExpressionTextParser()
                .parse("documentId == '" + id + "'");
        vectorStore.delete(filter);
        String path = doc.getStoragePath();
        if (path != null && !path.isBlank() && !"(deleted)".equals(path)) {
            Files.deleteIfExists(Path.of(path));
        }
        repository.delete(doc);
    }
}
