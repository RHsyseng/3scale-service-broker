package com.redhat.syseng.openshift.service.broker.service;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.PathParam;

import com.redhat.syseng.openshift.service.broker.model.Error;
import com.redhat.syseng.openshift.service.broker.model.amp.Plans;
import com.redhat.syseng.openshift.service.broker.model.amp.Services;
import com.redhat.syseng.openshift.service.broker.model.binding.Binding;
import com.redhat.syseng.openshift.service.broker.model.binding.BindingResult;
import com.redhat.syseng.openshift.service.broker.model.catalog.ApplicationName;
import com.redhat.syseng.openshift.service.broker.model.catalog.Create;
import com.redhat.syseng.openshift.service.broker.model.catalog.Description;
import com.redhat.syseng.openshift.service.broker.model.catalog.Plan;
import com.redhat.syseng.openshift.service.broker.model.catalog.Properties;
import com.redhat.syseng.openshift.service.broker.model.catalog.Schemas;
import com.redhat.syseng.openshift.service.broker.model.catalog.Service;
import com.redhat.syseng.openshift.service.broker.model.catalog.ServiceBinding;
import com.redhat.syseng.openshift.service.broker.model.catalog.ServiceInstance;
import com.redhat.syseng.openshift.service.broker.model.provision.Provision;
import com.redhat.syseng.openshift.service.broker.model.provision.ProvisionResult;
import com.redhat.syseng.openshift.service.broker.model.service.ServiceParameters;
import com.redhat.syseng.openshift.service.broker.model.update.UpdateObject;
import com.redhat.syseng.openshift.service.broker.model.update.UpdateResult;
import com.redhat.syseng.openshift.service.broker.persistence.Persistence;
import com.redhat.syseng.openshift.service.broker.persistence.PlatformConfig;
import com.redhat.syseng.openshift.service.broker.service.util.BrokerUtil;

import static com.redhat.syseng.openshift.service.broker.service.util.BrokerUtil.getThreeScaleApiService;

public class SecuredMarket {

    private Logger logger = Logger.getLogger(getClass().getName());

    ProvisionResult provision(@PathParam("instance_id") String instance_id, Provision provision) throws URISyntaxException {
        Map<String, Object> inputParameters = provision.getParameters();

        PlatformConfig platformConfig = Persistence.getInstance().getPlatformConfig();
        String applicationId = "";

        String userKey = BrokerUtil.searchExistingApplicationBaseOnName((String) inputParameters.get("applicationName"), platformConfig.getAccountId());

        if (userKey.equals("")) {
            //create new Application to use the Plan, which will generate a new user_key
            ServiceParameters sp = new ServiceParameters();
            sp.setName((String) inputParameters.get("applicationName"));
            sp.setDescription((String) inputParameters.get("description"));
            sp.setPlan_id(provision.getPlan_id());

            //after this step, in the API Integration page, the user_key will automatically replaced with the new one created below
            com.redhat.syseng.openshift.service.broker.model.amp.Application application = getThreeScaleApiService().createApplication(String.valueOf(platformConfig.getAccountId()), sp);
            logger.info("---------------------application is created, id is : " + application.getId());
            applicationId = String.valueOf(application.getId());

            userKey = application.getUserKey();
            logger.info("new created user_key : " + userKey);
        }

        String endpoint = BrokerUtil.searchEndPointBasedOnServiceId(provision.getService_id());
        String url = endpoint + "/?user_key=" + userKey;
        ProvisionResult result = new ProvisionResult(url, null);
        result.setAppliationId(applicationId);
        logger.info("provisioning result" + result);
        return result;
    }

    Service[] getCatalog() throws URISyntaxException {
        Services ampServices = getThreeScaleApiService().listServices();
        logger.info("AMP services: " + ampServices);
        List<Service> svcList = new ArrayList<>();
        for (com.redhat.syseng.openshift.service.broker.model.amp.Service service : ampServices.getService()) {
            Service svc = new Service();
            svc.setId(String.valueOf(service.getId()));
            svc.setDescription(service.getName());
            svc.setName(service.getSystemName());
            svc.setBindable(true);
            svc.setPlan_updateable(true);
            svc.setPlans(readPlansForOneService(svc.getId()));
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("displayName", "secured-service-market: " + svc.getDescription());
            metadata.put("documentationUrl", "https://github.com/RHsyseng/3scale-service-broker");
            metadata.put("longDescription", "secured service through 3scale-AMP, name is: " + svc.getDescription());
            svc.setMetadata(metadata);

            svcList.add(svc);
        }
        return svcList.toArray(new Service[svcList.size()]);
    }

