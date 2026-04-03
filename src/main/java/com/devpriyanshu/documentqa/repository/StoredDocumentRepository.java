package com.devpriyanshu.documentqa.repository;

import com.devpriyanshu.documentqa.domain.StoredDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredDocumentRepository extends JpaRepository<StoredDocument, Long> {
}
