package com.redhat.syseng.openshift.service.broker.model.catalog;

public class ApplicationName {
    private String title;

    private String type;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ApplicationName{" +
                "title='" + title + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}