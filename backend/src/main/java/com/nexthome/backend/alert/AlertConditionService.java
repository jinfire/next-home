package com.nexthome.backend.alert;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertConditionService {
    private final AlertConditionRepository repository;

    AlertConditionService(AlertConditionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AlertConditionResponse create(AlertConditionRequest request) {
        AlertCondition condition = repository.findAllByBrowserId(request.browserId()).stream()
                .filter(existing -> existing.sameTarget(request))
                .findFirst()
                .orElseGet(() -> new AlertCondition(request));
        condition.update(request);
        return repository.save(condition).response();
    }
}
