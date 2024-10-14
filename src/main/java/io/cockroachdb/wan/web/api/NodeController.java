package io.cockroachdb.wan.web.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.wan.cluster.ClusterRepository;
import io.cockroachdb.wan.model.NodeModel;
import io.cockroachdb.wan.model.nodes.NodeDetail;
import io.cockroachdb.wan.model.status.NodeStatus;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/nodes")
public class NodeController {
    public static final RepresentationModelAssembler<NodeModel, NodeModel> nodeModelAssembler
            = new RepresentationModelAssemblerSupport<>(NodeController.class, NodeModel.class) {
        @Override
        public NodeModel toModel(NodeModel entity) {
            entity.add(linkTo(methodOn(NodeController.class)
                    .getNode(entity.getId()))
                    .withSelfRel());

            entity.add(linkTo(methodOn(NodeController.class)
                    .getNodeDetail(entity.getId()))
                    .withRel(LinkRelations.NODE_DETAIL_REL));

            entity.add(linkTo(methodOn(NodeController.class)
                    .getNodeStatus(entity.getId()))
                    .withRel(LinkRelations.NODE_STATUS_REL));

            if ("true".equals(entity.getNodeStatus().getIsLive())) {
                if ("true".equals(entity.getNodeStatus().getIsAvailable())) {
                    entity.add(linkTo(methodOn(NodeController.class)
                            .disruptNode(entity.getId()))
                            .withRel(LinkRelations.DISRUPT_REL));
                } else {
                    entity.add(linkTo(methodOn(NodeController.class)
                            .recoverNode(entity.getId()))
                            .withRel(LinkRelations.RECOVER_REL));
                }
            }

            return entity;
        }
    };

    @Autowired
    private ClusterRepository clusterRepository;

    @GetMapping("/{id}")
    public ResponseEntity<NodeModel> getNode(@PathVariable("id") Integer id) {
        NodeModel nodeModel = clusterRepository.queryNodeById(id);
        return ResponseEntity.ok(nodeModelAssembler.toModel(nodeModel));
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<EntityModel<NodeDetail>> getNodeDetail(@PathVariable("id") Integer id) {
        NodeDetail nodeDetail = clusterRepository.queryNodeDetailById(id);
        return ResponseEntity.ok(EntityModel.of(nodeDetail)
                .add(linkTo(methodOn(getClass())
                        .getNodeDetail(id))
                        .withSelfRel()));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<EntityModel<NodeStatus>> getNodeStatus(@PathVariable("id") Integer id) {
        NodeStatus node = clusterRepository.queryNodeStatusById(id + "");
        return ResponseEntity.ok(EntityModel.of(node)
                .add(linkTo(methodOn(getClass())
                        .getNodeStatus(id))
                        .withSelfRel()));
    }

    @PostMapping("/{id}/disrupt")
    public ResponseEntity<NodeModel> disruptNode(@PathVariable("id") Integer id) {
        NodeModel nodeModel = clusterRepository.queryNodeById(id);
        clusterRepository.disruptNode(nodeModel);
        return getNode(id);
    }

    @PostMapping("/{id}/recover")
    public ResponseEntity<NodeModel> recoverNode(@PathVariable("id") Integer id) {
        NodeModel nodeModel = clusterRepository.queryNodeById(id);
        clusterRepository.recoverNode(nodeModel);
        return getNode(id);
    }
}
