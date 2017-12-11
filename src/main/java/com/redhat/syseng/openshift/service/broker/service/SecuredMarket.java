package com.redhat.syseng.openshift.service.broker.service;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.PathParam;
import javax.xml.bind.JAXBException;

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
import com.redhat.syseng.openshift.service.broker.model.provision.Result;
//import com.redhat.syseng.openshift.service.broker.persistence.PersistHashMapDAO;
import com.redhat.syseng.openshift.service.broker.persistence.PersistSqlLiteDAO;
import com.redhat.syseng.openshift.service.broker.service.util.BrokerUtil;

import static com.redhat.syseng.openshift.service.broker.service.util.BrokerUtil.getThreeScaleApiService;

public class SecuredMarket {
    
    private Logger logger = Logger.getLogger(getClass().getName());
    
    Result provision(@PathParam("instance_id") String instance_id, Provision provision) throws URISyntaxException {
        logger.info("!!!!!!!!!!provisioning /service_instances/{instance_id} : " + instance_id);
        logger.info("provision.getOrganization_guid() : " + provision.getOrganization_guid());
        logger.info("provision.getService_id() : " + provision.getService_id());
        logger.info("provision.getPlan_id() : " + provision.getPlan_id());
        Map<String, Object> inputParameters = provision.getParameters();
        
        logger.info("provision.getParameters().getApplicationName() : " + (String) inputParameters.get("applicationName"));
        logger.info("provision.getParameters().getDescription() : " + (String) inputParameters.get("description"));
        
        PersistSqlLiteDAO persistence = PersistSqlLiteDAO.getInstance();
        String applicationId = "";
        
        String userKey = BrokerUtil.searchExistingApplicationBaseOnName((String) inputParameters.get("applicationName"), persistence.getAccountId());
        
        if (userKey.equals("")) {
            //create new Application to use the Plan, which will generate a new user_key
            //Add GUID in the description, so later the binding can find this application based on guid. 
            //update on Oct 23, it seems GUID is not unique for each binding...leave it for now, but adding instance_id as well
            String desc = (String) inputParameters.get("description") + " GUID:" + provision.getOrganization_guid() + " instance_id:" + instance_id;
            
            HashMap parameters = new HashMap();
            parameters.put("name", (String) inputParameters.get("applicationName"));
            parameters.put("description", desc);
            parameters.put("plan_id", provision.getPlan_id());

            //after this step, in the API Integration page, the user_key will automatically replaced with the new one created below
            com.redhat.syseng.openshift.service.broker.model.amp.Application application = getThreeScaleApiService().createApplication(String.valueOf(persistence.getAccountId()), parameters);
            logger.info("---------------------application is created, id is : " + application.getId());
            applicationId = String.valueOf(application.getId());
            
            userKey = application.getUserKey();
            logger.info("new created user_key : " + userKey);
        }
        
        String endpoint = BrokerUtil.searchEndPointBasedOnServiceId(provision.getService_id());
        String url = endpoint + "/?user_key=" + userKey;
        Result result = new Result("task_10", url, null);
        result.setAppliationId(applicationId);
        logger.info("provisioning result" + result);
        return result;
    }
    
    Service[] getCatalog() throws JAXBException, URISyntaxException {
        Services ampServices = getThreeScaleApiService().listServices();
        logger.info("AMP services: " + ampServices);
        List<Service> svcList = new ArrayList<>();
        for (com.redhat.syseng.openshift.service.broker.model.amp.Service service : ampServices.getService()) {
            Service svc = new Service();
            svc.setId(String.valueOf(service.getId()));
            svc.setDescription(service.getName());
            svc.setName(service.getSystemName());
            svc.setBindable(true);
            svc.setPlans(readPlansForOneService(svc.getId()));
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("displayName", "secured-service-market: " + svc.getDescription());
            metadata.put("documentationUrl", "https://access.redhat.com/documentation/en-us/reference_architectures/2017/html/api_management_with_red_hat_3scale_api_management_platform");
            metadata.put("longDescription", "secured service through 3scale-AMP, name is: " + svc.getDescription());
            svc.setMetadata(metadata);
            
            svcList.add(svc);
        }
        return svcList.toArray(new Service[svcList.size()]);
    }
    
