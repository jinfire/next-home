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
        return repository.save(new AlertCondition(request)).response();
    }
}
