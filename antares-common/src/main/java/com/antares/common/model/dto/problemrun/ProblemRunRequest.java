package com.antares.common.model.dto.problemrun;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class ProblemRunRequest {
    @NotBlank
    private String code;
    private String input;
    @NotBlank
    private String language;
}
