package io.cockroachdb.wan.cluster;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cockroachdb.wan.config.ApplicationProfiles;
import io.cockroachdb.wan.model.NodeModel;
import io.cockroachdb.wan.model.disrupt.DisruptorSpecifications;
import io.cockroachdb.wan.model.disrupt.RegionalDisruptorSpecification;
import io.cockroachdb.wan.model.nodes.Locality;

@Component
@Profile({ApplicationProfiles.CLOUD})
public class CloudCommands implements ClusterCommands {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${application.disruptionSpecificationPath}")
    private Path disruptionSpecificationPath;

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
        Locality locality = nodeModel.getLocality();

        String region = locality.getTiers().stream()
                .filter(tier -> tier.getKey().equals("region"))
                .findFirst().orElseThrow(() ->
                        new IllegalArgumentException("No locality or region key found: " + nodeModel.getLocality()))
                .getValue();

        RegionalDisruptorSpecification regionalDisruptorSpecification = new RegionalDisruptorSpecification();
        regionalDisruptorSpecification.setIsWholeRegion(false);
        regionalDisruptorSpecification.setRegionCode(region);
        regionalDisruptorSpecification.getPods().add("cockroachdb-" + nodeModel.getId());

        DisruptorSpecifications disruptorSpecifications = new DisruptorSpecifications();
        disruptorSpecifications.addRegionalDisruptorSpecification(regionalDisruptorSpecification);

        try (FileWriter fileWriter = new FileWriter(disruptionSpecificationPath.toFile())) {
            objectMapper.writeValue(fileWriter, disruptorSpecifications);
        } catch (IOException e) {
            throw new CommandException("I/O error creating disruption specification", e);
        }

        try {
            String body = Files.readString(disruptionSpecificationPath);
            logger.info("%s:\n%s".formatted(disruptionSpecificationPath.toFile(), body));
        } catch (IOException e) {
            throw new CommandException("I/O error creating disruption specification", e);
        }

        return List.of("./cluster-admin", "disrupt", disruptionSpecificationPath.toString());
    }

    @Override
    public List<String> recover(NodeModel nodeModel) {
        return List.of("./cluster-admin", "recover");
    }
}
