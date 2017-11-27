package com.redhat.syseng.openshift.service.broker.model.catalog;

public class Schemas {
    private ServiceBinding service_binding;

    private ServiceInstance service_instance;

    public ServiceBinding getService_binding() {
        return service_binding;
    }

    public void setService_binding(ServiceBinding service_binding) {
        this.service_binding = service_binding;
    }

    public ServiceInstance getService_instance() {
        return service_instance;
    }

    public void setService_instance(ServiceInstance service_instance) {
        this.service_instance = service_instance;
    }

    @Override
    public String toString() {
        return "Schemas{" +
                "service_binding=" + service_binding +
                ", service_instance=" + service_instance +
                '}';
    }
}
