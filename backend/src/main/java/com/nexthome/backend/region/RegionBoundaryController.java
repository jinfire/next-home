package com.nexthome.backend.region;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/region-boundaries")
public class RegionBoundaryController {
    private static final MediaType GEO_JSON = MediaType.parseMediaType("application/geo+json");
    private final RegionBoundaryService service;

    public RegionBoundaryController(RegionBoundaryService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<String> find(@RequestParam int year) {
        return ResponseEntity.ok().contentType(GEO_JSON).body(service.findByYear(year));
    }
}
