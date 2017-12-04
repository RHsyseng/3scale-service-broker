package com.redhat.syseng.openshift.service.broker.service;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.commons.lang3.RandomStringUtils;

import com.redhat.syseng.openshift.service.broker.model.amp.Plan;
import com.redhat.syseng.openshift.service.broker.model.amp.Proxy;
import com.redhat.syseng.openshift.service.broker.model.amp.User;
import com.redhat.syseng.openshift.service.broker.model.binding.BindingResult;
import com.redhat.syseng.openshift.service.broker.model.provision.Provision;
import com.redhat.syseng.openshift.service.broker.model.provision.Result;
//import com.redhat.syseng.openshift.service.broker.persistence.PersistHashMapDAO;
import com.redhat.syseng.openshift.service.broker.persistence.PersistSqlLiteDAO;
import static com.redhat.syseng.openshift.service.broker.service.util.BrokerUtil.createMappingRules;
import static com.redhat.syseng.openshift.service.broker.service.util.BrokerUtil.getThreeScaleApiService;
import static com.redhat.syseng.openshift.service.broker.service.util.BrokerUtil.searchServiceInstance;

public class ServiceSecurer {

    private Logger logger = Logger.getLogger(getClass().getName());

    Result provisioningForSecureService(String instance_id, Provision provision) throws URISyntaxException //public String provisioning( String testString) {
    {
        logger.info("!!!!!!!!!!provisioning /service_instances/{instance_id} : " + instance_id);
        logger.info("provision.getService_id() : " + provision.getService_id());
        logger.info("provision.getOrganization_guid() : " + provision.getOrganization_guid());

        Map<String, Object> inputParameters = provision.getParameters();

        logger.info("provision.getParameters().getService_name() : " + (String) inputParameters.get("service_name"));
        logger.info("provision.getParameters().getApplication_plan() : " + (String) inputParameters.get("application_plan"));
        logger.info("provision.getParameters().getInput_url() : " + (String) inputParameters.get("input_url"));
        logger.info("provision.getParameters().getApplication_name() : " + (String) inputParameters.get("application_name"));

        PersistSqlLiteDAO persistence = PersistSqlLiteDAO.getInstance();
        String url = searchServiceInstance((String) inputParameters.get("service_name"));
        //no existing service, need to create one
        if ("".equals(url)) {

            HashMap parameters = new HashMap();
            parameters.put("name", (String) inputParameters.get("service_name"));
            parameters.put("system_name", (String) inputParameters.get("service_name"));
            parameters.put("description", "instance_id:" + instance_id);

            com.redhat.syseng.openshift.service.broker.model.amp.Service service = getThreeScaleApiService().createService(parameters);
            logger.info("---------------------services is created : " + service.getName());
            String serviceId = String.valueOf(service.getId());
            logger.info("serviceId : " + serviceId);

            //create applicaiton plan
            parameters = new HashMap();
            parameters.put("name", (String) inputParameters.get("application_plan"));
            parameters.put("system_name", (String) inputParameters.get("application_plan"));
            Plan plan = getThreeScaleApiService().createApplicationPlan(serviceId, parameters);

            logger.info("---------------------application plan is created: " + plan.getName());
            logger.info("planId : " + plan.getId());

            createMappingRules(serviceId);

            //API integration
            parameters = new HashMap();
            parameters.put("service_id", serviceId);
            parameters.put("api_backend", (String) inputParameters.get("input_url"));
            Proxy proxy = getThreeScaleApiService().updateProxy(serviceId, parameters);

            logger.info("---------------------integration result endPoint : " + proxy.getEndpoint());

            //create Application to use the Plan, which will generate a valid user_key
            parameters = new HashMap();
            parameters.put("name", (String) inputParameters.get("application_name"));
            parameters.put("description", (String) inputParameters.get("application_name"));
            parameters.put("plan_id", plan.getId());

            //after this step, in the API Integration page, the user_key will automatically replaced with the new one created below
            com.redhat.syseng.openshift.service.broker.model.amp.Application application = getThreeScaleApiService().createApplication(persistence.getAccountId(), parameters);

            logger.info("---------------------application is created : " + application.getName());
            logger.info("user_key : " + application.getUserKey());

            String domain = "-3scale-apicast-staging.middleware.ocp.cloud.lab.eng.bos.redhat.com:443";
            url = "https://" + (String) inputParameters.get("service_name") + domain + "/?user_key=" + application.getUserKey();
        }

        return new Result("task_10", url);
    }

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

        PersistSqlLiteDAO persistence = PersistSqlLiteDAO.getInstance();
        User user = getThreeScaleApiService().createUser(persistence.getAccountId(), parameters);
        logger.info("user is created  : " + user.getId());

        //now activate the new user, the default state is "pending"
        parameters = new HashMap();
        getThreeScaleApiService().activeUser(String.valueOf(persistence.getAccountId()), String.valueOf(user.getId()), parameters);
        logger.info("user is activated");

    }

}
