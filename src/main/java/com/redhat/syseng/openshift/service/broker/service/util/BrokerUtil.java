/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.syseng.openshift.service.broker.service.util;

import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.logging.Logger;
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
import com.redhat.syseng.openshift.service.broker.persistence.PersistSqlLiteDAO;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.util.logging.Level;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * @author czhu
 */
public class BrokerUtil {

    private static Logger logger = Logger.getLogger(BrokerUtil.class.getName());

    private static ResteasyClient createRestClientWithCerts() {
        ResteasyClient client = null;

        PersistSqlLiteDAO dao = PersistSqlLiteDAO.getInstance();
        Boolean useOcpCertification = Boolean.valueOf(dao.getUseOcpCertification());

        if (!useOcpCertification) {
            client = new ResteasyClientBuilder().build();
        } else {
            //use the OCP certificate which exist here in every pod: /var/run/secrets/kubernetes.io/serviceaccount/ca.crt
            FileInputStream in = null;
            try {
                in = new FileInputStream("/var/run/secrets/kubernetes.io/serviceaccount/ca.crt");
                //in = new FileInputStream("/home/czhu/works/openServiceBroker/middleware.ocp.cloud.lab.eng.bos.redhat.com.crt");
                //in = new FileInputStream("/home/czhu/works/openServiceBroker/rsaebs.corp.redhat.com.crt");
                //in = new FileInputStream("/home/czhu/works/openServiceBroker/mojo.redhat.com.crt");

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

                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(BrokerUtil.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (FileNotFoundException ex) {
                Logger.getLogger(BrokerUtil.class.getName()).log(Level.SEVERE, null, ex);
            } catch (CertificateExpiredException ex) {
                Logger.getLogger(BrokerUtil.class.getName()).log(Level.SEVERE, null, ex);
            } catch (CertificateNotYetValidException ex) {
                Logger.getLogger(BrokerUtil.class.getName()).log(Level.SEVERE, null, ex);
            } catch (CertificateException ex) {
                Logger.getLogger(BrokerUtil.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(BrokerUtil.class.getName()).log(Level.SEVERE, null, ex);
            } catch (KeyStoreException ex) {
                Logger.getLogger(BrokerUtil.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(BrokerUtil.class.getName()).log(Level.SEVERE, null, ex);
            } catch (KeyManagementException ex) {
                Logger.getLogger(BrokerUtil.class.getName()).log(Level.SEVERE, null, ex);
            }

            //use filter to add http header
            RestClientRequestFilter filter = new RestClientRequestFilter();
            client.register(filter);

        }
        return client;
    }

    public static ThreeScaleApiService getThreeScaleApiService() throws URISyntaxException {
        ResteasyClient client = createRestClientWithCerts();

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
