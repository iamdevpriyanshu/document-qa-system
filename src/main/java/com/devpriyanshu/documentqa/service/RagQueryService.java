package com.devpriyanshu.documentqa.service;

import com.devpriyanshu.documentqa.config.DocumentQaProperties;
import com.devpriyanshu.documentqa.domain.IngestionStatus;
import com.devpriyanshu.documentqa.domain.StoredDocument;
import com.devpriyanshu.documentqa.exception.DocumentNotFoundException;
import com.devpriyanshu.documentqa.repository.StoredDocumentRepository;
import com.devpriyanshu.documentqa.web.dto.SourceSnippetDto;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RagQueryService {

    private static final String SYSTEM_INSTRUCTION = """
            You are a precise assistant. Answer ONLY using the CONTEXT below.
            If the context does not contain enough information, reply exactly: "I do not have enough information in the uploaded documents to answer that."
            Do not invent facts. Use concise sentences.
            """;

    private final VectorStore vectorStore;
    private final ChatModel chatModel;
    private final DocumentQaProperties properties;
    private final StoredDocumentRepository documentRepository;

    public RagQueryService(
            VectorStore vectorStore,
            ChatModel chatModel,
            DocumentQaProperties properties,
            StoredDocumentRepository documentRepository) {
        this.vectorStore = vectorStore;
        this.chatModel = chatModel;
        this.properties = properties;
        this.documentRepository = documentRepository;
    }

    public RagAnswer answer(Long documentId, String question) {
        String q = question != null ? question.strip() : "";
        if (q.isEmpty()) {
            throw new IllegalArgumentException("question must not be blank");
        }
        if (documentId != null) {
            StoredDocument doc = documentRepository
                    .findById(documentId)
                    .orElseThrow(() -> new DocumentNotFoundException(documentId));
            if (doc.getStatus() != IngestionStatus.INDEXED) {
                throw new IllegalStateException("Document is not indexed (status=" + doc.getStatus() + ")");
            }
        }

        SearchRequest.Builder search = SearchRequest.builder()
                .query(q)
                .topK(properties.getRag().getTopK())
                .similarityThreshold(properties.getRag().getMinSimilarity());
        if (documentId != null) {
            search.filterExpression("documentId == '" + documentId + "'");
        }
        List<Document> retrieved = vectorStore.similaritySearch(search.build());

        String context = buildContext(retrieved);
        List<SourceSnippetDto> sources = buildSources(retrieved);

        String userContent =
                "CONTEXT:\n" + context + "\n\nQUESTION:\n" + q + "\n\nAnswer in plain text.";
        Prompt prompt = new Prompt(List.of(new SystemMessage(SYSTEM_INSTRUCTION), new UserMessage(userContent)));
        ChatResponse response = chatModel.call(prompt);
        String text = response.getResult().getOutput().getText();
        return new RagAnswer(text, documentId, sources);
    }

    private String buildContext(List<Document> docs) {
        if (docs.isEmpty()) {
            return "(no matching passages retrieved)";
        }
        int max = properties.getRag().getMaxContextChars();
        StringBuilder sb = new StringBuilder(Math.min(max, 4096));
        String sep = "\n\n---\n\n";
        for (Document d : docs) {
            String piece = d.getText();
            if (piece == null || piece.isBlank()) {
                continue;
            }
            if (sb.length() + sep.length() + piece.length() > max) {
                int room = max - sb.length() - sep.length();
                if (room > 100) {
                    sb.append(sep).append(piece, 0, Math.min(piece.length(), room));
                    sb.append("\n...[context truncated for token safety]");
                }
                break;
            }
            if (!sb.isEmpty()) {
                sb.append(sep);
            }
            sb.append(piece);
        }
        return sb.isEmpty() ? "(no matching passages retrieved)" : sb.toString();
    }

    private List<SourceSnippetDto> buildSources(List<Document> docs) {
        List<SourceSnippetDto> list = new ArrayList<>();
        int i = 0;
        for (Document d : docs) {
            String filename = d.getMetadata().getOrDefault("filename", "").toString();
            String snippet = excerpt(d.getText(), 240);
            list.add(new SourceSnippetDto(++i, filename, snippet));
        }
        return list;
    }

    private static String excerpt(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        String t = text.replace('\n', ' ').strip();
        if (t.length() <= maxLen) {
            return t;
        }
        return t.substring(0, maxLen) + "…";
    }

    public record RagAnswer(String answer, Long documentId, List<SourceSnippetDto> sources) {}
}
