package com.redhat.syseng.openshift.service.broker.model.service;

public class MappingRulesParameters {

    private String pattern;
    private String delta;
    private String metric_id;
    private String http_method;
    

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getDelta() {
        return delta;
    }

    public void setDelta(String delta) {
        this.delta = delta;
    }

    public String getMetric_id() {
        return metric_id;
    }

    public void setMetric_id(String metric_id) {
        this.metric_id = metric_id;
    }

    public String getHttp_method() {
        return http_method;
    }

    public void setHttp_method(String http_method) {
        this.http_method = http_method;
    }    
}
