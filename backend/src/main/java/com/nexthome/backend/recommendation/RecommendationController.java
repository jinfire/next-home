package com.nexthome.backend.recommendation;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService service;

    public RecommendationController(RecommendationService service) {
        this.service = service;
    }

    @GetMapping("/upgrades")
    public Object upgrades(
            @RequestParam(required = false) Integer currentGrade,
            @RequestParam(required = false) Long regionId,
            @RequestParam int year) {
        if (regionId != null) return service.recommendRegion(regionId, year);
        if (currentGrade == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "regionId가 필요합니다.");
        }
        if (currentGrade < 1 || currentGrade > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "currentGrade must be between 1 and 10");
        }
        return service.recommend(currentGrade, year);
    }

    @GetMapping("/apartments")
    public List<LifestyleApartmentRecommendation> apartments(
            @RequestParam long apartmentId,
            @RequestParam int year) {
        return service.recommendApartments(apartmentId, year);
    }

    @GetMapping("/apartments/current")
    public CurrentApartmentPrice currentApartment(
            @RequestParam long apartmentId,
            @RequestParam int year) {
        return service.currentApartmentPrice(apartmentId, year);
    }
}
