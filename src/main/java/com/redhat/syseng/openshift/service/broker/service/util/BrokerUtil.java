package com.redhat.syseng.openshift.service.broker.service.util;

import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import com.redhat.syseng.openshift.service.broker.persistence.Persistence;
import com.redhat.syseng.openshift.service.broker.persistence.PlatformConfig;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.TextUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.redhat.syseng.openshift.service.broker.model.amp.Application;
import com.redhat.syseng.openshift.service.broker.model.amp.Applications;
import com.redhat.syseng.openshift.service.broker.model.amp.MappingRule;

import com.redhat.syseng.openshift.service.broker.model.amp.Metrics;
import com.redhat.syseng.openshift.service.broker.model.amp.Metrics.Metric;
import com.redhat.syseng.openshift.service.broker.model.amp.Proxy;
import com.redhat.syseng.openshift.service.broker.model.amp.Service;
import com.redhat.syseng.openshift.service.broker.model.amp.Services;
import com.redhat.syseng.openshift.service.broker.model.service.MappingRulesParameters;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.logging.Level;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class BrokerUtil {

    private static Logger logger = Logger.getLogger(BrokerUtil.class.getName());

    private static ResteasyClient createRestClientWithCerts(boolean useOcpCertificate) {
        ResteasyClient client;

        if (useOcpCertificate) {
            //use the OCP certificate which exist here in every pod: /var/run/secrets/kubernetes.io/serviceaccount/ca.crt
            try (FileInputStream in = new FileInputStream("/var/run/secrets/kubernetes.io/serviceaccount/ca.crt")) {

                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                Certificate cert = cf.generateCertificate(in);
                //logger.info("createRestClientWithCerts, created Certificate from /var/run/secrets/kubernetes.io/serviceaccount/ca.crt");

                // load the keystore that includes self-signed cert as a "trusted" entry
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null, null);
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                keyStore.setCertificateEntry("ocp-cert", cert);
                tmf.init(keyStore);
                SSLContext ctx = SSLContext.getInstance("TLS");
                ctx.init(null, tmf.getTrustManagers(), null);
                //logger.info("createRestClientWithCerts, created SSLContext");

                //For proper HTTPS authentication
                ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder();
                clientBuilder.sslContext(ctx);
                client = clientBuilder.build();
            } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException
                    ex) {
                logger.log(Level.SEVERE, null, ex);
                throw new IllegalStateException(ex);
            }

            //use filter to add http header
            //RestClientRequestFilter filter = new RestClientRequestFilter();
            //client.register(filter);
        } else {
            client = new ResteasyClientBuilder().build();
        }
        return client;
    }

    public static ThreeScaleApiService getThreeScaleApiService() throws URISyntaxException {
        boolean useOcpCertificate;
        PlatformConfig platformConfig = Persistence.getInstance().getPlatformConfig();
        if (platformConfig == null) {
            useOcpCertificate = false;
        } else {
            useOcpCertificate = platformConfig.isUseOcpCertificate();
        }
        ResteasyClient client = createRestClientWithCerts(useOcpCertificate);

        URIBuilder uriBuilder = getUriBuilder();
        String url = uriBuilder.build().toString();
        ResteasyWebTarget webTarget = client.target(url);
        return webTarget.proxy(ThreeScaleApiService.class);
    }

    private static URIBuilder getUriBuilder(Object... path) {
        PlatformConfig platformConfig = Persistence.getInstance().getPlatformConfig();
        logger.info(String.valueOf(platformConfig));

        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("https");
        uriBuilder.setHost(platformConfig.getAdminAddress());

        uriBuilder.setPort(443);
        uriBuilder.addParameter("access_token", platformConfig.getAccessToken());

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

    private static String searchAnyUserKeyBasedOnServiceId(String serviceId) throws URISyntaxException {
        Applications applications = getThreeScaleApiService().listApplications();
        logger.info("searchAnyUserKeyBasedOnServiceId: " + applications);

        String userKey = "";
        for (Application application : applications.getApplications()) {
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
        logger.info("searchExistingApplicationBaseOnName: " + applicationName);

        String userKey = "";
        for (Application application : applications.getApplications()) {
            String name = application.getName();
            if (name.equals(applicationName)) {
                userKey = application.getUserKey();
                logger.info("found existing application, userKey is : " + userKey);
            }
        }

        logger.info("searchExistingApplicationBaseOnName will return userKey: " + userKey);
        return userKey;
    }

    public static String searchUserKeyBasedOnServiceAndPlanId(String serviceId, String planId, String accountId) throws URISyntaxException {

        Applications applications = getThreeScaleApiService().getApplications(accountId);
        logger.info("searchUserKeyBasedOnServiceAndPlanId: " + applications);

        String userKey = "";
        for (Application application : applications.getApplications()) {
            String svcId = String.valueOf(application.getServiceId());
            if (svcId.endsWith(serviceId)) {
                Application.Plan plan = application.getPlan();
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
        Metric metric = metrics.getMetric();
        logger.info("metricId name: " + metric.getName());
        if (metric.getName().equalsIgnoreCase("Hits")) {
            metricId = String.valueOf(metric.getId());
        }

        logger.info("metricId : " + metricId);

        MappingRulesParameters mp = new MappingRulesParameters();
        mp.setPattern("/");
        mp.setDelta("1");
        mp.setMetric_id(metricId);
        mp.setHttp_method("POST");

        MappingRule rule = getThreeScaleApiService().createMappingRules(serviceId, mp);
        logger.info("creating mapping result : " + rule.getHttpMethod());

        //now create mapping rule for PUT under metric "hit"
        mp.setHttp_method("PUT");
        rule = getThreeScaleApiService().createMappingRules(serviceId, mp);
        logger.info("creating mapping result : " + rule.getHttpMethod());

        //now create mapping rule for PATCH under metric "hit"
        mp.setHttp_method("PATCH");
        rule = getThreeScaleApiService().createMappingRules(serviceId, mp);
        logger.info("creating mapping result : " + rule.getHttpMethod());

        //now create mapping rule for DELETE under metric "hit"
        mp.setHttp_method("DELETE");
        rule = getThreeScaleApiService().createMappingRules(serviceId, mp);
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
                logger.info("found same system_name service, id2 : " + service.getId());

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
