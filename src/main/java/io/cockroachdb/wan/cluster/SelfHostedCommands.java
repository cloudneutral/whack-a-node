package io.cockroachdb.wan.cluster;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.cockroachdb.wan.config.ApplicationProfiles;
import io.cockroachdb.wan.model.NodeModel;

@Component
@Profile({ApplicationProfiles.SECURE, ApplicationProfiles.INSECURE})
public class SelfHostedCommands implements ClusterCommands {
    @Override
    public List<String> nodes() {
        return List.of("./cluster-admin", "nodes");
    }

    @Override
    public List<String> status() {
        return List.of("./cluster-admin", "status");
    }

    @Override
    public List<String> disrupt(NodeModel nodeModel) {
        return List.of("./cluster-admin", "disrupt", nodeModel.getNodeDetail().getSqlAddressPort());
    }

    @Override
    public List<String> recover(NodeModel nodeModel) {
        return List.of("./cluster-admin", "recover", nodeModel.getNodeDetail().getSqlAddressPort());
    }
}
