package io.cockroachdb.wan.cluster;

import java.util.List;

import io.cockroachdb.wan.model.NodeModel;
import io.cockroachdb.wan.model.nodes.NodeDetail;
import io.cockroachdb.wan.model.status.NodeStatus;

public interface ClusterRepository {
    List<NodeModel> queryNodes();

    NodeModel queryNodeById(Integer id);

    NodeDetail queryNodeDetailById(Integer id);

    NodeStatus queryNodeStatusById(String id);

    int disruptNode(NodeModel nodeModel);

    int recoverNode(NodeModel nodeModel);
}
