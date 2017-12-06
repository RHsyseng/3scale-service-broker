package com.redhat.syseng.openshift.service.broker.model.binding;


public class Binding {
    private String service_id;

    private Bind_resource bind_resource;

    private String plan_id;

    public String getService_id ()
    {
        return service_id;
    }

    public void setService_id (String service_id)
    {
        this.service_id = service_id;
    }

    public Bind_resource getBind_resource ()
    {
        return bind_resource;
    }

    public void setBind_resource (Bind_resource bind_resource)
    {
        this.bind_resource = bind_resource;
    }

    public String getPlan_id ()
    {
        return plan_id;
    }

    public void setPlan_id (String plan_id)
    {
        this.plan_id = plan_id;
    }

    @Override
    public String toString()
    {
        return "Binding {service_id = "+service_id+", bind_resource = "+bind_resource+", plan_id = "+plan_id+"]}";
    }
    
}
