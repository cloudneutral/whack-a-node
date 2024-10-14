package io.cockroachdb.wan.cluster;

import java.util.List;

import io.cockroachdb.wan.model.NodeModel;

public interface ClusterCommands {
    List<String> nodes();

    List<String> status();

    List<String> disrupt(NodeModel nodeModel);

    List<String> recover(NodeModel nodeModel);
}