    private Plan[] readPlansForOneService(String serviceId) throws JAXBException, URISyntaxException {
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
            logger.info("!!!!!!!!!!!!!!!!!! lower the case for ampPlan.getName()" + planName);
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

    /*
    BindingResult binding(String inputStr) throws URISyntaxException {
        //public String binding(@PathParam("instance_id") String instance_id, @PathParam("binding_id") String binding_id) {
        logger.info("binding inputStr: " + inputStr);
        String guid = inputStr.substring(inputStr.indexOf("app_guid\":\"") + "app_guid\":\"".length(), inputStr.indexOf("\",\"plan_id"));
        logger.info("binding guid: " + guid);
        String planId = inputStr.substring(inputStr.indexOf("plan_id\":\"") + "plan_id\":\"".length(), inputStr.indexOf("\",\"service_id"));
        logger.info("binding planId: " + planId);
        String serviceId = inputStr.substring(inputStr.indexOf("service_id\":\"") + "service_id\":\"".length(), inputStr.indexOf("\",\"bind_resource"));
        logger.info("binding serviceId: " + serviceId);

        PersistSqlLiteDAO persistence = PersistSqlLiteDAO.getInstance();
        String endpoint = BrokerUtil.searchEndPointBasedOnServiceId(serviceId);
        if (endpoint == null) {
            Error error = new Error(410, "Service ID " + serviceId + " not found!");
            logger.severe("Failed to bind\n" + error);
            throw error.asException();
        } else {
            String user_key = BrokerUtil.searchUserKeyBasedOnServiceAndPlanId(serviceId, planId, persistence.getAccountId());
            BindingResult result = new BindingResult(new BindingResult.Credentials(endpoint, user_key));
            logger.info("binding result:  " + result);
            return result;
        }
    }
     */
    BindingResult binding(Binding binding) throws URISyntaxException {
        String guid = binding.getBind_resource().getApp_guid();
        logger.info("binding guid: " + guid);
        String planId = binding.getPlan_id();
        logger.info("binding planId: " + planId);
        String serviceId = binding.getService_id();
        logger.info("binding serviceId: " + serviceId);
        
        PersistSqlLiteDAO persistence = PersistSqlLiteDAO.getInstance();
        String endpoint = BrokerUtil.searchEndPointBasedOnServiceId(serviceId);
        if (endpoint == null) {
            Error error = new Error(410, "Service ID " + serviceId + " not found!");
            logger.severe("Failed to bind\n" + error);
            throw error.asException();
        } else {
            String user_key = BrokerUtil.searchUserKeyBasedOnServiceAndPlanId(serviceId, planId, persistence.getAccountId());
            BindingResult result = new BindingResult(new BindingResult.Credentials(endpoint, user_key));
            logger.info("binding result:  " + result);
            return result;
        }
    }
    
    public void deProvisioning(String instanceId) throws URISyntaxException {
        PersistSqlLiteDAO persistence = PersistSqlLiteDAO.getInstance();
        String provisionInfo = persistence.retrieveProvisionInfo(instanceId);
        if (null != provisionInfo && !"".equals(provisionInfo)) {
            //logger.info("SecuredMarket.deProvisioning(), provisionInfo: " + provisionInfo);
            int i = provisionInfo.indexOf("applicationId=");
            //logger.info("SecuredMarket.deProvisioning(), i: " + i);
            String applicationId = provisionInfo.substring(i + "applicationId=".length(), provisionInfo.indexOf("}", i));
            if (null != applicationId && !"".equals(applicationId)) {
                logger.info("SecuredMarket.deProvisioning(), applicationId: " + applicationId);
                getThreeScaleApiService().deleteApplication(persistence.getAccountId(), applicationId);
            }
        }
    }
    
}
