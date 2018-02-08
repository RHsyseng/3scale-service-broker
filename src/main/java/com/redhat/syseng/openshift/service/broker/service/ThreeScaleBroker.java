package com.redhat.syseng.openshift.service.broker.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.syseng.openshift.service.broker.model.binding.Binding;
import com.redhat.syseng.openshift.service.broker.model.binding.BindingResult;
import com.redhat.syseng.openshift.service.broker.model.catalog.Catalog;
import com.redhat.syseng.openshift.service.broker.model.catalog.Service;
import com.redhat.syseng.openshift.service.broker.model.provision.Provision;
import com.redhat.syseng.openshift.service.broker.model.provision.ProvisionResult;
import com.redhat.syseng.openshift.service.broker.model.update.UpdateObject;
import com.redhat.syseng.openshift.service.broker.model.update.UpdateResult;
import com.redhat.syseng.openshift.service.broker.persistence.Persistence;
import com.redhat.syseng.openshift.service.broker.persistence.PlatformConfig;
import com.redhat.syseng.openshift.service.broker.service.util.PATCH;

@Path("/v2")
public class ThreeScaleBroker {

    /**
     * This is the main broker, which act as facade for the detail
     * implementation of SeviceSecure broker and SecuredMarket broker 1) There
     * is no need to implement Asynchronous Operations since all the work just
     * talk to back end 3scale AMP through RESTful web service call, all request
     * can be finished in a timely manner.
     *
     * 2) Because it's synchronous operation, no need to implement Polling Last
     * Operation from spec.
     *
     * 3) The operation this broker implements are: Provisioning Deprovisioning
     * Binding and Unbinding (just for SecuredMarket broker) Updating
     *
     *
     */
    private Logger logger = Logger.getLogger(getClass().getName());
    private final static String X_BROKER_API_VERSION = "2.13";

    @GET
    @Path("/catalog")
    @Produces(MediaType.APPLICATION_JSON)
    public Catalog getCatalog(@HeaderParam("X-Broker-Api-Version") String version, @HeaderParam("X-Broker-API-Originating-Identity") String originatingIdentity) throws IOException, JAXBException, URISyntaxException {
        handleHttpHeaderInfo("getCatalog", version, originatingIdentity);

        Persistence persistence = Persistence.getInstance();
        Catalog catalog = new Catalog();
        Service[] services;

        if (persistence.getPlatformConfig() == null) {
            //read the configuration AMP catalog , which is static
            logger.info("need to read the init catalog");
            Reader catalogReader = new InputStreamReader(getClass().getResourceAsStream("/catalog_init_configure.json"));
            Service configurationService = new ObjectMapper().readValue(catalogReader, Service.class);
            services = new Service[]{configurationService};
        } else {
            //read the catalog for secure service, which is static
            Reader catalogReader = new InputStreamReader(getClass().getResourceAsStream("/catalog.json"));
            Service threeScaleService = new ObjectMapper().readValue(catalogReader, Service.class);

            if (persistence.isLoadSecuredMarket()) {
                Service[] publishedServices = new SecuredMarket().getCatalog();
                services = new Service[publishedServices.length + 1];
                services[0] = threeScaleService;
                System.arraycopy(publishedServices, 0, services, 1, publishedServices.length);
            } else {
                services = new Service[]{threeScaleService};
            }
        }
        catalog.setServices(services);

        logger.info("Catalog is:\n\n" + catalog);
        return catalog;
    }

