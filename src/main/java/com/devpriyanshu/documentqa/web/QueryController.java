package com.devpriyanshu.documentqa.web;

import com.devpriyanshu.documentqa.service.RagQueryService;
import com.devpriyanshu.documentqa.web.dto.QueryRequestDto;
import com.devpriyanshu.documentqa.web.dto.QueryResponseDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class QueryController {

    private final RagQueryService ragQueryService;

    public QueryController(RagQueryService ragQueryService) {
        this.ragQueryService = ragQueryService;
    }

    @PostMapping("/query")
    public QueryResponseDto query(@Valid @RequestBody QueryRequestDto body) {
        RagQueryService.RagAnswer answer = ragQueryService.answer(body.documentId(), body.question());
        return QueryResponseDto.from(answer);
    }
}
