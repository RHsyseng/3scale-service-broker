package com.redhat.syseng.openshift.service.broker.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.syseng.openshift.service.broker.model.binding.Binding;
import com.redhat.syseng.openshift.service.broker.model.binding.BindingResult;
import com.redhat.syseng.openshift.service.broker.model.catalog.Catalog;
import com.redhat.syseng.openshift.service.broker.model.catalog.Service;
import com.redhat.syseng.openshift.service.broker.model.provision.Provision;
import com.redhat.syseng.openshift.service.broker.model.provision.Result;
//import com.redhat.syseng.openshift.service.broker.persistence.PersistHashMapDAO;
import com.redhat.syseng.openshift.service.broker.persistence.PersistSqlLiteDAO;

@Path("/v2")
public class ThreeScaleBroker {

    private Logger logger = Logger.getLogger(getClass().getName());

    @GET
    @Path("/catalog")
    @Produces({MediaType.APPLICATION_JSON})
    public Catalog getCatalog(@HeaderParam("X-Broker-Api-Version") String version) throws IOException, JAXBException, URISyntaxException {
        logger.info("Catalog called by version " + version);
        PersistSqlLiteDAO persistence = PersistSqlLiteDAO.getInstance();
        Catalog catalog = new Catalog();
        Service[] services;

        if (null == persistence.getAccessToken() || null == persistence.getAmpAdminAddress()) {
            //read the configuration AMP catalog , which is static
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
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public synchronized Result provision(@PathParam("instance_id") String instance_id, Provision provision) throws URISyntaxException {
        logger.info("Provisioning " + instance_id + " with data " + provision);

        //PersistHashMapDAO persistence = PersistHashMapDAO.getInstance();
        PersistSqlLiteDAO persistence = PersistSqlLiteDAO.getInstance();
        Result result;
        if (provision.getParameters().containsKey("input_url")) {
            result = new ServiceSecurer().provisioningForSecureService(instance_id, provision);
                    logger.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ServiceSecurer Provisioning finished: " );

        } else if (provision.getParameters().containsKey("access_token")) {
            //This is the provision to setup the AMP configuration
            Map<String, Object> parameters = provision.getParameters();
            String ampAddress = (String) parameters.get("amp_address");
            String accessToken = (String) parameters.get("access_token");
            String configurationName = (String) parameters.get("configuration_name");
            String accountId = (String) parameters.get("account_id");
            
            persistence.persistAmpConfiguration(ampAddress, accessToken, configurationName, accountId);
            result = new Result("task_10", null);
        } else {
            result = new SecuredMarket().provision(instance_id, provision);
        }
        persistence.persistProvisionInfo(instance_id, provision);
        logger.info("persist provision : " + persistence.retrieveProvisionInfo(instance_id).toString());
        logger.info("provision.result : " + result);

        return result;

    }

    /*
    @PUT
    @Path("/service_instances/{instance_id}/service_bindings/{binding_id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public synchronized BindingResult binding(@PathParam("instance_id") String instance_id, String inputStr, @Context final HttpServletResponse response) throws URISyntaxException {
        try {

            BindingResult result = new SecuredMarket().binding(inputStr);
            logger.info("binding.result : " + result);
            return result;
        } catch (WebApplicationException e) {
            response.setStatus(410);
            return new BindingResult(null);
        }
    }
    */
    
    @PUT
    @Path("/service_instances/{instance_id}/service_bindings/{binding_id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public synchronized BindingResult binding(@PathParam("instance_id") String instance_id, Binding binding, @Context final HttpServletResponse response) throws URISyntaxException {
        try {
            /*
            BindingResult result = new SecuredMarket().binding(inputStr);
            logger.info("binding.result : " + result);
            return result;
*/
            logger.info("binding: " + binding.toString());
            BindingResult result = new SecuredMarket().binding(binding);
            logger.info("binding.result : " + result);
            return result;
        } catch (WebApplicationException e) {
            response.setStatus(410);
            return new BindingResult(null);
        }
    }
    

    @DELETE
    @Path("/service_instances/{instance_id}")
    @Produces({MediaType.APPLICATION_JSON})
    public synchronized Result deProvisioning(@PathParam("instance_id") String instanceId) {
        logger.info("deProvisioning instance_id: " + instanceId);
        return new Result(null, null);
    }

    @DELETE
    @Path("/service_instances/{instance_id}/service_bindings/{binding_id}")
    @Produces({MediaType.APPLICATION_JSON})
    public synchronized BindingResult unBinding(@PathParam("instance_id") String instanceId, @PathParam("binding_id") String bindingId) {
        logger.info("unBinding instance_id:" + instanceId + ", binding_id: " + bindingId);
        return new BindingResult(null);
    }
}
