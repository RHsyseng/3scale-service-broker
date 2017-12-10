package com.redhat.syseng.openshift.service.broker.model.provision;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Result {
    private String serviceId;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String operation;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("dashboard_url")
    private String dashboardUrl;

    public Result(String operation, String dashboardUrl, String serviceId) {
        this.operation = operation;
        this.dashboardUrl = dashboardUrl;
        this.serviceId = serviceId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getDashboardUrl() {
        return dashboardUrl;
    }

    public void setDashboardUrl(String dashboardUrl) {
        this.dashboardUrl = dashboardUrl;
    }

    @Override
    public String toString() {
        return "BindingResult{" +
                "operation='" + operation + '\'' +
                ", dashboardUrl='" + dashboardUrl + '\'' +
                '}';
    }
}
