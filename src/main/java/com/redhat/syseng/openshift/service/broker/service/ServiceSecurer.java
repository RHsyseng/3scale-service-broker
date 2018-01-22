package com.redhat.syseng.openshift.service.broker.service;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.redhat.syseng.openshift.service.broker.model.amp.Application;
import com.redhat.syseng.openshift.service.broker.model.amp.Plan;
import com.redhat.syseng.openshift.service.broker.model.amp.Proxy;
import com.redhat.syseng.openshift.service.broker.model.provision.Provision;
import com.redhat.syseng.openshift.service.broker.model.provision.Result;
import com.redhat.syseng.openshift.service.broker.model.service.ServiceParameters;
import com.redhat.syseng.openshift.service.broker.persistence.Persistence;
import com.redhat.syseng.openshift.service.broker.persistence.PlatformConfig;

import static com.redhat.syseng.openshift.service.broker.service.util.BrokerUtil.createMappingRules;
import static com.redhat.syseng.openshift.service.broker.service.util.BrokerUtil.getThreeScaleApiService;
import static com.redhat.syseng.openshift.service.broker.service.util.BrokerUtil.searchServiceInstance;

public class ServiceSecurer {

    private Logger logger = Logger.getLogger(getClass().getName());

    Result provisioningForSecureService(String instanceId, Provision provision) throws URISyntaxException //public String provisioning( String testString) {
    {
        Map<String, Object> inputParameters = provision.getParameters();

        logger.info("Provisioning /service_instances/" + instanceId);

        PlatformConfig platformConfig = Persistence.getInstance().getPlatformConfig();
        String url = searchServiceInstance((String) inputParameters.get("service_name"));

        String newServiceId = "";
        //no existing service, need to create one
        if ("".equals(url)) {
            ServiceParameters sp = new ServiceParameters();
            sp.setName((String) inputParameters.get("service_name"));
            sp.setSystem_name((String) inputParameters.get("service_name"));
            sp.setDescription("instance_id:" + instanceId);

            com.redhat.syseng.openshift.service.broker.model.amp.Service service = getThreeScaleApiService().createService(sp);
            String serviceId = String.valueOf(service.getId());
            logger.info("serviceId : " + serviceId);
            //use the real service id to replace old one from catalog.json, which will be passed back and persist.
            newServiceId = serviceId;

            sp = new ServiceParameters();
            sp.setName((String) inputParameters.get("application_plan"));
            sp.setSystem_name((String) inputParameters.get("application_plan"));
            Plan plan = getThreeScaleApiService().createApplicationPlan(serviceId, sp);

            logger.info("---------------------application plan is created: " + plan.getName());
            logger.info("planId : " + plan.getId());

            createMappingRules(serviceId);

            //API integration
            sp = new ServiceParameters();
            sp.setService_id(serviceId);
            sp.setApi_backend((String) inputParameters.get("input_url"));
            Proxy proxy = getThreeScaleApiService().updateProxy(serviceId, sp);
            logger.info("---------------------integration result endPoint : " + proxy.getEndpoint());

            //create Application to use the Plan, which will generate a valid user_key
            sp = new ServiceParameters();
            sp.setName((String) inputParameters.get("application_name"));
            sp.setDescription((String) inputParameters.get("application_name"));
            sp.setPlan_id(String.valueOf(plan.getId()));

            //after this step, in the API Integration page, the user_key will automatically replaced with the new one created below
            Application application = getThreeScaleApiService().createApplication(platformConfig.getAccountId(), sp);

            logger.info("---------------------application is created : " + application.getName());
            logger.info("user_key : " + application.getUserKey());

            String domain = "-3scale-apicast-staging.middleware.ocp.cloud.lab.eng.bos.redhat.com:443";
            url = "https://" + (String) inputParameters.get("service_name") + domain + "/?user_key=" + application.getUserKey();
        }

        return new Result("task_10", url, newServiceId);
    }

    public void deProvisioning(String instanceId) throws URISyntaxException {
        Persistence persistence = Persistence.getInstance();
        String provisionInfo = persistence.retrieveProvisionInfo(instanceId);
        if (null != provisionInfo && !"".equals(provisionInfo)) {
            String serviceId = provisionInfo.substring(provisionInfo.indexOf("service_id='") + "service_id='".length(), provisionInfo.indexOf("', organization_guid"));
            if (!"".equals(serviceId)) {
                try {
                    logger.info("ServiceSecurer.deProvisioning, before deleting serviceId from 3scale AMP : " + serviceId);
                    getThreeScaleApiService().deleteService(serviceId);
                } catch (javax.ws.rs.NotFoundException e) {
                    //this is ignorable exception might be result of testing, but have to have catch it, otherwise the deProvisioning won't return
                    //thus OCP won't stop invoking with the same instanceId again and again. 
                    logger.info("deProvisioning: at 3 scale side couldn't find the service with this serviceId: " + serviceId);
                } catch (javax.ws.rs.client.ResponseProcessingException e) {
                    if (e.getMessage().contains("org.xml.sax.SAXParseException; Premature end of file.")) {
                        logger.info("deProvisioning: org.xml.sax.SAXParseException; Premature end of file, ingore since it doesn't cause problem because 3scale side service is indeed deleted");
                    }else{
                        throw new IllegalStateException(e);
                    }
                }
                logger.info("ServiceSecurer.deProvisioning, serviceId is deleted from 3scale AMP : " + serviceId);
            }
        }
    }

}
