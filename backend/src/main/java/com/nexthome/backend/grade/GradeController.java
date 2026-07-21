package com.nexthome.backend.grade;
import java.util.List;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/grades")
public class GradeController {
    private final GradeService service; public GradeController(GradeService service){this.service=service;}
    @GetMapping public List<GradeSummary> find(@RequestParam int year){return service.findByYear(year);}
    @GetMapping("/years") public List<Integer> years(){return service.availableYears();}
    @PostMapping("/recalculate") public List<GradeSummary> recalculate(@RequestParam int year){return service.recalculate(year);}
}
