package com.antares.judge.service;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

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

    @Test
    public void loadCheckIn() {
        String[] dates = {
            "2025-03-21",
            "2025-03-22",
            "2025-09-11",
            "2025-12-10",
            "2025-12-11",
        };

        List<Future<Boolean>> futures = new ArrayList<>();
        for(String date : dates) {
            futures.add(problemSubmitService.checkInAsync(1848653149812953088L, LocalDate.parse(date)));
        }


        for(Future<Boolean> future : futures) {
            try {
                System.out.println("Check-in result: " + future.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void getCheckInDates() {
        List<String> checkInDates = problemSubmitService.getCheckInDatesByRedis(1848653149812953088L, "2025-03");
        System.out.println("Check-in dates: " + checkInDates);
    }
}
