package com.redhat.syseng.openshift.service.broker.model.catalog;

public class Plan {
    private String id;

    private Schemas schemas;

    private boolean free;

    private String description;

    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Schemas getSchemas() {
        return schemas;
    }

    public void setSchemas(Schemas schemas) {
        this.schemas = schemas;
    }

    public boolean getFree() {
        return free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Plan{" + "id='" + id + '\'' + ", schemas=" + schemas + ", free=" + free + ", description='" + description + '\'' + ", name='" + name + '\'' + '}';
    }
}
