package com.redhat.syseng.openshift.service.broker.service;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.redhat.syseng.openshift.service.broker.model.amp.Plan;
import com.redhat.syseng.openshift.service.broker.model.amp.Proxy;
import com.redhat.syseng.openshift.service.broker.model.amp.User;
import com.redhat.syseng.openshift.service.broker.model.binding.BindingResult;
import com.redhat.syseng.openshift.service.broker.model.provision.Provision;
import com.redhat.syseng.openshift.service.broker.model.provision.Result;
import org.apache.commons.lang3.RandomStringUtils;

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

        //looks like I need to have an account ready first, and I don't see a REST api for create account, so I manually create one "brokerGroup", id is "5"
        int accountId = 5;
        String url = searchServiceInstance((String) inputParameters.get("service_name"), accountId);
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
            String ampUrl = "/admin/api/services/" + serviceId + "/proxy.xml";

            parameters = new HashMap();
            parameters.put("service_id", serviceId);
            parameters.put("api_backend", (String) inputParameters.get("input_url"));
            Proxy proxy = getThreeScaleApiService().updateProxy(serviceId, parameters);

            logger.info("---------------------integration result endPoint : " + proxy.getEndpoint());

            //create Application to use the Plan, which will generate a valid user_key
            //ampUrl = "/admin/api/accounts/" + account_id + "/applications.xml";
            parameters = new HashMap();
            parameters.put("name", (String) inputParameters.get("application_name"));
            parameters.put("description", (String) inputParameters.get("application_name"));
            parameters.put("plan_id", plan.getId());

            //after this step, in the API Integration page, the user_key will automatically replaced with the new one created below
            com.redhat.syseng.openshift.service.broker.model.amp.Application application = getThreeScaleApiService().createApplication(String.valueOf(accountId), parameters);

            logger.info("---------------------application is created : " + application.getName());
            //String user_key = result.substring(result.indexOf("<user_key>") + "<user_key>".length(), result.indexOf("</user_key>"));
            logger.info("user_key : " + application.getUserKey());

            String domain = "-3scale-apicast-staging.middleware.ocp.cloud.lab.eng.bos.redhat.com:443";
            url = "https://" + (String) inputParameters.get("service_name") + domain + "/?user_key=" + application.getUserKey();
        }

        return new Result("task_10", url);
    }

    private void createUser(String userName, String password) throws URISyntaxException {

//        ArrayList<NameValuePair> postParameters;
//        postParameters = new ArrayList();
//        postParameters.add(new BasicNameValuePair("username", userName));
//        postParameters.add(new BasicNameValuePair("password", password));
        String email = userName + "@example.com";
//        postParameters.add(new BasicNameValuePair("email", email));

        HashMap parameters = new HashMap();
        parameters.put("username", userName);
        parameters.put("password", password);
        parameters.put("email", email);

        //looks like I need to have an account ready first, and I don't see a REST api for create account, so I manually create one "brokerGroup", id is "5"
        int accountId = 5;
        User user = getThreeScaleApiService().createUser(String.valueOf(accountId), parameters);
        logger.info("user is created : " + user.getUsername());

        logger.info("user Id : " + user.getId());

        //now activate the new user, the default state is "pending"
        parameters = new HashMap();
        getThreeScaleApiService().activeUser(String.valueOf(accountId), String.valueOf(user.getId()), parameters);
        logger.info("user is activated");

    }

    public synchronized BindingResult binding(String inputStr) throws URISyntaxException {
        boolean useLetters = true;
        boolean useNumbers = false;
        String userName = "user_" + RandomStringUtils.random(4, useLetters, useNumbers);

        useNumbers = true;
        String passWord = RandomStringUtils.random(15, useLetters, useNumbers);
        logger.info("binding userName: " + userName);
        logger.info("binding passWord: " + passWord);

        createUser(userName, passWord);
//        String responseStr = System.getenv("RESPONSE_STRING");
        //String result = "{\"route_service_url\":\"http://172.30.244.67:8080\"}";
        //String result = "{\"credentials\":{\"username\":\"mysqluser\",\"password\":\"pass\"}}";
        BindingResult result = new BindingResult(new BindingResult.Credentials("https://3scale.middleware.ocp.cloud.lab.eng.bos.redhat.com/login", userName, passWord));
        //logger.info("binding instance_id : " + instance_id);
        //logger.info("binding binding_id : " + binding_id);
        logger.info("binding inputStr 6: " + inputStr);
        logger.info("binding result: " + result);
        //logger.info("!!!!!!!!!!!!!!!binding instance_id: " + instance_id);
        //logger.info("binding binding_id: " + binding_id);
        //result = "{/\"credentials/\":{/\"uri/\":/\"mysql://mysqluser:pass@mysqlhost:3306/dbname/\",/\"username/\":/\"mysqluser/\",/\"password/\":/\"pass/\",/\"host/\":/\"mysqlhost/\",/\"port/\":3306,/\"database/\":/\"dbname/\"}}";
        //result = "{/\"credentials/\":{/\"ttt/\":/\"12345678901111111111111111111111111111111111/\",/\"username/\":/\"mysqluser/\",/\"password/\":/\"pass/\",/\"hhhh/\":/\"222222222/\",/\"port/\":3306,/\"database/\":/\"dbname/\"}}";
        //result = "{\"dashboard_url\":\"\",\"operation\":\"task_10\"}";
        //result = "{\"test\":\"111111111111111\",\"test2\":\"task_10\"}";
        return result;
    }

}
