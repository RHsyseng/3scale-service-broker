package com.redhat.syseng.openshift.service.broker.model.binding;

public class Bind_resource {

    private String app_guid;

    public String getApp_guid() {
        return app_guid;
    }

    public void setApp_guid(String app_guid) {
        this.app_guid = app_guid;
    }

    @Override
    public String toString() {
        return "ClassPojo [app_guid = " + app_guid + "]";
    }
}
