package io.cockroachdb.wan.workload;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.cockroachdb.wan.cluster.NotFoundException;
import io.cockroachdb.wan.util.timeseries.DataPoint;
import io.cockroachdb.wan.util.timeseries.Metrics;
import io.cockroachdb.wan.web.model.MessageModel;
import io.cockroachdb.wan.web.model.WorkloadType;
import io.cockroachdb.wan.web.push.SimpMessagePublisher;
import io.cockroachdb.wan.web.push.TopicName;

@Component
public class WorkloadManager {
    private static final AtomicInteger monotonicIdGenerator = new AtomicInteger();

    private static final long samplePeriodSeconds = TimeUnit.MINUTES.toSeconds(5);

    private final List<WorkloadEntity> workloads = Collections.synchronizedList(new ArrayList<>());

    private final List<DataPoint<Integer>> dataPoints = new LinkedList<>();

    @Autowired
    private WorkloadExecutor executorHelper;

    @Autowired
    private SimpMessagePublisher messagePublisher;

    @Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void scheduledClusterStatusUpdate() {
        workloads.forEach(workload ->
                messagePublisher.convertAndSendNow(TopicName.WORKLOAD_ITEM, workload));
    }

    /**
     * Walk through all workloads and grab call metrics for a new data point.
     */
    @Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void addMetricsDataPoint() {
        // Purge old data points older than sample period
        dataPoints.removeIf(item -> item.getInstant()
                .isBefore(Instant.now().minusSeconds(samplePeriodSeconds)));

        // Add new datapoint by sampling all workload metrics
        DataPoint<Integer> dataPoint = new DataPoint<>(Instant.now());
        workloads.forEach(workload ->
                dataPoint.put(workload.getId(), workload.getMetrics()));

        dataPoints.add(dataPoint);

        messagePublisher.convertAndSendNow(TopicName.WORKLOAD_CHART, MessageModel.from(""));
        messagePublisher.convertAndSendNow(TopicName.WORKLOAD_METRICS, getAggregatedMetrics());
    }

    /**
     * Return time series sample interval (x-axis)
     */
    public List<Instant> getTimeSeriesInterval() {
        return dataPoints.stream().map(DataPoint::getInstant).toList();
    }

    /**
     * Return time series sample values/metrics per workload (y-axis)
     *
     * @param id workload id
     */
    public List<Metrics> getTimeSeriesValues(Integer id) {
        List<Metrics> latencies = new ArrayList<>();
        dataPoints.forEach(dataPoint -> latencies.add(dataPoint.get(id)));
        return latencies;
    }

    public Metrics getAggregatedMetrics() {
        List<Metrics> metrics = workloads.stream()
                .map(WorkloadEntity::getMetrics).toList();
        return Metrics.builder()
                .withTime(Instant.now())
                .withMeanTimeMillis(metrics.stream()
                        .mapToDouble(Metrics::getMeanTimeMillis).average().orElse(0))
                .withOps(metrics.stream().mapToDouble(Metrics::getOpsPerSec).sum(),
                        metrics.stream().mapToDouble(Metrics::getOpsPerMin).sum())
                .withP50(metrics.stream().mapToDouble(Metrics::getP50).average().orElse(0))
                .withP90(metrics.stream().mapToDouble(Metrics::getP90).average().orElse(0))
                .withP95(metrics.stream().mapToDouble(Metrics::getP95).average().orElse(0))
                .withP99(metrics.stream().mapToDouble(Metrics::getP99).average().orElse(0))
                .withP999(metrics.stream().mapToDouble(Metrics::getP999).average().orElse(0))
                .withMeanTimeMillis(metrics.stream().mapToDouble(Metrics::getMeanTimeMillis).average().orElse(0))
                .withSuccessful(metrics.stream().mapToInt(Metrics::getSuccess).sum())
                .withFails(metrics.stream().mapToInt(Metrics::getTransientFail).sum(),
                        metrics.stream().mapToInt(Metrics::getNonTransientFail).sum())
                .build();
    }

    public <V> WorkloadEntity addWorkload(Callable<V> callable, Duration duration, WorkloadType workloadType) {
        final Instant stopTime = Instant.now().plus(duration);

        Metrics metrics = Metrics.empty();

        Future<?> future = executorHelper.submit(
                callable,
                metrics,
                calls -> Instant.now().isBefore(stopTime));

        WorkloadEntity workload = new WorkloadEntity(
                monotonicIdGenerator.incrementAndGet(),
                stopTime,
                workloadType.getDisplayValue(),
                future,
                metrics);

        workloads.add(workload);

        return workload;
    }

    public List<WorkloadEntity> getWorkloads() {
        return Collections.unmodifiableList(workloads);
    }

    public WorkloadEntity findById(Integer id) {
        return workloads.stream()
                .filter(workload -> Objects.equals(workload.getId(), id))
                .findAny()
                .orElseThrow(() -> new NotFoundException("No such workload with id: " + id));
    }

    public void deleteById(Integer id) {
        WorkloadEntity workload = findById(id);
        if (workload.isRunning()) {
            throw new IllegalStateException("Workload is running: " + id);
        }
        workloads.remove(workload);
    }

    public void cancelAll() {
        workloads.stream()
                .filter(WorkloadEntity::isRunning)
                .forEach(WorkloadEntity::cancel);
    }

    public void deleteAll() {
        cancelAll();
        workloads.clear();
    }
}
