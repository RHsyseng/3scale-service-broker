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
        logger.info("!!!!!!!!!!provisioning /service_instances/{instance_id} : " + instanceId);
        logger.info("provision.getService_id() : " + provision.getService_id());
        logger.info("provision.getOrganization_guid() : " + provision.getOrganization_guid());

        Map<String, Object> inputParameters = provision.getParameters();

        logger.info("provision.getParameters().getService_name() : " + (String) inputParameters.get("service_name"));
        logger.info("provision.getParameters().getApplication_plan() : " + (String) inputParameters.get("application_plan"));
        logger.info("provision.getParameters().getInput_url() : " + (String) inputParameters.get("input_url"));
        logger.info("provision.getParameters().getApplication_name() : " + (String) inputParameters.get("application_name"));

        PlatformConfig platformConfig = Persistence.getInstance().getPlatformConfig();
        String url = searchServiceInstance((String) inputParameters.get("service_name"));

        String newServiceId = "";
        //no existing service, need to create one
        if ("".equals(url)) {

            HashMap parameters = new HashMap();
            parameters.put("name", (String) inputParameters.get("service_name"));
            parameters.put("system_name", (String) inputParameters.get("service_name"));
            parameters.put("description", "instance_id:" + instanceId);

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

    /*
    public synchronized BindingResult binding(String inputStr) throws URISyntaxException {
        logger.info("binding inputStr: " + inputStr);

        //TODO: right now it seems binding couldn't accept input parameters defined in catalog, I generated random username and password for tetsing for now.
        boolean useLetters = true;
        boolean useNumbers = false;
        String userName = "user_" + RandomStringUtils.random(4, useLetters, useNumbers);

        useNumbers = true;
        String passWord = RandomStringUtils.random(15, useLetters, useNumbers);
        logger.info("binding userName: " + userName);
        logger.info("binding passWord: " + passWord);

        createUser(userName, passWord);
        BindingResult result = new BindingResult(new BindingResult.Credentials("https://3scale.middleware.ocp.cloud.lab.eng.bos.redhat.com/login", userName, passWord));

        logger.info("binding result: " + result);
        return result;
    }

    private void createUser(String userName, String password) throws URISyntaxException {
        String email = userName + "@example.com";
        HashMap parameters = new HashMap();
        parameters.put("username", userName);
        parameters.put("password", password);
        parameters.put("email", email);

        Persistence persistence = Persistence.getInstance();
        User user = getThreeScaleApiService().createUser(persistence.getAccountId(), parameters);
        logger.info("user is created  : " + user.getId());

        //now activate the new user, the default state is "pending"
        parameters = new HashMap();
        getThreeScaleApiService().activeUser(String.valueOf(persistence.getAccountId()), String.valueOf(user.getId()), parameters);
        logger.info("user is activated");

    }
*/

    public void deProvisioning(String instanceId) throws URISyntaxException {
        Persistence persistence = Persistence.getInstance();
        String provisionInfo = persistence.retrieveProvisionInfo(instanceId);
        if (null != provisionInfo && !"".equals(provisionInfo)) {
            String serviceId = provisionInfo.substring(provisionInfo.indexOf("service_id='") + "service_id='".length(), provisionInfo.indexOf("', organization_guid"));
            logger.info("ServiceSecurer.deProvisioning, serviceId  : " + serviceId);
            if (null != serviceId && !"".equals(serviceId)) {
                getThreeScaleApiService().deleteService(serviceId);
            }
        }
    }

}