    @PUT
    @Path("/service_instances/{instance_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ProvisionResult provision(@HeaderParam("X-Broker-Api-Version") String version, @HeaderParam("X-Broker-API-Originating-Identity") String originatingIdentity, @PathParam("instance_id") String instance_id, Provision provision) throws URISyntaxException {
        handleHttpHeaderInfo("provision", version, originatingIdentity);

        logger.info("Provisioning " + instance_id + " with data " + provision);
        Persistence persistence = Persistence.getInstance();
        ProvisionResult result;

        //Add this check here because for one provision request, OCP spawns mulitple threads, added this "if else check" to make sure only the 1st one go through and recorded
        //otherwise it might cause primary key violation issue in database since the instance ID is PK. 
        if (!persistence.isProvisionInfoExist(instance_id)) {
            
            logger.info("persistence.persistProvisionInfo with request_success == 0");

            persistence.persistProvisionInfo(instance_id, provision);
            if (provision.getParameters().containsKey("input_url")) {
                //This is the provision for SecureService 
                result = new ServiceSecurer().provisioningForSecureService(instance_id, provision);
                provision.setService_id(result.getServiceId());

            } else if (provision.getParameters().containsKey("access_token")) {
                //This is the provision to setup the AMP configuration
                Map<String, Object> parameters = provision.getParameters();
                PlatformConfig platformConfig = new PlatformConfig();
                platformConfig.setAdminAddress((String) parameters.get("amp_address"));
                platformConfig.setAccessToken((String) parameters.get("access_token"));
                platformConfig.setAccountId((String) parameters.get("account_id"));
                platformConfig.setUseOcpCertificate((Boolean) parameters.get("use_OCP_certification"));
                String configurationName = (String) parameters.get("configuration_name");
                logger.info(configurationName + ": " + platformConfig);

                //for setup AMP configuration provision, no backend, so just return an dummy result to indicate it finished.
                result = new ProvisionResult(null, null);
                persistence.setConfiguration(instance_id, configurationName, platformConfig);

            } else {
                //This is the provision for SecuredMarket 
                result = new SecuredMarket().provision(instance_id, provision);
                Map<String, Object> parameters = provision.getParameters();
                parameters.put("applicationId", result.getAppliationId());
            }
            
            // For specification Orphans requirement, first we persist the provisionInfo in DB with request_success == 0 (means false)
            // Then when the request comes back from 3scale successfully, the same record will be updated with request_success == 1
            // Thus any record's request_success remains 0 means an orphoan might be exist at 3scale AMP side, need special treatment.
            persistence.updateProvisionInfoWithSuccessFlag(instance_id, provision);
            logger.info("persistence.updateProvisionRecordWithSuccessFlag == 1");

            logger.info("provision.result: " + result);

        } else {
            logger.info("ProvisionInfo already exists, skip provision again");
            result = new ProvisionResult(null, null);
        }

        return result;
    }

    @DELETE
    @Path("/service_instances/{instance_id}")
    @Produces({MediaType.APPLICATION_JSON})
    public synchronized ProvisionResult deProvisioning(@HeaderParam("X-Broker-Api-Version") String version, @HeaderParam("X-Broker-API-Originating-Identity") String originatingIdentity, @PathParam("instance_id") String instanceId, @QueryParam("service_id") String serviceId, @QueryParam("plan_id") String planId) throws URISyntaxException {
        handleHttpHeaderInfo("deProvisioning", version, originatingIdentity);

        logger.info("deProvisioning instance_id: " + instanceId);
        logger.info("deProvisioning serviceId: " + serviceId);
        logger.info("deProvisioning planId: " + planId);

        Persistence persistence = Persistence.getInstance();
        if (planId.equals("configure-3scale-amp-plan-id")) {
            persistence.deleteAmpConfiguration(instanceId);
            logger.info("deProvisioning for configure-3scale-amp, the persisted configuration is deleted");
        } else if (planId.equals("secure-service-plan-id")) {
            logger.info("deProvisioning for secure service, will delete the provisoned service from 3scale AMP");

            //Note the serviceId passed from Queryparam is from catalog, which is a static one. 
            //The real serviceId need to get from the persistence layer. 
            new ServiceSecurer().deProvisioning(instanceId);
        } else {
            //The rest are secured market plan, which the plan id is integer
            logger.info("deProvisioning for secured market, will delete the application from 3scale AMP");
            new SecuredMarket().deProvisioning(instanceId);
        }
        logger.info("now delete the provisioning info from persistence layer");
        persistence.deleteProvisionInfo(instanceId);
        logger.info("Provisioning is deleted from persistence, deProvisioning finished ");

        return new ProvisionResult(null, null);

    }

