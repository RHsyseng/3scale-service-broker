package com.redhat.syseng.openshift.service.broker.model.catalog;

import java.util.Arrays;

public class Catalog {
    private Service[] services;

    public Service[] getServices() {
        return services;
    }

    public void setServices(Service[] services) {
        this.services = services;
    }

    @Override
    public String toString() {
        return "Catalog{" + "services=" + Arrays.toString(services) + '}';
    }
}
