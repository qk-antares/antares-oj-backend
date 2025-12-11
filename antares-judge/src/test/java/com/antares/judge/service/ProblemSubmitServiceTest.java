package com.antares.judge.service;


import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ProblemSubmitServiceTest {
    @Resource
    private ProblemSubmitService problemSubmitService;
    
    @Test
    public void testGetSubmitSummary() {
        problemSubmitService.getSubmitSummary(1848653149812953088L);
    }
}
