package io.cockroachdb.wan.web.api;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.cockroachdb.wan.web.api.model.MessageModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api")
public class IndexController {
    @Autowired
    private BuildProperties buildProperties;

    @Autowired
    private Environment environment;

    @GetMapping
    public ResponseEntity<MessageModel> index() {
        MessageModel index = MessageModel.from("Welcome to %s %s"
                .formatted(buildProperties.getName(), buildProperties.getVersion()));
        index.add(linkTo(methodOn(getClass())
                .index())
                .withSelfRel());
        index.add(linkTo(methodOn(ClusterController.class)
                .getCluster())
                .withRel(LinkRelations.CLUSTER_REL));

        Arrays.stream(environment.getActiveProfiles())
                .findFirst()
                .ifPresent(profile -> {
                    index.add(linkTo(methodOn(WorkloadController.class)
                            .listWorkloads())
                            .withRel(LinkRelations.WORKLOADS_REL));
                });

        index.add(Link.of(ServletUriComponentsBuilder.fromCurrentContextPath()
                        .pathSegment("actuator")
                        .buildAndExpand()
                        .toUriString())
                .withRel(LinkRelations.ACTUATORS_REL)
                .withTitle("Spring boot actuators"));

        return ResponseEntity.ok(index);
    }
}
