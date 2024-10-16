package io.cockroachdb.wan.web.api.model;

public enum WorkloadType {
    profile_insert("Profile singleton insert"),
    profile_batch_insert("Profile batch insert"),
    profile_update("Profile point read and update"),
    profile_delete("Profile point read and delete"),
    profile_read("Profile point read"),
    profile_scan("Profile full scan"),
    select_one("Select one"),
    random_wait("Random wait"),
    fixed_wait("Fixed wait")
    ;

    private final String displayValue;

    WorkloadType(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
