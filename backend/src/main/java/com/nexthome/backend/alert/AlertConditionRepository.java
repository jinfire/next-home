package com.nexthome.backend.alert;

import org.springframework.data.jpa.repository.JpaRepository;

interface AlertConditionRepository extends JpaRepository<AlertCondition, Long> {
}
