package com.nexthome.backend.trade;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class TradeQueryService {
    private final TradeRepository repository;
    TradeQueryService(TradeRepository repository) { this.repository = repository; }
    @Transactional(readOnly = true)
    public List<TradeSummary> recentByApartment(Long id) { return repository.findTop100ByApartmentIdOrderByContractDateDescIdDesc(id).stream().map(TradeSummary::from).toList(); }
}
