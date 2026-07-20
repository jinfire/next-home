package com.nexthome.backend.alert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AlertConditionServiceTest {
    @Test
    void updatesTheSameBrowserRegionAndTargetInsteadOfDuplicatingIt() {
        UUID browserId = UUID.randomUUID();
        AlertConditionRepository repository = mock(AlertConditionRepository.class);
        AlertCondition existing = new AlertCondition(new AlertConditionRequest(
                browserId, 10, null, 4, new BigDecimal("20"), null));
        ReflectionTestUtils.setField(existing, "id", 1L);
        AlertConditionRequest changed = new AlertConditionRequest(
                browserId, 10, null, 4, new BigDecimal("15"), null);
        when(repository.findAllByBrowserId(browserId)).thenReturn(List.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        AlertConditionResponse response = new AlertConditionService(repository).create(changed);

        assertThat(response.targetGapPercent()).isEqualByComparingTo("15");
        verify(repository).save(existing);
    }
}
