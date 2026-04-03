# AI-Powered Document Q&A System

Spring Boot backend: upload documents (PDF and other Tika-supported formats), chunk and embed text, store vectors in a file-backed `SimpleVectorStore`, and answer questions with **RAG** over OpenAI **chat** and **embedding** APIs. Document metadata lives in **H2** (file `./data/documentqa`).

## Requirements

- Java **17+**
- Maven **3.9+**
- **`OPENAI_API_KEY`** with access to `gpt-4o-mini` and `text-embedding-3-small` (or change models in `src/main/resources/application.yml`)

## Run

```bash
export OPENAI_API_KEY="sk-..."
cd document-qa-system
mvn spring-boot:run
```

API base URL: `http://localhost:8080`

## HTTP API (JSON)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/documents` | Multipart form field **`file`**: upload and index |
| `GET` | `/api/v1/documents` | List documents and ingestion status |
| `GET` | `/api/v1/documents/{id}` | Document metadata |
| `DELETE` | `/api/v1/documents/{id}` | Delete file, DB row, and vector chunks |
| `POST` | `/api/v1/query` | Body: `{"documentId": 1, "question": "..."}` (`documentId` optional = search all indexed docs) |

### Example: query

```bash
curl -sS http://localhost:8080/api/v1/query \
  -H "Content-Type: application/json" \
  -d '{"documentId":1,"question":"What is the main topic?"}'
```

Vectors are persisted to `./data/vector-store.json` on shutdown and reloaded on startup (delete this file if you change embedding models or dimension).

## Configuration choices (resume-aligned)

| Topic | Decision |
|--------|-----------|
| LLM / embeddings | OpenAI via Spring AI (`spring-ai-starter-model-openai`) |
| SQL | H2 file DB for `StoredDocument` metadata (swap URL for PostgreSQL in production) |
| Vector store | `SimpleVectorStore` + JSON persistence (good for demoes; use pgvector/Milvus at scale) |
| Ingestion | PDF via `PagePdfDocumentReader`; other types via `TikaDocumentReader` |
| Token / context limits | Chunk size and max context length tunable under `app.*` in `application.yml` |
| Errors | JSON `ApiErrorDto` via `GlobalExceptionHandler`; upload size returns **413**; provider errors **502** where applicable |