    /**
     * Note: Because 2 other brokers (configureAMP broker and serviceSecure
     * broker) don't allow binding so this binding here is only for
     * securedMarket Broker
     *
     * Note: 1) OCP spawns multi thread for the binding 2) And if only first
     * result return the user_key, URL, but the other return null, then in OCP
     * console, the secret is set to empty 3) Since the securedMarket binding is
     * read-only, it can be executed multiple times 4) So let it run, only limit
     * the persist binding info for the 1st request to avoid DB exception in the
     * log.
     *
     * For specification Orphans requirement, since binding in 3scale AMP doesn't
     * really create anything, it just return existing URL and user_key, so no
     * need to handle orphans for bindings.
     */
    @PUT
    @Path("/service_instances/{instance_id}/service_bindings/{binding_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized BindingResult binding(@HeaderParam("X-Broker-Api-Version") String version, @HeaderParam("X-Broker-API-Originating-Identity") String originatingIdentity, @PathParam("instance_id") String instance_id, Binding binding) throws URISyntaxException {
        handleHttpHeaderInfo("binding", version, originatingIdentity);

        Persistence persistence = Persistence.getInstance();
        BindingResult result = new SecuredMarket().binding(binding);
        logger.info("binding.result : " + result);
        if (!persistence.isBindingInfoExist(instance_id)) {
            persistence.persistBindingInfo(instance_id, binding);
            logger.info("persist binding for this instance_id : " + instance_id);
        } else {
            logger.info("Skip persistence because binding already exists for this instance_id: " + instance_id);
        }
        return result;
    }

    /**
     * Since for configureAMP and serviceSecure there are no binding, and for
     * securedMarket binding, it doesn't really create anything in 3scale AMP,
     * just return URL and user_key, so nothing need to be deleted for unbinding
     * at 3scale side, just delete the binding info from persistence.
     */
    @DELETE
    @Path("/service_instances/{instance_id}/service_bindings/{binding_id}")
    @Produces({MediaType.APPLICATION_JSON})
    public synchronized BindingResult unBinding(@HeaderParam("X-Broker-Api-Version") String version, @HeaderParam("X-Broker-API-Originating-Identity") String originatingIdentity, @PathParam("instance_id") String instanceId, @PathParam("binding_id") String bindingId) {
        handleHttpHeaderInfo("unBinding", version, originatingIdentity);
        Persistence persistence = Persistence.getInstance();
        persistence.deleteBindingInfo(instanceId);
        logger.info("unBinding finished, instance_id:" + instanceId + ", binding_id: " + bindingId);
        return new BindingResult(null);
    }

    /**
     * UpdateSerivceInstance only targets securedMarket broker, because 2 other
     * brokers (configureAMP broker and serviceSecure broker) doesn't have more
     * than one plan to choose from
     */
    @PATCH
    @Path("/service_instances/{instance_id}")
    @Produces({MediaType.APPLICATION_JSON})
    public synchronized UpdateResult updateServiceInstance(@HeaderParam("X-Broker-Api-Version") String version, @HeaderParam("X-Broker-API-Originating-Identity") String originatingIdentity, @PathParam("instance_id") String instanceId, UpdateObject updateObject) throws URISyntaxException {
        handleHttpHeaderInfo("updateServiceInstance", version, originatingIdentity);
        logger.info("updateServiceInstance, instance_id:" + instanceId);
        logger.info("updateServiceInstance, service_id:" + updateObject.getService_id());
        logger.info("updateServiceInstance, plan_id:" + updateObject.getPlan_id());

        //Please note the persistency of the request is inside the updateServiceInstance
        //and the record is: 1) persist to the provision table 2) only persist when there is update.
        UpdateResult result = new SecuredMarket().updateServiceInstance(instanceId, updateObject);
        return result;
    }

    private void handleHttpHeaderInfo(String methodName, String version, String originatingIdentity) {
        //just display this http header field as the spec, we don't use it yet
        logger.info(methodName + " X-Broker-API-Originating-Identity: " + originatingIdentity);

        if (null == version) {
            logger.warning(methodName + " X-Broker-Api-Version is null!");
        } else if (!X_BROKER_API_VERSION.equals(X_BROKER_API_VERSION)) {
            logger.warning(methodName + " request header X-Broker-Api-Version is " + version + ", this broker might not work properly, because it is built based on API version " + X_BROKER_API_VERSION);
        } else {
            logger.info(methodName + " X-Broker-Api-Version: " + version);
        }

    }

}
