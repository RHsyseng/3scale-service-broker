package com.redhat.syseng.openshift.service.broker.model.service;


public class ServiceParameters {
    private String name;
    private String system_name;
    private String description;
    private String plan_id;
    private String service_id;
    private String api_backend;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSystem_name() {
        return system_name;
    }

    public void setSystem_name(String system_name) {
        this.system_name = system_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlan_id() {
        return plan_id;
    }

    public void setPlan_id(String plan_id) {
        this.plan_id = plan_id;
    }    

    public String getService_id() {
        return service_id;
    }

    public void setService_id(String service_id) {
        this.service_id = service_id;
    }

    public String getApi_backend() {
        return api_backend;
    }

    public void setApi_backend(String api_backend) {
        this.api_backend = api_backend;
    }
    
}
