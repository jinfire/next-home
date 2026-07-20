package com.nexthome.backend.trade;
import java.util.List;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/apartments/{apartmentId}/trades")
public class TradeController {
    private final TradeQueryService service;
    public TradeController(TradeQueryService service) { this.service = service; }
    @GetMapping public List<TradeSummary> recent(@PathVariable Long apartmentId) { return service.recentByApartment(apartmentId); }
}
