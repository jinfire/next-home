package com.nexthome.backend.apartment;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/apartments")
public class ApartmentController {
    private final ApartmentSearchService service;
    public ApartmentController(ApartmentSearchService service) { this.service = service; }
    @GetMapping
    public List<ApartmentSummary> search(@RequestParam String query, @RequestParam(required = false) Long regionId) {
        if (query == null || query.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "검색어가 필요합니다.");
        return service.search(query, regionId);
    }
}
