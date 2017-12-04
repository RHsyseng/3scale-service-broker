/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.syseng.openshift.service.broker.service.util;

import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.logging.Logger;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.TextUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.redhat.syseng.openshift.service.broker.model.amp.Application;
import com.redhat.syseng.openshift.service.broker.model.amp.Applications;
import com.redhat.syseng.openshift.service.broker.model.amp.MappingRule;
import com.redhat.syseng.openshift.service.broker.model.amp.Metric;
import com.redhat.syseng.openshift.service.broker.model.amp.Metrics;
import com.redhat.syseng.openshift.service.broker.model.amp.Plan;
import com.redhat.syseng.openshift.service.broker.model.amp.Proxy;
import com.redhat.syseng.openshift.service.broker.model.amp.Service;
import com.redhat.syseng.openshift.service.broker.model.amp.Services;
//import com.redhat.syseng.openshift.service.broker.persistence.PersistHashMapDAO;
import com.redhat.syseng.openshift.service.broker.persistence.PersistSqlLiteDAO;

/**
 * @author czhu
 */
public class BrokerUtil {

    private static Logger logger = Logger.getLogger(BrokerUtil.class.getName());

    private static ResteasyClient createRestClientAcceptsUntrustedCerts() {
        ResteasyClient client = null;

        //TODO: remove hard-coded
        String trustAllCertificates = System.getenv("TRUST_ALL_CERTIFICATES");
        if (null == trustAllCertificates) {
            trustAllCertificates = "true";
            logger.warning("!!!!!!!!!!!!hard-coded value for TRUST_ALL_CERTIFICATES: " + trustAllCertificates);
        }
        if ("true".equalsIgnoreCase(trustAllCertificates)) {
            logger.warning("Allow all certificates. Not for production!");
//            ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine(createHttpClient_AcceptsUntrustedCerts());
//            ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine(createAllTrustingClient());
//            client = new ResteasyClientBuilder().httpEngine(engine).build();
            client = new ResteasyClientBuilder().disableTrustManager().build();
        } else {
            client = new ResteasyClientBuilder().build();
        }
        return client;
    }

    public static ThreeScaleApiService getThreeScaleApiService() throws URISyntaxException {
        ResteasyClient client = createRestClientAcceptsUntrustedCerts();

        URIBuilder uriBuilder = getUriBuilder();
        String url = uriBuilder.build().toString();
        ResteasyWebTarget rtarget = client.target(url);
        return rtarget.proxy(ThreeScaleApiService.class);
    }

    private static URIBuilder getUriBuilder(Object... path) {
        PersistSqlLiteDAO persistence = PersistSqlLiteDAO.getInstance();
        logger.info("accessToken: " + persistence.getAccessToken());
        logger.info("ampAddress: " + persistence.getAmpAdminAddress());

        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("https");
        uriBuilder.setHost(persistence.getAmpAdminAddress());

        uriBuilder.setPort(443);
        uriBuilder.addParameter("access_token", persistence.getAccessToken());

        StringWriter stringWriter = new StringWriter();
        for (Object part : path) {
            stringWriter.append('/').append(String.valueOf(part));
        }
        uriBuilder.setPath(stringWriter.toString());
        return uriBuilder;
    }

    public static String searchEndPointBasedOnServiceId(String serviceId) throws URISyntaxException {
        logger.info("searchEndPointBasedOnServiceId, serviceId: " + serviceId);
        Proxy proxy = getThreeScaleApiService().readProxy(serviceId);
        return proxy.getEndpoint();
    }

    public static String searchAnyUserKeyBasedOnServiceId(String serviceId) throws URISyntaxException {
        Applications applications = getThreeScaleApiService().listApplications();
        logger.info("searchAnyUserKeyBasedOnServiceId: " + applications);
        
        String userKey = "";
        for (Application application : applications.getApplication()) {
            String svcId = String.valueOf(application.getServiceId());
            if (svcId.endsWith(serviceId)) {
                userKey = application.getUserKey();
                logger.info("found user_key for this service id : " + userKey);
            }
        }

        return userKey;

    }

