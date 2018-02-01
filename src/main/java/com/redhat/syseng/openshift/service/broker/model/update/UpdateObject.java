package com.redhat.syseng.openshift.service.broker.model.update;

import com.redhat.syseng.openshift.service.broker.model.provision.Context;
import java.util.Map;

public class UpdateObject
{
    private String service_id;

    private Context context;

    private String plan_id;

    private Map<String, Object> parameters;

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    
    public String getService_id ()
    {
        return service_id;
    }

    public void setService_id (String service_id)
    {
        this.service_id = service_id;
    }

    public Context getContext ()
    {
        return context;
    }

    public void setContext (Context context)
    {
        this.context = context;
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
        return "Update {" +
                "service_id='" + service_id + '\'' +
                ", context=" + context +
                ", parameters=" + this.getParameters() +
                ", plan_id='" + plan_id + '\'' +
                '}';    }
}
