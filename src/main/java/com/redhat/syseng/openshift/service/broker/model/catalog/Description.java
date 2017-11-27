package com.redhat.syseng.openshift.service.broker.model.catalog;

public class Description {
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
        return "Description{" +
                "title='" + title + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