    public static String searchExistingApplicationBaseOnName(String applicationName, String accountId) throws URISyntaxException {

        Applications applications = getThreeScaleApiService().getApplications(accountId);
        logger.info("searchExistingApplicationBaseOnName: " + applications);

        String userKey = "";
        for (Application application : applications.getApplication()) {
            String name = application.getName();
            if (name.endsWith(applicationName)) {
                userKey = application.getUserKey();
                logger.info("found existing application, userKey is : " + userKey);
            }
        }

        return userKey;
    }

    public static String searchUserKeyBasedOnServiceAndPlanId(String serviceId, String planId, String accountId) throws URISyntaxException {

        Applications applications = getThreeScaleApiService().getApplications(accountId);
        logger.info("searchUserKeyBasedOnServiceAndPlanId: " + applications);

        String userKey = "";
        for (Application application : applications.getApplication()) {
            String svcId = String.valueOf(application.getServiceId());
            if (svcId.endsWith(serviceId)) {
                Plan plan = application.getPlan();
                String pid = String.valueOf(plan.getId());
                if (pid.equals(planId)) {
                    userKey = application.getUserKey();
                }

                logger.info("found existing application, userKey is : " + userKey);
            }
        }

        if (!userKey.equals("")) {
            logger.info("found user_key for this service id : " + userKey);

        } else {
            logger.info("didn't found user_key for this serviceId: " + serviceId + " and planId: " + planId);
        }
        return userKey;

    }

    public static void createMappingRules(String serviceId) throws URISyntaxException {
        //create mapping rule, first need to get the "hit" metric id.
        String metricId = "";
        Metrics metrics = getThreeScaleApiService().listMetric(serviceId);
        for (Metric metric : metrics.getMetric()) {
            logger.info("metricId name: " + metric.getName());
            if (metric.getName().equalsIgnoreCase("Hits")) {
                metricId = String.valueOf(metric.getId());
            }
        }
        logger.info("metricId : " + metricId);

        HashMap parameters = new HashMap();
        parameters.put("pattern", "/");
        parameters.put("delta", "1");
        parameters.put("metric_id", metricId);
        parameters.put("http_method", "POST");

        MappingRule rule = getThreeScaleApiService().createMappingRules(serviceId, parameters);
        logger.info("creating mapping result : " + rule.getHttpMethod());

        //now create mapping rule for PUT under metric "hit"
        parameters.put("http_method", "PUT");
        rule = getThreeScaleApiService().createMappingRules(serviceId, parameters);
        logger.info("creating mapping result : " + rule.getHttpMethod());

        //now create mapping rule for PATCH under metric "hit"
        parameters.put("http_method", "PATCH");
        rule = getThreeScaleApiService().createMappingRules(serviceId, parameters);
        logger.info("creating mapping result : " + rule.getHttpMethod());

        //now create mapping rule for DELETE under metric "hit"
        parameters.put("http_method", "DELETE");
        rule = getThreeScaleApiService().createMappingRules(serviceId, parameters);
        logger.info("creating mapping result : " + rule.getHttpMethod());

    }

    public static String searchServiceInstance(String inputServiceSystemName) throws URISyntaxException {
        logger.info("searchServiceInstance : ");
        Services ampServices = getThreeScaleApiService().listServices();
        boolean found = false;
        String url = "";
        for (Service service : ampServices.getService()) {
            String systemName = service.getSystemName();
            if (systemName.equals(inputServiceSystemName)) {
                found = true;
                String serviceId = String.valueOf(service.getId());
                logger.info("found same system_name service, id : " + serviceId);
                String user_key = searchAnyUserKeyBasedOnServiceId(serviceId);
                String endpoint = searchEndPointBasedOnServiceId(serviceId);
                if (TextUtils.isEmpty(endpoint)) {
                    com.redhat.syseng.openshift.service.broker.model.Error error = new com.redhat.syseng.openshift.service.broker.model.Error(410, "Service ID " + serviceId + " not found!");
                    logger.severe("Failed to bind\n" + error);
                    throw error.asException();
                } else {
                    url = endpoint + "/?user_key=" + user_key;
                }

            }
        }
        if (!found) {
            logger.info("didn't found same system_name service : " + inputServiceSystemName);
        }
        return url;

    }
}
