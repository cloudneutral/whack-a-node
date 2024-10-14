package io.cockroachdb.wan.cluster;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cockroachdb.wan.model.NodeModel;
import io.cockroachdb.wan.model.nodes.NodeDetail;
import io.cockroachdb.wan.model.nodes.NodeDetails;
import io.cockroachdb.wan.model.status.NodeStatus;

@Component
class ClusterOperations implements ClusterRepository {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${application.scriptPath}")
    private Path scriptPath;

    @Autowired
    private ClusterCommands clusterCommands;

    @Autowired
    private ObjectMapper objectMapper;

    // Used as fallback
    private Optional<List<NodeModel>> cache = Optional.empty();

    @Override
    public List<NodeModel> queryNodes() {
        List<NodeModel> models = doQueryNodes();
        cache = Optional.of(models);
        return models;
    }

    private List<NodeModel> queryNodesWithFallback() {
        try {
            return queryNodes();
        } catch (CommandException e) {
            cache.ifPresent(nodeModels -> {
                logger.warn("Using cached model as fallback");
                // Mark node status as unknown and keep the rest from last successful query
                nodeModels.forEach(nodeModel -> {
                    nodeModel.getNodeStatus().setIsAvailable("false");
                    nodeModel.getNodeStatus().setIsLive("false");
                });
            });
            return cache.orElseThrow(() -> e);
        }
    }

    private List<NodeModel> doQueryNodes() {
        List<NodeModel> nodeModels = new ArrayList<>();

        List<NodeStatus> nodeStatuses = queryNodeStatus();
        List<NodeDetail> nodeDetails = queryNodeDetails();

        nodeDetails.forEach(nodeDetail -> nodeStatuses.stream()
                .filter(nodeStatus -> nodeStatus.getId().equals(nodeDetail.getNodeId() + ""))
                .findFirst()
                .ifPresentOrElse(nodeStatus -> {
                    nodeModels.add(new NodeModel(nodeDetail, nodeStatus));
                }, () -> {
                    nodeModels.add(new NodeModel(nodeDetail, new NodeStatus()));
                }));

        return nodeModels;
    }

    private List<NodeDetail> queryNodeDetails() {
        ByteArrayOutputStream barr = new ByteArrayOutputStream();

        try {
            int code = executeProcess(clusterCommands.nodes(), barr);
            if (code != 0) {
                throw new CommandException(
                        StreamUtils.copyToString(barr, Charset.defaultCharset()),
                        code);
            }
            NodeDetails nodeDetails = objectMapper.readValue(
                    new ByteArrayInputStream(barr.toByteArray()), new TypeReference<>() {
                    });
            nodeDetails.getAdditionalProperties().clear();
            nodeDetails.getNodes().forEach(nodeDetail -> nodeDetail.getAdditionalProperties().clear());
            return nodeDetails.getNodes();
        } catch (IOException e) {
            String content = barr.toString(Charset.defaultCharset());
            throw new CommandException(content, e);
        }
    }

    private List<NodeStatus> queryNodeStatus() {
        ByteArrayOutputStream barr = new ByteArrayOutputStream();

        try {
            int code = executeProcess(clusterCommands.status(), barr);
            if (code != 0) {
                throw new CommandException(
                        StreamUtils.copyToString(barr, Charset.defaultCharset()),
                        code);
            }
            return objectMapper.readValue(
                    new ByteArrayInputStream(barr.toByteArray()), new TypeReference<>() {
                    });
        } catch (IOException e) {
            String content = barr.toString(Charset.defaultCharset());
            throw new CommandException(content, e);
        }
    }

    @Override
    public NodeModel queryNodeById(Integer id) {
        return queryNodesWithFallback()
                .stream()
                .filter(node -> node.getNodeDetail().getNodeId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No such node with ID: " + id));
    }

    @Override
    public NodeDetail queryNodeDetailById(Integer id) {
        return queryNodeDetails().stream()
                .filter(nodeStatus -> nodeStatus.getNodeId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No such node with ID: " + id));
    }

    @Override
    public NodeStatus queryNodeStatusById(String id) {
        return queryNodeStatus().stream()
                .filter(nodeStatus -> nodeStatus.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No such node with ID: " + id));
    }

    @Override
    public int disruptNode(NodeModel nodeModel) {
        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        int code = executeProcess(clusterCommands.disrupt(nodeModel), barr);
        if (code != 0) {
            throw new CommandException(StreamUtils.copyToString(barr, Charset.defaultCharset()), code);
        }
        return code;
    }

    @Override
    public int recoverNode(NodeModel nodeModel) {
        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        int code = executeProcess(clusterCommands.recover(nodeModel), barr);
        if (code != 0) {
            throw new CommandException(StreamUtils.copyToString(barr, Charset.defaultCharset()), code);
        }
        return code;
    }

    @Async
    public Future<Integer> disruptNodeAsync(NodeModel nodeModel) {
        try {
            return CompletableFuture.completedFuture(disruptNode(nodeModel));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async
    public Future<Integer> recoverNodeAsync(NodeModel nodeModel) {
        try {
            return CompletableFuture.completedFuture(recoverNode(nodeModel));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private int executeProcess(List<String> commands, ByteArrayOutputStream barr) {
        int code = -1;
        Instant start = Instant.now();

        try {
            logger.debug("Executing command: %s".formatted(String.join(" ", commands)));

            Process process = new ProcessBuilder()
                    .command(commands)
                    .directory(scriptPath.toFile())
                    .start();

            logger.debug("Process info: %s".formatted(process.info()));

            try (InputStream inputStream = process.getInputStream();
                 InputStream errorStream = process.getErrorStream()) {
                copy(inputStream, barr);
                copy(errorStream, barr);
            }

            code = process.waitFor();
            return code;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new CommandException(StreamUtils.copyToString(barr, Charset.defaultCharset()), e);
        } finally {
            logger.debug("Command finished in %s with exit code %d"
                    .formatted(Duration.between(start, Instant.now()), code));
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        try (InputStream is = new BufferedInputStream(in)) {
            byte[] buffer = new byte[1024 * 8];
            int len;
            while ((len = is.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        }
    }
}
