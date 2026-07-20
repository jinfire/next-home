package com.nexthome.backend.alert;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AlertConditionRepository extends JpaRepository<AlertCondition, Long> {
    List<AlertCondition> findAllByEnabledTrue();
    List<AlertCondition> findAllByBrowserId(UUID browserId);
}
