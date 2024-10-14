package io.cockroachdb.wan.web.push;

public enum TopicName {
    DASHBOARD_STATUS("/topic/dashboard/status"),
    DASHBOARD_REFRESH("/topic/dashboard/refresh"),
    DASHBOARD_TOAST("/topic/dashboard/toast"),
    WORKLOAD_ITEM("/topic/workload/item"),
    WORKLOAD_METRICS("/topic/workload/summary"),
    WORKLOAD_CHART("/topic/workload/chart");

    final String value;

    TopicName(java.lang.String value) {
        this.value = value;
    }
}
