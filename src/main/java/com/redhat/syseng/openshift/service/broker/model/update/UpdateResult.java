package com.redhat.syseng.openshift.service.broker.model.update;

public class UpdateResult {

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "UpdateResult {"
                + "status='" + status + '\''
                + '}';
    }
}