    private Plan[] readPlansForOneService(String serviceId) throws URISyntaxException {
        //call the Application PLan List function
        Plans plans = getThreeScaleApiService().listApplicationPlan(serviceId);
        logger.info("AMP plans: " + plans);
        List<Plan> planList = new ArrayList<>();
        for (com.redhat.syseng.openshift.service.broker.model.amp.Plan ampPlan : plans.getPlan()) {
            Plan plan = new Plan();
            plan.setId(String.valueOf(ampPlan.getId()));

            //need to convert to lowercase, otherwise this error in getCatalog: 
            //ClusterServicePlan.servicecatalog.k8s.io "6" is invalid: spec.externalName: Invalid value: "Basic": [-a-z0-9]+ (regex used for validation is 'plan-name-40d-0983-1b89')
            String planName = ampPlan.getName();
            logger.info("Change ampPlan.getName() " + planName + " to lowercase");
            planName = planName.toLowerCase();
            plan.setName(planName);

            plan.setDescription(" plan description ...");
            plan.setFree(true);

            //create service instance
            Properties properties = new Properties();

            Description description = new Description();
            description.setTitle("description");
            description.setType("string");
            ApplicationName applicationName = new ApplicationName();
            applicationName.setTitle("application name");
            applicationName.setType("string");

            properties.setDescription(description);
            properties.setApplicationName(applicationName);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("$schema", "http://json-schema.org/draft-04/schema");
            parameters.put("additionalProperties", false);
            parameters.put("type", "object");
            String[] required = new String[]{"applicationName", "description"};
            parameters.put("required", required);
            parameters.put("properties", properties);

            Create create = new Create();
            create.setParameters(parameters);
            ServiceInstance si = new ServiceInstance();
            si.setCreate(create);

            //update structure is the same as create
            Create update = new Create();
            si.setUpdate(update);

            ServiceBinding sb = new ServiceBinding();

            Schemas schemas = new Schemas();
            schemas.setService_binding(sb);
            schemas.setService_instance(si);
            plan.setSchemas(schemas);

            planList.add(plan);
        }
        return planList.toArray(new Plan[planList.size()]);
    }

    BindingResult binding(Binding binding) throws URISyntaxException {
        String guid = binding.getBind_resource().getApp_guid();
        logger.info("binding guid: " + guid);
        String planId = binding.getPlan_id();
        logger.info("binding planId: " + planId);
        String serviceId = binding.getService_id();
        logger.info("binding serviceId: " + serviceId);

        PlatformConfig platformConfig = Persistence.getInstance().getPlatformConfig();
        String endpoint = BrokerUtil.searchEndPointBasedOnServiceId(serviceId);
        if (endpoint == null) {
            Error error = new Error(410, "Service ID " + serviceId + " not found!");
            logger.severe("Failed to bind\n" + error);
            throw error.asException();
        } else {
            String user_key = BrokerUtil.searchUserKeyBasedOnServiceAndPlanId(serviceId, planId, platformConfig.getAccountId());
            BindingResult result = new BindingResult(new BindingResult.Credentials(endpoint, user_key));
            logger.info("binding result:  " + result);
            return result;
        }
    }

    public void deProvisioning(String instanceId) throws URISyntaxException {
        Persistence persistence = Persistence.getInstance();
        PlatformConfig platformConfig = persistence.getPlatformConfig();
        String provisionInfo = persistence.retrieveProvisionInfo(instanceId);
        if (null != provisionInfo && !"".equals(provisionInfo)) {
            //logger.info("SecuredMarket.deProvisioning(), provisionInfo: " + provisionInfo);
            int i = provisionInfo.indexOf("applicationId=");
            //logger.info("SecuredMarket.deProvisioning(), i: " + i);
            String applicationId = provisionInfo.substring(i + "applicationId=".length(), provisionInfo.indexOf("}", i));
            if (!"".equals(applicationId)) {
                logger.info("SecuredMarket.deProvisioning(), applicationId: " + applicationId);
                getThreeScaleApiService().deleteApplication(platformConfig.getAccountId(), applicationId);
            }
        }
    }

    /*
     * For the same instanceId, compare the new planId with the planId in the
     * provision table, if it's the same, do nothing, 
     * else just update the application to use the new plan
     */
    public UpdateResult updateServiceInstance(String instanceId, UpdateObject updateObject) throws URISyntaxException {
        UpdateResult result = new UpdateResult();
        Persistence persistence = Persistence.getInstance();

        String provisionInfo = persistence.retrieveProvisionInfo(instanceId);
        if (null != provisionInfo && !"".equals(provisionInfo)) {
            logger.info("SecuredMarket.updateServiceInstance(), provisionInfo: " + provisionInfo);

            int j = provisionInfo.indexOf("plan_id='");
            String existingPlanId = provisionInfo.substring(j + "plan_id='".length(), provisionInfo.indexOf("'", j + "plan_id='".length()));
            logger.info("SecuredMarket.updateServiceInstance(), existingPlanId: " + existingPlanId);
            if (!existingPlanId.equals(updateObject.getPlan_id())) {
                //need to update application plan based on applicationId
                int i = provisionInfo.indexOf("applicationId=");

                String applicationId = provisionInfo.substring(i + "applicationId=".length(), provisionInfo.indexOf("}", i));
                if (!"".equals(applicationId)) {
                    logger.info("SecuredMarket.updateServiceInstance(), applicationId: " + applicationId);
                    ServiceParameters sp = new ServiceParameters();
                    sp.setPlan_id(updateObject.getPlan_id());
                    getThreeScaleApiService().changeApplicationPlan(persistence.getPlatformConfig().getAccountId(), applicationId, sp);
                    result.setStatus("success");
                    
                    String oldPlanString = "plan_id='" + existingPlanId + "'";
                    String newPlanString = "plan_id='" + updateObject.getPlan_id() + "'";
                    String newProvisionInfo = provisionInfo.replace(oldPlanString, newPlanString);
                    persistence.updateProvisionInfo(instanceId, newProvisionInfo);
                    logger.info("SecuredMarket.updateServiceInstance(), updated old plan in 3scale AMP and persistence");
                }

            } else {
                String status = "no need to update, plan id is the same: " + existingPlanId;
                logger.info(status);
                result.setStatus(status);
            }

        } else {
                String status = "no need to update, couldn't find existing instance with same instance id: " + instanceId;
                logger.info(status);
                result.setStatus(status);
        }
        
        

        
        return result;
    }

}
