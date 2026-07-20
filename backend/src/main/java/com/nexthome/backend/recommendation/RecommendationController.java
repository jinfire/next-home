package com.nexthome.backend.recommendation;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService service;
    public RecommendationController(RecommendationService service){this.service=service;}
    @GetMapping("/upgrades")
    public List<UpgradeRecommendation> upgrades(@RequestParam int currentGrade, @RequestParam int year) {
        if(currentGrade<1 || currentGrade>10) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"현재 급지는 1~10이어야 합니다.");
        return service.recommend(currentGrade,year);
    }
}
