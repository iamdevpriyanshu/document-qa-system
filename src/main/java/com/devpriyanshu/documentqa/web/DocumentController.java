package com.devpriyanshu.documentqa.web;

import com.devpriyanshu.documentqa.service.DocumentCatalogService;
import com.devpriyanshu.documentqa.service.DocumentIngestionService;
import com.devpriyanshu.documentqa.web.dto.DocumentSummaryDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentIngestionService ingestionService;
    private final DocumentCatalogService catalogService;

    public DocumentController(DocumentIngestionService ingestionService, DocumentCatalogService catalogService) {
        this.ingestionService = ingestionService;
        this.catalogService = catalogService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentSummaryDto upload(@RequestPart("file") MultipartFile file) throws IOException {
        return DocumentSummaryDto.from(ingestionService.ingest(file));
    }

    @GetMapping
    public List<DocumentSummaryDto> list() {
        return catalogService.listAll().stream().map(DocumentSummaryDto::from).toList();
    }

    @GetMapping("/{id}")
    public DocumentSummaryDto get(@PathVariable Long id) {
        return DocumentSummaryDto.from(catalogService.require(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) throws IOException {
        catalogService.delete(id);
    }
}
