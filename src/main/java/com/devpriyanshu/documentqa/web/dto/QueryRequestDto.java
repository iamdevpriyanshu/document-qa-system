package com.devpriyanshu.documentqa.web.dto;

import jakarta.validation.constraints.NotBlank;

public record QueryRequestDto(Long documentId, @NotBlank(message = "question is required") String question) {}
