package com.redhat.syseng.openshift.service.broker.model.catalog;

import java.util.Map;

public class Create {
    private Map<String, Object> parameters;

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "Create{" + "parameters=" + parameters + '}';
    }
}