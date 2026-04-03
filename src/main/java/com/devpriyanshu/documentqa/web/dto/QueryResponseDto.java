package com.devpriyanshu.documentqa.web.dto;

import com.devpriyanshu.documentqa.service.RagQueryService;

import java.util.List;

public record QueryResponseDto(String answer, Long documentId, List<SourceSnippetDto> sources) {

    public static QueryResponseDto from(RagQueryService.RagAnswer a) {
        return new QueryResponseDto(a.answer(), a.documentId(), a.sources());
    }
}
