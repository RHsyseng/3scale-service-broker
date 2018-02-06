package com.redhat.syseng.openshift.service.broker.model.provision;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProvisionResult {
    private String serviceId;
    private String appliationId;

    //For asynchronous responses, service brokers MAY return an identifier representing the operation. 
    //since for 3scale broker, it won't be async, so this field is not used for now.
    private String operation;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("dashboard_url")
    private String dashboardUrl;

    public ProvisionResult( String dashboardUrl, String serviceId) {
        this.dashboardUrl = dashboardUrl;
        this.serviceId = serviceId;
    }

     public String getAppliationId() {
        return appliationId;
    }

    public void setAppliationId(String appliationId) {
        this.appliationId = appliationId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }
    
    public String getDashboardUrl() {
        return dashboardUrl;
    }

    public void setDashboardUrl(String dashboardUrl) {
        this.dashboardUrl = dashboardUrl;
    }

    @Override
    public String toString() {
        return "ProvisionResult{" +
                "operation='" + operation + '\'' +
                ", dashboardUrl='" + dashboardUrl + '\'' +
                '}';
    }
}
