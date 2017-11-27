/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.syseng.openshift.service.broker.model.catalog;

import java.util.Arrays;
import java.util.Map;

/**
 * @author czhu
 */
public class Service {
    private String id;

    private Plan[] plans;

    private String description;

    private String name;

    private Map<String, Object> metadata;

    private boolean bindable;

    private String[] requires;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Plan[] getPlans() {
        return plans;
    }

    public void setPlans(Plan[] plans) {
        this.plans = plans;
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public boolean getBindable() {
        return bindable;
    }

    public void setBindable(boolean bindable) {
        this.bindable = bindable;
    }

    public String[] getRequires() {
        return requires;
    }

    public void setRequires(String[] requires) {
        this.requires = requires;
    }

    @Override
    public String toString() {
        return "Service{" + "id='" + id + '\'' + ", plans=" + Arrays.toString(plans) + ", description='" + description + '\'' + ", name='" + name + '\'' + ", metadata=" + metadata + ", bindable=" + bindable + ", requires=" + Arrays.toString(requires) + '}';
    }
}