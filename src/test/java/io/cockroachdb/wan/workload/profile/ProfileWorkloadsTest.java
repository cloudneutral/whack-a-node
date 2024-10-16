package io.cockroachdb.wan.workload.profile;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.cockroachdb.wan.web.api.model.WorkloadType;
import io.cockroachdb.wan.workload.WorkloadEntity;

public class ProfileWorkloadsTest extends AbstractIntegrationTest {
    @Autowired
    private ProfileWorkloads profileWorkloads;

    @Autowired
    private ProfileRepository profileRepository;

    @BeforeAll
    public void setupTest() {
        profileRepository.deleteAll();
    }

    @Order(0)
    @Test
    public void whenStartingInsertWorkload_thenExpectRows() {
        List<ProfileEntity> before = profileRepository.findAll(65536);

        WorkloadEntity workload = profileWorkloads.addWorkload(WorkloadType.profile_insert, Duration.ofSeconds(5));
        workload.awaitCompletion();

        List<ProfileEntity> after = profileRepository.findAll(65536);
        Assertions.assertEquals(before.size() + workload.getMetrics().getSuccess(), after.size());
    }

    @Order(1)
    @Test
    public void whenStartingBatchInsertWorkload_thenExpectRows() {
        List<ProfileEntity> before = profileRepository.findAll(65536);

        WorkloadEntity workload = profileWorkloads.addWorkload(WorkloadType.profile_batch_insert,
                Duration.ofSeconds(5));
        workload.awaitCompletion();

        List<ProfileEntity> after = profileRepository.findAll(65536);
        Assertions.assertEquals(before.size() + workload.getMetrics().getSuccess() * 32, after.size());
    }

    @Order(2)
    @Test
    public void whenStartingUpdateWorkload_thenExpectRowsAffected() {
        WorkloadEntity workload = profileWorkloads.addWorkload(WorkloadType.profile_update, Duration.ofSeconds(5));
        workload.awaitCompletion();

        Assertions.assertTrue(workload.getMetrics().getSuccess() > 0);
    }

    @Order(4)
    @Test
    public void whenStartingDeleteWorkload_thenExpectRowsAffected() {
        WorkloadEntity workload = profileWorkloads.addWorkload(WorkloadType.profile_delete, Duration.ofSeconds(5));
        workload.awaitCompletion();

        Assertions.assertTrue(workload.getMetrics().getSuccess() > 0);
    }

    @Order(5)
    @Test
    public void whenStartingReadWorkload_thenExpectRows() {
        WorkloadEntity workload = profileWorkloads.addWorkload(WorkloadType.profile_read, Duration.ofSeconds(5));
        workload.awaitCompletion();

        Assertions.assertTrue(workload.getMetrics().getSuccess() > 0);
    }

    @Order(5)
    @Test
    public void whenStartingScanWorkload_thenExpectRows() {
        WorkloadEntity workload = profileWorkloads.addWorkload(WorkloadType.profile_scan, Duration.ofSeconds(5));
        workload.awaitCompletion();

        Assertions.assertTrue(workload.getMetrics().getSuccess() > 0);
    }
}
