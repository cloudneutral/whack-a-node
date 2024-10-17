package io.cockroachdb.wan.web.frontend;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.cockroachdb.wan.cluster.ClusterRepository;
import io.cockroachdb.wan.cluster.CommandException;
import io.cockroachdb.wan.model.ClusterModel;
import io.cockroachdb.wan.model.NodeModel;
import io.cockroachdb.wan.web.api.ClusterController;
import io.cockroachdb.wan.web.model.MessageModel;
import io.cockroachdb.wan.web.model.MessageType;
import io.cockroachdb.wan.web.push.SimpMessagePublisher;
import io.cockroachdb.wan.web.push.TopicName;

@Controller
@RequestMapping("/")
public class HomePageController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ClusterController clusterController;

    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private SimpMessagePublisher messagePublisher;

    private ClusterModel clusterModel = new ClusterModel();

    @Value("${application.adminUrl}")
    private String adminUrl;

    @Scheduled(fixedRate = 5, initialDelay = 15, timeUnit = TimeUnit.SECONDS)
    public void scheduledStatusUpdate() {
        try {
            logger.debug(">> Performing cluster update");

            List<NodeModel> model = clusterRepository.queryNodes();
            model.forEach(node -> messagePublisher.convertAndSendNow(TopicName.DASHBOARD_STATUS, node));

            if (!this.clusterModel.isAvailable()) {
                this.clusterModel.setAvailable(true);

//                messagePublisher.convertAndSendDelayed(TopicName.DASHBOARD_TOAST,
//                        MessageModel.from("Cluster recovered.")
//                                .setMessageType(MessageType.information));

                messagePublisher.convertAndSendDelayed(TopicName.DASHBOARD_REFRESH);
            } else {
                if (!clusterModel.getNodes().isEmpty()
                    && clusterModel.getNodes().size() != model.size()) {
                    logger.info("Node count %d != %d - forcing refresh"
                            .formatted(clusterModel.getNodes().size(), model.size()));
                    messagePublisher.convertAndSendDelayed(TopicName.DASHBOARD_REFRESH);
                }
            }

        } catch (CommandException e) {
            logger.warn("Cluster update failed: %s".formatted(e));

            if (this.clusterModel.isAvailable()) {
                this.clusterModel.setAvailable(false);

                messagePublisher.convertAndSendDelayed(TopicName.DASHBOARD_TOAST,
                        MessageModel.from("Cluster update failed: " + e.getMessage())
                                .setMessageType(MessageType.error));

                messagePublisher.convertAndSendDelayed(
                        TopicName.DASHBOARD_REFRESH);
            }
        } finally {
            logger.debug("<< Done performing cluster update");
        }
    }

    @GetMapping
    public Callable<String> homePage(
            @RequestParam(name = "level", defaultValue = "1", required = false) Integer level,
            Model model) {

        try {
            this.clusterModel = clusterController.getCluster().getBody();
        } catch (CommandException e) {
            logger.warn("Error retrieving cluster status: %s".formatted(e));

            this.clusterModel.setAvailable(false);

            messagePublisher.convertAndSendDelayed(TopicName.DASHBOARD_TOAST,
                    MessageModel.from(e.getMessage())
                            .setMessageType(MessageType.error));
        }

        model.addAttribute("cluster", this.clusterModel);
        model.addAttribute("adminUrl", this.adminUrl);
        model.addAttribute("level", level);

        return () -> "home";
    }

    @PostMapping("/node-action")
    public Callable<String> nodeAction(@ModelAttribute("node-id") Integer id,
                                       @ModelAttribute("node-action") String action) {
        logger.debug(">> Performing cluster action: id=%s, action=%s".formatted(id, action));

        NodeModel nodeModel = clusterRepository.queryNodeById(id);

        try {
            if ("disrupt".equalsIgnoreCase(action)) {
                messagePublisher.convertAndSendNow(TopicName.DASHBOARD_TOAST,
                        MessageModel.from("Disrupt " + nodeModel.getDescription())
                                .setMessageType(MessageType.warning));
                clusterRepository.disruptNode(nodeModel);
            } else if ("recover".equalsIgnoreCase(action)) {
                messagePublisher.convertAndSendNow(TopicName.DASHBOARD_TOAST,
                        MessageModel.from("Recover " + nodeModel.getDescription())
                                .setMessageType(MessageType.information));
                clusterRepository.recoverNode(nodeModel);
            }
        } catch (CommandException e) {
            messagePublisher.convertAndSendDelayed(TopicName.DASHBOARD_TOAST,
                    MessageModel.from(e.getMessage())
                            .setMessageType(MessageType.error));
        } finally {
            logger.debug("<< Done performing cluster action: id=%s, action=%s".formatted(id, action));
        }

        return () -> "redirect:/";
    }

    @GetMapping("/notice")
    public String noticePage(Model model) {
        return "notice";
    }
}
