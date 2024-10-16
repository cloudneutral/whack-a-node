package io.cockroachdb.wan.workload.profile;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import io.cockroachdb.wan.web.api.model.WorkloadType;
import io.cockroachdb.wan.workload.WorkloadEntity;
import io.cockroachdb.wan.workload.WorkloadManager;

@Component
public class ProfileWorkloads {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private WorkloadManager workloadManager;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    private Optional<ProfileEntity> latest = Optional.empty();

    /**
     * Cyclic profile iteration using keyset approach to avoid full scans.
     *
     * @return next profile
     */
    private Optional<ProfileEntity> nextProfile() {
        latest.ifPresentOrElse(profileEntity -> {
            latest = profileRepository.findByNextId(profileEntity.getId());
        }, () -> {
            latest = profileRepository.findFirst();
        });
        return latest;
    }

    private void initSchema() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setCommentPrefix("--");
        populator.setIgnoreFailedDrops(true);
        populator.addScript(new ClassPathResource("db/profile/profile-create.sql"));

        try {
            DatabasePopulatorUtils.execute(populator, dataSource);
        } catch (DataAccessException e) {
            logger.warn("Unable to create db schema - continuing");
        }
    }

    public WorkloadEntity addWorkload(WorkloadType workloadType, Duration duration) {
        initSchema();

        Callable<?> callable =
                switch (workloadType) {
                    case profile_insert -> createInsertWorkload();
                    case profile_batch_insert -> createBatchInsertWorkload();
                    case profile_update -> createUpdateWorkload();
                    case profile_delete -> createDeleteWorkload();
                    case profile_read -> createReadWorkload();
                    case profile_scan -> createScanWorkload();
                    case select_one -> createSelectOneWorkload();
                    case random_wait -> createRandomSleepWorkload();
                    case fixed_wait -> createFixedSleepWorkload();
                };
        return workloadManager
                .addWorkload(callable, duration, workloadType.getDisplayValue());
    }

    private Callable<?> createRandomSleepWorkload() {
        return () -> {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            if (random.nextDouble(1.0) > 0.95) {
                TimeUnit.MILLISECONDS.sleep(random.nextLong(500, 2500));
            } else {
                TimeUnit.MILLISECONDS.sleep(random.nextLong(0, 5));
            }
            return 0;
        };
    }

    private Callable<?> createFixedSleepWorkload() {
        return () -> {
            TimeUnit.MILLISECONDS.sleep(500);
            return 0;
        };
    }

    private Callable<?> createInsertWorkload() {
        return () -> {
            profileRepository.insertProfileSingleton();
            return 0;
        };
    }

    private Callable<?> createBatchInsertWorkload() {
        return () -> {
            profileRepository.insertProfileBatch();
            return 0;
        };
    }

    private Callable<?> createUpdateWorkload() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);

        return () -> {
            transactionTemplate.executeWithoutResult(transactionStatus -> {
                nextProfile().ifPresent(profileEntity -> {
                    profileRepository.updateProfile(profileEntity);
                });
            });
            return 0;
        };
    }

    private Callable<?> createDeleteWorkload() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);

        return () -> {
            transactionTemplate.executeWithoutResult(transactionStatus -> {
                nextProfile().ifPresent(profileEntity ->
                        profileRepository.deleteProfileById(profileEntity.getId()));
            });
            return 0;
        };
    }

    private Callable<?> createReadWorkload() {
        return () -> {
            nextProfile();
            return 0;
        };
    }

    private Callable<?> createScanWorkload() {
        return () -> {
            profileRepository.findByRandomId();
            return 0;
        };
    }

    private Callable<?> createSelectOneWorkload() {
        return () -> {
            jdbcTemplate.execute("select 1");
            return 0;
        };
    }
}
