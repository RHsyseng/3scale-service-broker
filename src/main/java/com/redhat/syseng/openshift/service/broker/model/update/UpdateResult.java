package com.redhat.syseng.openshift.service.broker.model.update;

public class UpdateResult {

    private String description;


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    @Override
    public String toString() {
        return "UpdateResult {"
                + "description='" + description + '\''
                + '}';
    }
}
