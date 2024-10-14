package io.cockroachdb.wan.web.api;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.wan.util.timeseries.Metrics;
import io.cockroachdb.wan.web.api.model.WorkloadForm;
import io.cockroachdb.wan.web.api.model.WorkloadType;
import io.cockroachdb.wan.web.frontend.WorkloadPageController;
import io.cockroachdb.wan.workload.WorkloadEntity;
import io.cockroachdb.wan.workload.WorkloadManager;
import io.cockroachdb.wan.workload.profile.ProfileWorkloads;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/workload")
public class WorkloadController {
    private static final SimpleRepresentationModelAssembler<WorkloadEntity> assembler
            = new SimpleRepresentationModelAssembler<>() {
        @Override
        public void addLinks(EntityModel<WorkloadEntity> resource) {
            WorkloadEntity workload = resource.getContent();
            Link selfLink = linkTo(methodOn(WorkloadController.class)
                    .getWorkload(workload.getId()))
                    .withSelfRel();
            if (workload.isRunning()) {
                selfLink = selfLink.andAffordance(afford(methodOn(WorkloadController.class)
                        .cancelWorkload(workload.getId())));

                resource.add(linkTo(methodOn(WorkloadPageController.class)
                        .cancelWorkload(workload.getId()))
                        .withRel(LinkRelations.CANCEL_REL));
            } else {
                selfLink = selfLink.andAffordance(afford(methodOn(WorkloadController.class)
                        .deleteWorkload(workload.getId())));

                resource.add(linkTo(methodOn(WorkloadPageController.class)
                        .deleteWorkload(workload.getId()))
                        .withRel(LinkRelations.DELETE_REL));
            }
            resource.add(selfLink);
        }

        @Override
        public void addLinks(CollectionModel<EntityModel<WorkloadEntity>> resources) {
            resources.add(linkTo(methodOn(WorkloadController.class)
                    .listWorkloads())
                    .withSelfRel());
            resources.add(linkTo(methodOn(WorkloadController.class)
                    .getWorkloadForm())
                    .withRel(LinkRelations.WORKLOAD_FORM_REL));
        }
    };

    @Autowired
    private WorkloadManager workloadManager;

    @Autowired
    private ProfileWorkloads profileWorkloads;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<WorkloadEntity>>> listWorkloads() {
        CollectionModel<EntityModel<WorkloadEntity>> models
                = assembler.toCollectionModel(workloadManager.getWorkloads());
        return ResponseEntity.ok(models);
    }

    @GetMapping(value = "/{id}")
    public HttpEntity<EntityModel<WorkloadEntity>> getWorkload(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(assembler.toModel(workloadManager.findById(id)));
    }

    @PutMapping(value = "/{id}/cancel")
    public HttpEntity<EntityModel<WorkloadEntity>> cancelWorkload(@PathVariable("id") Integer id) {
        WorkloadEntity workload = workloadManager.findById(id);
        if (workload.cancel()) {
            return ResponseEntity.ok(assembler.toModel(workload));
        } else {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(assembler.toModel(workload));
        }
    }

    @DeleteMapping(value = "/{id}/delete")
    public HttpEntity<Void> deleteWorkload(@PathVariable("id") Integer id) {
        try {
            workloadManager.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping(value = "/form")
    public HttpEntity<WorkloadForm> getWorkloadForm() {
        WorkloadForm form = new WorkloadForm();
        form.setWorkloadType(WorkloadType.random_wait);
        form.setDuration("00:15");

        return ResponseEntity.ok(form
                .add(linkTo(methodOn(WorkloadController.class)
                        .getWorkloadForm())
                        .withSelfRel()
                        .andAffordance(
                                afford(methodOn(WorkloadController.class)
                                        .startWorkload(null)))
                ));
    }

    @PostMapping
    public HttpEntity<CollectionModel<EntityModel<WorkloadEntity>>> startWorkload(
            @RequestBody WorkloadForm form) {
        LocalTime time = LocalTime.parse(form.getDuration(), DateTimeFormatter.ofPattern("HH:mm"));
        Duration duration = Duration.ofHours(time.getHour()).plusMinutes(time.getMinute());

        List<WorkloadEntity> entities = new ArrayList<>();

        IntStream.rangeClosed(1, form.getWorkloadCount()).forEach(value -> {
            WorkloadEntity workload = profileWorkloads.addWorkload(form.getWorkloadType(), duration);
            entities.add(workload);
        });

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toCollectionModel(entities));
    }

    @GetMapping(value = "/data-points/p95")
    public List<Map<String, Object>> getDataPointsP95() {
        return getDataPoints(Metrics::getP95);
    }

    @GetMapping(value = "/data-points/p99")
    public List<Map<String, Object>> getDataPointsP99() {
        return getDataPoints(Metrics::getP99);
    }

    @GetMapping(value = "/data-points/tps")
    public List<Map<String, Object>> getDataPointsTPS() {
        return getDataPoints(Metrics::getOpsPerSec);
    }

    private List<Map<String, Object>> getDataPoints(Function<Metrics, Double> mapper) {
        final List<Map<String, Object>> columnData = new ArrayList<>();

        {
            final Map<String, Object> headerElement = new HashMap<>();
            List<String> labels = workloadManager
                    .getTimeSeriesInterval()
                    .stream()
                    .map(instant -> {
                        LocalTime time = LocalTime.ofInstant(instant, ZoneId.systemDefault());
                        return "%02d:%02d".formatted(time.getMinute(), time.getSecond());
                    })
                    .toList();
            headerElement.put("data", labels.toArray());
            columnData.add(headerElement);
        }

        workloadManager.getWorkloads().forEach(workload -> {
            Map<String, Object> dataElement = new HashMap<>();

            List<Double> data = workloadManager
                    .getTimeSeriesValues(workload.getId())
                    .stream()
                    .filter(metric -> !metric.isExpired())
                    .map(mapper)
                    .toList();

            dataElement.put("id", workload.getId());
            dataElement.put("name", "%s (%d)".formatted(workload.getTitle(), workload.getId()));
            dataElement.put("data", data.toArray());

            columnData.add(dataElement);
        });

        return columnData;
    }
}

