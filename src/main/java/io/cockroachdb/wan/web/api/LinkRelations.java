package io.cockroachdb.wan.web.api;

public class LinkRelations {
    public static final String ACTUATORS_REL = "actuators";

    public static final String WORKLOADS_REL = "workload-list";

    public static final String WORKLOAD_FORM_REL = "workload-form";

    public static final String CANCEL_REL = "cancel";

    public static final String DELETE_REL = "delete";

    public static final String VERSION_REL = "version";

    public static final String CLUSTER_REL = "cluster";

    public static final String UPDATE_REL = "update";

    public static final String DISRUPT_REL = "disrupt";

    public static final String RECOVER_REL = "recover";

    public static final String NODE_STATUS_REL = "status";

    public static final String NODE_DETAIL_REL = "detail";

    // IANA standard link relations:
    // http://www.iana.org/assignments/link-relations/link-relations.xhtml

    public static final String CURIE_NAMESPACE = "wan";

    private LinkRelations() {
    }

}
