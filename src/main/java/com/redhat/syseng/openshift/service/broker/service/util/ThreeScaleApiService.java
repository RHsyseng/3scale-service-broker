/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.syseng.openshift.service.broker.service.util;

import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.redhat.syseng.openshift.service.broker.model.amp.Plans;
import com.redhat.syseng.openshift.service.broker.model.amp.Proxy;
import com.redhat.syseng.openshift.service.broker.model.amp.Services;
import com.redhat.syseng.openshift.service.broker.model.amp.Metrics;
import com.redhat.syseng.openshift.service.broker.model.amp.Plan;
import com.redhat.syseng.openshift.service.broker.model.amp.Service;
import com.redhat.syseng.openshift.service.broker.model.amp.User;
import com.redhat.syseng.openshift.service.broker.model.amp.Applications;
import com.redhat.syseng.openshift.service.broker.model.amp.Application;
import com.redhat.syseng.openshift.service.broker.model.amp.MappingRule;


public interface ThreeScaleApiService {


    @GET
    @Path("/admin/api/services.xml")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Services listServices();

    @POST
    @Path("/admin/api/services.xml")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Service createService(HashMap parameters);


    @GET
    @Path("/admin/api/services/{serviceId}/application_plans.xml")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Plans listApplicationPlan(@PathParam("serviceId") String serviceId);

    @POST
    @Path("/admin/api/services/{serviceId}/application_plans.xml")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Plan createApplicationPlan(@PathParam("serviceId") String serviceId, HashMap parameters);


    @POST
    @Path("/admin/api/services/{serviceId}/proxy/mapping_rules.xml")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    MappingRule createMappingRules(@PathParam("serviceId") String serviceId, HashMap parameters);

    @POST
    @Path("/admin/api/accounts/{accountId}/users.xml")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    User createUser(@PathParam("accountId") String accountId, HashMap parameters);

    @GET
    @Path("/admin/api/applications.xml")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Applications listApplications();

    @GET
    @Path("/admin/api/accounts/{accountId}/applications.xml")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Applications getApplications(@PathParam("accountId") String serviceId);

    @POST
    @Path("/admin/api/accounts/{accountId}/applications.xml")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Application createApplication(@PathParam("accountId") String serviceId, HashMap parameters);

    @GET
    @Path("/admin/api/services/{serviceId}/proxy.xml")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Proxy readProxy(@PathParam("serviceId") String serviceId);

    @PATCH
    @Path("/admin/api/services/{serviceId}/proxy.xml")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Proxy updateProxy(@PathParam("serviceId") String serviceId, HashMap parameters);


    @GET
    @Path("/admin/api/services/{serviceId}/metrics.xml")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Metrics listMetric(@PathParam("serviceId") String serviceId);

    @PUT
    @Path("/admin/api/accounts/{accountId}/users/{id}/activate.xml")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    String activeUser(@PathParam("accountId") String accountId, @PathParam("id") String id, HashMap parameters);
}
