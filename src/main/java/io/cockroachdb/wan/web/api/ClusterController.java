package io.cockroachdb.wan.web.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.wan.cluster.ClusterRepository;
import io.cockroachdb.wan.model.ClusterModel;
import io.cockroachdb.wan.web.model.MessageModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/cluster")
public class ClusterController {
    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public ResponseEntity<ClusterModel> getCluster() {
        ClusterModel clusterModel = new ClusterModel();
        clusterModel.setNodes(NodeController.nodeModelAssembler
                .toCollectionModel(clusterRepository.queryNodes())
                .getContent());
        clusterModel.add(linkTo(methodOn(ClusterController.class)
                .getVersion())
                .withRel(LinkRelations.VERSION_REL));
        clusterModel.add(linkTo(methodOn(ClusterController.class)
                .getCluster())
                .withSelfRel());
        return ResponseEntity.ok(clusterModel);
    }

    @GetMapping("/version")
    public ResponseEntity<MessageModel> getVersion() {
        MessageModel model = MessageModel
                .from(jdbcTemplate.queryForObject("select version()", String.class));
        model.add(linkTo(methodOn(ClusterController.class)
                .getVersion())
                .withSelfRel());
        return ResponseEntity.ok(model);
    }
}
