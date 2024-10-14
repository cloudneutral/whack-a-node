package io.cockroachdb.wan.web.push;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class SimpMessagePublisher {
    private static final int SEND_DELAY_MS = 2500;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ScheduledExecutorService scheduledExecutorService;

    public <T> void convertAndSendNow(TopicName topic, Object payload) {
        simpMessagingTemplate.convertAndSend(topic.value, payload);
    }

    public <T> void convertAndSendNow(TopicName topic) {
        convertAndSendNow(topic,"");
    }

    public <T> void convertAndSendDelayed(TopicName topic, Object payload) {
        scheduledExecutorService.schedule(
                () -> convertAndSendNow(topic, payload), SEND_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    public <T> void convertAndSendDelayed(TopicName topic) {
        convertAndSendDelayed(topic,"");
    }
}
