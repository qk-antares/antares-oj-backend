package com.antares.oj.model.dto.problemrun;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ProblemRunRequest {
    @NotBlank
    private String code;
    private String input;
    @NotBlank
    private String language;
}
