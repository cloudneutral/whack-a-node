package io.cockroachdb.wan.model;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.cockroachdb.wan.model.nodes.Locality;
import io.cockroachdb.wan.model.nodes.NodeDetail;
import io.cockroachdb.wan.model.status.NodeStatus;
import io.cockroachdb.wan.web.api.LinkRelations;

/**
 * Combination of node status returned by 'cockroach node status --all --format json'
 * and API endpoint '/api/v2/nodes/'.
 */
@Relation(value = LinkRelations.CURIE_NAMESPACE + ":node",
        collectionRelation = LinkRelations.CURIE_NAMESPACE + ":node-list")
@JsonPropertyOrder({"links", "templates", "id", "locality", "detail", "status"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeModel extends RepresentationModel<NodeModel> {
    @JsonProperty("detail")
    private NodeDetail nodeDetail;

    @JsonProperty("status")
    private NodeStatus nodeStatus;

    public NodeModel(NodeDetail nodeDetail, NodeStatus nodeStatus) {
        Assert.notNull(nodeDetail, "nodeDetail is null");
        Assert.notNull(nodeStatus, "nodeStatus is null");
        this.nodeDetail = nodeDetail;
        this.nodeStatus = nodeStatus;
    }

    public String getDescription() {
        return "Node " + getId()
                + ", " + nodeDetail.getLocality()
                + ", " + nodeDetail.getSqlAddress().getAddressField();
    }

    public Integer getId() {
        return nodeDetail.getNodeId();
    }

    public Locality getLocality() {
        return nodeDetail.getLocality();
    }

    public NodeDetail getNodeDetail() {
        return nodeDetail;
    }

    public NodeStatus getNodeStatus() {
        return nodeStatus;
    }
}
