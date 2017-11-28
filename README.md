# Overview
This repository contains a working prototype for the implementation of the Open Service Broker API, for Red Hat 3scale API Management Platform.

# Build and Deployment
Build and deploy an OpenShift pod by authenticating to an OpenShift cluster using `oc login`, and then running:

`mvn clean fabric8:deploy -Popenshift`

# Configuration
To configure a service broker in OCP 3.6, use the provided yaml file:

`oc create -f 3scale-broker.yml`

This assumes that the pod has been deployed to an OpenShift project named 3scale-broker-poc.

The tech preview version of the service catalog in OpenShift Container Platform 3.6 does not refresh call to broker catalogs. To see changes, after configuring the API management platform through the service catalog, or after publishing new services to 3scale, delete the broker configuration:

`oc delete broker 3scale-broker`

Once recreated, the broker will be called by the service catalog again.